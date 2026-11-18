package com.wdcftgg.farmersdelightlegacy.common.compat;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CampfireCookingRecipe;
import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.CampfireCookingRecipeManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CampfireCookingCompat {

    private static final String CAMPFIRE_MOD_ID = "campfire";
    private static final String FUTURE_MC_MOD_ID = "futuremc";
    private static final float DEFAULT_EXPERIENCE = 0.35F;

    private CampfireCookingCompat() {
    }

    public static void registerAll() {
        List<CampfireCookingRecipe> recipes = CampfireCookingRecipeManager.getRecipes();
        registerCampfireRecipes(recipes);
        registerFutureMcRecipes(recipes);
    }

    private static void registerCampfireRecipes(List<CampfireCookingRecipe> recipes) {
        if (Loader.isModLoaded(CAMPFIRE_MOD_ID)) {
            CampfireRecipes.register(recipes);
        }
    }

    private static void registerFutureMcRecipes(List<CampfireCookingRecipe> recipes) {
        if (Loader.isModLoaded(FUTURE_MC_MOD_ID)) {
            FutureMcRecipes.register(recipes);
        }
    }

    private static final class CampfireRecipes {
        private static void register(List<CampfireCookingRecipe> recipes) {
            for (CampfireCookingRecipe recipe : recipes) {
                List<ItemStack> inputs = expandInputs(recipe);
                if (!inputs.isEmpty()) {
                    git.jbredwards.campfire.common.recipe.campfire.CampfireRecipeHandler.createRecipe(
                            inputs, recipe.getResultStack(), recipe.getCookingTime(), DEFAULT_EXPERIENCE);
                }
            }
            FarmersDelightLegacy.LOGGER.info("Registered Farmers Delight cooking recipes for Campfire.");
        }
    }

    private static final class FutureMcRecipes {
        private static void register(List<CampfireCookingRecipe> recipes) {
            for (CampfireCookingRecipe recipe : recipes) {
                List<ItemStack> inputs = expandInputs(recipe);
                if (!inputs.isEmpty()) {
                    thedarkcolour.futuremc.recipe.campfire.CampfireRecipes.INSTANCE.addRecipe(
                            Ingredient.fromStacks(inputs.toArray(new ItemStack[0])),
                            recipe.getResultStack(), recipe.getCookingTime());
                }
            }
            FarmersDelightLegacy.LOGGER.info("Registered Farmers Delight cooking recipes for Future MC.");
        }
    }

    static List<ItemStack> expandInputs(CampfireCookingRecipe recipe) {
        List<ItemStack> inputs = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();
        for (CampfireCookingRecipe.IngredientEntry entry : recipe.getIngredients()) {
            if (entry.getItem() != null) {
                ItemStack stack = new ItemStack(entry.getItem(), 1, entry.getMetadata());
                addUnique(inputs, seenKeys, stack);
                continue;
            }
            if (entry.getOreDictName() != null) {
                for (ItemStack oreStack : OreDictionary.getOres(entry.getOreDictName(), false)) {
                    ItemStack stack = oreStack.copy();
                    stack.setCount(1);
                    addUnique(inputs, seenKeys, stack);
                }
            }
        }
        return inputs;
    }

    private static void addUnique(List<ItemStack> inputs, Set<String> seenKeys, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        String key = stack.getItem().getRegistryName() + "@" + stack.getMetadata();
        if (seenKeys.add(key)) {
            inputs.add(stack);
        }
    }
}
