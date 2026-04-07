package com.wdcftgg.farmersdelightlegacy.common.recipe;

import com.wdcftgg.farmersdelight.Tags;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class CookingPotRecipeManager {

    private static final Gson GSON = new Gson();
    private static final String RECIPES_BASE = "assets/farmersdelight/fd_recipes/cooking_pot/";
    private static final String RECIPES_INDEX = RECIPES_BASE + "_index.json";

    private static final List<CookingPotRecipe> RECIPES = new ArrayList<>();
    private static boolean loaded;

    private CookingPotRecipeManager() {
    }

    public static CookingPotRecipe findRecipe(List<ItemStack> inputStacks) {
        ensureLoaded();
        for (CookingPotRecipe recipe : RECIPES) {
            if (matches(recipe, inputStacks)) {
                return recipe;
            }
        }
        return null;
    }

    public static List<CookingPotRecipe> getRecipes() {
        ensureLoaded();
        return new ArrayList<>(RECIPES);
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
                    ingredients.add(ingredientObject.get("item").getAsString());
                }
            }

            String resultItem = resultJson.has("item") ? resultJson.get("item").getAsString() : "";
            int resultCount = resultJson.has("count") ? resultJson.get("count").getAsInt() : 1;
            int cookTime = recipeJson.has("cookingtime") ? Math.max(1, recipeJson.get("cookingtime").getAsInt()) : 200;
            ItemStack outputContainer = ItemStack.EMPTY;
            boolean hasContainerDefinition = recipeJson.has("container");
            if (recipeJson.has("container") && recipeJson.get("container").isJsonObject()) {
                JsonObject containerJson = recipeJson.getAsJsonObject("container");
                String containerItemPath = containerJson.has("item") ? containerJson.get("item").getAsString() : "";
                int containerCount = containerJson.has("count") ? Math.max(1, containerJson.get("count").getAsInt()) : 1;
                if (!containerItemPath.isEmpty()) {
                    outputContainer = stackOf(containerItemPath, containerCount);
                }
            }
            if (!resultItem.isEmpty()) {
                addRecipe(ingredients.toArray(new String[0]), stackOf(resultItem, Math.max(1, resultCount)), outputContainer, cookTime, hasContainerDefinition);
            }
        }
    }

    private static void addRecipe(String[] ingredientPaths, ItemStack resultStack, ItemStack outputContainer, int cookTime, boolean hasContainerDefinition) {
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

            Item ingredientItem = itemOf(ingredientPath);
            if (ingredientItem == null) {
                return;
            }
            ingredients.add(CookingPotRecipe.IngredientEntry.forItem(ingredientItem));
        }
        RECIPES.add(new CookingPotRecipe(ingredients, resultStack, outputContainer, cookTime, hasContainerDefinition));
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

    private static ItemStack stackOf(String path, int count) {
        Item item = itemOf(path);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, count);
    }

    private static JsonArray readJsonArray(String classpathPath) {
        try (InputStream inputStream = CookingPotRecipeManager.class.getClassLoader().getResourceAsStream(classpathPath)) {
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
        try (InputStream inputStream = CookingPotRecipeManager.class.getClassLoader().getResourceAsStream(classpathPath)) {
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

