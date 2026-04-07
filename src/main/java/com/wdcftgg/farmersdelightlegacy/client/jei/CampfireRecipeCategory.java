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

public final class CampfireRecipeCategory implements IRecipeCategory<CampfireJeiRecipe> {

    private static final ResourceLocation JEI_TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/gui/jei/campfire.png");
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated arrow;
    private final IDrawableAnimated flame;

    private static final int OUTPUT_X = 61;
    private static final int OUTPUT_Y = 9;
    private static final int ARROW_X = 24;
    private static final int ARROW_Y = 8;
    private static final int TIME_ICON_X = 1;
    private static final int TIME_ICON_Y = 20;

    public CampfireRecipeCategory(IGuiHelper guiHelper, IDrawable icon) {
        this.background = guiHelper.createDrawable(JEI_TEXTURE, 0, 0, 82, 35);
        this.icon = icon;
        this.flame = guiHelper.drawableBuilder(GUI_TEXTURE, 176, 0, 14, 14)
                .buildAnimated(300, IDrawableAnimated.StartDirection.TOP, true);
        this.arrow = guiHelper.drawableBuilder(GUI_TEXTURE, 176, 14, 24, 17)
                .buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public String getUid() {
        return JeiUids.CAMPFIRE;
    }

    @Override
    public String getTitle() {
        return I18n.format("farmersdelight.jei.campfire");
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
    public void setRecipe(IRecipeLayout recipeLayout, CampfireJeiRecipe recipeWrapper, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 0, 0);
        recipeLayout.getItemStacks().init(1, false, OUTPUT_X, OUTPUT_Y);
        recipeLayout.getItemStacks().set(ingredients);
    }

    @Override
    public void drawExtras(net.minecraft.client.Minecraft minecraft) {
        this.arrow.draw(minecraft, ARROW_X, ARROW_Y);
        this.flame.draw(minecraft, TIME_ICON_X, TIME_ICON_Y);
    }
}

