package com.wdcftgg.farmersdelightlegacy.common.compat.crafttweaker;

import com.wdcftgg.farmersdelightlegacy.common.recipe.CookingPotRecipe;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

@ZenRegister
@ZenClass("mods.farmersdelight.CookingRecipe")
public final class ZenCookingRecipe {
    @ZenProperty public String id;
    @ZenProperty public String group;
    @ZenProperty public IIngredient[] inputs;
    @ZenProperty public IItemStack output;
    @ZenProperty public IItemStack container;
    @ZenProperty public int cookingTime;
    @ZenProperty public float experience;
    @ZenProperty public boolean hasContainerDefinition;

    ZenCookingRecipe(CookingPotRecipe recipe) {
        id = recipe.getRecipeId();
        group = recipe.getGroup();
        inputs = recipe.getIngredients().stream().map(RecipeScript::ingredient).toArray(IIngredient[]::new);
        output = CraftTweakerMC.getIItemStack(recipe.getResultStack());
        container = CraftTweakerMC.getIItemStack(recipe.getOutputContainer());
        cookingTime = recipe.getCookTime();
        experience = recipe.getExperience();
        hasContainerDefinition = recipe.hasContainerDefinition();
    }

    @ZenMethod
    public boolean register() {
        return ZenCookingPotRecipes.addRecipeAdvanced(id, inputs, output, container, cookingTime, experience, hasContainerDefinition, group);
    }

    @ZenMethod
    public String toCommandString() {
        return "mods.farmersdelight.CookingPot.addRecipeAdvanced(" + RecipeScript.quote(id) + ", "
                + RecipeScript.ingredients(inputs) + ", " + RecipeScript.stack(output) + ", "
                + RecipeScript.stack(container) + ", " + cookingTime + ", " + experience + " as float, "
                + hasContainerDefinition + ", " + RecipeScript.quote(group) + ");";
    }
}
