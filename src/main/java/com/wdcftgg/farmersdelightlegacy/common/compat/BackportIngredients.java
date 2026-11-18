package com.wdcftgg.farmersdelightlegacy.common.compat;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

public final class BackportIngredients {

    private static final String FFD = "farmers_future_delight:";

    private BackportIngredients() {
    }

    public static void registerAll() {
        register("cropSweetBerry", "futuremc:sweet_berries", FFD + "sweet_berries");
        register("cropGlowBerry", "futuremc:glow_berries", FFD + "glow_berries");
        register("listAllberry", "futuremc:sweet_berries", "futuremc:glow_berries",
                FFD + "sweet_berries", FFD + "glow_berries");
        register("foodHoney", "futuremc:honey_bottle", FFD + "honey_bottle");
        register("fdHoneycomb", "futuremc:honeycomb", FFD + "honeycomb");
        register("fdGlowInkSac", "oe:glow_ink_sac", FFD + "glow_ink_sac");
        register("mayBeKelp", "oe:dried_kelp", FFD + "dried_kelp");
        register("fdBoneBrothIngredients", "futuremc:glow_berries", FFD + "glow_berries",
                FFD + "hanging_roots", FFD + "glow_lichen");
        register("fdCrimsonFungus", "nb:crimson_fungus", FFD + "crimson_fungus");
        register("fdWarpedFungus", "nb:warped_fungus", FFD + "warped_fungus");
        register("crimsonPlank", "nb:crimson_planks", FFD + "crimson_planks");
        register("warpedPlank", "nb:warped_planks", FFD + "warped_planks");
        register("ingotNetherite", "futuremc:netherite_ingot", "nb:netherite_ingot", FFD + "netherite_ingot");
        register("fdNetheriteUpgradeTemplate", FFD + "netherite_upgrade_smithing_template");
        register("campfire", "futuremc:campfire", "campfire:campfire", FFD + "campfire");
        register("bamboo", "futuremc:bamboo", "sakura:bamboo", FFD + "bamboo");
        register("fdIronChain", "futuremc:chain", "nb:chain_block", FFD + "iron_chain");
        register("fdCornflower", "futuremc:cornflower", FFD + "cornflower");
        register("fdLilyOfTheValley", "futuremc:lily_of_the_valley", FFD + "lily_of_the_valley");
        register("fdWitherRose", "futuremc:wither_rose", FFD + "wither_rose");
        register("fdLeatherHorseArmor", "futuremc:leather_horse_armor", FFD + "leather_horse_armor");
        OreDictionary.registerOre("fdBoneBrothIngredients", new ItemStack(Blocks.BROWN_MUSHROOM));
        OreDictionary.registerOre("fdBoneBrothIngredients", new ItemStack(Blocks.RED_MUSHROOM));
        for (ItemStack mushroom : OreDictionary.getOres("listAllmushroom", false)) {
            registerStack("fdBoneBrothIngredients", mushroom);
        }
    }

    private static void register(String oreName, String... itemIds) {
        for (String itemId : itemIds) {
            ResourceLocation id = new ResourceLocation(itemId);
            if (ForgeRegistries.ITEMS.containsKey(id)) {
                Item item = ForgeRegistries.ITEMS.getValue(id);
                registerStack(oreName, new ItemStack(item));
            }
        }
    }

    private static void registerStack(String oreName, ItemStack stack) {
        for (ItemStack existing : OreDictionary.getOres(oreName, false)) {
            if (ItemStack.areItemsEqual(existing, stack)) {
                return;
            }
        }
        OreDictionary.registerOre(oreName, stack);
    }
}
