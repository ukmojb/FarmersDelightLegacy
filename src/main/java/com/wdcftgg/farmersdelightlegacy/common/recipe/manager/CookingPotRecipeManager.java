package com.wdcftgg.farmersdelightlegacy.common.recipe.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CookingPotRecipe;
import com.wdcftgg.farmersdelightlegacy.common.recipe.condition.IngredientsAvailableCondition;
import com.wdcftgg.farmersdelightlegacy.common.util.RecipeResourceScanner;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class CookingPotRecipeManager {

    private static final float DEFAULT_RECIPE_EXPERIENCE = 0.35F;

    private static final List<CookingPotRecipe> RECIPES = new ArrayList<>();
    private static final Map<String, CookingPotRecipe> SCRIPT_RECIPES = new LinkedHashMap<>();
    private static final List<Predicate<CookingPotRecipe>> REMOVED_BUILT_IN_RECIPES = new ArrayList<>();
    private static boolean loaded;

    private CookingPotRecipeManager() {
    }

    public static CookingPotRecipe findRecipe(List<ItemStack> inputStacks) {
        ensureLoaded();
        for (CookingPotRecipe recipe : getAllRecipes()) {
            if (recipe.matchIngredients(inputStacks) != null) {
                return recipe;
            }
        }
        return null;
    }

    public static List<CookingPotRecipe> getRecipes() {
        ensureLoaded();
        return getAllRecipes();
    }

    public static boolean registerRecipe(CookingPotRecipe recipe) {
        if (recipe == null || recipe.getRecipeId().isEmpty() || recipe.getIngredients().isEmpty()
                || recipe.getIngredients().size() > 6 || recipe.getIngredients().contains(null)
                || recipe.getResultStack().isEmpty()) {
            FarmersDelightLegacy.LOGGER.error("Invalid cooking recipe: expected an ID, 1 to 6 ingredients and a nonempty display output");
            return false;
        }
        synchronized (SCRIPT_RECIPES) {
            SCRIPT_RECIPES.put(recipe.getRecipeId(), recipe);
        }
        return true;
    }

    public static boolean registerScriptRecipe(String key, String[] ingredientPaths, ItemStack resultStack, ItemStack outputContainer,
                                               int cookTime, float experience, boolean hasContainerDefinition) {
        if (key == null || key.isEmpty() || ingredientPaths == null || resultStack.isEmpty()) {
            return false;
        }

        List<CookingPotRecipe.IngredientEntry> ingredients = new ArrayList<>();
        for (String ingredientPath : ingredientPaths) {
            if (ingredientPath == null || ingredientPath.isEmpty()) {
                return false;
            }

            if (ingredientPath.startsWith("ore:")) {
                String oreDictName = ingredientPath.substring(4);
                if (oreDictName.isEmpty()) {
                    return false;
                }
                ingredients.add(CookingPotRecipe.IngredientEntry.forOreDict(oreDictName));
                continue;
            }

            ParsedItemToken parsedItemToken = parseItemToken(ingredientPath, FarmersDelightLegacy.MOD_ID);
            if (parsedItemToken == null || parsedItemToken.item == null) {
                return false;
            }
            ingredients.add(CookingPotRecipe.IngredientEntry.forItem(parsedItemToken.item, parsedItemToken.metadata));
        }

        return registerScriptRecipe(key, ingredients, resultStack, outputContainer, cookTime, experience, hasContainerDefinition);
    }

    public static boolean registerScriptRecipe(String key, List<CookingPotRecipe.IngredientEntry> ingredients,
                                               ItemStack resultStack, ItemStack outputContainer,
                                               int cookTime, float experience, boolean hasContainerDefinition) {
        return registerScriptRecipe(key, ingredients, resultStack, outputContainer, cookTime, experience, hasContainerDefinition, "");
    }

    public static boolean registerScriptRecipe(String key, List<CookingPotRecipe.IngredientEntry> ingredients,
                                               ItemStack resultStack, ItemStack outputContainer,
                                               int cookTime, float experience, boolean hasContainerDefinition, String group) {
        if (key == null || key.isEmpty() || ingredients == null || ingredients.isEmpty()
                || ingredients.size() > 6 || ingredients.contains(null) || resultStack.isEmpty() || group == null) {
            FarmersDelightLegacy.LOGGER.error("Invalid cooking recipe {}: expected 1 to 6 ingredients and a nonempty output", key);
            return false;
        }

        CookingPotRecipe recipe = new CookingPotRecipe(key, ingredients, resultStack, outputContainer,
                Math.max(1, cookTime), Math.max(0.0F, experience), hasContainerDefinition, group);
        return registerRecipe(recipe);
    }

    public static int removeRecipesByOutput(ItemStack outputStack) {
        ensureLoaded();
        if (outputStack.isEmpty()) {
            return 0;
        }
        ItemStack removedOutput = outputStack.copy();
        REMOVED_BUILT_IN_RECIPES.add(recipe -> matchesOutput(recipe, removedOutput));
        int removedCount = 0;
        synchronized (SCRIPT_RECIPES) {
            java.util.Iterator<Map.Entry<String, CookingPotRecipe>> iterator = SCRIPT_RECIPES.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, CookingPotRecipe> entry = iterator.next();
                if (matchesOutput(entry.getValue(), outputStack)) {
                    iterator.remove();
                    removedCount++;
                }
            }
        }

        java.util.Iterator<CookingPotRecipe> iterator = RECIPES.iterator();
        while (iterator.hasNext()) {
            if (matchesOutput(iterator.next(), outputStack)) {
                iterator.remove();
                removedCount++;
            }
        }
        return removedCount;
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
        REMOVED_BUILT_IN_RECIPES.add(recipe -> recipe.getRecipeId().equals(key));
        boolean removed = unregisterScriptRecipe(key);
        return RECIPES.removeIf(recipe -> recipe.getRecipeId().equals(key)) || removed;
    }

    public static int removeRecipes(java.util.function.Predicate<CookingPotRecipe> predicate) {
        ensureLoaded();
        REMOVED_BUILT_IN_RECIPES.add(predicate);
        int before = RECIPES.size() + SCRIPT_RECIPES.size();
        RECIPES.removeIf(predicate);
        synchronized (SCRIPT_RECIPES) {
            SCRIPT_RECIPES.values().removeIf(predicate);
        }
        return before - RECIPES.size() - SCRIPT_RECIPES.size();
    }

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }
        reloadBuiltInRecipes();
    }

    public static void reloadBuiltInRecipes() {
        RECIPES.clear();
        loadJsonRecipes();
        loaded = true;
    }

    private static void loadJsonRecipes() {
        for (RecipeResourceScanner.RecipeJsonResource recipeResource : RecipeResourceScanner.scan("cooking_pot")) {
            JsonObject recipeJson = recipeResource.getJsonObject();
            if (recipeJson.has("conditions")
                    && !IngredientsAvailableCondition.matches(recipeJson.getAsJsonObject("conditions"))) {
                continue;
            }
            String sourceModId = recipeResource.getModId();
            JsonArray ingredientsJson = recipeJson.getAsJsonArray("ingredients");
            JsonObject resultJson = recipeJson.getAsJsonObject("result");
            if (ingredientsJson == null || resultJson == null) {
                continue;
            }

            List<CookingPotRecipe.IngredientEntry> ingredients = new ArrayList<>();
            for (JsonElement ingredientElement : ingredientsJson) {
                CookingPotRecipe.IngredientEntry ingredient = readIngredient(ingredientElement, sourceModId);
                if (ingredient == null) {
                    FarmersDelightLegacy.LOGGER.error("Invalid cooking ingredient in {}: {}", recipeResource.getRecipeId(), ingredientElement);
                    ingredients.clear();
                    break;
                }
                ingredients.add(ingredient);
            }
            if (ingredients.isEmpty() || ingredients.size() > 6) {
                continue;
            }

            String resultItem = resultJson.has("item") ? resultJson.get("item").getAsString() : "";
            int resultCount = resultJson.has("count") ? resultJson.get("count").getAsInt() : 1;
            int resultMetadata = readMetadata(resultJson);
            boolean hasResultMetadata = resultJson.has("data");
            int cookTime = recipeJson.has("cookingtime") ? Math.max(1, recipeJson.get("cookingtime").getAsInt()) : 200;
            float experience = recipeJson.has("experience") ? Math.max(0.0F, recipeJson.get("experience").getAsFloat()) : DEFAULT_RECIPE_EXPERIENCE;
            ItemStack outputContainer = ItemStack.EMPTY;
            boolean hasContainerDefinition = recipeJson.has("container");
            if (recipeJson.has("container") && recipeJson.get("container").isJsonObject()) {
                JsonObject containerJson = recipeJson.getAsJsonObject("container");
                String containerItemPath = containerJson.has("item") ? containerJson.get("item").getAsString() : "";
                int containerCount = containerJson.has("count") ? Math.max(1, containerJson.get("count").getAsInt()) : 1;
                if (!containerItemPath.isEmpty()) {
                    outputContainer = stackOf(containerItemPath, containerCount, readMetadata(containerJson),
                            containerJson.has("data"), sourceModId);
                    if (!outputContainer.isEmpty() && containerJson.has("nbt")) {
                        outputContainer = CraftingHelper.getItemStack(containerJson, new JsonContext(sourceModId));
                    }
                }
            }
            if (!resultItem.isEmpty()) {
                ItemStack result = stackOf(resultItem, Math.max(1, resultCount), resultMetadata, hasResultMetadata, sourceModId);
                if (!result.isEmpty() && resultJson.has("nbt")) {
                    result = CraftingHelper.getItemStack(resultJson, new JsonContext(sourceModId));
                }
                addRecipe(ingredients,
                        result,
                        outputContainer,
                        cookTime, experience, hasContainerDefinition, recipeResource.getRecipeId(),
                        recipeJson.has("group") ? recipeJson.get("group").getAsString() : "");
            }
        }
    }

    private static void addRecipe(List<CookingPotRecipe.IngredientEntry> ingredients, ItemStack resultStack, ItemStack outputContainer,
                                  int cookTime, float experience, boolean hasContainerDefinition, String recipeId, String group) {
        if (resultStack.isEmpty()) {
            return;
        }

        CookingPotRecipe recipe = new CookingPotRecipe(recipeId, ingredients, resultStack, outputContainer, cookTime, experience, hasContainerDefinition, group);
        if (REMOVED_BUILT_IN_RECIPES.stream().noneMatch(predicate -> predicate.test(recipe))) {
            RECIPES.add(recipe);
        }
    }

    private static CookingPotRecipe.IngredientEntry readIngredient(JsonElement json, String namespace) {
        if (json.isJsonArray()) {
            List<CookingPotRecipe.IngredientEntry> alternatives = new ArrayList<>();
            for (JsonElement entry : json.getAsJsonArray()) {
                CookingPotRecipe.IngredientEntry ingredient = readIngredient(entry, namespace);
                if (ingredient == null) {
                    return null;
                }
                alternatives.add(ingredient);
            }
            return alternatives.isEmpty() ? null : CookingPotRecipe.IngredientEntry.forAlternatives(alternatives);
        }
        String token;
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            token = json.getAsString();
        } else if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            if (object.has("ore")) {
                token = "ore:" + object.get("ore").getAsString();
            } else if (object.has("item")) {
                token = composeItemToken(object.get("item").getAsString(), readMetadata(object), object.has("data"));
            } else {
                return null;
            }
        } else {
            return null;
        }
        if (token.startsWith("ore:")) {
            return token.length() > 4 ? CookingPotRecipe.IngredientEntry.forOreDict(token.substring(4)) : null;
        }
        ParsedItemToken parsed = parseItemToken(token, namespace);
        return parsed == null ? null : CookingPotRecipe.IngredientEntry.forItem(parsed.item, parsed.metadata);
    }

    private static boolean matchesOutput(CookingPotRecipe recipe, ItemStack outputStack) {
        ItemStack resultStack = recipe.getResultStack();
        return !resultStack.isEmpty() && ItemStack.areItemsEqual(resultStack, outputStack);
    }

    private static Item itemOf(String path, String defaultNamespace) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        int separatorIndex = path.lastIndexOf('@');
        if (separatorIndex > 0) {
            path = path.substring(0, separatorIndex);
        }
        ResourceLocation itemId;
        if (path.contains(":")) {
            itemId = new ResourceLocation(path);
        } else {
            itemId = new ResourceLocation(defaultNamespace, path);
        }
        return ForgeRegistries.ITEMS.getValue(itemId);
    }

    private static ItemStack stackOf(String path, int count, String defaultNamespace) {
        return stackOf(path, count, 0, false, defaultNamespace);
    }

    private static ItemStack stackOf(String path, int count, int metadata, boolean hasMetadata, String defaultNamespace) {
        Item item = itemOf(path, defaultNamespace);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, count, hasMetadata ? metadata : 0);
    }

    private static int readMetadata(JsonObject jsonObject) {
        if (jsonObject == null || !jsonObject.has("data")) {
            return 0;
        }
        return Math.max(0, jsonObject.get("data").getAsInt());
    }

    private static String composeItemToken(String itemPath, int metadata, boolean hasMetadata) {
        if (itemPath == null || itemPath.isEmpty()) {
            return itemPath;
        }
        return hasMetadata ? itemPath + "@" + metadata : itemPath;
    }

    private static ParsedItemToken parseItemToken(String token, String defaultNamespace) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        String itemPath = token;
        int metadata = OreDictionary.WILDCARD_VALUE;
        int separatorIndex = token.lastIndexOf('@');
        if (separatorIndex >= 0 && separatorIndex + 1 < token.length()) {
            itemPath = token.substring(0, separatorIndex);
            String metadataToken = token.substring(separatorIndex + 1);
            if ("*".equals(metadataToken)) {
                metadata = OreDictionary.WILDCARD_VALUE;
            } else {
                try {
                    metadata = Math.max(0, Integer.parseInt(metadataToken));
                } catch (NumberFormatException exception) {
                    FarmersDelightLegacy.LOGGER.error("Invalid cooking ingredient metadata: {}", token, exception);
                    return null;
                }
            }
        }

        Item item = itemOf(itemPath, defaultNamespace);
        if (item == null) {
            return null;
        }
        return new ParsedItemToken(item, metadata);
    }

    private static List<CookingPotRecipe> getAllRecipes() {
        List<CookingPotRecipe> result = new ArrayList<>();
        synchronized (SCRIPT_RECIPES) {
            result.addAll(SCRIPT_RECIPES.values());
        }
        for (CookingPotRecipe recipe : RECIPES) {
            if (!SCRIPT_RECIPES.containsKey(recipe.getRecipeId())) {
                result.add(recipe);
            }
        }
        return result;
    }

    private static final class ParsedItemToken {
        private final Item item;
        private final int metadata;

        private ParsedItemToken(Item item, int metadata) {
            this.item = item;
            this.metadata = metadata;
        }
    }
}

