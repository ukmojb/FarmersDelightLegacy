package com.wdcftgg.farmersdelightlegacy.common.compat.crafttweaker;

import com.wdcftgg.farmersdelightlegacy.common.recipe.CuttingBoardRecipeManager;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.farmersdelight.CuttingBoard")
public final class ZenCuttingBoardRecipes {

    private static final String[] DEFAULT_TOOL_TOKENS = new String[]{"ore:toolKnife"};

    private ZenCuttingBoardRecipes() {
    }

    @ZenMethod
    public static boolean addRecipe(String key, IIngredient[] inputIngredients, IItemStack[] resultStacks) {
        return registerRecipe(key, CraftTweakerCompatHelper.toStrictIngredientTokens(inputIngredients), DEFAULT_TOOL_TOKENS, resultStacks, null);
    }

    @ZenMethod
    public static boolean addRecipeWithTool(String key, IIngredient[] inputIngredients, IIngredient[] toolIngredients,
                                            IItemStack[] resultStacks) {
        String[] toolTokens = CraftTweakerCompatHelper.toStrictIngredientTokens(toolIngredients);
        if (toolTokens == null || toolTokens.length == 0) {
            return false;
        }
        return registerRecipe(key, CraftTweakerCompatHelper.toStrictIngredientTokens(inputIngredients), toolTokens, resultStacks, null);
    }

    @ZenMethod
    public static boolean addRecipeWithoutTool(String key, IIngredient[] inputIngredients, IItemStack[] resultStacks) {
        return registerRecipe(key, CraftTweakerCompatHelper.toStrictIngredientTokens(inputIngredients), new String[0], resultStacks, null);
    }

    @ZenMethod
    public static boolean addRecipeAdvanced(String key, IIngredient[] inputIngredients, IIngredient[] toolIngredients,
                                            IItemStack[] resultStacks, float[] resultChances) {
        String[] inputTokens = CraftTweakerCompatHelper.toStrictIngredientTokens(inputIngredients);
        String[] toolTokens = CraftTweakerCompatHelper.toStrictIngredientTokens(toolIngredients);
        if (toolIngredients == null) {
            toolTokens = DEFAULT_TOOL_TOKENS;
        }
        return registerRecipe(key, inputTokens, toolTokens, resultStacks, resultChances);
    }

    @ZenMethod
    public static boolean addRecipe(String key, String[] inputTokens, String[] resultTokens) {
        return addRecipeAdvanced(key, inputTokens, DEFAULT_TOOL_TOKENS, resultTokens, null, null);
    }

    @ZenMethod
    public static boolean addRecipeWithTool(String key, String[] inputTokens, String[] toolTokens, String[] resultTokens) {
        if (toolTokens == null || toolTokens.length == 0) {
            return false;
        }
        return addRecipeAdvanced(key, inputTokens, toolTokens, resultTokens, null, null);
    }

    @ZenMethod
    public static boolean addRecipeWithoutTool(String key, String[] inputTokens, String[] resultTokens) {
        return addRecipeAdvanced(key, inputTokens, new String[0], resultTokens, null, null);
    }

    @ZenMethod
    public static boolean addRecipeAdvanced(String key, String[] inputTokens, String[] toolTokens,
                                            String[] resultTokens, int[] resultCounts, float[] resultChances) {
        String[] resolvedToolTokens = toolTokens == null ? DEFAULT_TOOL_TOKENS : toolTokens;
        return CuttingBoardRecipeManager.registerScriptRecipe(key, inputTokens, resolvedToolTokens, resultTokens, resultCounts, resultChances);
    }

    private static boolean registerRecipe(String key, String[] inputTokens, String[] toolTokens,
                                          IItemStack[] resultStacks, float[] resultChances) {
        if (resultStacks == null || resultStacks.length == 0) {
            return false;
        }
        String[] resultTokens = new String[resultStacks.length];
        int[] resultCounts = new int[resultStacks.length];
        for (int index = 0; index < resultStacks.length; index++) {
            IItemStack resultStack = resultStacks[index];
            String itemId = CraftTweakerCompatHelper.itemTokenOf(resultStack);
            net.minecraft.item.ItemStack nativeResult = CraftTweakerCompatHelper.stackOf(resultStack);
            if (itemId == null || nativeResult.isEmpty()) {
                return false;
            }
            resultTokens[index] = itemId;
            resultCounts[index] = Math.max(1, nativeResult.getCount());
        }

        if (inputTokens == null || inputTokens.length == 0) {
            return false;
        }
        return CuttingBoardRecipeManager.registerScriptRecipe(key, inputTokens, toolTokens, resultTokens, resultCounts, resultChances);
    }

    @ZenMethod
    public static boolean removeRecipe(String key) {
        return CuttingBoardRecipeManager.unregisterScriptRecipe(key);
    }

    @ZenMethod
    public static int removeRecipesByOutput(IItemStack outputStack) {
        return CuttingBoardRecipeManager.removeRecipesByOutput(CraftTweakerCompatHelper.stackOf(outputStack));
    }

    @ZenMethod
    public static int removeRecipesByOutput(String outputItemId) {
        return removeRecipesByOutput(outputItemId, 0);
    }

    @ZenMethod
    public static int removeRecipesByOutput(String outputItemId, int outputMetadata) {
        return CuttingBoardRecipeManager.removeRecipesByOutput(CraftTweakerCompatHelper.stackOf(outputItemId, 1, outputMetadata));
    }
}

