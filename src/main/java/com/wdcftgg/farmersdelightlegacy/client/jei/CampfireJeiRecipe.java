package com.wdcftgg.farmersdelightlegacy.client.jei;

import com.wdcftgg.farmersdelightlegacy.common.recipe.CampfireCookingRecipe;
import net.minecraft.client.resources.I18n;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CampfireJeiRecipe implements IRecipeWrapper {

    private final List<List<ItemStack>> inputOptions;
    private final ItemStack output;
    private final int cookingTime;

    private CampfireJeiRecipe(List<List<ItemStack>> inputOptions, ItemStack output, int cookingTime) {
        this.inputOptions = inputOptions;
        this.output = output;
        this.cookingTime = Math.max(1, cookingTime);
    }

    public static CampfireJeiRecipe of(CampfireCookingRecipe recipe) {
        List<ItemStack> options = new ArrayList<>();
        for (CampfireCookingRecipe.IngredientEntry entry : recipe.getIngredients()) {
            if (entry.getItem() != null) {
                options.add(new ItemStack(entry.getItem()));
                continue;
            }
            if (entry.getOreDictName() != null) {
                options.addAll(OreDictionary.getOres(entry.getOreDictName()));
            }
        }
        if (options.isEmpty()) {
            options.add(ItemStack.EMPTY);
        }

        List<List<ItemStack>> inputLists = new ArrayList<>();
        inputLists.add(options);
        return new CampfireJeiRecipe(inputLists, recipe.getResultStack(), recipe.getCookingTime());
    }


    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, inputOptions);
        ingredients.setOutput(VanillaTypes.ITEM, output);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (mouseX >= 24 && mouseX <= 48 && mouseY >= 8 && mouseY <= 25) {
            int seconds = Math.max(1, cookingTime / 20);
            return Collections.singletonList(I18n.format("farmersdelight.jei.campfire.time", seconds));
        }
        return Collections.emptyList();
    }
}

