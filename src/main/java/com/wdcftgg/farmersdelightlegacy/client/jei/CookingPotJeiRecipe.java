package com.wdcftgg.farmersdelightlegacy.client.jei;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CookingPotRecipe;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCookingPot;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CookingPotJeiRecipe implements IRecipeWrapper {

    private static final int INGREDIENT_SLOT_COUNT = 6;
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(FarmersDelightLegacy.MOD_ID, "textures/gui/cooking_pot.png");
    private static final int TIME_ICON_X = 64;
    private static final int TIME_ICON_Y = 2;
    private static final int EXPERIENCE_ICON_X = 63;
    private static final int EXPERIENCE_ICON_Y = 21;
    private static final int INFO_AREA_X = 61;
    private static final int INFO_AREA_Y = 2;
    private static final int INFO_AREA_W = 22;
    private static final int INFO_AREA_H = 28;

    private final List<List<ItemStack>> inputLists;
    private final List<ItemStack> outputs;
    private final int cookingTime;
    private final float experience;
    private final String recipeId;

    private CookingPotJeiRecipe(List<List<ItemStack>> inputLists, List<ItemStack> outputs, int cookingTime, float experience, String recipeId) {
        this.inputLists = inputLists;
        this.outputs = outputs;
        this.cookingTime = Math.max(1, cookingTime);
        this.experience = Math.max(0.0F, experience);
        this.recipeId = recipeId;
    }

    public static CookingPotJeiRecipe of(CookingPotRecipe recipe) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        for (CookingPotRecipe.IngredientEntry entry : recipe.getIngredients()) {
            List<ItemStack> options = new ArrayList<>();
            if (entry.getItem() != null) {
                int metadata = entry.getMetadata() == OreDictionary.WILDCARD_VALUE ? 0 : entry.getMetadata();
                options.add(new ItemStack(entry.getItem(), 1, metadata));
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
        return new CookingPotJeiRecipe(inputs, outputStacks, recipe.getCookTime(), recipe.getExperience(), recipe.getRecipeId());
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, inputLists);
        ingredients.setOutputs(VanillaTypes.ITEM, outputs);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
        Gui.drawModalRectWithCustomSizedTexture(TIME_ICON_X, TIME_ICON_Y, 176, 32, 8, 11, 256, 256);
        if (this.experience > 0.0F) {
            Gui.drawModalRectWithCustomSizedTexture(EXPERIENCE_ICON_X, EXPERIENCE_ICON_Y, 176, 43, 9, 9, 256, 256);
        }
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (mouseX < INFO_AREA_X || mouseX > INFO_AREA_X + INFO_AREA_W || mouseY < INFO_AREA_Y || mouseY > INFO_AREA_Y + INFO_AREA_H) {
            return Collections.emptyList();
        }

        List<String> tooltip = new ArrayList<>();
        int seconds = Math.max(1, this.cookingTime / 20);
        tooltip.add(I18n.format("farmersdelight.jei.cooking.time", seconds));
        if (this.experience > 0.0F) {
            tooltip.add(I18n.format("farmersdelight.jei.cooking.experience", this.experience));
        }
        return tooltip;
    }

    public List<List<ItemStack>> getInputLists() {
        return Collections.unmodifiableList(inputLists);
    }

    public List<ItemStack> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    public String getRecipeId() {
        return recipeId;
    }
}

