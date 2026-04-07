package com.wdcftgg.farmersdelightlegacy.client.jei;

import com.wdcftgg.farmersdelight.Tags;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public final class CuttingBoardRecipeCategory implements IRecipeCategory<CuttingBoardJeiRecipe> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/gui/jei/cutting_board.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable slotChance;

    public CuttingBoardRecipeCategory(IGuiHelper guiHelper, IDrawable icon) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 117, 57);
        this.icon = icon;
        this.slot = guiHelper.createDrawable(TEXTURE, 0, 58, 18, 18);
        this.slotChance = guiHelper.createDrawable(TEXTURE, 18, 58, 18, 18);
    }

    @Override
    public String getUid() {
        return JeiUids.CUTTING_BOARD;
    }

    @Override
    public String getTitle() {
        return I18n.format("farmersdelight.jei.cutting");
    }

    @Override
    public String getModName() {
        return Tags.MOD_NAME;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, CuttingBoardJeiRecipe recipeWrapper, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 16, 8);
        recipeLayout.getItemStacks().init(1, true, 16, 27);

        int outputCount = recipeWrapper.getOutputCount();
        for (int i = 0; i < outputCount; i++) {
            int slotIndex = 2 + i;
            recipeLayout.getItemStacks().init(slotIndex, false, recipeWrapper.getOutputSlotX(i), recipeWrapper.getOutputSlotY(i));
            float chance = recipeWrapper.getOutputChance(i);
            if (chance < 1.0F) {
                recipeLayout.getItemStacks().setBackground(slotIndex, slotChance);
            } else {
                recipeLayout.getItemStacks().setBackground(slotIndex, slot);
            }
        }

        recipeLayout.getItemStacks().addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
            if (input || slotIndex < 2) {
                return;
            }
            int outputIndex = slotIndex - 2;
            float chance = recipeWrapper.getOutputChance(outputIndex);
            if (chance >= 1.0F) {
                return;
            }
            String chancePercent = chance < 0.01F ? "<1" : Integer.toString((int) (chance * 100.0F));
            tooltip.add(1, TextFormatting.GOLD + I18n.format("farmersdelight.jei.chance", chancePercent));
        });

        recipeLayout.getItemStacks().set(ingredients);
    }
}

