package com.wdcftgg.farmersdelightlegacy.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModOreDictionary;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

public final class CuttingBoardRecipeManager {

    private static final String DEFAULT_TOOL_TOKEN = "ore:toolKnife";
    private static final Map<String, String> TAG_TO_OREDICT = ModOreDictionary.getTagToOreDictMap();

    private static final List<CuttingRecipeEntry> RECIPES = new ArrayList<>();
    private static final Map<String, CuttingRecipeEntry> SCRIPT_RECIPES = new LinkedHashMap<>();
    private static boolean loaded;

    private CuttingBoardRecipeManager() {
    }

    public static boolean hasRecipe(ItemStack inputStack, ItemStack toolStack) {
        ensureLoaded();
        if (inputStack.isEmpty()) {
            return false;
        }

        for (CuttingRecipeEntry recipeEntry : getAllRecipeEntries()) {
            if (recipeEntry.matches(inputStack, toolStack)) {
                return true;
            }
        }
        return false;
    }

    public static List<ItemStack> getProcessedResults(ItemStack inputStack, ItemStack toolStack, Random random) {
        ensureLoaded();
        if (inputStack.isEmpty()) {
            return Collections.emptyList();
        }

        for (CuttingRecipeEntry recipeEntry : getAllRecipeEntries()) {
            if (recipeEntry.matches(inputStack, toolStack)) {
                return recipeEntry.rollResults(random, getFortuneLevel(toolStack));
            }
        }
        return Collections.emptyList();
    }

    public static boolean isUsedAsRecipeTool(ItemStack toolStack) {
        ensureLoaded();
        if (toolStack.isEmpty()) {
            return false;
        }
        for (CuttingRecipeEntry recipeEntry : getAllRecipeEntries()) {
            if (recipeEntry.toolMatcher.matchesTool(toolStack)) {
                return true;
            }
        }
        return false;
    }

    public static List<CuttingBoardRecipeView> getRecipes() {
        ensureLoaded();
        List<CuttingBoardRecipeView> result = new ArrayList<>();
        for (CuttingRecipeEntry recipeEntry : getAllRecipeEntries()) {
            result.add(new CuttingBoardRecipeView(
                    recipeEntry.recipeId,
                    recipeEntry.inputMatcher.asStacks(),
                    recipeEntry.toolMatcher.asStacks(),
                    recipeEntry.getDisplayResults(),
                    recipeEntry.getDisplayResultChances()));
        }
        return result;
    }

    public static boolean registerScriptRecipe(String key, String[] inputTokens, String[] toolTokens,
                                               String[] resultTokens, int[] resultCounts, float[] resultChances) {
        if (key == null || key.isEmpty() || inputTokens == null || resultTokens == null || resultTokens.length == 0) {
            return false;
        }

        IngredientMatcher inputMatcher = IngredientMatcher.fromTokens(inputTokens, FarmersDelightLegacy.MOD_ID);
        IngredientMatcher toolMatcher;
        if (toolTokens == null) {
            toolMatcher = IngredientMatcher.fromToken(DEFAULT_TOOL_TOKEN, FarmersDelightLegacy.MOD_ID);
        } else if (toolTokens.length == 0) {
            toolMatcher = IngredientMatcher.noTool();
        } else {
            toolMatcher = IngredientMatcher.fromTokens(toolTokens, FarmersDelightLegacy.MOD_ID);
        }
        if (!inputMatcher.isValid() || !toolMatcher.isValid()) {
            return false;
        }

        List<ResultEntry> resultEntries = new ArrayList<>();
        for (int i = 0; i < resultTokens.length; i++) {
            String token = resultTokens[i];
            if (token == null || token.isEmpty()) {
                return false;
            }
            ParsedItemToken parsedItemToken = parseItemToken(token, FarmersDelightLegacy.MOD_ID);
            if (parsedItemToken == null || parsedItemToken.item == null) {
                return false;
            }

            int count = 1;
            if (resultCounts != null && i < resultCounts.length) {
                count = Math.max(1, resultCounts[i]);
            }

            float chance = 1.0F;
            if (resultChances != null && i < resultChances.length) {
                chance = clampChance(resultChances[i]);
            }

            int metadata = parsedItemToken.hasMetadata ? parsedItemToken.metadata : 0;
            resultEntries.add(new ResultEntry(new ItemStack(parsedItemToken.item, count, metadata), chance));
        }

        CuttingRecipeEntry recipeEntry = new CuttingRecipeEntry(key, inputMatcher, toolMatcher, resultEntries);
        synchronized (SCRIPT_RECIPES) {
            SCRIPT_RECIPES.put(key, recipeEntry);
        }
        return true;
    }

    public static boolean unregisterScriptRecipe(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        synchronized (SCRIPT_RECIPES) {
            return SCRIPT_RECIPES.remove(key) != null;
        }
    }

    public static int removeRecipesByOutput(ItemStack outputStack) {
        ensureLoaded();
        if (outputStack.isEmpty()) {
            return 0;
        }

        int removedCount = 0;
        synchronized (SCRIPT_RECIPES) {
            java.util.Iterator<Map.Entry<String, CuttingRecipeEntry>> iterator = SCRIPT_RECIPES.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, CuttingRecipeEntry> entry = iterator.next();
                if (entry.getValue().hasOutput(outputStack)) {
                    iterator.remove();
                    removedCount++;
                }
            }
        }

        java.util.Iterator<CuttingRecipeEntry> iterator = RECIPES.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().hasOutput(outputStack)) {
                iterator.remove();
                removedCount++;
            }
        }
        return removedCount;
    }

    public static final class CuttingBoardRecipeView {
        private final String recipeId;
        private final List<ItemStack> inputOptions;
        private final List<ItemStack> toolOptions;
        private final List<ItemStack> resultStacks;
        private final List<Float> resultChances;

        private CuttingBoardRecipeView(String recipeId, List<ItemStack> inputOptions, List<ItemStack> toolOptions, List<ItemStack> resultStacks, List<Float> resultChances) {
            this.recipeId = recipeId;
            this.inputOptions = copyStacks(inputOptions);
            this.toolOptions = copyStacks(toolOptions);
            this.resultStacks = copyStacks(resultStacks);
            this.resultChances = new ArrayList<>(resultChances);
        }

        public String getRecipeId() {
            return recipeId;
        }

        public List<ItemStack> getInputOptions() {
            return copyStacks(inputOptions);
        }

        public List<ItemStack> getToolOptions() {
            return copyStacks(toolOptions);
        }

        public List<ItemStack> getResultStacks() {
            return copyStacks(resultStacks);
        }

        public List<Float> getResultChances() {
            return new ArrayList<>(resultChances);
        }
    }

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }

        RECIPES.clear();
        loadJsonRecipes();
        loaded = true;
    }

    private static void loadJsonRecipes() {
        for (RecipeResourceScanner.RecipeJsonResource recipeResource : RecipeResourceScanner.scan("cutting_board")) {
            parseAndRegister(recipeResource.getJsonObject(), recipeResource.getModId(), recipeResource.getRecipeId());
        }
    }

    private static void parseAndRegister(JsonObject recipeJson, String defaultNamespace, String recipeId) {
        if (!passesConditions(recipeJson)) {
            return;
        }
        IngredientMatcher inputMatcher = readInput(recipeJson, defaultNamespace);
        IngredientMatcher toolMatcher = readTool(recipeJson, defaultNamespace);
        List<ResultEntry> resultEntries = readResults(recipeJson, defaultNamespace);
        if (!inputMatcher.isValid() || !toolMatcher.isValid() || resultEntries.isEmpty()) {
            return;
        }
        RECIPES.add(new CuttingRecipeEntry(recipeId, inputMatcher, toolMatcher, resultEntries));
    }

    private static boolean passesConditions(JsonObject recipeJson) {
        if (!recipeJson.has("conditions") || !recipeJson.get("conditions").isJsonObject()) {
            return true;
        }

        JsonObject conditionsObject = recipeJson.getAsJsonObject("conditions");
        return matchesModCondition(conditionsObject.get("mod_loaded"), true)
                && matchesModCondition(conditionsObject.get("mod_not_loaded"), false);
    }

    private static boolean matchesModCondition(JsonElement conditionElement, boolean expectedLoaded) {
        if (conditionElement == null || conditionElement.isJsonNull()) {
            return true;
        }

        List<String> modIds = new ArrayList<>();
        collectConditionStrings(conditionElement, modIds);
        for (String modId : modIds) {
            if (Loader.isModLoaded(modId) != expectedLoaded) {
                return false;
            }
        }
        return true;
    }

    private static void collectConditionStrings(JsonElement element, List<String> values) {
        if (element == null || element.isJsonNull()) {
            return;
        }

        if (element.isJsonPrimitive()) {
            String value = element.getAsString();
            if (!value.isEmpty()) {
                values.add(value);
            }
            return;
        }

        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                collectConditionStrings(child, values);
            }
        }
    }

    private static IngredientMatcher readInput(JsonObject recipeJson, String defaultNamespace) {
        if (recipeJson.has("ingredient")) {
            IngredientMatcher ingredientMatcher = IngredientMatcher.fromJson(recipeJson.get("ingredient"), defaultNamespace);
            if (ingredientMatcher.isValid()) {
                return ingredientMatcher;
            }
        }
        if (recipeJson.has("input") && recipeJson.get("input").isJsonPrimitive()) {
            return IngredientMatcher.fromToken(recipeJson.get("input").getAsString(), defaultNamespace);
        }
        if (recipeJson.has("ingredients") && recipeJson.get("ingredients").isJsonArray()) {
            return IngredientMatcher.fromJson(recipeJson.get("ingredients"), defaultNamespace);
        }
        return IngredientMatcher.invalid();
    }

    private static IngredientMatcher readTool(JsonObject recipeJson, String defaultNamespace) {
        if (recipeJson.has("tool")) {
            JsonElement toolElement = recipeJson.get("tool");
            if (isEmptyToolDefinition(toolElement)) {
                return IngredientMatcher.noTool();
            }
            IngredientMatcher toolMatcher = IngredientMatcher.fromJson(toolElement, defaultNamespace);
            if (toolMatcher.isValid()) {
                return toolMatcher;
            }
        }
        return IngredientMatcher.fromToken(DEFAULT_TOOL_TOKEN, defaultNamespace);
    }

    private static boolean isEmptyToolDefinition(JsonElement toolElement) {
        if (toolElement == null || toolElement.isJsonNull()) {
            return true;
        }
        if (toolElement.isJsonArray()) {
            return toolElement.getAsJsonArray().size() == 0;
        }
        if (toolElement.isJsonPrimitive()) {
            return toolElement.getAsString().isEmpty();
        }
        return false;
    }

    private static List<ResultEntry> readResults(JsonObject recipeJson, String defaultNamespace) {
        List<ResultEntry> resultEntries = new ArrayList<>();

        if (recipeJson.has("output") && recipeJson.get("output").isJsonPrimitive()) {
            String outputPath = recipeJson.get("output").getAsString();
            int outputCount = recipeJson.has("count") ? Math.max(1, recipeJson.get("count").getAsInt()) : 1;
            Item outputItem = itemOf(outputPath, defaultNamespace);
            if (outputItem != null) {
                resultEntries.add(new ResultEntry(new ItemStack(outputItem, outputCount), 1.0F));
            }
            return resultEntries;
        }

        if (recipeJson.has("result")) {
            JsonElement resultElement = recipeJson.get("result");
            if (resultElement.isJsonArray()) {
                JsonArray resultArray = resultElement.getAsJsonArray();
                for (JsonElement element : resultArray) {
                    ResultEntry entry = parseResultEntry(element, defaultNamespace);
                    if (entry != null) {
                        resultEntries.add(entry);
                    }
                }
            } else {
                ResultEntry single = parseResultEntry(resultElement, defaultNamespace);
                if (single != null) {
                    resultEntries.add(single);
                }
            }
        }

        return resultEntries;
    }

    private static ResultEntry parseResultEntry(JsonElement resultElement, String defaultNamespace) {
        if (resultElement == null || !resultElement.isJsonObject()) {
            return null;
        }

        JsonObject resultObject = resultElement.getAsJsonObject();
        if (!resultObject.has("item") || !resultObject.get("item").isJsonPrimitive()) {
            return null;
        }

        Item resultItem = itemOf(resultObject.get("item").getAsString(), defaultNamespace);
        if (resultItem == null) {
            return null;
        }

        int count = 1;
        if (resultObject.has("count")) {
            count = Math.max(1, resultObject.get("count").getAsInt());
        }

        int metadata = 0;
        if (resultObject.has("data")) {
            metadata = Math.max(0, resultObject.get("data").getAsInt());
        }

        float chance = 1.0F;
        if (resultObject.has("chance")) {
            chance = clampChance((float) resultObject.get("chance").getAsDouble());
        } else if (resultObject.has("probability")) {
            chance = clampChance((float) resultObject.get("probability").getAsDouble());
        }

        return new ResultEntry(new ItemStack(resultItem, count, metadata), chance);
    }

    private static float clampChance(float chance) {
        if (chance < 0.0F) {
            return 0.0F;
        }
        if (chance > 1.0F) {
            return 1.0F;
        }
        return chance;
    }

    private static int getFortuneLevel(ItemStack toolStack) {
        if (toolStack.isEmpty()) {
            return 0;
        }
        return EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, toolStack);
    }

    private static List<ItemStack> copyStacks(List<ItemStack> source) {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack stack : source) {
            if (!stack.isEmpty()) {
                result.add(stack.copy());
            }
        }
        return result;
    }

    private static final class CuttingRecipeEntry {

        private final String recipeId;
        private final IngredientMatcher inputMatcher;
        private final IngredientMatcher toolMatcher;
        private final List<ResultEntry> resultEntries;

        private CuttingRecipeEntry(String recipeId, IngredientMatcher inputMatcher, IngredientMatcher toolMatcher, List<ResultEntry> resultEntries) {
            this.recipeId = recipeId;
            this.inputMatcher = inputMatcher;
            this.toolMatcher = toolMatcher;
            this.resultEntries = new ArrayList<>(resultEntries);
        }

        private boolean matches(ItemStack inputStack, ItemStack toolStack) {
            return inputMatcher.matches(inputStack) && toolMatcher.matchesTool(toolStack);
        }

        private List<ItemStack> rollResults(Random random, int fortuneLevel) {
            Random actualRandom = random == null ? new Random() : random;
            List<ItemStack> rolledResults = new ArrayList<>();
            for (ResultEntry resultEntry : resultEntries) {
                double chance = Math.min(1.0D, resultEntry.chance + Configuration.cuttingBoardFortuneBonus * fortuneLevel);
                int outputCount = resultEntry.stack.getCount();
                for (int roll = 0; roll < resultEntry.stack.getCount(); roll++) {
                    if (actualRandom.nextFloat() > chance) {
                        outputCount--;
                    }
                }
                if (outputCount > 0) {
                    ItemStack resultStack = resultEntry.stack.copy();
                    resultStack.setCount(outputCount);
                    rolledResults.add(resultStack);
                }
            }
            return rolledResults;
        }

        private List<ItemStack> getDisplayResults() {
            List<ItemStack> display = new ArrayList<>();
            for (ResultEntry entry : resultEntries) {
                display.add(entry.stack.copy());
            }
            return display;
        }

        private List<Float> getDisplayResultChances() {
            List<Float> display = new ArrayList<>();
            for (ResultEntry entry : resultEntries) {
                display.add(entry.chance);
            }
            return display;
        }

        private boolean hasOutput(ItemStack outputStack) {
            for (ResultEntry entry : resultEntries) {
                if (!entry.stack.isEmpty() && ItemStack.areItemsEqual(entry.stack, outputStack)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final class ResultEntry {

        private final ItemStack stack;
        private final float chance;

        private ResultEntry(ItemStack stack, float chance) {
            this.stack = stack.copy();
            this.chance = clampChance(chance);
        }
    }

    private static final class IngredientMatcher {

        private final List<ItemStack> itemStacks;
        private final List<String> oreDictNames;
        private final boolean noToolRequired;

        private IngredientMatcher(List<ItemStack> itemStacks, List<String> oreDictNames, boolean noToolRequired) {
            this.itemStacks = itemStacks;
            this.oreDictNames = oreDictNames;
            this.noToolRequired = noToolRequired;
        }

        private static IngredientMatcher invalid() {
            return new IngredientMatcher(Collections.<ItemStack>emptyList(), Collections.<String>emptyList(), false);
        }

        private static IngredientMatcher noTool() {
            return new IngredientMatcher(Collections.<ItemStack>emptyList(), Collections.<String>emptyList(), true);
        }

        private static IngredientMatcher fromToken(String token, String defaultNamespace) {
            if (token == null || token.isEmpty()) {
                return invalid();
            }

            List<ItemStack> itemStacks = new ArrayList<>();
            List<String> oreDictNames = new ArrayList<>();
            addToken(token, OreDictionary.WILDCARD_VALUE, itemStacks, oreDictNames, defaultNamespace);
            return new IngredientMatcher(itemStacks, oreDictNames, false);
        }

        private static IngredientMatcher fromTokens(String[] tokens, String defaultNamespace) {
            if (tokens == null || tokens.length == 0) {
                return invalid();
            }

            List<ItemStack> itemStacks = new ArrayList<>();
            List<String> oreDictNames = new ArrayList<>();
            for (String token : tokens) {
                addToken(token, OreDictionary.WILDCARD_VALUE, itemStacks, oreDictNames, defaultNamespace);
            }
            return new IngredientMatcher(itemStacks, oreDictNames, false);
        }

        private static IngredientMatcher fromJson(JsonElement element, String defaultNamespace) {
            if (element == null || element.isJsonNull()) {
                return invalid();
            }

            List<ItemStack> itemStacks = new ArrayList<>();
            List<String> oreDictNames = new ArrayList<>();
            collectTokens(element, itemStacks, oreDictNames, defaultNamespace);
            return new IngredientMatcher(itemStacks, oreDictNames, false);
        }

        private static void collectTokens(JsonElement element, List<ItemStack> itemStacks, List<String> oreDictNames, String defaultNamespace) {
            if (element == null || element.isJsonNull()) {
                return;
            }

            if (element.isJsonPrimitive()) {
                addToken(element.getAsString(), OreDictionary.WILDCARD_VALUE, itemStacks, oreDictNames, defaultNamespace);
                return;
            }

            if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                for (JsonElement child : array) {
                    collectTokens(child, itemStacks, oreDictNames, defaultNamespace);
                }
                return;
            }

            if (!element.isJsonObject()) {
                return;
            }

            JsonObject jsonObject = element.getAsJsonObject();
            if (jsonObject.has("item") && jsonObject.get("item").isJsonPrimitive()) {
                int metadata = OreDictionary.WILDCARD_VALUE;
                if (jsonObject.has("data") && jsonObject.get("data").isJsonPrimitive()) {
                    metadata = Math.max(0, jsonObject.get("data").getAsInt());
                }
                addToken(jsonObject.get("item").getAsString(), metadata, itemStacks, oreDictNames, defaultNamespace);
            }
            if (jsonObject.has("tag") && jsonObject.get("tag").isJsonPrimitive()) {
                String oreToken = tagToOreToken(jsonObject.get("tag").getAsString());
                if (oreToken != null) {
                    addToken(oreToken, OreDictionary.WILDCARD_VALUE, itemStacks, oreDictNames, defaultNamespace);
                }
            }
            if (jsonObject.has("action") && jsonObject.get("action").isJsonPrimitive()) {
                String actionToken = actionToOreToken(jsonObject.get("action").getAsString());
                if (actionToken != null) {
                    addToken(actionToken, OreDictionary.WILDCARD_VALUE, itemStacks, oreDictNames, defaultNamespace);
                }
            }
        }

        private static void addToken(String token, int metadata, List<ItemStack> itemStacks, List<String> oreDictNames, String defaultNamespace) {
            if (token == null || token.isEmpty()) {
                return;
            }

            if (token.startsWith("ore:")) {
                String oreName = token.substring(4);
                if (!oreName.isEmpty() && !oreDictNames.contains(oreName)) {
                    oreDictNames.add(oreName);
                }
                return;
            }

            ParsedItemToken parsedItemToken = parseItemToken(token, defaultNamespace);
            Item item = parsedItemToken == null ? null : parsedItemToken.item;
            if (item != null) {
                int resolvedMetadata = parsedItemToken.hasMetadata ? parsedItemToken.metadata : metadata;
                for (ItemStack candidate : itemStacks) {
                    if (candidate.getItem() == item && candidate.getMetadata() == resolvedMetadata) {
                        return;
                    }
                }
                itemStacks.add(new ItemStack(item, 1, resolvedMetadata));
            }
        }

        private boolean isValid() {
            return noToolRequired || !itemStacks.isEmpty() || !oreDictNames.isEmpty();
        }

        private boolean matches(ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            for (ItemStack candidate : itemStacks) {
                if (candidate.getItem() == stack.getItem()) {
                    int candidateMeta = candidate.getMetadata();
                    if (candidateMeta == OreDictionary.WILDCARD_VALUE || candidateMeta == stack.getMetadata()) {
                        return true;
                    }
                }
            }
            if (oreDictNames.isEmpty()) {
                return false;
            }

            int[] stackOreIds = OreDictionary.getOreIDs(stack);
            for (String oreName : oreDictNames) {
                int expectedId = OreDictionary.getOreID(oreName);
                if (expectedId < 0) {
                    continue;
                }
                for (int oreId : stackOreIds) {
                    if (oreId == expectedId) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean matchesTool(ItemStack stack) {
            if (noToolRequired) {
                return stack.isEmpty();
            }
            if (stack.isEmpty()) {
                return false;
            }

            if (matchesDirectItem(stack)) {
                return true;
            }

            for (String oreName : oreDictNames) {
                if (Configuration.toolAxeUsesItemAxeCheck && "toolAxe".equals(oreName)) {
                    if (stack.getItem() instanceof ItemAxe) {
                        return true;
                    }
                    continue;
                }
                for (ItemStack oreStack : OreDictionary.getOres(oreName)) {
                    if (isItemAndMetaMatch(oreStack, stack)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean matchesDirectItem(ItemStack stack) {
            for (ItemStack candidate : itemStacks) {
                if (isItemAndMetaMatch(candidate, stack)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isItemAndMetaMatch(ItemStack candidate, ItemStack target) {
            if (candidate.isEmpty() || target.isEmpty() || candidate.getItem() != target.getItem()) {
                return false;
            }

            int candidateMeta = candidate.getMetadata();
            if (candidateMeta == OreDictionary.WILDCARD_VALUE) {
                return true;
            }
            return candidateMeta == target.getMetadata();
        }

        private List<ItemStack> asStacks() {
            if (noToolRequired) {
                return Collections.emptyList();
            }
            List<ItemStack> result = new ArrayList<>();
            for (ItemStack itemStack : itemStacks) {
                int metadata = itemStack.getMetadata();
                if (metadata == OreDictionary.WILDCARD_VALUE) {
                    result.add(new ItemStack(itemStack.getItem()));
                } else {
                    result.add(itemStack.copy());
                }
            }
            for (String oreName : oreDictNames) {
                for (ItemStack oreStack : OreDictionary.getOres(oreName)) {
                    result.add(oreStack.copy());
                }
            }
            return result;
        }
    }

    private static String tagToOreToken(String tagPath) {
        if (tagPath == null || tagPath.isEmpty()) {
            return null;
        }
        String oreName = TAG_TO_OREDICT.get(tagPath);
        if (oreName == null || oreName.isEmpty()) {
            return null;
        }
        return "ore:" + oreName;
    }

    private static String actionToOreToken(String actionPath) {
        if (actionPath == null || actionPath.isEmpty()) {
            return null;
        }
        if ("axe_dig".equals(actionPath) || "axe_strip".equals(actionPath)) {
            return "ore:toolAxe";
        }
        if ("pickaxe_dig".equals(actionPath)) {
            return "ore:toolPickaxe";
        }
        if ("shovel_dig".equals(actionPath)) {
            return "ore:toolShovel";
        }
        return null;
    }


    private static Item itemOf(String path, String defaultNamespace) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        ResourceLocation itemId;
        if (path.contains(":")) {
            itemId = new ResourceLocation(path);
        } else {
            itemId = new ResourceLocation(defaultNamespace, path);
        }
        return ForgeRegistries.ITEMS.getValue(itemId);
    }

    private static ParsedItemToken parseItemToken(String token, String defaultNamespace) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        String itemPath = token;
        boolean hasMetadata = false;
        int metadata = 0;

        int separatorIndex = token.lastIndexOf('@');
        if (separatorIndex >= 0 && separatorIndex + 1 < token.length()) {
            itemPath = token.substring(0, separatorIndex);
            String metadataToken = token.substring(separatorIndex + 1);
            if ("*".equals(metadataToken)) {
                metadata = OreDictionary.WILDCARD_VALUE;
                hasMetadata = true;
            } else {
                try {
                    metadata = Integer.parseInt(metadataToken);
                    hasMetadata = true;
                } catch (NumberFormatException ignored) {
                    itemPath = token;
                    metadata = 0;
                    hasMetadata = false;
                }
            }
        }

        Item item = itemOf(itemPath, defaultNamespace);
        if (item == null) {
            return null;
        }
        return new ParsedItemToken(item, metadata, hasMetadata);
    }

    private static final class ParsedItemToken {
        private final Item item;
        private final int metadata;
        private final boolean hasMetadata;

        private ParsedItemToken(Item item, int metadata, boolean hasMetadata) {
            this.item = item;
            this.metadata = metadata;
            this.hasMetadata = hasMetadata;
        }
    }

    private static List<CuttingRecipeEntry> getAllRecipeEntries() {
        List<CuttingRecipeEntry> result = new ArrayList<>();
        synchronized (SCRIPT_RECIPES) {
            result.addAll(SCRIPT_RECIPES.values());
        }
        result.addAll(RECIPES);
        return result;
    }
}

