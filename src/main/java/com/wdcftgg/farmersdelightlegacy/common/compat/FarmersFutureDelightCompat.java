package com.wdcftgg.farmersdelightlegacy.common.compat;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.api.heat.HeatSourceApi;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CampfireCookingRecipe;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModItems;
import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.CampfireCookingRecipeManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.api.WorkstationRegistryEvent;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;
import xy177.farmersfuturedelight.api.CompostingRules;
import xy177.farmersfuturedelight.api.WorkstationRecipes;
import xy177.farmersfuturedelight.common.block.BlockCampfire;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class FarmersFutureDelightCompat {
    public static final String MOD_ID = "farmers_future_delight";

    private FarmersFutureDelightCompat() {
    }

    public static void registerAll() {
        MinecraftForge.EVENT_BUS.register(new FarmersFutureDelightCompat());
        HeatSourceApi.registerDirectHeatSourcePredicate(MOD_ID + ":campfires",
                (world, pos, state) -> BlockCampfire.isLit(state, world, pos));
        ResourceLocation lavaCauldron = new ResourceLocation(MOD_ID, "lava_cauldron");
        if (ForgeRegistries.BLOCKS.containsKey(lavaCauldron)) {
            HeatSourceApi.registerDirectHeatSourcePredicate(MOD_ID + ":lava_cauldron",
                    (world, pos, state) -> lavaCauldron.equals(state.getBlock().getRegistryName()));
        }
    }

    @SubscribeEvent
    public void registerWorkstations(WorkstationRegistryEvent event) {
        registerRecipes(CampfireCookingRecipeManager.getRecipes());
    }

    static void registerRecipes(List<CampfireCookingRecipe> recipes) {
        Compostables.forEach((stack, chance) -> CompostingRules.register(
                stack.getItem().getRegistryName(), Ingredient.fromStacks(stack), chance / 100.0D, true));
        registerCampfireRecipes(recipes);
        registerSmithingRecipe();
        FarmersDelightLegacy.LOGGER.info("Registered Farmer's Future Delight cooking, composting and smithing compatibility.");
    }

    static void registerCampfireRecipes(List<CampfireCookingRecipe> recipes) {
        for (int index = recipes.size() - 1; index >= 0; index--) {
            CampfireCookingRecipe recipe = recipes.get(index);
            List<ItemStack> inputs = CampfireCookingCompat.expandInputs(recipe);
            if (!inputs.isEmpty()) {
                WorkstationRecipes.register(WorkstationRecipes.Type.CAMPFIRE_COOKING,
                        new ResourceLocation(recipe.getRecipeId()),
                        Collections.singletonList(Ingredient.fromStacks(inputs.toArray(new ItemStack[0]))),
                        recipe.getResultStack(), recipe.getCookingTime(), 0.35F);
            }
        }
    }

    private static void registerSmithingRecipe() {
        if (ModItems.get("netherite_knife") != null
                && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(MOD_ID, "smithing_table"))
                && !OreDictionary.getOres("fdNetheriteUpgradeTemplate", false).isEmpty()
                && !OreDictionary.getOres("ingotNetherite", false).isEmpty()) {
            Ingredient knife = Ingredient.fromStacks(new ItemStack(ModItems.get("diamond_knife"), 1, OreDictionary.WILDCARD_VALUE));
            WorkstationRecipes.register(WorkstationRecipes.Type.SMITHING,
                    new ResourceLocation(FarmersDelightLegacy.MOD_ID, "netherite_knife"),
                    Arrays.asList(new OreIngredient("fdNetheriteUpgradeTemplate"), knife, new OreIngredient("ingotNetherite")),
                    inputs -> WorkstationRecipes.copySmithingData(inputs.get(1), new ItemStack(ModItems.get("netherite_knife"))),
                    1, 0.0F);
        }
    }
}
