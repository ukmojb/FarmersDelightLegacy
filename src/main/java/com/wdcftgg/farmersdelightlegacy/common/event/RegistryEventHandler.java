package com.wdcftgg.farmersdelightlegacy.common.event;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModEffects;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModEnchantments;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModItems;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModOreDictionary;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModSounds;
import com.wdcftgg.farmersdelightlegacy.common.recipe.FoodServingRecipe;
import com.wdcftgg.farmersdelightlegacy.common.recipe.WaterDoughRecipe;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.Potion;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID)
public final class RegistryEventHandler {

    private RegistryEventHandler() {
    }

    @SubscribeEvent
    public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        ModBlocks.registerAll(event);
    }

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        ModBlocks.registerItemBlocks(event);
        ModItems.registerAll(event);
        ModOreDictionary.registerAll();
        Configuration.applyRuntimeOverrides();
    }

    @SubscribeEvent
    public static void onRegisterEffects(RegistryEvent.Register<Potion> event) {
        ModEffects.registerAll(event);
    }

    @SubscribeEvent
    public static void onRegisterEnchantments(RegistryEvent.Register<Enchantment> event) {
        ModEnchantments.registerAll(event);
    }

    @SubscribeEvent
    public static void onRegisterSounds(RegistryEvent.Register<SoundEvent> event) {
        ModSounds.registerAll(event);
    }

    @SubscribeEvent
    public static void onRegisterRecipes(RegistryEvent.Register<IRecipe> event) {
        event.getRegistry().register(new FoodServingRecipe().setRegistryName(new ResourceLocation(FarmersDelightLegacy.MOD_ID, "food_serving")));
        event.getRegistry().register(new WaterDoughRecipe().setRegistryName(new ResourceLocation(FarmersDelightLegacy.MOD_ID, "wheat_dough_from_water")));
    }
}
