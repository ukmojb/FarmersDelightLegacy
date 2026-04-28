package com.wdcftgg.farmersdelightlegacy.common.registry;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.ModCreativeTab;
import com.wdcftgg.farmersdelightlegacy.common.item.ItemBowlFoodTooltip;
import com.wdcftgg.farmersdelightlegacy.common.item.ItemDrinkableTooltip;
import com.wdcftgg.farmersdelightlegacy.common.item.ItemDogFood;
import com.wdcftgg.farmersdelightlegacy.common.item.ItemFoodTooltip;
import com.wdcftgg.farmersdelightlegacy.common.item.ItemHorseFeed;
import com.wdcftgg.farmersdelightlegacy.common.item.ItemKnife;
import com.wdcftgg.farmersdelightlegacy.common.item.ItemPlantableFood;
import com.wdcftgg.farmersdelightlegacy.common.item.ItemRice;
import com.wdcftgg.farmersdelightlegacy.common.item.ItemRottenTomato;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;

import java.util.*;

public final class ModItems {
    private static final int BRIEF_DURATION = 600;
    private static final int SHORT_DURATION = 1200;
    private static final int MEDIUM_DURATION = 3600;
    private static final int LONG_DURATION = 6000;

    private static final String[] ITEM_TEXTURE_NAMES = new String[]{
            "apple_cider", "apple_pie", "apple_pie_slice", "bacon", "bacon_and_eggs", "bacon_sandwich", "baked_cod_stew",
            "barbecue_stick", "beef_patty", "beef_stew", "black_canvas_sign", "black_hanging_canvas_sign", "blue_canvas_sign",
            "blue_hanging_canvas_sign", "bone_broth", "brown_canvas_sign", "brown_hanging_canvas_sign", "cabbage", "cabbage_leaf",
            "cabbage_rolls", "cabbage_seeds", "cake_slice", "canvas", "canvas_sign", "chicken_cuts", "chicken_sandwich",
            "chicken_soup", "chocolate_pie", "chocolate_pie_slice", "cod_roll", "cod_slice", "cooked_bacon",
            "cooked_chicken_cuts", "cooked_cod_slice", "cooked_mutton_chops", "cooked_rice", "cooked_salmon_slice", "cooking_pot",
            "cyan_canvas_sign", "cyan_hanging_canvas_sign", "diamond_knife", "dog_food", "dumplings", "egg_sandwich",
            "fish_stew", "flint_knife", "fried_egg", "fried_rice", "fruit_salad", "full_tatami_mat",
            "glow_berry_custard", "golden_knife", "gray_canvas_sign", "gray_hanging_canvas_sign", "green_canvas_sign",
            "green_hanging_canvas_sign", "grilled_salmon", "half_tatami_mat", "ham", "hamburger", "hanging_canvas_sign",
            "honey_cookie", "honey_glazed_ham", "honey_glazed_ham_block", "horse_feed", "hot_cocoa", "iron_knife", "kelp_roll",
            "kelp_roll_slice", "light_blue_canvas_sign", "light_blue_hanging_canvas_sign", "light_gray_canvas_sign",
            "light_gray_hanging_canvas_sign", "lime_canvas_sign", "lime_hanging_canvas_sign", "magenta_canvas_sign",
            "magenta_hanging_canvas_sign", "melon_juice", "melon_popsicle", "milk_bottle", "minced_beef", "mixed_salad",
            "mushroom_rice", "mutton_chops", "mutton_wrap", "nether_salad", "netherite_knife", "noodle_soup", "onion",
            "orange_canvas_sign", "orange_hanging_canvas_sign", "pasta_with_meatballs", "pasta_with_mutton_chop", "pie_crust",
            "pink_canvas_sign", "pink_hanging_canvas_sign", "pumpkin_pie_slice", "pumpkin_slice", "pumpkin_soup",
            "purple_canvas_sign", "purple_hanging_canvas_sign", "ratatouille", "raw_pasta", "red_canvas_sign",
            "red_hanging_canvas_sign", "rice", "rice_panicle", "rice_roll_medley_block", "roast_chicken", "roast_chicken_block",
            "roasted_mutton_chops", "rope", "rotten_tomato", "salmon_roll", "salmon_slice", "shepherds_pie", "shepherds_pie_block",
            "smoked_ham", "squid_ink_pasta", "steak_and_potatoes", "straw", "stuffed_potato", "stuffed_pumpkin",
            "stuffed_pumpkin_block", "sweet_berry_cheesecake", "sweet_berry_cheesecake_slice", "sweet_berry_cookie", "tomato",
            "tomato_sauce", "tomato_seeds", "tree_bark", "vegetable_noodles", "vegetable_soup", "wheat_dough", "white_canvas_sign",
            "white_hanging_canvas_sign", "yellow_canvas_sign", "yellow_hanging_canvas_sign"
    };

    private static final Set<String> FOOD_NAMES = new HashSet<>(Arrays.asList(
            "apple_cider", "apple_pie", "apple_pie_slice", "bacon", "bacon_and_eggs", "bacon_sandwich", "baked_cod_stew",
            "barbecue_stick", "beef_patty", "beef_stew", "cabbage", "cabbage_leaf", "cabbage_rolls", "cake_slice", "chicken_cuts",
            "chicken_sandwich", "chicken_soup", "chocolate_pie", "chocolate_pie_slice", "cod_roll", "cod_slice", "cooked_bacon",
            "cooked_chicken_cuts", "cooked_cod_slice", "cooked_mutton_chops", "cooked_rice", "cooked_salmon_slice", "dumplings",
            "egg_sandwich", "fish_stew", "fried_egg", "fried_rice", "fruit_salad", "glow_berry_custard", "grilled_salmon", "ham",
            "hamburger", "honey_cookie", "honey_glazed_ham", "hot_cocoa", "kelp_roll", "kelp_roll_slice",
            "melon_juice", "melon_popsicle", "milk_bottle", "minced_beef", "mixed_salad", "mushroom_rice", "mutton_chops",
            "mutton_wrap", "nether_salad", "noodle_soup", "onion", "pasta_with_meatballs", "pasta_with_mutton_chop", "pie_crust",
            "pumpkin_pie_slice", "pumpkin_slice", "pumpkin_soup", "ratatouille", "raw_pasta", "rice", "rice_panicle",
            "roast_chicken", "roasted_mutton_chops", "salmon_roll", "salmon_slice", "shepherds_pie", "smoked_ham",
            "squid_ink_pasta", "steak_and_potatoes", "stuffed_potato", "stuffed_pumpkin", "sweet_berry_cheesecake",
            "sweet_berry_cheesecake_slice", "sweet_berry_cookie", "tomato", "tomato_sauce", "vegetable_noodles", "vegetable_soup",
            "wheat_dough"
    ));

    private static final Set<String> STACK_TO_16 = new HashSet<>(Arrays.asList(
            "apple_cider", "melon_juice", "milk_bottle", "hot_cocoa",
            "glow_berry_custard", "fruit_salad", "mixed_salad", "nether_salad",
            "cooked_rice", "bone_broth", "beef_stew", "chicken_soup", "vegetable_soup", "fish_stew",
            "fried_rice", "pumpkin_soup", "baked_cod_stew", "noodle_soup",
            "bacon_and_eggs", "pasta_with_meatballs", "pasta_with_mutton_chop", "mushroom_rice",
            "roasted_mutton_chops", "vegetable_noodles", "steak_and_potatoes", "ratatouille",
            "squid_ink_pasta", "grilled_salmon", "roast_chicken", "stuffed_pumpkin",
            "honey_glazed_ham", "shepherds_pie", "dog_food", "horse_feed", "rotten_tomato"
    ));

    private static final Set<String> BOWL_FOOD_NAMES = new HashSet<>(Arrays.asList(
            "fruit_salad", "mixed_salad", "nether_salad",
            "cooked_rice", "bone_broth", "beef_stew", "chicken_soup", "vegetable_soup", "fish_stew",
            "fried_rice", "pumpkin_soup", "baked_cod_stew", "noodle_soup",
            "bacon_and_eggs", "pasta_with_meatballs", "pasta_with_mutton_chop", "mushroom_rice",
            "roasted_mutton_chops", "vegetable_noodles", "steak_and_potatoes", "ratatouille",
            "squid_ink_pasta", "grilled_salmon", "roast_chicken", "stuffed_pumpkin",
            "honey_glazed_ham", "shepherds_pie", "dog_food", "tomato_sauce"
    ));

    public static final Map<String, Item> ITEMS = new LinkedHashMap<>();

    public static Item TOMATO;
    public static Item TOMATO_SEEDS;
    public static Item ONION;
    public static Item RICE;
    public static Item RICE_PANICLE;

    private ModItems() {
    }

    public static void registerAll(RegistryEvent.Register<Item> event) {
        if (!ITEMS.isEmpty()) {
            return;
        }

        TOMATO = registerFood("tomato", 1, 0.3F);
        TOMATO_SEEDS = register("tomato_seeds", new ItemSeeds(ModBlocks.BUDDING_TOMATOES, Blocks.FARMLAND));
        register("cabbage_seeds", new ItemSeeds(ModBlocks.CABBAGES, Blocks.FARMLAND));
        ONION = register("onion", new ItemPlantableFood(2, 0.4F, ModBlocks.ONIONS, Blocks.FARMLAND, ModBlocks.RICH_SOIL_FARMLAND));
        RICE = register("rice", createRiceItem(ModBlocks.RICE));
        RICE_PANICLE = registerSimple("rice_panicle");
        registerFood("cabbage", 2, 0.4F);
        registerDrink("apple_cider", 4, 0.4F, true, "minecraft:absorption", SHORT_DURATION, 0, 1.0F,
                ItemDrinkableTooltip.DrinkEffect.NONE);
        registerFood("fried_egg", 4, 0.4F);
        registerFood("wheat_dough", 2, 0.3F, false, "minecraft:hunger", BRIEF_DURATION, 0, 0.3F);
        registerFood("raw_pasta", 2, 0.3F, false, "minecraft:hunger", BRIEF_DURATION, 0, 0.3F);
        registerFood("pie_crust", 2, 0.2F);
        registerFood("pumpkin_slice", 3, 0.3F);
        registerFood("cabbage_leaf", 1, 0.4F);
        registerFood("minced_beef", 2, 0.3F);
        registerFood("beef_patty", 4, 0.8F);
        registerFood("chicken_cuts", 1, 0.3F, false, "minecraft:hunger", BRIEF_DURATION, 0, 0.3F);
        registerFood("cooked_chicken_cuts", 3, 0.6F);
        registerFood("bacon", 2, 0.3F);
        registerFood("cooked_bacon", 4, 0.8F);
        registerFood("cod_slice", 1, 0.1F);
        registerFood("cooked_cod_slice", 3, 0.5F);
        registerFood("salmon_slice", 1, 0.1F);
        registerFood("cooked_salmon_slice", 3, 0.8F);
        registerFood("mutton_chops", 1, 0.3F);
        registerFood("cooked_mutton_chops", 3, 0.8F);
        registerFood("ham", 5, 0.3F);
        registerFood("smoked_ham", 10, 0.8F);
        registerFood("sweet_berry_cookie", 2, 0.1F);
        registerFood("honey_cookie", 2, 0.1F);
        registerFood("melon_popsicle", 3, 0.2F, true, null, 0, 0, 0.0F);
        registerFood("cake_slice", 2, 0.1F, false, "minecraft:speed", 400, 0, 1.0F);
        registerFood("apple_pie_slice", 3, 0.3F, false, "minecraft:speed", BRIEF_DURATION, 0, 1.0F);
        registerFood("sweet_berry_cheesecake_slice", 3, 0.3F, false, "minecraft:speed", BRIEF_DURATION, 0, 1.0F);
        registerFood("chocolate_pie_slice", 3, 0.3F, false, "minecraft:speed", BRIEF_DURATION, 0, 1.0F);
        registerFood("pumpkin_pie_slice", 3, 0.3F);
        registerFood("fruit_salad", 6, 0.6F, false, "minecraft:regeneration", 100, 0, 1.0F);
        registerFood("glow_berry_custard", 7, 0.6F, true, "minecraft:glowing", 100, 0, 1.0F);
        registerFood("mixed_salad", 6, 0.6F, false, "minecraft:regeneration", 100, 0, 1.0F);
        registerFood("nether_salad", 5, 0.4F, false, "minecraft:nausea", 240, 0, 0.3F);
        registerFood("barbecue_stick", 8, 0.9F);
        registerFood("egg_sandwich", 8, 0.8F);
        registerFood("chicken_sandwich", 10, 0.8F);
        registerFood("hamburger", 11, 0.8F);
        registerFood("bacon_sandwich", 10, 0.8F);
        registerFood("mutton_wrap", 10, 0.8F);
        registerFood("dumplings", 8, 0.8F);
        registerFood("stuffed_potato", 10, 0.7F);
        registerFood("cabbage_rolls", 5, 0.5F);
        registerFood("salmon_roll", 7, 0.6F);
        registerFood("cod_roll", 7, 0.6F);
        registerFood("kelp_roll", 12, 0.6F);
        registerFood("kelp_roll_slice", 6, 0.5F);
        registerFood("cooked_rice", 6, 0.4F, false, "farmersdelight:comfort", BRIEF_DURATION, 0, 1.0F);
        registerFood("bone_broth", 8, 0.7F, false, "farmersdelight:comfort", SHORT_DURATION, 0, 1.0F);
        registerFood("beef_stew", 12, 0.8F, false, "farmersdelight:comfort", MEDIUM_DURATION, 0, 1.0F);
        registerFood("vegetable_soup", 12, 0.8F, false, "farmersdelight:comfort", MEDIUM_DURATION, 0, 1.0F);
        registerFood("fish_stew", 12, 0.8F, false, "farmersdelight:comfort", MEDIUM_DURATION, 0, 1.0F);
        registerFood("chicken_soup", 14, 0.75F, false, "farmersdelight:comfort", LONG_DURATION, 0, 1.0F);
        registerFood("fried_rice", 14, 0.75F, false, "farmersdelight:comfort", LONG_DURATION, 0, 1.0F);
        registerFood("pumpkin_soup", 14, 0.75F, false, "farmersdelight:comfort", LONG_DURATION, 0, 1.0F);
        registerFood("baked_cod_stew", 14, 0.75F, false, "farmersdelight:comfort", LONG_DURATION, 0, 1.0F);
        registerFood("noodle_soup", 14, 0.75F, false, "farmersdelight:comfort", LONG_DURATION, 0, 1.0F);
        registerFood("bacon_and_eggs", 10, 0.6F, false, "farmersdelight:nourishment", SHORT_DURATION, 0, 1.0F);
        registerFood("ratatouille", 10, 0.6F, false, "farmersdelight:nourishment", SHORT_DURATION, 0, 1.0F);
        registerFood("steak_and_potatoes", 12, 0.8F, false, "farmersdelight:nourishment", MEDIUM_DURATION, 0, 1.0F);
        registerFood("pasta_with_meatballs", 12, 0.8F, false, "farmersdelight:nourishment", MEDIUM_DURATION, 0, 1.0F);
        registerFood("pasta_with_mutton_chop", 12, 0.8F, false, "farmersdelight:nourishment", MEDIUM_DURATION, 0, 1.0F);
        registerFood("mushroom_rice", 12, 0.8F, false, "farmersdelight:nourishment", MEDIUM_DURATION, 0, 1.0F);
        registerFood("grilled_salmon", 14, 0.75F, false, "farmersdelight:nourishment", MEDIUM_DURATION, 0, 1.0F);
        registerFood("roasted_mutton_chops", 14, 0.75F, false, "farmersdelight:nourishment", LONG_DURATION, 0, 1.0F);
        registerFood("vegetable_noodles", 14, 0.75F, false, "farmersdelight:nourishment", LONG_DURATION, 0, 1.0F);
        registerFood("squid_ink_pasta", 14, 0.75F, false, "farmersdelight:nourishment", LONG_DURATION, 0, 1.0F);
        registerFood("roast_chicken", 14, 0.75F, false, "farmersdelight:nourishment", LONG_DURATION, 0, 1.0F);
        registerFood("stuffed_pumpkin", 14, 0.75F, false, "farmersdelight:nourishment", LONG_DURATION, 0, 1.0F);
        registerFood("honey_glazed_ham", 14, 0.75F, false, "farmersdelight:nourishment", LONG_DURATION, 0, 1.0F);
        registerFood("shepherds_pie", 14, 0.75F, false, "farmersdelight:nourishment", LONG_DURATION, 0, 1.0F);
        registerDrink("milk_bottle", 4, 0.4F, false, null, 0, 0, 0.0F,
                ItemDrinkableTooltip.DrinkEffect.CLEAR_ONE, "farmersdelight.tooltip.milk_bottle");
        registerDrink("hot_cocoa", 4, 0.4F, false, null, 0, 0, 0.0F,
                ItemDrinkableTooltip.DrinkEffect.CLEAR_ONE_HARMFUL, "farmersdelight.tooltip.hot_cocoa");
        registerDrink("melon_juice", 4, 0.4F, false, null, 0, 0, 0.0F,
                ItemDrinkableTooltip.DrinkEffect.HEAL_MINOR, "farmersdelight.tooltip.melon_juice");
        registerFood("tomato_sauce", 4, 0.4F);
        register("rotten_tomato", new ItemRottenTomato());
        register("dog_food", new ItemDogFood(6, 0.6F));
        register("horse_feed", new ItemHorseFeed(8, 0.8F));

        registerKnife("flint_knife", Item.ToolMaterial.WOOD, 1.5D);
        registerKnife("iron_knife", Item.ToolMaterial.IRON, 2.5D);
        registerKnife("golden_knife", Item.ToolMaterial.GOLD, 0.5D);
        registerKnife("diamond_knife", Item.ToolMaterial.DIAMOND, 3.5D);
        registerKnife("netherite_knife", Item.ToolMaterial.DIAMOND, 4.5D);

        Set<String> blockNames = new LinkedHashSet<>(ModBlocks.BLOCKS.keySet());
        Set<String> existingNames = new LinkedHashSet<>(ITEMS.keySet());

        for (String itemName : ITEM_TEXTURE_NAMES) {
            if (existingNames.contains(itemName) || blockNames.contains(itemName)) {
                continue;
            }

            if (itemName.endsWith("_knife")) {
                continue;
            }

            if (FOOD_NAMES.contains(itemName)) {
                registerFood(itemName, 4, 0.4F);
            } else {
                registerSimple(itemName);
            }
            existingNames.add(itemName);
        }

        for (Item blockItem : ModBlocks.BLOCK_ITEMS) {
            if (blockItem.getRegistryName() != null) {
                ITEMS.put(blockItem.getRegistryName().getPath(), blockItem);
            }
        }

        for (Item item : ITEMS.values()) {
            if (item instanceof ItemBlock) {
                continue;
            }
            event.getRegistry().register(item);
        }
    }

    private static Item registerSimple(String path) {
        return register(path, new Item());
    }

    private static Item registerFood(String path, int amount, float saturation) {
        return registerFood(path, amount, saturation, false, null, 0, 0, 0.0F);
    }

    private static Item registerFood(String path, int amount, float saturation, boolean alwaysEdible, String effectId, int duration,
                                     int amplifier, float chance, String... extraTooltipKeys) {
        ResourceLocation effectResource = effectId == null ? null : new ResourceLocation(effectId);
        ItemFoodTooltip item = BOWL_FOOD_NAMES.contains(path)
                ? new ItemBowlFoodTooltip(amount, saturation, false, effectResource, duration, amplifier, chance, extraTooltipKeys)
                : new ItemFoodTooltip(amount, saturation, false, effectResource, duration, amplifier, chance, extraTooltipKeys);
        if (alwaysEdible) {
            item.setAlwaysEdible();
        }
        return register(path, item);
    }

    private static Item registerDrink(String path, int amount, float saturation, boolean alwaysEdible, String effectId, int duration,
                                      int amplifier, float chance, ItemDrinkableTooltip.DrinkEffect drinkEffect,
                                      String... extraTooltipKeys) {
        return register(path, new ItemDrinkableTooltip(amount, saturation, alwaysEdible,
                effectId == null ? null : new ResourceLocation(effectId), duration, amplifier, chance, drinkEffect, extraTooltipKeys));
    }

    private static Item registerKnife(String path, Item.ToolMaterial material, double attackDamage) {
        return register(path, new ItemKnife(material, attackDamage));
    }

    private static Item createRiceItem(Block cropBlock) {
        if (!Loader.isModLoaded("fluidlogged_api")) {
            return new ItemRice(cropBlock);
        }

        Item compatItem = instantiateCompatItem("com.wdcftgg.farmersdelightlegacy.common.compat.fluidlogged.ItemFluidloggedRice", cropBlock);
        return compatItem != null ? compatItem : new ItemRice(cropBlock);
    }

    private static Item instantiateCompatItem(String className, Block cropBlock) {
        try {
            return (Item) Class.forName(className).getDeclaredConstructor(Block.class).newInstance(cropBlock);
        } catch (ReflectiveOperationException | LinkageError exception) {
            FarmersDelightLegacy.LOGGER.warn("加载可选兼容物品失败：{}，已回退到默认实现。", className, exception);
            return null;
        }
    }

    private static Item register(String path, Item item) {
        item.setRegistryName(new ResourceLocation(FarmersDelightLegacy.MOD_ID, path));
        item.setTranslationKey(FarmersDelightLegacy.MOD_ID + "." + path);
        item.setCreativeTab(ModCreativeTab.TAB);
        if (STACK_TO_16.contains(path)) {
            item.setMaxStackSize(16);
        }
        ITEMS.put(path, item);
        return item;
    }

    public static Item get(String path) {
        return ITEMS.get(path);
    }
}
