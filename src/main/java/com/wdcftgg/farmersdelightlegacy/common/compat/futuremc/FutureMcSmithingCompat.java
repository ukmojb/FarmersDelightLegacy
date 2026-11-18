package com.wdcftgg.farmersdelightlegacy.common.compat.futuremc;

import com.wdcftgg.farmersdelightlegacy.common.registry.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;
import thedarkcolour.futuremc.recipe.smithing.SmithingRecipe;
import thedarkcolour.futuremc.recipe.smithing.SmithingRecipes;

public final class FutureMcSmithingCompat {
    private FutureMcSmithingCompat() {
    }

    public static void registerAll() {
        if (ModItems.get("netherite_knife") == null || OreDictionary.getOres("ingotNetherite", false).isEmpty()) {
            return;
        }
        Ingredient base = Ingredient.fromStacks(new ItemStack(ModItems.get("diamond_knife"), 1, OreDictionary.WILDCARD_VALUE));
        SmithingRecipes.INSTANCE.getRecipes().add(new SmithingRecipe(base, new OreIngredient("ingotNetherite"),
                new ItemStack(ModItems.get("netherite_knife"))));
    }
}
