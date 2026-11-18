package com.wdcftgg.farmersdelightlegacy.common.compat.crafttweaker;

import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.CuttingBoardRecipeManager;
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
        return addRecipeAdvanced(key, inputIngredients, null, resultStacks, null);
    }

    @ZenMethod
    public static boolean addRecipeWithTool(String key, IIngredient[] inputIngredients, IIngredient[] toolIngredients,
                                            IItemStack[] resultStacks) {
        if (toolIngredients == null || toolIngredients.length == 0) {
            crafttweaker.CraftTweakerAPI.logError("Cutting recipe requires a tool: " + key);
            return false;
        }
        return addRecipeAdvanced(key, inputIngredients, toolIngredients, resultStacks, null);
    }

    @ZenMethod
    public static boolean addRecipeWithoutTool(String key, IIngredient[] inputIngredients, IItemStack[] resultStacks) {
        return addRecipeAdvanced(key, inputIngredients, new IIngredient[0], resultStacks, null);
    }

    @ZenMethod
    public static boolean addRecipeAdvanced(String key, IIngredient[] inputIngredients, IIngredient[] toolIngredients,
                                            IItemStack[] resultStacks, float[] resultChances) {
        return addRecipeAdvanced(key, inputIngredients, toolIngredients, resultStacks, resultChances, "");
    }

    @ZenMethod
    public static boolean addRecipeAdvanced(String key, IIngredient[] inputs, IIngredient[] tools,
                                            IItemStack[] outputs, float[] chances, String sound) {
        return addRecipeAdvanced(key, inputs, tools, outputs, chances, sound, "");
    }

    @ZenMethod
    public static boolean addRecipeAdvanced(String key, IIngredient[] inputs, IIngredient[] tools,
                                            IItemStack[] outputs, float[] chances, String sound, String group) {
        net.minecraft.util.SoundEvent event = RecipeScript.sound(sound);
        if (sound != null && !sound.isEmpty() && event == null) {
            return false;
        }
        if (outputs == null) {
            crafttweaker.CraftTweakerAPI.logError("Cutting recipe outputs must not be null: " + key);
            return false;
        }
        net.minecraft.item.ItemStack[] stacks = new net.minecraft.item.ItemStack[outputs.length];
        for (int index = 0; index < outputs.length; index++) {
            stacks[index] = CraftTweakerCompatHelper.stackOf(outputs[index]);
        }
        return CuttingBoardRecipeManager.registerScriptRecipe(key,
                CraftTweakerCompatHelper.toCuttingIngredients(inputs, false),
                CraftTweakerCompatHelper.toCuttingIngredients(tools, true), stacks, chances, event, group);
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

    @ZenMethod
    public static boolean removeRecipe(String key) {
        return CuttingBoardRecipeManager.removeRecipe(key);
    }

    @ZenMethod
    public static int removeRecipe(IItemStack[] outputs) {
        if (outputs == null || outputs.length == 0 || java.util.Arrays.asList(outputs).contains(null)) {
            crafttweaker.CraftTweakerAPI.logError("Cutting recipe removal requires a nonempty output list");
            return 0;
        }
        IItemStack[] expected = outputs.clone();
        return CuttingBoardRecipeManager.removeRecipes(recipe -> {
            java.util.List<net.minecraft.item.ItemStack> actual = recipe.getResultStacks();
            if (actual.size() != expected.length) {
                return false;
            }
            for (int index = 0; index < expected.length; index++) {
                if (!expected[index].matches(crafttweaker.api.minecraft.CraftTweakerMC.getIItemStack(actual.get(index)))) {
                    return false;
                }
            }
            return true;
        });
    }

    @ZenMethod
    public static int removeAll() {
        return CuttingBoardRecipeManager.removeRecipes(recipe -> true);
    }

    @ZenMethod
    public static ZenCuttingRecipe[] getRecipes() {
        return CuttingBoardRecipeManager.getRecipes().stream().map(ZenCuttingRecipe::new).toArray(ZenCuttingRecipe[]::new);
    }

    @ZenMethod
    public static ZenCuttingRecipe getRecipe(String id) {
        for (CuttingBoardRecipeManager.CuttingBoardRecipeView recipe : CuttingBoardRecipeManager.getRecipes()) {
            if (recipe.getRecipeId().equals(id)) {
                return new ZenCuttingRecipe(recipe);
            }
        }
        crafttweaker.CraftTweakerAPI.logError("Unknown cutting recipe: " + id);
        return null;
    }

    @ZenMethod
    public static String[] dumpRecipes() {
        return java.util.Arrays.stream(getRecipes()).map(ZenCuttingRecipe::toCommandString).toArray(String[]::new);
    }

    @ZenMethod
    public static int removeRecipesByOutput(IItemStack outputStack) {
        if (outputStack == null) {
            crafttweaker.CraftTweakerAPI.logError("Cutting recipe removal requires an output");
            return 0;
        }
        return CuttingBoardRecipeManager.removeRecipes(recipe -> recipe.getResultStacks().stream()
                .anyMatch(stack -> outputStack.matches(crafttweaker.api.minecraft.CraftTweakerMC.getIItemStack(stack))));
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

