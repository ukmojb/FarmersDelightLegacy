package com.wdcftgg.farmersdelightlegacy.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.CuttingBoardRecipeManager;
import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.CuttingBoardRecipeManager.IngredientMatcher;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public final class CuttingBoardRecipeBuilder {
    private final IngredientMatcher ingredient;
    private final IngredientMatcher tool;
    private final List<ItemStack> results = new ArrayList<>();
    private final List<Float> chances = new ArrayList<>();
    private SoundEvent sound;
    private String namespace;
    private String folder = "cutting";
    private String group = "";

    public CuttingBoardRecipeBuilder(IngredientMatcher ingredient, IngredientMatcher tool, ItemStack result, float chance) {
        this.ingredient = Objects.requireNonNull(ingredient);
        this.tool = Objects.requireNonNull(tool);
        addResultWithChance(result, chance);
    }

    public static CuttingBoardRecipeBuilder cuttingRecipe(IngredientMatcher ingredient, IngredientMatcher tool, Item result) {
        return cuttingRecipe(ingredient, tool, result, 1);
    }

    public static CuttingBoardRecipeBuilder cuttingRecipe(IngredientMatcher ingredient, IngredientMatcher tool, Item result, int count) {
        return cuttingRecipe(ingredient, tool, result, count, 1);
    }

    public static CuttingBoardRecipeBuilder cuttingRecipe(IngredientMatcher ingredient, IngredientMatcher tool, Item result, int count, float chance) {
        return new CuttingBoardRecipeBuilder(ingredient, tool, new ItemStack(result, count), chance);
    }

    public CuttingBoardRecipeBuilder addResult(Item result) {
        return addResult(result, 1);
    }

    public CuttingBoardRecipeBuilder addResult(Item result, int count) {
        return addResultWithChance(new ItemStack(result, count), 1);
    }

    public CuttingBoardRecipeBuilder addResultWithChance(Item result, float chance) {
        return addResultWithChance(result, chance, 1);
    }

    public CuttingBoardRecipeBuilder addResultWithChance(Item result, float chance, int count) {
        return addResultWithChance(new ItemStack(result, count), chance);
    }

    public CuttingBoardRecipeBuilder addResultWithChance(ItemStack result, float chance) {
        if (results.size() >= 4 || result.isEmpty() || !Float.isFinite(chance) || chance < 0 || chance > 1) {
            throw new IllegalArgumentException("Cutting recipes support 1 to 4 nonempty results with probabilities between 0 and 1");
        }
        results.add(result.copy());
        chances.add(chance);
        return this;
    }

    public CuttingBoardRecipeBuilder addSound(SoundEvent sound) {
        this.sound = Objects.requireNonNull(sound);
        return this;
    }

    public CuttingBoardRecipeBuilder group(String group) {
        this.group = Objects.requireNonNull(group);
        return this;
    }

    public CuttingBoardRecipeBuilder setNamespace(String namespace) {
        this.namespace = Objects.requireNonNull(namespace);
        return this;
    }

    public CuttingBoardRecipeBuilder salvaging() {
        folder = "salvaging";
        return this;
    }

    public Item getResult() {
        List<ItemStack> options = ingredient.asStacks();
        if (options.isEmpty()) {
            throw new IllegalStateException("Cutting recipe default ID requires a registered ingredient");
        }
        return options.get(0).getItem();
    }

    public boolean register(ResourceLocation id) {
        float[] probabilities = new float[chances.size()];
        for (int i = 0; i < probabilities.length; i++) {
            probabilities[i] = chances.get(i);
        }
        return CuttingBoardRecipeManager.registerScriptRecipe(id.toString(), ingredient, tool,
                results.toArray(new ItemStack[0]), probabilities, sound, group);
    }

    public void save(BiConsumer<ResourceLocation, JsonObject> output, ResourceLocation id) {
        JsonArray inputs = RecipeJson.ingredient(ingredient);
        if (inputs.size() == 0) {
            throw new IllegalStateException("Cutting recipe has no ingredients");
        }
        JsonArray tools = RecipeJson.ingredient(tool);
        if (tools.size() == 0 && !tool.isNoToolRequired()) {
            throw new IllegalStateException("Cutting recipe has no valid tool definition");
        }
        JsonObject json = new JsonObject();
        json.add("ingredients", inputs);
        json.add("tool", tools);
        JsonArray outputs = new JsonArray();
        for (int i = 0; i < results.size(); i++) {
            JsonObject result = RecipeJson.stack(results.get(i));
            result.addProperty("chance", chances.get(i));
            outputs.add(result);
        }
        json.add("result", outputs);
        json.addProperty("group", group);
        if (sound != null) {
            json.addProperty("sound", Objects.requireNonNull(sound.getRegistryName()).toString());
        }
        output.accept(id, json);
    }

    public void save(BiConsumer<ResourceLocation, JsonObject> output) {
        ResourceLocation inputId = Objects.requireNonNull(getResult().getRegistryName());
        save(output, new ResourceLocation(namespace == null ? inputId.getNamespace() : namespace, folder + "/" + inputId.getPath()));
    }

    public void saveToFD(BiConsumer<ResourceLocation, JsonObject> output) {
        setNamespace(FarmersDelightLegacy.MOD_ID).save(output);
    }
}
