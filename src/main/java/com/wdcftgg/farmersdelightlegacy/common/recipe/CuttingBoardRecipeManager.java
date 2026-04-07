package com.wdcftgg.farmersdelightlegacy.common.recipe;

import com.wdcftgg.farmersdelight.Tags;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModOreDictionary;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class CuttingBoardRecipeManager {

    private static final Gson GSON = new Gson();
    private static final String RECIPES_BASE = "assets/farmersdelight/fd_recipes/cutting_board/";
    private static final String RECIPES_INDEX = RECIPES_BASE + "_index.json";
    private static final String DEFAULT_TOOL_TOKEN = "ore:toolKnife";
    private static final Map<String, String> TAG_TO_OREDICT = ModOreDictionary.getTagToOreDictMap();

    private static final List<CuttingRecipeEntry> RECIPES = new ArrayList<>();
    private static boolean loaded;

    private CuttingBoardRecipeManager() {
    }

    public static boolean hasRecipe(ItemStack inputStack, ItemStack toolStack) {
        ensureLoaded();
        if (inputStack.isEmpty() || toolStack.isEmpty()) {
            return false;
        }

        for (CuttingRecipeEntry recipeEntry : RECIPES) {
            if (recipeEntry.matches(inputStack, toolStack)) {
                return true;
            }
        }
        return false;
    }

    public static List<ItemStack> getProcessedResults(ItemStack inputStack, ItemStack toolStack, Random random) {
        ensureLoaded();
        if (inputStack.isEmpty() || toolStack.isEmpty()) {
            return Collections.emptyList();
        }

        for (CuttingRecipeEntry recipeEntry : RECIPES) {
            if (recipeEntry.matches(inputStack, toolStack)) {
                return recipeEntry.rollResults(random);
            }
        }
        return Collections.emptyList();
    }

    public static boolean isUsedAsRecipeTool(ItemStack toolStack) {
        ensureLoaded();
        if (toolStack.isEmpty()) {
            return false;
        }
        for (CuttingRecipeEntry recipeEntry : RECIPES) {
            if (recipeEntry.toolMatcher.matches(toolStack)) {
                return true;
            }
        }
        return false;
    }

    public static List<CuttingBoardRecipeView> getRecipes() {
        ensureLoaded();
        List<CuttingBoardRecipeView> result = new ArrayList<>();
        for (CuttingRecipeEntry recipeEntry : RECIPES) {
            result.add(new CuttingBoardRecipeView(
                    recipeEntry.inputMatcher.asStacks(),
                    recipeEntry.toolMatcher.asStacks(),
                    recipeEntry.getDisplayResults(),
                    recipeEntry.getDisplayResultChances()));
        }
        return result;
    }

    public static final class CuttingBoardRecipeView {
        private final List<ItemStack> inputOptions;
        private final List<ItemStack> toolOptions;
        private final List<ItemStack> resultStacks;
        private final List<Float> resultChances;

        private CuttingBoardRecipeView(List<ItemStack> inputOptions, List<ItemStack> toolOptions, List<ItemStack> resultStacks, List<Float> resultChances) {
            this.inputOptions = copyStacks(inputOptions);
            this.toolOptions = copyStacks(toolOptions);
            this.resultStacks = copyStacks(resultStacks);
            this.resultChances = new ArrayList<>(resultChances);
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
        JsonArray indexArray = readJsonArray(RECIPES_INDEX);
        if (indexArray == null) {
            return;
        }

        for (JsonElement indexElement : indexArray) {
            if (!indexElement.isJsonPrimitive()) {
                continue;
            }

            String recipePath = indexElement.getAsString();
            JsonObject recipeJson = readJsonObject(RECIPES_BASE + recipePath);
            if (recipeJson == null) {
                continue;
            }

            parseAndRegister(recipeJson);
        }
    }

    private static void parseAndRegister(JsonObject recipeJson) {
        IngredientMatcher inputMatcher = readInput(recipeJson);
        IngredientMatcher toolMatcher = readTool(recipeJson);
        List<ResultEntry> resultEntries = readResults(recipeJson);
        if (!inputMatcher.isValid() || !toolMatcher.isValid() || resultEntries.isEmpty()) {
            return;
        }
        RECIPES.add(new CuttingRecipeEntry(inputMatcher, toolMatcher, resultEntries));
    }

    private static IngredientMatcher readInput(JsonObject recipeJson) {
        if (recipeJson.has("ingredient")) {
            IngredientMatcher ingredientMatcher = IngredientMatcher.fromJson(recipeJson.get("ingredient"));
            if (ingredientMatcher.isValid()) {
                return ingredientMatcher;
            }
        }
        if (recipeJson.has("input") && recipeJson.get("input").isJsonPrimitive()) {
            return IngredientMatcher.fromToken(recipeJson.get("input").getAsString());
        }
        if (recipeJson.has("ingredients") && recipeJson.get("ingredients").isJsonArray()) {
            return IngredientMatcher.fromJson(recipeJson.get("ingredients"));
        }
        return IngredientMatcher.invalid();
    }

    private static IngredientMatcher readTool(JsonObject recipeJson) {
        if (recipeJson.has("tool")) {
            IngredientMatcher toolMatcher = IngredientMatcher.fromJson(recipeJson.get("tool"));
            if (toolMatcher.isValid()) {
                return toolMatcher;
            }
        }
        return IngredientMatcher.fromToken(DEFAULT_TOOL_TOKEN);
    }

    private static List<ResultEntry> readResults(JsonObject recipeJson) {
        List<ResultEntry> resultEntries = new ArrayList<>();

        if (recipeJson.has("output") && recipeJson.get("output").isJsonPrimitive()) {
            String outputPath = recipeJson.get("output").getAsString();
            int outputCount = recipeJson.has("count") ? Math.max(1, recipeJson.get("count").getAsInt()) : 1;
            Item outputItem = itemOf(outputPath);
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
                    ResultEntry entry = parseResultEntry(element);
                    if (entry != null) {
                        resultEntries.add(entry);
                    }
                }
            } else {
                ResultEntry single = parseResultEntry(resultElement);
                if (single != null) {
                    resultEntries.add(single);
                }
            }
        }

        return resultEntries;
    }

    private static ResultEntry parseResultEntry(JsonElement resultElement) {
        if (resultElement == null || !resultElement.isJsonObject()) {
            return null;
        }

        JsonObject resultObject = resultElement.getAsJsonObject();
        if (!resultObject.has("item") || !resultObject.get("item").isJsonPrimitive()) {
            return null;
        }

        Item resultItem = itemOf(resultObject.get("item").getAsString());
        if (resultItem == null) {
            return null;
        }

        int count = 1;
        if (resultObject.has("count")) {
            count = Math.max(1, resultObject.get("count").getAsInt());
        }

        float chance = 1.0F;
        if (resultObject.has("chance")) {
            chance = clampChance((float) resultObject.get("chance").getAsDouble());
        } else if (resultObject.has("probability")) {
            chance = clampChance((float) resultObject.get("probability").getAsDouble());
        }

        return new ResultEntry(new ItemStack(resultItem, count), chance);
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

        private final IngredientMatcher inputMatcher;
        private final IngredientMatcher toolMatcher;
        private final List<ResultEntry> resultEntries;

        private CuttingRecipeEntry(IngredientMatcher inputMatcher, IngredientMatcher toolMatcher, List<ResultEntry> resultEntries) {
            this.inputMatcher = inputMatcher;
            this.toolMatcher = toolMatcher;
            this.resultEntries = new ArrayList<>(resultEntries);
        }

        private boolean matches(ItemStack inputStack, ItemStack toolStack) {
            return inputMatcher.matches(inputStack) && toolMatcher.matches(toolStack);
        }

        private List<ItemStack> rollResults(Random random) {
            Random actualRandom = random == null ? new Random() : random;
            List<ItemStack> rolledResults = new ArrayList<>();
            for (ResultEntry resultEntry : resultEntries) {
                if (resultEntry.chance >= 1.0F || actualRandom.nextFloat() <= resultEntry.chance) {
                    rolledResults.add(resultEntry.stack.copy());
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

        private final List<Item> items;
        private final List<String> oreDictNames;

        private IngredientMatcher(List<Item> items, List<String> oreDictNames) {
            this.items = items;
            this.oreDictNames = oreDictNames;
        }

        private static IngredientMatcher invalid() {
            return new IngredientMatcher(Collections.<Item>emptyList(), Collections.<String>emptyList());
        }

        private static IngredientMatcher fromToken(String token) {
            if (token == null || token.isEmpty()) {
                return invalid();
            }

            List<Item> items = new ArrayList<>();
            List<String> oreDictNames = new ArrayList<>();
            addToken(token, items, oreDictNames);
            return new IngredientMatcher(items, oreDictNames);
        }

        private static IngredientMatcher fromJson(JsonElement element) {
            if (element == null || element.isJsonNull()) {
                return invalid();
            }

            List<Item> items = new ArrayList<>();
            List<String> oreDictNames = new ArrayList<>();
            collectTokens(element, items, oreDictNames);
            return new IngredientMatcher(items, oreDictNames);
        }

        private static void collectTokens(JsonElement element, List<Item> items, List<String> oreDictNames) {
            if (element == null || element.isJsonNull()) {
                return;
            }

            if (element.isJsonPrimitive()) {
                addToken(element.getAsString(), items, oreDictNames);
                return;
            }

            if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                for (JsonElement child : array) {
                    collectTokens(child, items, oreDictNames);
                }
                return;
            }

            if (!element.isJsonObject()) {
                return;
            }

            JsonObject jsonObject = element.getAsJsonObject();
            if (jsonObject.has("item") && jsonObject.get("item").isJsonPrimitive()) {
                addToken(jsonObject.get("item").getAsString(), items, oreDictNames);
            }
            if (jsonObject.has("tag") && jsonObject.get("tag").isJsonPrimitive()) {
                String oreToken = tagToOreToken(jsonObject.get("tag").getAsString());
                if (oreToken != null) {
                    addToken(oreToken, items, oreDictNames);
                }
            }
            if (jsonObject.has("action") && jsonObject.get("action").isJsonPrimitive()) {
                String actionToken = actionToOreToken(jsonObject.get("action").getAsString());
                if (actionToken != null) {
                    addToken(actionToken, items, oreDictNames);
                }
            }
        }

        private static void addToken(String token, List<Item> items, List<String> oreDictNames) {
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

            Item item = itemOf(token);
            if (item != null && !items.contains(item)) {
                items.add(item);
            }
        }

        private boolean isValid() {
            return !items.isEmpty() || !oreDictNames.isEmpty();
        }

        private boolean matches(ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            if (items.contains(stack.getItem())) {
                return true;
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

        private List<ItemStack> asStacks() {
            List<ItemStack> result = new ArrayList<>();
            for (Item item : items) {
                result.add(new ItemStack(item));
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


    private static Item itemOf(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        ResourceLocation itemId;
        if (path.contains(":")) {
            itemId = new ResourceLocation(path);
        } else {
            itemId = new ResourceLocation(Tags.MOD_ID, path);
        }
        return ForgeRegistries.ITEMS.getValue(itemId);
    }

    private static JsonArray readJsonArray(String classpathPath) {
        try (InputStream inputStream = CuttingBoardRecipeManager.class.getClassLoader().getResourceAsStream(classpathPath)) {
            if (inputStream == null) {
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return GSON.fromJson(reader, JsonArray.class);
            }
        } catch (IOException ignored) {
            return null;
        }
    }

    private static JsonObject readJsonObject(String classpathPath) {
        try (InputStream inputStream = CuttingBoardRecipeManager.class.getClassLoader().getResourceAsStream(classpathPath)) {
            if (inputStream == null) {
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return GSON.fromJson(reader, JsonObject.class);
            }
        } catch (IOException ignored) {
            return null;
        }
    }
}

