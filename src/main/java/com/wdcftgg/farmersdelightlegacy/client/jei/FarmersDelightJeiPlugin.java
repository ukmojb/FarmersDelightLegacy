package com.wdcftgg.farmersdelightlegacy.client.jei;

import com.wdcftgg.farmersdelightlegacy.client.gui.GuiCookingPot;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CampfireCookingRecipe;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CampfireCookingRecipeManager;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CookingPotRecipe;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CookingPotRecipeManager;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CuttingBoardRecipeManager;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModItems;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@JEIPlugin
public final class FarmersDelightJeiPlugin implements IModPlugin {

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        IDrawable cookingPotIcon = guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.COOKING_POT));
        IDrawable cuttingBoardIcon = guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.CUTTING_BOARD));
        IDrawable campfireIcon = guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.STOVE));

        registry.addRecipeCategories(
                new CookingPotRecipeCategory(guiHelper, cookingPotIcon),
                new CuttingBoardRecipeCategory(guiHelper, cuttingBoardIcon),
                new CampfireRecipeCategory(guiHelper, campfireIcon)
        );
    }

    @Override
    public void register(IModRegistry registry) {
        registry.addRecipes(buildCookingPotRecipes(), JeiUids.COOKING_POT);
        registry.addRecipes(buildCuttingBoardRecipes(), JeiUids.CUTTING_BOARD);
        registry.addRecipes(buildCampfireRecipes(), JeiUids.CAMPFIRE);

        registry.addRecipeCatalyst(new ItemStack(ModBlocks.COOKING_POT), JeiUids.COOKING_POT);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.CUTTING_BOARD), JeiUids.CUTTING_BOARD);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.STOVE), JeiUids.CAMPFIRE);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.SKILLET), JeiUids.CAMPFIRE);

        registry.addRecipeClickArea(GuiCookingPot.class, 89, 25, 24, 17, JeiUids.COOKING_POT);

        addIngredientInfo(registry, "wheat_dough", "farmersdelight.jei.info.dough");
        addIngredientInfo(registry, "straw", "farmersdelight.jei.info.straw");
        addIngredientInfo(registry, "ham", "farmersdelight.jei.info.ham");
        addIngredientInfo(registry, "smoked_ham", "farmersdelight.jei.info.ham");
        addIngredientInfo(registry, "flint_knife", "farmersdelight.jei.info.knife");
        addIngredientInfo(registry, "iron_knife", "farmersdelight.jei.info.knife");
        addIngredientInfo(registry, "golden_knife", "farmersdelight.jei.info.knife");
        addIngredientInfo(registry, "diamond_knife", "farmersdelight.jei.info.knife");
        addIngredientInfo(registry, "netherite_knife", "farmersdelight.jei.info.knife");
        addIngredientInfo(registry, "stove", "farmersdelight.jei.info.stove");
        addIngredientInfo(registry, "skillet", "farmersdelight.jei.info.skillet");
    }

    private static void addIngredientInfo(IModRegistry registry, String itemName, String key) {
        Item item = ModItems.ITEMS.get(itemName);
        if (item != null) {
            registry.addIngredientInfo(new ItemStack(item), VanillaTypes.ITEM, key);
        }
    }

    private static List<CookingPotJeiRecipe> buildCookingPotRecipes() {
        List<CookingPotJeiRecipe> result = new ArrayList<>();
        for (CookingPotRecipe recipe : CookingPotRecipeManager.getRecipes()) {
            result.add(CookingPotJeiRecipe.of(recipe));
        }
        return result;
    }

    private static List<CuttingBoardJeiRecipe> buildCuttingBoardRecipes() {
        List<CuttingBoardJeiRecipe> result = new ArrayList<>();
        for (CuttingBoardRecipeManager.CuttingBoardRecipeView recipe : CuttingBoardRecipeManager.getRecipes()) {
            result.add(CuttingBoardJeiRecipe.of(recipe));
        }
        return result;
    }

    private static List<CampfireJeiRecipe> buildCampfireRecipes() {
        List<CampfireJeiRecipe> result = new ArrayList<>();
        for (CampfireCookingRecipe recipe : CampfireCookingRecipeManager.getRecipes()) {
            result.add(CampfireJeiRecipe.of(recipe));
        }
        return result;
    }
}

