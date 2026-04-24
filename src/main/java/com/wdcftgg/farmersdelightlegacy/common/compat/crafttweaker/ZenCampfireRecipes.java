package com.wdcftgg.farmersdelightlegacy.common.compat.crafttweaker;

import com.wdcftgg.farmersdelightlegacy.common.recipe.CampfireCookingRecipeManager;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.farmersdelight.Campfire")
public final class ZenCampfireRecipes {

    private ZenCampfireRecipes() {
    }

    @ZenMethod
    public static boolean addRecipe(String key, IIngredient[] ingredients, IItemStack result) {
        return addRecipeAdvanced(key, ingredients, result, 600);
    }

    @ZenMethod
    public static boolean addRecipeAdvanced(String key, IIngredient[] ingredients, IItemStack result, int cookingTime) {
        String[] ingredientTokens = CraftTweakerCompatHelper.toStrictIngredientTokens(ingredients);
        ItemStack resultStack = CraftTweakerCompatHelper.stackOf(result);
        if (ingredientTokens == null || resultStack.isEmpty()) {
            return false;
        }
        return CampfireCookingRecipeManager.registerScriptRecipe(key, ingredientTokens, resultStack, cookingTime);
    }

    @ZenMethod
    public static boolean addRecipe(String key, String[] ingredientTokens, String resultItemId) {
        return addRecipeAdvanced(key, ingredientTokens, resultItemId, 1, 600);
    }

    @ZenMethod
    public static boolean addRecipeAdvanced(String key, String[] ingredientTokens, String resultItemId, int resultCount, int cookingTime) {
        ItemStack resultStack = CraftTweakerCompatHelper.stackOf(resultItemId, resultCount);
        return CampfireCookingRecipeManager.registerScriptRecipe(key, ingredientTokens, resultStack, cookingTime);
    }

    @ZenMethod
    public static boolean removeRecipe(String key) {
        return CampfireCookingRecipeManager.unregisterScriptRecipe(key);
    }

    @ZenMethod
    public static int removeRecipesByOutput(IItemStack outputStack) {
        return CampfireCookingRecipeManager.removeRecipesByOutput(CraftTweakerCompatHelper.stackOf(outputStack));
    }

    @ZenMethod
    public static int removeRecipesByOutput(String outputItemId) {
        return CampfireCookingRecipeManager.removeRecipesByOutput(CraftTweakerCompatHelper.stackOf(outputItemId, 1));
    }

    @ZenMethod
    public static int removeRecipesByOutput(String outputItemId, int outputMetadata) {
        return CampfireCookingRecipeManager.removeRecipesByOutput(CraftTweakerCompatHelper.stackOf(outputItemId, 1, outputMetadata));
    }
}

