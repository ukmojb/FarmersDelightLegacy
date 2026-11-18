package com.wdcftgg.farmersdelightlegacy.common.compat.crafttweaker;

import com.wdcftgg.farmersdelightlegacy.common.recipe.CookingPotRecipe.IngredientEntry;
import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.CookingPotRecipeManager;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.List;

@ZenRegister
@ZenClass("mods.farmersdelight.CookingPot")
public final class ZenCookingPotRecipes {

    private ZenCookingPotRecipes() {
    }

    @ZenMethod
    public static boolean addRecipe(String key, IIngredient[] ingredients, IItemStack result) {
        return addRecipeAdvanced(key, ingredients, result, null, 200, 0.35F, false);
    }

    @ZenMethod
    public static boolean addRecipeWithContainer(String key, IIngredient[] ingredients, IItemStack result, IItemStack outputContainerStack) {
        return addRecipeAdvanced(key, ingredients, result, outputContainerStack, 200, 0.35F, true);
    }

    @ZenMethod
    public static boolean addRecipeWithoutContainer(String key, IIngredient[] ingredients, IItemStack result) {
        return addRecipeAdvanced(key, ingredients, result, null, 200, 0.35F, true);
    }

    @ZenMethod
    public static boolean addRecipeAdvanced(String key, IIngredient[] ingredients, IItemStack result,
                                            IItemStack outputContainerStack,
                                            int cookingTime, float experience, boolean hasContainerDefinition) {
        return addRecipeAdvanced(key, ingredients, result, outputContainerStack, cookingTime, experience, hasContainerDefinition, "");
    }

    @ZenMethod
    public static boolean addRecipeAdvanced(String key, IIngredient[] ingredients, IItemStack result,
                                            IItemStack outputContainerStack, int cookingTime, float experience,
                                            boolean hasContainerDefinition, String group) {
        List<IngredientEntry> ingredientEntries = CraftTweakerCompatHelper.toCookingPotIngredients(ingredients);
        ItemStack resultStack = CraftTweakerCompatHelper.stackOf(result);
        ItemStack outputContainer = CraftTweakerCompatHelper.stackOf(outputContainerStack);
        if (ingredientEntries == null || resultStack.isEmpty()) {
            return false;
        }
        return CookingPotRecipeManager.registerScriptRecipe(key, ingredientEntries, resultStack, outputContainer,
                cookingTime, experience, hasContainerDefinition, group);
    }

    @ZenMethod
    public static boolean addRecipe(String key, String[] ingredientTokens, String resultItemId) {
        return addRecipeAdvanced(key, ingredientTokens, resultItemId, 1, "", 1, 200, 0.35F, false);
    }

    @ZenMethod
    public static boolean addRecipeWithContainer(String key, String[] ingredientTokens, String resultItemId, int resultCount,
                                                 String outputContainerItemId, int outputContainerCount) {
        return addRecipeAdvanced(key, ingredientTokens, resultItemId, resultCount,
                outputContainerItemId, outputContainerCount, 200, 0.35F, true);
    }

    @ZenMethod
    public static boolean addRecipeWithoutContainer(String key, String[] ingredientTokens, String resultItemId, int resultCount) {
        return addRecipeAdvanced(key, ingredientTokens, resultItemId, resultCount, "", 1, 200, 0.35F, true);
    }

    @ZenMethod
    public static boolean addRecipeAdvanced(String key, String[] ingredientTokens, String resultItemId, int resultCount,
                                            String outputContainerItemId, int outputContainerCount,
                                            int cookingTime, float experience, boolean hasContainerDefinition) {
        ItemStack resultStack = CraftTweakerCompatHelper.stackOf(resultItemId, resultCount);
        ItemStack outputContainer = outputContainerItemId == null || outputContainerItemId.isEmpty()
                ? ItemStack.EMPTY
                : CraftTweakerCompatHelper.stackOf(outputContainerItemId, outputContainerCount);
        return CookingPotRecipeManager.registerScriptRecipe(key, ingredientTokens, resultStack, outputContainer,
                cookingTime, experience, hasContainerDefinition);
    }

    @ZenMethod
    public static boolean removeRecipe(String key) {
        return CookingPotRecipeManager.removeRecipe(key);
    }

    @ZenMethod
    public static int removeAll() {
        return CookingPotRecipeManager.removeRecipes(recipe -> true);
    }

    @ZenMethod
    public static ZenCookingRecipe[] getRecipes() {
        return CookingPotRecipeManager.getRecipes().stream().map(ZenCookingRecipe::new).toArray(ZenCookingRecipe[]::new);
    }

    @ZenMethod
    public static ZenCookingRecipe getRecipe(String id) {
        for (com.wdcftgg.farmersdelightlegacy.common.recipe.CookingPotRecipe recipe : CookingPotRecipeManager.getRecipes()) {
            if (recipe.getRecipeId().equals(id)) {
                return new ZenCookingRecipe(recipe);
            }
        }
        crafttweaker.CraftTweakerAPI.logError("Unknown cooking recipe: " + id);
        return null;
    }

    @ZenMethod
    public static String[] dumpRecipes() {
        return java.util.Arrays.stream(getRecipes()).map(ZenCookingRecipe::toCommandString).toArray(String[]::new);
    }

    @ZenMethod
    public static int removeRecipesByOutput(IItemStack outputStack) {
        if (outputStack == null) {
            crafttweaker.CraftTweakerAPI.logError("Cooking recipe removal requires an output");
            return 0;
        }
        return CookingPotRecipeManager.removeRecipes(recipe ->
                outputStack.matches(crafttweaker.api.minecraft.CraftTweakerMC.getIItemStack(recipe.getResultStack())));
    }

    @ZenMethod
    public static int removeRecipesByOutput(String outputItemId) {
        return CookingPotRecipeManager.removeRecipesByOutput(CraftTweakerCompatHelper.stackOf(outputItemId, 1));
    }

    @ZenMethod
    public static int removeRecipesByOutput(String outputItemId, int outputMetadata) {
        return CookingPotRecipeManager.removeRecipesByOutput(CraftTweakerCompatHelper.stackOf(outputItemId, 1, outputMetadata));
    }
}

