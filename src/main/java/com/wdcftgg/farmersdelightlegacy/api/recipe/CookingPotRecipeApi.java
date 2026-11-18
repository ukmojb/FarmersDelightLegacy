package com.wdcftgg.farmersdelightlegacy.api.recipe;

import com.wdcftgg.farmersdelightlegacy.common.recipe.CookingPotRecipe;
import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.CookingPotRecipeManager;
import net.minecraft.item.ItemStack;

import java.util.List;

public final class CookingPotRecipeApi {

    private CookingPotRecipeApi() {
    }

    public static boolean registerRecipe(String key, String[] ingredientTokens, ItemStack resultStack,
                                         ItemStack outputContainer, int cookTime, float experience) {
        return CookingPotRecipeManager.registerScriptRecipe(key, ingredientTokens, resultStack, outputContainer,
                cookTime, experience, !outputContainer.isEmpty());
    }

    public static boolean registerRecipe(String key, String[] ingredientTokens, ItemStack resultStack,
                                         int cookTime, float experience) {
        return CookingPotRecipeManager.registerScriptRecipe(key, ingredientTokens, resultStack, ItemStack.EMPTY,
                cookTime, experience, false);
    }

    public static boolean registerRecipe(CookingPotRecipe recipe) {
        return CookingPotRecipeManager.registerRecipe(recipe);
    }

    public static boolean unregisterRecipe(String key) {
        return CookingPotRecipeManager.unregisterScriptRecipe(key);
    }

    public static int removeRecipesByOutput(ItemStack outputStack) {
        return CookingPotRecipeManager.removeRecipesByOutput(outputStack);
    }

    public static List<CookingPotRecipe> getRecipes() {
        return CookingPotRecipeManager.getRecipes();
    }
}
