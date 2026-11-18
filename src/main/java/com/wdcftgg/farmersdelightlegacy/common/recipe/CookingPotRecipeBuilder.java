package com.wdcftgg.farmersdelightlegacy.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CookingPotRecipe.IngredientEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public final class CookingPotRecipeBuilder {
    private final List<IngredientEntry> ingredients = new ArrayList<>();
    private final ItemStack result;
    private final int cookingTime;
    private final float experience;
    private ItemStack container = ItemStack.EMPTY;
    private boolean hasContainer;
    private String namespace;
    private String group = "";

    public CookingPotRecipeBuilder(ItemStack result, int cookingTime, float experience) {
        if (result.isEmpty() || result.getCount() > 64 || cookingTime <= 0
                || !Float.isFinite(experience) || experience < 0) {
            throw new IllegalArgumentException("Cooking recipe requires an output of 1 to 64, positive time and nonnegative finite experience");
        }
        this.result = result.copy();
        this.cookingTime = cookingTime;
        this.experience = experience;
    }

    public static CookingPotRecipeBuilder cookingPotRecipe(Item result, int count, int time, float experience) {
        return new CookingPotRecipeBuilder(new ItemStack(result, count), time, experience);
    }

    public static CookingPotRecipeBuilder cookingPotRecipe(Item result, int count, int time, float experience, Item container) {
        return cookingPotRecipe(result, count, time, experience).container(new ItemStack(container));
    }

    public CookingPotRecipeBuilder addIngredient(Item item) {
        return addIngredient(item, 1);
    }

    public CookingPotRecipeBuilder addIngredient(Item item, int quantity) {
        return addIngredient(IngredientEntry.forItem(item), quantity);
    }

    public CookingPotRecipeBuilder addIngredient(IngredientEntry ingredient) {
        return addIngredient(ingredient, 1);
    }

    public CookingPotRecipeBuilder addIngredient(IngredientEntry ingredient, int quantity) {
        Objects.requireNonNull(ingredient);
        if (quantity < 1 || quantity > 6 - ingredients.size()) {
            throw new IllegalArgumentException("Cooking recipes require 1 to 6 occupied ingredient slots");
        }
        for (int i = 0; i < quantity; i++) {
            ingredients.add(ingredient);
        }
        return this;
    }

    public CookingPotRecipeBuilder addOreIngredient(String ore) {
        return addIngredient(IngredientEntry.forOreDict(ore));
    }

    public CookingPotRecipeBuilder addAlternatives(IngredientEntry... alternatives) {
        return addIngredient(IngredientEntry.forAlternatives(java.util.Arrays.asList(alternatives)));
    }

    public CookingPotRecipeBuilder container(ItemStack container) {
        this.container = container.copy();
        hasContainer = true;
        return this;
    }

    public CookingPotRecipeBuilder group(String group) {
        this.group = Objects.requireNonNull(group);
        return this;
    }

    public CookingPotRecipeBuilder setNamespace(String namespace) {
        this.namespace = Objects.requireNonNull(namespace);
        return this;
    }

    public Item getResult() {
        return result.getItem();
    }

    public CookingPotRecipe build(ResourceLocation id) {
        if (ingredients.isEmpty()) {
            throw new IllegalStateException("Cooking recipe has no ingredients");
        }
        return new CookingPotRecipe(id.toString(), ingredients, result, container, cookingTime, experience, hasContainer, group);
    }

    public void save(BiConsumer<ResourceLocation, JsonObject> output, ResourceLocation id) {
        CookingPotRecipe recipe = build(id);
        JsonObject json = new JsonObject();
        JsonArray inputs = new JsonArray();
        for (IngredientEntry ingredient : recipe.getIngredients()) {
            inputs.add(RecipeJson.ingredient(ingredient));
        }
        json.add("ingredients", inputs);
        json.add("result", RecipeJson.stack(result));
        if (hasContainer) {
            json.add("container", container.isEmpty() ? new JsonObject() : RecipeJson.stack(container));
        }
        json.addProperty("cookingtime", cookingTime);
        json.addProperty("experience", experience);
        json.addProperty("group", group);
        output.accept(id, json);
    }

    public void save(BiConsumer<ResourceLocation, JsonObject> output) {
        ResourceLocation resultId = Objects.requireNonNull(result.getItem().getRegistryName());
        save(output, new ResourceLocation(namespace == null ? resultId.getNamespace() : namespace, "cooking/" + resultId.getPath()));
    }

    public void saveToFD(BiConsumer<ResourceLocation, JsonObject> output) {
        setNamespace(FarmersDelightLegacy.MOD_ID).save(output);
    }
}
