package com.wdcftgg.farmersdelightlegacy.client.jei;

import com.wdcftgg.farmersdelight.Tags;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public final class CookingPotRecipeCategory implements IRecipeCategory<CookingPotJeiRecipe> {

    private static final ResourceLocation JEI_TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/gui/jei/cooking_pot.png");
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/gui/cooking_pot.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable heatIndicator;
    private final IDrawableAnimated arrow;

    public CookingPotRecipeCategory(IGuiHelper guiHelper, IDrawable icon) {
        this.background = guiHelper.createDrawable(JEI_TEXTURE, 0, 0, 116, 56);
        this.icon = icon;
        this.heatIndicator = guiHelper.createDrawable(GUI_TEXTURE, 176, 0, 17, 15);
        this.arrow = guiHelper.drawableBuilder(GUI_TEXTURE, 176, 15, 24, 17)
                .buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public String getUid() {
        return JeiUids.COOKING_POT;
    }

    @Override
    public String getTitle() {
        return I18n.format("farmersdelight.jei.cooking");
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
    public void setRecipe(IRecipeLayout recipeLayout, CookingPotJeiRecipe recipeWrapper, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 0, 0);
        recipeLayout.getItemStacks().init(1, true, 18, 0);
        recipeLayout.getItemStacks().init(2, true, 36, 0);
        recipeLayout.getItemStacks().init(3, true, 0, 18);
        recipeLayout.getItemStacks().init(4, true, 18, 18);
        recipeLayout.getItemStacks().init(5, true, 36, 18);
        recipeLayout.getItemStacks().init(6, true, 62, 38);
        recipeLayout.getItemStacks().init(7, false, 94, 10);
        recipeLayout.getItemStacks().init(8, false, 94, 38);
        recipeLayout.getItemStacks().set(ingredients);
    }

    @Override
    public void drawExtras(net.minecraft.client.Minecraft minecraft) {
        this.arrow.draw(minecraft, 60, 9);
        this.heatIndicator.draw(minecraft, 18, 39);
    }
}

