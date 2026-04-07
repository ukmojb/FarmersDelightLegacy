package com.wdcftgg.farmersdelightlegacy.common.recipe;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wdcftgg.farmersdelight.Tags;
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

public final class CampfireCookingRecipeManager {

    private static final Gson GSON = new Gson();
    private static final String RECIPES_BASE = "assets/farmersdelight/fd_recipes/campfire/";
    private static final String RECIPES_INDEX = RECIPES_BASE + "_index.json";

    private static final List<CampfireCookingRecipe> RECIPES = new ArrayList<>();
    private static boolean loaded;

    private CampfireCookingRecipeManager() {
    }

    public static CampfireCookingRecipe findRecipe(ItemStack input) {
        ensureLoaded();
        for (CampfireCookingRecipe recipe : RECIPES) {
            if (recipe.matches(input)) {
                return recipe;
            }
        }
        return null;
    }

    public static List<CampfireCookingRecipe> getRecipes() {
        ensureLoaded();
        return new ArrayList<>(RECIPES);
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

            JsonElement ingredientElement = recipeJson.get("ingredient");
            int cookingTime = recipeJson.has("cookingtime") ? recipeJson.get("cookingtime").getAsInt() : 600;
            ItemStack result = parseResult(recipeJson.get("result"));
            if (ingredientElement == null || result.isEmpty()) {
                continue;
            }

            List<CampfireCookingRecipe.IngredientEntry> ingredients = parseIngredients(ingredientElement);
            if (ingredients.isEmpty() || result.isEmpty()) {
                continue;
            }
            RECIPES.add(new CampfireCookingRecipe(ingredients, result, cookingTime));
        }
    }

    private static List<CampfireCookingRecipe.IngredientEntry> parseIngredients(JsonElement ingredientElement) {
        List<CampfireCookingRecipe.IngredientEntry> result = new ArrayList<>();
        if (ingredientElement.isJsonObject()) {
            addIngredientEntry(result, ingredientElement.getAsJsonObject());
            return result;
        }
        if (!ingredientElement.isJsonArray()) {
            return result;
        }
        for (JsonElement candidate : ingredientElement.getAsJsonArray()) {
            if (candidate.isJsonObject()) {
                addIngredientEntry(result, candidate.getAsJsonObject());
            }
        }
        return result;
    }

    private static void addIngredientEntry(List<CampfireCookingRecipe.IngredientEntry> target, JsonObject ingredientObject) {
        if (ingredientObject.has("item")) {
            addItemIngredient(target, ingredientObject.get("item"));
            return;
        }

        if (ingredientObject.has("tag")) {
            String oreDictName = convertTagToOreDict(ingredientObject.get("tag").getAsString());
            if (oreDictName != null && !oreDictName.isEmpty()) {
                target.add(CampfireCookingRecipe.IngredientEntry.forOreDict(oreDictName));
            }
        }
    }

    private static void addItemIngredient(List<CampfireCookingRecipe.IngredientEntry> target, JsonElement itemElement) {
        if (itemElement == null || itemElement.isJsonNull()) {
            return;
        }
        if (itemElement.isJsonPrimitive()) {
            Item item = itemOf(itemElement.getAsString());
            if (item != null) {
                target.add(CampfireCookingRecipe.IngredientEntry.forItem(item));
            }
            return;
        }
        if (itemElement.isJsonObject()) {
            JsonObject nestedItemObject = itemElement.getAsJsonObject();
            if (nestedItemObject.has("item")) {
                addItemIngredient(target, nestedItemObject.get("item"));
            }
        }
    }

    private static ItemStack parseResult(JsonElement resultElement) {
        if (resultElement == null || resultElement.isJsonNull()) {
            return ItemStack.EMPTY;
        }
        if (resultElement.isJsonPrimitive()) {
            return stackOf(resultElement.getAsString(), 1);
        }
        if (!resultElement.isJsonObject()) {
            return ItemStack.EMPTY;
        }

        JsonObject resultObject = resultElement.getAsJsonObject();
        if (!resultObject.has("item")) {
            return ItemStack.EMPTY;
        }

        String resultId = resultObject.get("item").getAsString();
        int resultCount = resultObject.has("count") ? Math.max(1, resultObject.get("count").getAsInt()) : 1;
        return stackOf(resultId, resultCount);
    }

    private static String convertTagToOreDict(String tagPath) {
        if (tagPath == null) {
            return null;
        }
        switch (tagPath) {
            case "forge:eggs":
            case "forge:cooked_eggs":
                return "listAllEgg";
            case "forge:raw_beef":
                return "listAllbeefraw";
            case "forge:raw_chicken":
                return "listAllchickenraw";
            case "forge:raw_mutton":
                return "listAllmuttonraw";
            case "forge:raw_fishes":
            case "forge:raw_fishes/cod":
                return "listAllfishraw";
            default:
                return null;
        }
    }

    private static Item itemOf(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        ResourceLocation itemId = path.contains(":") ? new ResourceLocation(path) : new ResourceLocation(Tags.MOD_ID, path);
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
        try (InputStream inputStream = CampfireCookingRecipeManager.class.getClassLoader().getResourceAsStream(classpathPath)) {
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
        try (InputStream inputStream = CampfireCookingRecipeManager.class.getClassLoader().getResourceAsStream(classpathPath)) {
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
