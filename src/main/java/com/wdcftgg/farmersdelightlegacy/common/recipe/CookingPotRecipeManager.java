package com.wdcftgg.farmersdelightlegacy.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CookingPotRecipeManager {

    private static final float DEFAULT_RECIPE_EXPERIENCE = 0.35F;

    private static final List<CookingPotRecipe> RECIPES = new ArrayList<>();
    private static final Map<String, CookingPotRecipe> SCRIPT_RECIPES = new LinkedHashMap<>();
    private static boolean loaded;

    private CookingPotRecipeManager() {
    }

    public static CookingPotRecipe findRecipe(List<ItemStack> inputStacks) {
        ensureLoaded();
        for (CookingPotRecipe recipe : getAllRecipes()) {
            if (matches(recipe, inputStacks)) {
                return recipe;
            }
        }
        return null;
    }

    public static List<CookingPotRecipe> getRecipes() {
        ensureLoaded();
        return getAllRecipes();
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

            Item ingredientItem = itemOf(ingredientPath, FarmersDelightLegacy.MOD_ID);
            if (ingredientItem == null) {
                return false;
            }
            ingredients.add(CookingPotRecipe.IngredientEntry.forItem(ingredientItem));
        }

        if (ingredients.isEmpty()) {
            return false;
        }

        CookingPotRecipe recipe = new CookingPotRecipe(key, ingredients, resultStack, outputContainer,
                Math.max(1, cookTime), Math.max(0.0F, experience), hasContainerDefinition);
        synchronized (SCRIPT_RECIPES) {
            SCRIPT_RECIPES.put(key, recipe);
        }
        return true;
    }

    public static int removeRecipesByOutput(ItemStack outputStack) {
        ensureLoaded();
        if (outputStack.isEmpty()) {
            return 0;
        }

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

    private static boolean matches(CookingPotRecipe recipe, List<ItemStack> inputStacks) {
        List<CookingPotRecipe.IngredientEntry> expected = new ArrayList<>(recipe.getIngredients());

        for (ItemStack inputStack : inputStacks) {
            if (inputStack.isEmpty()) {
                continue;
            }

            int matchedIndex = -1;
            for (int index = 0; index < expected.size(); index++) {
                if (expected.get(index).matches(inputStack)) {
                    matchedIndex = index;
                    break;
                }
            }
            if (matchedIndex < 0) {
                return false;
            }
            expected.remove(matchedIndex);
        }

        return expected.isEmpty();
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
        for (RecipeResourceScanner.RecipeJsonResource recipeResource : RecipeResourceScanner.scan("cooking_pot")) {
            JsonObject recipeJson = recipeResource.getJsonObject();
            String sourceModId = recipeResource.getModId();
            JsonArray ingredientsJson = recipeJson.getAsJsonArray("ingredients");
            JsonObject resultJson = recipeJson.getAsJsonObject("result");
            if (ingredientsJson == null || resultJson == null) {
                continue;
            }

            List<String> ingredients = new ArrayList<>();
            for (JsonElement ingredientElement : ingredientsJson) {
                if (ingredientElement.isJsonPrimitive()) {
                    ingredients.add(ingredientElement.getAsString());
                    continue;
                }
                if (!ingredientElement.isJsonObject()) {
                    continue;
                }

                JsonObject ingredientObject = ingredientElement.getAsJsonObject();
                if (ingredientObject.has("item")) {
                    ingredients.add(composeItemToken(
                            ingredientObject.get("item").getAsString(),
                            readMetadata(ingredientObject),
                            ingredientObject.has("data")));
                }
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
                }
            }
            if (!resultItem.isEmpty()) {
                addRecipe(ingredients.toArray(new String[0]),
                        stackOf(resultItem, Math.max(1, resultCount), resultMetadata, hasResultMetadata, sourceModId),
                        outputContainer,
                        cookTime, experience, hasContainerDefinition, recipeResource.getRecipeId(), sourceModId);
            }
        }
    }

    private static void addRecipe(String[] ingredientPaths, ItemStack resultStack, ItemStack outputContainer,
                                  int cookTime, float experience, boolean hasContainerDefinition, String recipeId, String defaultNamespace) {
        if (resultStack.isEmpty()) {
            return;
        }

        List<CookingPotRecipe.IngredientEntry> ingredients = new ArrayList<>();
        for (String ingredientPath : ingredientPaths) {
            if (ingredientPath == null || ingredientPath.isEmpty()) {
                return;
            }

            if (ingredientPath.startsWith("ore:")) {
                String oreDictName = ingredientPath.substring(4);
                if (oreDictName.isEmpty()) {
                    return;
                }
                ingredients.add(CookingPotRecipe.IngredientEntry.forOreDict(oreDictName));
                continue;
            }

            Item ingredientItem = itemOf(ingredientPath, defaultNamespace);
            if (ingredientItem == null && parseItemToken(ingredientPath, defaultNamespace) == null) {
                return;
            }

            ParsedItemToken parsedItemToken = parseItemToken(ingredientPath, defaultNamespace);
            if (parsedItemToken == null || parsedItemToken.item == null) {
                return;
            }
            ingredients.add(CookingPotRecipe.IngredientEntry.forItem(parsedItemToken.item, parsedItemToken.metadata));
        }
        RECIPES.add(new CookingPotRecipe(recipeId, ingredients, resultStack, outputContainer, cookTime, experience, hasContainerDefinition));
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
                } catch (NumberFormatException ignored) {
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
        result.addAll(RECIPES);
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

