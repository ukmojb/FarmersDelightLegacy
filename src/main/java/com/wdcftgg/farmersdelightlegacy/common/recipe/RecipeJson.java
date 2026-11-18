package com.wdcftgg.farmersdelightlegacy.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CookingPotRecipe.IngredientEntry;
import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.CuttingBoardRecipeManager.IngredientMatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Objects;

final class RecipeJson {
    private RecipeJson() {
    }

    static JsonObject stack(ItemStack stack) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Recipe stack must not be empty");
        }
        JsonObject json = new JsonObject();
        json.addProperty("item", Objects.requireNonNull(stack.getItem().getRegistryName()).toString());
        json.addProperty("data", stack.getMetadata());
        json.addProperty("count", stack.getCount());
        NBTTagCompound serialized = stack.serializeNBT();
        NBTTagCompound data = serialized.getCompoundTag("tag");
        if (serialized.hasKey("ForgeCaps", 10)) {
            data.setTag("ForgeCaps", serialized.getTag("ForgeCaps"));
        }
        if (!data.isEmpty()) {
            json.addProperty("nbt", data.toString());
        }
        return json;
    }

    static JsonElement ingredient(IngredientEntry ingredient) {
        if (!ingredient.getAlternatives().isEmpty()) {
            JsonArray alternatives = new JsonArray();
            for (IngredientEntry alternative : ingredient.getAlternatives()) {
                alternatives.add(ingredient(alternative));
            }
            return alternatives;
        }
        if (ingredient.getStackMatcher() != null || ingredient.hasCustomRemainderProvider()) {
            throw new IllegalStateException("Custom ingredient callbacks require runtime recipe registration");
        }
        if (ingredient.getOreDictName() != null) {
            return new JsonPrimitive("ore:" + ingredient.getOreDictName());
        }
        return stack(new ItemStack(ingredient.getItem(), 1, ingredient.getMetadata()));
    }

    static JsonArray ingredient(IngredientMatcher ingredient) {
        JsonArray json = new JsonArray();
        for (ItemStack stack : ingredient.getItemStacks()) {
            json.add(stack(stack));
        }
        for (String ore : ingredient.getOreDictNames()) {
            json.add(new JsonPrimitive("ore:" + ore));
        }
        for (IngredientEntry entry : ingredient.getCustomIngredients()) {
            if (entry.getStackMatcher() instanceof ToolAction && !entry.hasCustomRemainderProvider()) {
                JsonObject action = new JsonObject();
                action.addProperty("action", ((ToolAction) entry.getStackMatcher()).getAction());
                json.add(action);
            } else {
                json.add(ingredient(entry));
            }
        }
        return json;
    }
}
