package com.wdcftgg.farmersdelightlegacy.common.compat.crafttweaker;

import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.CuttingBoardRecipeManager.CuttingBoardRecipeView;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

import java.util.Arrays;
import java.util.stream.Collectors;

@ZenRegister
@ZenClass("mods.farmersdelight.CuttingRecipe")
public final class ZenCuttingRecipe {
    @ZenProperty public String id;
    @ZenProperty public String group;
    @ZenProperty public IIngredient[] inputs;
    @ZenProperty public IIngredient[] tools;
    @ZenProperty public IItemStack[] outputs;
    @ZenProperty public float[] chances;
    @ZenProperty public String sound;

    ZenCuttingRecipe(CuttingBoardRecipeView recipe) {
        id = recipe.getRecipeId();
        group = recipe.getGroup();
        inputs = RecipeScript.ingredients(recipe.getInput(), false);
        tools = RecipeScript.ingredients(recipe.getTool(), true);
        outputs = CraftTweakerMC.getIItemStacks(recipe.getResultStacks());
        chances = new float[recipe.getResultChances().size()];
        for (int index = 0; index < chances.length; index++) {
            chances[index] = recipe.getResultChances().get(index);
        }
        sound = recipe.getSound() == null ? "" : recipe.getSound().getRegistryName().toString();
    }

    @ZenMethod
    public boolean register() {
        return ZenCuttingBoardRecipes.addRecipeAdvanced(id, inputs, tools, outputs, chances, sound, group);
    }

    @ZenMethod
    public String toCommandString() {
        String[] probabilities = new String[chances.length];
        for (int index = 0; index < chances.length; index++) {
            probabilities[index] = chances[index] + " as float";
        }
        String results = Arrays.stream(outputs).map(RecipeScript::stack).collect(Collectors.joining(", ", "[", "]"));
        return "mods.farmersdelight.CuttingBoard.addRecipeAdvanced(" + RecipeScript.quote(id) + ", "
                + RecipeScript.ingredients(inputs) + " as crafttweaker.item.IIngredient[], "
                + RecipeScript.ingredients(tools) + " as crafttweaker.item.IIngredient[], "
                + results + " as crafttweaker.item.IItemStack[], [" + String.join(", ", probabilities)
                + "] as float[], " + RecipeScript.quote(sound) + ", " + RecipeScript.quote(group) + ");";
    }
}
