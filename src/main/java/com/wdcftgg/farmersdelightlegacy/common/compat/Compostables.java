package com.wdcftgg.farmersdelightlegacy.common.compat;

import com.wdcftgg.farmersdelightlegacy.common.registry.ModItems;
import com.wdcftgg.farmersdelightlegacy.common.registry.BackportContent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import java.util.function.BiConsumer;

public final class Compostables {
    private Compostables() {
    }

    public static void forEach(BiConsumer<ItemStack, Integer> consumer) {
        accept(consumer, 30, "tree_bark", "straw", "cabbage_seeds", "tomato_seeds", "rice", "rice_panicle", "sandy_shrub");
        accept(consumer, 50, "pumpkin_slice", "cabbage_leaf", "kelp_roll_slice");
        accept(consumer, 65, "cabbage", "onion", "tomato", "wild_cabbages", "wild_onions", "wild_tomatoes",
                "wild_carrots", "wild_potatoes", "wild_beetroots", "wild_rice", "pie_crust");
        accept(consumer, 85, "rice_bale", "sweet_berry_cookie", "honey_cookie", "cake_slice", "apple_pie_slice",
                "sweet_berry_cheesecake_slice", "chocolate_pie_slice", "raw_pasta", "rotten_tomato", "kelp_roll", "pumpkin_pie_slice");
        accept(consumer, 100, "apple_pie", "sweet_berry_cheesecake", "chocolate_pie", "dumplings",
                "stuffed_pumpkin_block", "brown_mushroom_colony", "red_mushroom_colony");
    }

    private static void accept(BiConsumer<ItemStack, Integer> consumer, int chance, String... names) {
        for (String name : names) {
            if (!BackportContent.isEnabled(name)) {
                continue;
            }
            Item item = ModItems.get(name);
            if (item == null) {
                throw new IllegalStateException("Missing compostable item: " + name);
            }
            consumer.accept(new ItemStack(item), chance);
        }
    }
}
