package com.wdcftgg.farmersdelightlegacy.common.recipe.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.recipe.condition.IngredientsAvailableCondition;
import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.item.ItemKnife;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CookingPotRecipe.IngredientEntry;
import com.wdcftgg.farmersdelightlegacy.common.recipe.ToolAction;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModSounds;
import net.minecraft.util.SoundEvent;
import com.wdcftgg.farmersdelightlegacy.common.util.RecipeResourceScanner;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModOreDictionary;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

public final class CuttingBoardRecipeManager {

    private static final String DEFAULT_TOOL_TOKEN = "ore:toolKnife";
    private static final Map<String, String> TAG_TO_OREDICT = ModOreDictionary.getTagToOreDictMap();

    private static final List<CuttingRecipeEntry> RECIPES = new ArrayList<>();
    private static final Map<String, CuttingRecipeEntry> SCRIPT_RECIPES = new LinkedHashMap<>();
    private static final List<ItemStack> REMOVED_BUILT_IN_OUTPUT_STACKS = new ArrayList<>();
    private static final Set<String> REMOVED_BUILT_IN_IDS = new HashSet<>();
    private static final List<java.util.function.Predicate<CuttingBoardRecipeView>> REMOVED_BUILT_IN_PREDICATES = new ArrayList<>();
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
            result.add(recipeEntry.view());
        }
        return result;
    }

    public static void reloadBuiltInRecipes() {
        RECIPES.clear();
        loadJsonRecipes();
        loaded = true;
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

    public static boolean registerScriptRecipe(String key, ItemStack[] inputStacks, ItemStack[] toolStacks,
                                               ItemStack[] resultStacks, float[] resultChances) {
        if (key == null || key.isEmpty() || inputStacks == null || resultStacks == null || resultStacks.length == 0) {
            return false;
        }

        IngredientMatcher inputMatcher = IngredientMatcher.fromStacks(inputStacks, false);
        IngredientMatcher toolMatcher;
        if (toolStacks == null) {
            toolMatcher = IngredientMatcher.fromToken(DEFAULT_TOOL_TOKEN, FarmersDelightLegacy.MOD_ID);
        } else if (toolStacks.length == 0) {
            toolMatcher = IngredientMatcher.noTool();
        } else {
            toolMatcher = IngredientMatcher.fromStacks(toolStacks, false);
        }
        if (!inputMatcher.isValid() || !toolMatcher.isValid()) {
            return false;
        }

        List<ResultEntry> resultEntries = new ArrayList<>();
        for (int i = 0; i < resultStacks.length; i++) {
            ItemStack resultStack = resultStacks[i];
            if (resultStack == null || resultStack.isEmpty()) {
                return false;
            }

            float chance = 1.0F;
            if (resultChances != null && i < resultChances.length) {
                chance = clampChance(resultChances[i]);
            }
            resultEntries.add(new ResultEntry(resultStack.copy(), chance));
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

    public static boolean removeRecipe(String key) {
        ensureLoaded();
        REMOVED_BUILT_IN_IDS.add(key);
        boolean removed = unregisterScriptRecipe(key);
        return RECIPES.removeIf(recipe -> recipe.recipeId.equals(key)) || removed;
    }

    public static int removeRecipes(java.util.function.Predicate<CuttingBoardRecipeView> predicate) {
        ensureLoaded();
        REMOVED_BUILT_IN_PREDICATES.add(predicate);
        int before = RECIPES.size() + SCRIPT_RECIPES.size();
        RECIPES.removeIf(recipe -> predicate.test(recipe.view()));
        synchronized (SCRIPT_RECIPES) {
            SCRIPT_RECIPES.values().removeIf(recipe -> predicate.test(recipe.view()));
        }
        return before - RECIPES.size() - SCRIPT_RECIPES.size();
    }

    public static boolean registerScriptRecipe(String key, IngredientMatcher input, IngredientMatcher tool,
                                               ItemStack[] outputs, float[] chances, SoundEvent sound) {
        return registerScriptRecipe(key, input, tool, outputs, chances, sound, "");
    }

    public static boolean registerScriptRecipe(String key, IngredientMatcher input, IngredientMatcher tool,
                                               ItemStack[] outputs, float[] chances, SoundEvent sound, String group) {
        if (key == null || key.isEmpty() || input == null || tool == null || !input.isValid() || !tool.isValid()
                || outputs == null || outputs.length == 0 || group == null || (chances != null && chances.length != outputs.length)) {
            FarmersDelightLegacy.LOGGER.error("Invalid cutting recipe: {}", key);
            return false;
        }
        List<ResultEntry> results = new ArrayList<>();
        for (int index = 0; index < outputs.length; index++) {
            float chance = chances == null ? 1.0F : chances[index];
            if (outputs[index] == null || outputs[index].isEmpty() || !Float.isFinite(chance) || chance < 0 || chance > 1) {
                FarmersDelightLegacy.LOGGER.error("Invalid cutting recipe output or probability: {}", key);
                return false;
            }
            results.add(new ResultEntry(outputs[index], chance));
        }
        synchronized (SCRIPT_RECIPES) {
            SCRIPT_RECIPES.put(key, new CuttingRecipeEntry(key, input, tool, results, sound, group));
        }
        return true;
    }

    public static SoundEvent getRecipeSound(ItemStack input, ItemStack tool) {
        ensureLoaded();
        for (CuttingRecipeEntry recipe : getAllRecipeEntries()) {
            if (recipe.matches(input, tool)) {
                return recipe.sound;
            }
        }
        throw new IllegalArgumentException("No cutting recipe matches the supplied input and tool");
    }

    public static int removeRecipesByOutput(ItemStack outputStack) {
        ensureLoaded();
        if (outputStack.isEmpty()) {
            return 0;
        }

        addRemovedBuiltInOutputStack(outputStack);
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
        private final String group;
        private final List<ItemStack> inputOptions;
        private final List<ItemStack> toolOptions;
        private final List<ItemStack> resultStacks;
        private final List<Float> resultChances;
        private final IngredientMatcher input;
        private final IngredientMatcher tool;
        private final SoundEvent sound;

        private CuttingBoardRecipeView(String recipeId, IngredientMatcher input, IngredientMatcher tool, List<ItemStack> resultStacks, List<Float> resultChances, SoundEvent sound, String group) {
            this.recipeId = recipeId;
            this.group = group;
            this.input = input;
            this.tool = tool;
            this.sound = sound;
            this.inputOptions = input.asStacks();
            this.toolOptions = tool.asStacks();
            this.resultStacks = copyStacks(resultStacks);
            this.resultChances = new ArrayList<>(resultChances);
        }

        public String getRecipeId() {
            return recipeId;
        }

        public IngredientMatcher getInput() { return input; }

        public IngredientMatcher getTool() { return tool; }

        public SoundEvent getSound() { return sound; }

        public String getGroup() { return group; }

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

        reloadBuiltInRecipes();
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
        if (!inputMatcher.isValid() || !toolMatcher.isValid() || resultEntries.isEmpty()
                || hasRemovedBuiltInOutput(resultEntries)) {
            return;
        }
        SoundEvent sound = null;
        if (recipeJson.has("sound")) {
            ResourceLocation soundId = new ResourceLocation(recipeJson.get("sound").getAsString());
            sound = ForgeRegistries.SOUND_EVENTS.getValue(soundId);
            if (sound == null) {
                FarmersDelightLegacy.LOGGER.error("Unknown cutting recipe sound {} in {}", soundId, recipeId);
                return;
            }
        }
        CuttingRecipeEntry recipe = new CuttingRecipeEntry(recipeId, inputMatcher, toolMatcher, resultEntries, sound,
                recipeJson.has("group") ? recipeJson.get("group").getAsString() : "");
        if (!REMOVED_BUILT_IN_IDS.contains(recipeId)
                && REMOVED_BUILT_IN_PREDICATES.stream().noneMatch(predicate -> predicate.test(recipe.view()))) {
            RECIPES.add(recipe);
        }
    }

    private static void addRemovedBuiltInOutputStack(ItemStack outputStack) {
        for (ItemStack removedOutputStack : REMOVED_BUILT_IN_OUTPUT_STACKS) {
            if (ItemStack.areItemsEqual(removedOutputStack, outputStack)) {
                return;
            }
        }

        ItemStack copiedStack = outputStack.copy();
        copiedStack.setCount(1);
        REMOVED_BUILT_IN_OUTPUT_STACKS.add(copiedStack);
    }

    private static boolean hasRemovedBuiltInOutput(List<ResultEntry> resultEntries) {
        for (ResultEntry resultEntry : resultEntries) {
            for (ItemStack removedOutputStack : REMOVED_BUILT_IN_OUTPUT_STACKS) {
                if (ItemStack.areItemsEqual(resultEntry.stack, removedOutputStack)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean passesConditions(JsonObject recipeJson) {
        if (!recipeJson.has("conditions") || !recipeJson.get("conditions").isJsonObject()) {
            return true;
        }

        JsonObject conditionsObject = recipeJson.getAsJsonObject("conditions");
        return matchesModCondition(conditionsObject.get("mod_loaded"), true)
                && matchesModCondition(conditionsObject.get("mod_not_loaded"), false)
                && matchesBlockRegistrationCondition(conditionsObject.get("block_registered"), true)
                && matchesBlockRegistrationCondition(conditionsObject.get("block_not_registered"), false)
                && IngredientsAvailableCondition.matches(conditionsObject);
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

    private static boolean matchesBlockRegistrationCondition(JsonElement conditionElement, boolean expectedRegistered) {
        if (conditionElement == null || conditionElement.isJsonNull()) {
            return true;
        }

        List<String> blockIds = new ArrayList<>();
        collectConditionStrings(conditionElement, blockIds);
        for (String blockId : blockIds) {
            if (isBlockRegistered(blockId) != expectedRegistered) {
                return false;
            }
        }
        return true;
    }

    private static boolean isBlockRegistered(String blockId) {
        try {
            return ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(blockId));
        } catch (IllegalArgumentException exception) {
            FarmersDelightLegacy.LOGGER.error("Invalid cutting recipe block condition: {}", blockId, exception);
            return false;
        }
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

        ItemStack result = resultObject.has("nbt")
                ? CraftingHelper.getItemStack(resultObject, new JsonContext(defaultNamespace))
                : new ItemStack(resultItem, count, metadata);
        return new ResultEntry(result, chance);
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
        private final String group;
        private final IngredientMatcher inputMatcher;
        private final IngredientMatcher toolMatcher;
        private final List<ResultEntry> resultEntries;
        private final SoundEvent sound;

        private CuttingRecipeEntry(String recipeId, IngredientMatcher inputMatcher, IngredientMatcher toolMatcher, List<ResultEntry> resultEntries) {
            this(recipeId, inputMatcher, toolMatcher, resultEntries, null);
        }

        private CuttingRecipeEntry(String recipeId, IngredientMatcher inputMatcher, IngredientMatcher toolMatcher, List<ResultEntry> resultEntries, SoundEvent sound) {
            this(recipeId, inputMatcher, toolMatcher, resultEntries, sound, "");
        }

        private CuttingRecipeEntry(String recipeId, IngredientMatcher inputMatcher, IngredientMatcher toolMatcher, List<ResultEntry> resultEntries, SoundEvent sound, String group) {
            this.recipeId = recipeId;
            this.group = group;
            this.inputMatcher = inputMatcher;
            this.toolMatcher = toolMatcher;
            this.resultEntries = new ArrayList<>(resultEntries);
            this.sound = sound;
        }

        private CuttingBoardRecipeView view() {
            return new CuttingBoardRecipeView(recipeId, inputMatcher, toolMatcher, getDisplayResults(), getDisplayResultChances(), sound, group);
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

    public static final class IngredientMatcher {

        private final List<ItemStack> itemStacks;
        private final List<String> oreDictNames;
        private final boolean noToolRequired;
        private final List<IngredientEntry> customIngredients = new ArrayList<>();

        private IngredientMatcher(List<ItemStack> itemStacks, List<String> oreDictNames, boolean noToolRequired) {
            this.itemStacks = itemStacks;
            this.oreDictNames = oreDictNames;
            this.noToolRequired = noToolRequired;
        }

        private static IngredientMatcher invalid() {
            return new IngredientMatcher(Collections.<ItemStack>emptyList(), Collections.<String>emptyList(), false);
        }

        public static IngredientMatcher noTool() {
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

        public static IngredientMatcher fromTokens(String[] tokens, String defaultNamespace) {
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

        public static IngredientMatcher fromIngredients(List<IngredientEntry> ingredients) {
            IngredientMatcher matcher = invalid();
            matcher.customIngredients.addAll(ingredients);
            return matcher;
        }

        public List<ItemStack> getItemStacks() { return copyStacks(itemStacks); }

        public List<String> getOreDictNames() { return new ArrayList<>(oreDictNames); }

        public List<IngredientEntry> getCustomIngredients() { return new ArrayList<>(customIngredients); }

        public boolean isNoToolRequired() { return noToolRequired; }

        private static IngredientMatcher fromStacks(ItemStack[] stacks, boolean noToolRequired) {
            if (noToolRequired) {
                return noTool();
            }
            if (stacks == null || stacks.length == 0) {
                return invalid();
            }

            List<ItemStack> itemStacks = new ArrayList<>();
            for (ItemStack stack : stacks) {
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                ItemStack matcherStack = stack.copy();
                matcherStack.setCount(1);
                boolean duplicate = false;
                for (ItemStack candidate : itemStacks) {
                    if (isItemAndMetaMatch(candidate, matcherStack) && isItemAndMetaMatch(matcherStack, candidate)) {
                        duplicate = true;
                        break;
                    }
                }
                if (!duplicate) {
                    itemStacks.add(matcherStack);
                }
            }
            return new IngredientMatcher(itemStacks, Collections.<String>emptyList(), false);
        }

        private static IngredientMatcher fromJson(JsonElement element, String defaultNamespace) {
            if (element == null || element.isJsonNull()) {
                return invalid();
            }

            if (element.isJsonObject() && element.getAsJsonObject().has("action")) {
                ToolAction action = new ToolAction(element.getAsJsonObject().get("action").getAsString());
                return fromIngredients(Collections.singletonList(IngredientEntry.forCustom(action, null, action.getDisplayStacks())));
            }
            if (element.isJsonArray()) {
                IngredientMatcher combined = new IngredientMatcher(new ArrayList<>(), new ArrayList<>(), false);
                for (JsonElement child : element.getAsJsonArray()) {
                    IngredientMatcher part = fromJson(child, defaultNamespace);
                    combined.itemStacks.addAll(part.itemStacks);
                    combined.oreDictNames.addAll(part.oreDictNames);
                    combined.customIngredients.addAll(part.customIngredients);
                }
                return combined;
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
            return noToolRequired || !itemStacks.isEmpty() || !oreDictNames.isEmpty() || !customIngredients.isEmpty();
        }

        public boolean matches(ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            for (IngredientEntry ingredient : customIngredients) {
                if (ingredient.matches(stack)) {
                    return true;
                }
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

        public boolean matchesTool(ItemStack stack) {
            if (noToolRequired) {
                return stack.isEmpty();
            }
            if (stack.isEmpty()) {
                return false;
            }

            if (matchesDirectItem(stack)) {
                return true;
            }
            for (IngredientEntry ingredient : customIngredients) {
                if (ingredient.matches(stack)) {
                    return true;
                }
            }

            for (String oreName : oreDictNames) {
                if (matchesToolOreName(oreName, stack)) {
                    return true;
                }
            }
            return false;
        }

        private boolean matchesToolOreName(String oreName, ItemStack stack) {
            if ("toolKnife".equals(oreName)) {
                return ItemKnife.isKnife(stack);
            }
            if (Configuration.toolAxeUsesItemAxeCheck && "toolAxe".equals(oreName)) {
                return stack.getItem() instanceof ItemAxe;
            }
            for (ItemStack oreStack : OreDictionary.getOres(oreName)) {
                if (isItemAndMetaMatch(oreStack, stack)) {
                    return true;
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

        private static boolean isItemAndMetaMatch(ItemStack candidate, ItemStack target) {
            if (candidate.isEmpty() || target.isEmpty() || candidate.getItem() != target.getItem()) {
                return false;
            }

            int candidateMeta = candidate.getMetadata();
            if (candidateMeta == OreDictionary.WILDCARD_VALUE) {
                return true;
            }
            return candidateMeta == target.getMetadata();
        }

        public List<ItemStack> asStacks() {
            if (noToolRequired) {
                return Collections.emptyList();
            }
            List<ItemStack> result = new ArrayList<>();
            for (IngredientEntry ingredient : customIngredients) {
                result.addAll(ingredient.getDisplayStacks());
            }
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
                } catch (NumberFormatException exception) {
                    FarmersDelightLegacy.LOGGER.error("Invalid item metadata in cutting recipe: {}", token, exception);
                    return null;
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
        for (CuttingRecipeEntry recipe : RECIPES) {
            if (!SCRIPT_RECIPES.containsKey(recipe.recipeId)) {
                result.add(recipe);
            }
        }
        return result;
    }
}

