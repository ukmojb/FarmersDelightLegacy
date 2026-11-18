package com.wdcftgg.farmersdelightlegacy.common.event;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModItems;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.KilledByPlayer;
import net.minecraft.world.storage.loot.conditions.RandomChanceWithLooting;
import net.minecraft.world.storage.loot.functions.*;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID)
public final class ModLootTableEventHandler {
    private static final LootCondition[] NO_CONDITIONS = new LootCondition[0];
    private static final LootFunction[] NO_FUNCTIONS = new LootFunction[0];
    private static final Set<ResourceLocation> ONION_DROP_TABLES = new HashSet<>(Arrays.asList(
            LootTableList.ENTITIES_ZOMBIE, LootTableList.ENTITIES_ZOMBIE_VILLAGER, LootTableList.ENTITIES_HUSK,
            LootTableList.ENTITIES_EVOCATION_ILLAGER, LootTableList.ENTITIES_VINDICATION_ILLAGER,
            new ResourceLocation("farmers_future_delight", "entities/pillager")));
    private ModLootTableEventHandler() {
    }
    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (ONION_DROP_TABLES.contains(event.getName())) {
            event.getTable().addPool(new LootPool(new LootEntry[]{new LootEntryItem(ModItems.ONION, 1, 0,
                    NO_FUNCTIONS, NO_CONDITIONS, "farmersdelight:onion")},
                    new LootCondition[]{new KilledByPlayer(false), new RandomChanceWithLooting(0.02F, 0.01F)},
                    fixedRolls(1), fixedRolls(0), "farmersdelight:player_kill_onion"));
        }
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
        } else if ("farmers_future_delight".equals(tableName.getNamespace())) {
            addModernStructureLoot(event, tableName.getPath());
        } else if (new ResourceLocation("oe", "chests/shipwreck_supply").equals(tableName)) {
            addShipwreckLoot(event);
        }
    }

    private static void addModernStructureLoot(LootTableLoadEvent event, String path) {
        switch (path) {
            case "chests/bastion_hoglin_stable":
                addPool(event, "fd_bastion_tools", fixedRolls(1),
                        entry("diamond_knife", 1, damage(0.15F, 0.8F), randomEnchantment()),
                        entry("golden_knife", 2, randomEnchantment()), empty(2));
                addPool(event, "fd_bastion_meat", rangedRolls(1, 2),
                        countEntry("ham", 4, 2, 5), countEntry("smoked_ham", 2, 2, 5), empty(2));
                break;
            case "chests/bastion_treasure":
                addPool(event, "fd_bastion_treasure", fixedRolls(1),
                        new LootEntryItem(ModItems.get("diamond_knife"), 1, 0,
                                new LootFunction[]{damage(0.8F, 1F), randomEnchantment()}, NO_CONDITIONS,
                                "farmersdelight:enchanted_diamond_knife_loot"),
                        entry("diamond_knife", 1), empty(6));
                break;
            case "chests/pillager_outpost":
                addPool(event, "fd_outpost_crops", rangedRolls(1, 2),
                        countEntry("onion", 5, 4, 12), countEntry("onion_crate", 2, 1, 3), empty(2));
                break;
            case "chests/ruined_portal":
                addPool(event, "fd_portal_knife", fixedRolls(1), entry("golden_knife", 1, randomEnchantment()), empty(3));
                break;
            case "chests/shipwreck_supply":
                addShipwreckLoot(event);
                break;
            case "chests/village/village_butcher":
                addVillageButcherLoot(event);
                break;
            case "chests/village/village_desert_house":
                addVillageCropLoot(event, "tomato_seeds", "rice");
                break;
            case "chests/village/village_plains_house":
                addVillageCropLoot(event, "onion", "tomato_seeds");
                break;
            case "chests/village/village_savanna_house":
                addVillageCropLoot(event, "tomato_seeds", "cabbage_seeds");
                break;
            case "chests/village/village_snowy_house":
                addVillageCropLoot(event, "onion", "cabbage_seeds");
                break;
            case "chests/village/village_taiga_house":
                addVillageCropLoot(event, "cabbage_seeds", "rice");
                break;
            default:
                break;
        }
    }

    private static LootFunction randomEnchantment() {
        return new EnchantRandomly(NO_CONDITIONS, java.util.Collections.emptyList());
    }

    private static void addShipwreckLoot(LootTableLoadEvent event) {
        addPool(event, "fd_shipwreck_crops", rangedRolls(1, 2),
                countEntry("tomato_seeds", 6, 2, 4), countEntry("cabbage_seeds", 6, 2, 4),
                countEntry("onion", 6, 2, 4), countEntry("rice", 6, 2, 4));
        addPool(event, "fd_shipwreck_rope", rangedRolls(1, 3), countEntry("rope", 2, 4, 12), empty(1));
    }

    private static void addVillageCropLoot(LootTableLoadEvent event, String first, String second) {
        addPool(event, "fd_village_crops", rangedRolls(1, 3), countEntry(first, 1, 1, 3), countEntry(second, 1, 1, 3));
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
        addPool(event, "fd_abandoned_mineshaft_rope", getRopeLootRolls(),
                countEntry("rope", 1, 2, 12),
                empty(2));
    }
    private static void addSimpleDungeonLoot(LootTableLoadEvent event) {
        addPool(event, "fd_simple_dungeon_seeds", rangedRolls(1, 4),
                countEntry("tomato_seeds", 1, 2, 4),
                countEntry("cabbage_seeds", 1, 2, 4),
                empty(2));
        addPool(event, "fd_simple_dungeon_rope", getRopeLootRolls(),
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
                entry("golden_knife", 1, randomEnchantment()),
                empty(4));
    }
    private static void addVillageBlacksmithLoot(LootTableLoadEvent event) {
        addVillageButcherLoot(event);
        addPool(event, "fd_village_crops", rangedRolls(1, 3),
                countEntry("onion", 2, 1, 3),
                countEntry("tomato_seeds", 2, 1, 3),
                countEntry("cabbage_seeds", 2, 1, 3),
                countEntry("rice", 2, 1, 3));
    }

    private static void addVillageButcherLoot(LootTableLoadEvent event) {
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
    private static RandomValueRange getRopeLootRolls() {
        return fixedRolls(Configuration.useReducedRopeChestLoot ? 1 : 3);
    }
    private static RandomValueRange rangedRolls(float min, float max) {
        return new RandomValueRange(min, max);
    }
}
