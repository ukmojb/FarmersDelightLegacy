package com.wdcftgg.farmersdelightlegacy.client.jei;

import com.wdcftgg.farmersdelightlegacy.common.recipe.CuttingBoardRecipeManager;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class CuttingBoardJeiRecipe implements IRecipeWrapper {

    private static final int OUTPUT_GRID_X = 76;
    private static final int OUTPUT_GRID_Y = 10;
    private static final int OUTPUT_GRID_COLUMNS = 2;
    private static final int OUTPUT_GRID_MAX_SLOTS = 4;
    private static final int OUTPUT_STEP = 19;

    private final List<List<ItemStack>> inputOptions;
    private final List<ItemStack> outputs;
    private final List<Float> outputChances;

    private CuttingBoardJeiRecipe(List<List<ItemStack>> inputOptions, List<ItemStack> outputs, List<Float> outputChances) {
        this.inputOptions = inputOptions;
        this.outputs = outputs;
        this.outputChances = outputChances;
    }

    public static CuttingBoardJeiRecipe of(CuttingBoardRecipeManager.CuttingBoardRecipeView recipe) {
        List<ItemStack> ingredientOptions = new ArrayList<>(recipe.getInputOptions());
        if (ingredientOptions.isEmpty()) {
            ingredientOptions.add(ItemStack.EMPTY);
        }

        List<ItemStack> toolOptions = new ArrayList<>(recipe.getToolOptions());
        if (toolOptions.isEmpty()) {
            toolOptions.add(ItemStack.EMPTY);
        }

        List<ItemStack> outputStacks = new ArrayList<>(recipe.getResultStacks());
        if (outputStacks.isEmpty()) {
            outputStacks.add(ItemStack.EMPTY);
        }
        if (outputStacks.size() > OUTPUT_GRID_MAX_SLOTS) {
            outputStacks = new ArrayList<>(outputStacks.subList(0, OUTPUT_GRID_MAX_SLOTS));
        }

        List<Float> resultChances = new ArrayList<>(recipe.getResultChances());
        while (resultChances.size() < outputStacks.size()) {
            resultChances.add(1.0F);
        }
        if (resultChances.size() > outputStacks.size()) {
            resultChances = new ArrayList<>(resultChances.subList(0, outputStacks.size()));
        }

        List<List<ItemStack>> inputLists = new ArrayList<>();
        inputLists.add(toolOptions);
        inputLists.add(ingredientOptions);
        return new CuttingBoardJeiRecipe(inputLists, outputStacks, resultChances);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, inputOptions);
        ingredients.setOutputs(VanillaTypes.ITEM, outputs);
    }

    public int getOutputCount() {
        return Math.min(outputs.size(), OUTPUT_GRID_MAX_SLOTS);
    }

    public int getOutputSlotX(int outputIndex) {
        int size = getOutputCount();
        int centerX = size > 1 ? 1 : 10;
        return OUTPUT_GRID_X + centerX + ((outputIndex % OUTPUT_GRID_COLUMNS) * OUTPUT_STEP);
    }

    public int getOutputSlotY(int outputIndex) {
        int size = getOutputCount();
        int centerY = size > 2 ? 1 : 10;
        return OUTPUT_GRID_Y + centerY + ((outputIndex / OUTPUT_GRID_COLUMNS) * OUTPUT_STEP);
    }

    public float getOutputChance(int outputIndex) {
        if (outputIndex < 0 || outputIndex >= outputChances.size()) {
            return 1.0F;
        }
        return outputChances.get(outputIndex);
    }
}

