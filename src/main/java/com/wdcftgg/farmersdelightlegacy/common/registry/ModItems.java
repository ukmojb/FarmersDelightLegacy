package com.wdcftgg.farmersdelightlegacy.common.registry;

import com.wdcftgg.farmersdelight.Tags;
import com.wdcftgg.farmersdelightlegacy.common.ModCreativeTab;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class ModItems {

    private static final String[] ITEM_TEXTURE_NAMES = new String[]{
            "apple_cider", "apple_pie", "apple_pie_slice", "bacon", "bacon_and_eggs", "bacon_sandwich", "baked_cod_stew",
            "barbecue_stick", "beef_patty", "beef_stew", "black_canvas_sign", "black_hanging_canvas_sign", "blue_canvas_sign",
            "blue_hanging_canvas_sign", "bone_broth", "brown_canvas_sign", "brown_hanging_canvas_sign", "cabbage", "cabbage_leaf",
            "cabbage_rolls", "cabbage_seeds", "cake_slice", "canvas", "canvas_sign", "chicken_cuts", "chicken_sandwich",
            "chicken_soup", "chocolate_pie", "chocolate_pie_slice", "cod_roll", "cod_slice", "cooked_bacon",
            "cooked_chicken_cuts", "cooked_cod_slice", "cooked_mutton_chops", "cooked_rice", "cooked_salmon_slice", "cooking_pot",
            "cyan_canvas_sign", "cyan_hanging_canvas_sign", "diamond_knife", "dog_food", "dumplings", "egg_sandwich",
            "empty_container_slot_bowl", "fish_stew", "flint_knife", "fried_egg", "fried_rice", "fruit_salad", "full_tatami_mat",
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
            "hamburger", "honey_cookie", "honey_glazed_ham", "horse_feed", "hot_cocoa", "kelp_roll", "kelp_roll_slice",
            "melon_juice", "melon_popsicle", "milk_bottle", "minced_beef", "mixed_salad", "mushroom_rice", "mutton_chops",
            "mutton_wrap", "nether_salad", "noodle_soup", "onion", "pasta_with_meatballs", "pasta_with_mutton_chop", "pie_crust",
            "pumpkin_pie_slice", "pumpkin_slice", "pumpkin_soup", "ratatouille", "raw_pasta", "rice", "rice_panicle",
            "roast_chicken", "roasted_mutton_chops", "salmon_roll", "salmon_slice", "shepherds_pie", "smoked_ham",
            "squid_ink_pasta", "steak_and_potatoes", "stuffed_potato", "stuffed_pumpkin", "sweet_berry_cheesecake",
            "sweet_berry_cheesecake_slice", "sweet_berry_cookie", "tomato", "tomato_sauce", "vegetable_noodles", "vegetable_soup",
            "wheat_dough"
    ));

    public static final Map<String, Item> ITEMS = new LinkedHashMap<>();

    public static Item TOMATO;
    public static Item TOMATO_SEEDS;
    public static Item RICE;
    public static Item RICE_PANICLE;

    private ModItems() {
    }

    public static void registerAll(RegistryEvent.Register<Item> event) {
        if (!ITEMS.isEmpty()) {
            return;
        }

        TOMATO = registerSimple("tomato");
        TOMATO_SEEDS = register("tomato_seeds", new ItemSeeds(ModBlocks.TOMATOES, Blocks.FARMLAND));
        RICE = register("rice", new ItemSeeds(ModBlocks.RICE, Blocks.FARMLAND));
        RICE_PANICLE = registerSimple("rice_panicle");

        registerKnife("flint_knife", Item.ToolMaterial.WOOD);
        registerKnife("iron_knife", Item.ToolMaterial.IRON);
        registerKnife("golden_knife", Item.ToolMaterial.GOLD);
        registerKnife("diamond_knife", Item.ToolMaterial.DIAMOND);
        registerKnife("netherite_knife", Item.ToolMaterial.DIAMOND);

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
        return register(path, new ItemFood(amount, saturation, false));
    }

    private static Item registerKnife(String path, Item.ToolMaterial material) {
        return register(path, new ItemSword(material));
    }

    private static Item register(String path, Item item) {
        item.setRegistryName(new ResourceLocation(Tags.MOD_ID, path));
        item.setTranslationKey(Tags.MOD_ID + "." + path);
        item.setCreativeTab(ModCreativeTab.TAB);
        ITEMS.put(path, item);
        return item;
    }
}

