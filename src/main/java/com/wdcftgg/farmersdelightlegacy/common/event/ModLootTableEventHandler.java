package com.wdcftgg.farmersdelightlegacy.common.event;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModItems;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryEmpty;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.EnchantRandomly;
import net.minecraft.world.storage.loot.functions.EnchantWithLevels;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraft.world.storage.loot.functions.SetDamage;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID)
public final class ModLootTableEventHandler {
    private static final LootCondition[] NO_CONDITIONS = new LootCondition[0];
    private static final LootFunction[] NO_FUNCTIONS = new LootFunction[0];
    private ModLootTableEventHandler() {
    }
    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (!Configuration.generateFDChestLoot) {
            return;
        }
        ResourceLocation tableName = event.getName();
        if (LootTableList.CHESTS_ABANDONED_MINESHAFT.equals(tableName)) {
            addAbandonedMineshaftLoot(event);
        } else if (LootTableList.CHESTS_SIMPLE_DUNGEON.equals(tableName)) {
            addSimpleDungeonLoot(event);
        } else if (LootTableList.CHESTS_END_CITY_TREASURE.equals(tableName)) {
            addEndCityLoot(event);
        } else if (LootTableList.CHESTS_DESERT_PYRAMID.equals(tableName)) {
            addDesertPyramidLoot(event);
        } else if (LootTableList.CHESTS_VILLAGE_BLACKSMITH.equals(tableName)) {
            addVillageBlacksmithLoot(event);
        }
    }
    private static void addAbandonedMineshaftLoot(LootTableLoadEvent event) {
        addPool(event, "fd_abandoned_mineshaft_tools", fixedRolls(1),
                entry("cooking_pot", 1),
                entry("skillet", 1, damage(0.15F, 0.8F)),
                empty(6));
        addPool(event, "fd_abandoned_mineshaft_seeds", rangedRolls(1, 4),
                countEntry("tomato_seeds", 1, 2, 4),
                countEntry("cabbage_seeds", 1, 2, 4),
                countEntry("rice", 1, 2, 4),
                empty(2));
        addPool(event, "fd_abandoned_mineshaft_rope", fixedRolls(3),
                countEntry("rope", 1, 2, 12),
                empty(2));
    }
    private static void addSimpleDungeonLoot(LootTableLoadEvent event) {
        addPool(event, "fd_simple_dungeon_seeds", rangedRolls(1, 4),
                countEntry("tomato_seeds", 1, 2, 4),
                countEntry("cabbage_seeds", 1, 2, 4),
                empty(2));
        addPool(event, "fd_simple_dungeon_rope", fixedRolls(3),
                countEntry("rope", 1, 2, 12),
                empty(2));
    }
    private static void addEndCityLoot(LootTableLoadEvent event) {
        LootFunction enchantWithLevels = new EnchantWithLevels(NO_CONDITIONS, rangedRolls(20, 39), true);
        addPool(event, "fd_end_city_treasure", fixedRolls(1),
                entry("diamond_knife", 1, enchantWithLevels),
                entry("iron_knife", 1, enchantWithLevels),
                empty(6));
    }
    private static void addDesertPyramidLoot(LootTableLoadEvent event) {
        addPool(event, "fd_desert_pyramid_supplies", rangedRolls(1, 2),
                countEntry("tomato_seeds", 4, 2, 4),
                countEntry("rice", 4, 2, 4),
                countEntry("onion", 3, 2, 4),
                entry("golden_knife", 1, new EnchantRandomly(NO_CONDITIONS, null)),
                empty(4));
    }
    private static void addVillageBlacksmithLoot(LootTableLoadEvent event) {
        addPool(event, "fd_village_tools", fixedRolls(1),
                entry("flint_knife", 1),
                entry("iron_knife", 1),
                empty(1));
        addPool(event, "fd_village_food", rangedRolls(1, 2),
                entry("ham", 1),
                countEntry("minced_beef", 3, 2, 6),
                countEntry("bacon", 3, 2, 6),
                countEntry("mutton_chops", 3, 2, 6),
                empty(1));
        addPool(event, "fd_village_crops", rangedRolls(1, 3),
                countEntry("onion", 2, 1, 3),
                countEntry("tomato_seeds", 2, 1, 3),
                countEntry("cabbage_seeds", 2, 1, 3),
                countEntry("rice", 2, 1, 3));
    }
    private static void addPool(LootTableLoadEvent event, String name, RandomValueRange rolls, LootEntry... entries) {
        List<LootEntry> validEntries = new ArrayList<>();
        for (LootEntry entry : entries) {
            if (entry != null) {
                validEntries.add(entry);
            }
        }
        if (validEntries.isEmpty()) {
            return;
        }
        event.getTable().addPool(new LootPool(
                validEntries.toArray(new LootEntry[0]),
                NO_CONDITIONS,
                rolls,
                fixedRolls(0),
                FarmersDelightLegacy.MOD_ID + ":" + name));
    }
    private static LootEntry entry(String itemName, int weight, LootFunction... functions) {
        Item item = ModItems.get(itemName);
        if (item == null || item == Items.AIR) {
            return null;
        }
        return new LootEntryItem(item, weight, 0, functions == null ? NO_FUNCTIONS : functions, NO_CONDITIONS,
                FarmersDelightLegacy.MOD_ID + ":" + itemName + "_loot");
    }
    private static LootEntry countEntry(String itemName, int weight, float min, float max) {
        return entry(itemName, weight, new SetCount(NO_CONDITIONS, rangedRolls(min, max)));
    }
    private static LootEntry empty(int weight) {
        return new LootEntryEmpty(weight, 0, NO_CONDITIONS, FarmersDelightLegacy.MOD_ID + ":empty_loot");
    }
    private static LootFunction damage(float min, float max) {
        return new SetDamage(NO_CONDITIONS, rangedRolls(min, max));
    }
    private static RandomValueRange fixedRolls(float value) {
        return new RandomValueRange(value);
    }
    private static RandomValueRange rangedRolls(float min, float max) {
        return new RandomValueRange(min, max);
    }
}
