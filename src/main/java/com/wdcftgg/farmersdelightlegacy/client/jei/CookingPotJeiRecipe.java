package com.wdcftgg.farmersdelightlegacy.client.jei;

import com.wdcftgg.farmersdelightlegacy.common.recipe.CookingPotRecipe;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCookingPot;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CookingPotJeiRecipe implements IRecipeWrapper {

    private static final int INGREDIENT_SLOT_COUNT = 6;

    private final List<List<ItemStack>> inputLists;
    private final List<ItemStack> outputs;

    private CookingPotJeiRecipe(List<List<ItemStack>> inputLists, List<ItemStack> outputs) {
        this.inputLists = inputLists;
        this.outputs = outputs;
    }

    public static CookingPotJeiRecipe of(CookingPotRecipe recipe) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        for (CookingPotRecipe.IngredientEntry entry : recipe.getIngredients()) {
            List<ItemStack> options = new ArrayList<>();
            if (entry.getItem() != null) {
                options.add(new ItemStack(entry.getItem()));
            } else if (entry.getOreDictName() != null) {
                options.addAll(OreDictionary.getOres(entry.getOreDictName()));
            }
            if (options.isEmpty()) {
                options.add(ItemStack.EMPTY);
            }
            inputs.add(options);
        }

        while (inputs.size() < INGREDIENT_SLOT_COUNT) {
            inputs.add(Collections.singletonList(ItemStack.EMPTY));
        }

        ItemStack servingContainer = TileEntityCookingPot.inferServingContainerForMeal(
                recipe.getResultStack(),
                recipe.getOutputContainer(),
                !recipe.hasContainerDefinition());
        inputs.add(servingContainer.isEmpty()
                ? Collections.singletonList(ItemStack.EMPTY)
                : Collections.singletonList(servingContainer));

        ItemStack resultStack = recipe.getResultStack();
        List<ItemStack> outputStacks = new ArrayList<>();
        outputStacks.add(resultStack.copy());
        outputStacks.add(resultStack.copy());
        return new CookingPotJeiRecipe(inputs, outputStacks);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, inputLists);
        ingredients.setOutputs(VanillaTypes.ITEM, outputs);
    }

    public List<List<ItemStack>> getInputLists() {
        return Collections.unmodifiableList(inputLists);
    }

    public List<ItemStack> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }
}

