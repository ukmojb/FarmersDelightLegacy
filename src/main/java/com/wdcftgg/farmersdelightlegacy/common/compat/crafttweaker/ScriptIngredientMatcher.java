package com.wdcftgg.farmersdelightlegacy.common.compat.crafttweaker;

import com.wdcftgg.farmersdelightlegacy.common.recipe.CookingPotRecipe;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.item.ItemStack;

final class ScriptIngredientMatcher implements CookingPotRecipe.IngredientStackMatcher {
    private final IIngredient ingredient;

    ScriptIngredientMatcher(IIngredient ingredient) {
        this.ingredient = ingredient;
    }

    IIngredient getIngredient() {
        return ingredient;
    }

    @Override
    public boolean matches(ItemStack stack) {
        return ingredient.matches(CraftTweakerMC.getIItemStack(stack));
    }
}
