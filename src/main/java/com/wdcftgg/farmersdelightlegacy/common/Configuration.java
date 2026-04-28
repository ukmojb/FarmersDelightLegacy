package com.wdcftgg.farmersdelightlegacy.common;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class Configuration {

    public static final String CATEGORY_SETTINGS = "settings";
    public static boolean enableVanillaCropCrates = true;
    public static boolean farmersBuyFDCrops = true;
    public static boolean wanderingTraderSellsFDItems = true;
    public static double richSoilBoostChance = 0.2D;
    public static double cuttingBoardFortuneBonus = 0.1D;
    public static boolean enableRopeReeling = true;
    public static String[] canvasSignDarkBackgroundList = new String[]{"gray", "purple", "blue", "brown", "green", "red", "black"};
    public static boolean toolAxeUsesItemAxeCheck = false;

    public static final String CATEGORY_FARMING = "farming";
    public static String defaultTomatoVineRope = "farmersdelight:rope";
    public static boolean enableTomatoVineClimbingTaggedRopes = true;

    public static final String CATEGORY_RECIPE_BOOK = "recipe_book";
    public static boolean enableRecipeBookCookingPot = true;

    public static final String CATEGORY_OVERRIDES = "overrides";
    public static boolean vanillaSoupExtraEffects = true;
    public static boolean rabbitStewJumpBoost = true;
    public static boolean dispenserUsesToolsOnCuttingBoard = true;

    public static final String CATEGORY_OVERRIDES_STACK_SIZE = CATEGORY_OVERRIDES + ".stack_size";
    public static boolean enableStackableSoupItems = true;
    public static String[] soupItemList = new String[]{"minecraft:mushroom_stew", "minecraft:beetroot_soup", "minecraft:rabbit_stew"};

    public static final String CATEGORY_WORLD = "world";
    public static boolean generateFDChestLoot = true;
    public static boolean generateVillageCompostHeaps = true;
    public static boolean generateVillageFarmFDCrops = true;
    public static int chanceWildCabbages = 30;
    public static int chanceWildBeetroots = 30;
    public static int chanceWildPotatoes = 100;
    public static int chanceWildCarrots = 120;
    public static int chanceWildOnions = 120;
    public static int chanceWildTomatoes = 100;
    public static int chanceWildRice = 20;
    public static boolean generateBrownMushroomColonies = true;
    public static int chanceBrownMushroomColonies = 15;
    public static boolean generateRedMushroomColonies = true;
    public static int chanceRedMushroomColonies = 15;

    public static final String CATEGORY_CLIENT = "client";
    public static boolean nourishmentHungerOverlay = true;
    public static boolean comfortHealthOverlay = true;
    public static boolean foodEffectTooltip = true;

    private static final String[] DEFAULT_DARK_CANVAS_BACKGROUNDS = new String[]{"gray", "purple", "blue", "brown", "green", "red", "black"};
    private static final String[] DEFAULT_SOUP_ITEMS = new String[]{"minecraft:mushroom_stew", "minecraft:beetroot_soup", "minecraft:rabbit_stew"};
    private static final String[] ROPE_ORE_NAMES = new String[]{"fdRopes", "rope", "blockRope"};
    private static final Set<Item> STACK_SIZE_OVERRIDDEN_SOUPS = new HashSet<>();
    private static net.minecraftforge.common.config.Configuration config;

    private Configuration() {
    }

    public static void load(File configFile) {
        config = new net.minecraftforge.common.config.Configuration(configFile);
        sync();
    }

    public static void sync() {
        if (config == null) {
            return;
        }

        config.load();

        enableVanillaCropCrates = config.getBoolean("enableVanillaCropCrates", CATEGORY_SETTINGS, true,
                "Farmer's Delight adds crates (3x3) for vanilla crops, similar to Quark and Thermal Cultivation. Should they be craftable?");
        farmersBuyFDCrops = config.getBoolean("farmersBuyFDCrops", CATEGORY_SETTINGS, true,
                "Should Novice and Apprentice Farmers buy this mod's crops? (Kept for upstream parity; vanilla 1.12.2 has no matching wandering-trade path here.)");
        wanderingTraderSellsFDItems = config.getBoolean("wanderingTraderSellsFDItems", CATEGORY_SETTINGS, true,
                "Should the Wandering Trader sell some of this mod's items? (Kept for upstream parity; vanilla 1.12.2 has no Wandering Trader.)");
        richSoilBoostChance = config.getFloat("richSoilBoostChance", CATEGORY_SETTINGS, 0.2F, 0.0F, 1.0F,
                "How often should Rich Soil Farmland boost a plant's growth at each random tick? Set it to 0.0 to disable this.");
        cuttingBoardFortuneBonus = config.getFloat("cuttingBoardFortuneBonus", CATEGORY_SETTINGS, 0.1F, 0.0F, 1.0F,
                "How much of a bonus should each level of Fortune grant to Cutting Board chances? Set it to 0.0 to disable this.");
        enableRopeReeling = config.getBoolean("enableRopeReeling", CATEGORY_SETTINGS, true,
                "Should players be able to reel back rope, bottom to top, when sneak-using with an empty hand on them?");
        canvasSignDarkBackgroundList = config.getStringList("canvasSignDarkBackgroundList", CATEGORY_SETTINGS, DEFAULT_DARK_CANVAS_BACKGROUNDS,
                "Dye colors that make Canvas Sign text render white by default. Valid values: white, orange, magenta, light_blue, yellow, lime, pink, gray, light_gray, cyan, purple, blue, brown, green, red, black.");
        toolAxeUsesItemAxeCheck = config.getBoolean("toolAxeUsesItemAxeCheck", CATEGORY_SETTINGS, false,
                "When a Cutting Board recipe tool is ore:toolAxe, match the held tool with instanceof ItemAxe instead of the OreDictionary list.");

        defaultTomatoVineRope = config.getString("defaultTomatoVineRope", CATEGORY_FARMING, "farmersdelight:rope",
                "Which rope should Tomato Vines leave behind when mined by hand?");
        enableTomatoVineClimbingTaggedRopes = config.getBoolean("enableTomatoVineClimbingTaggedRopes", CATEGORY_FARMING, true,
                "Should tomato vines be able to climb ropes registered through 1.12.2 OreDictionary entries fdRopes, rope or blockRope?");

        enableRecipeBookCookingPot = config.getBoolean("enableRecipeBookCookingPot", CATEGORY_RECIPE_BOOK, true,
                "Should the Cooking Pot have a Recipe Book available on its interface? (Kept for upstream parity; 1.12.2 GUI has no recipe book widget.)");

        vanillaSoupExtraEffects = config.getBoolean("vanillaSoupExtraEffects", CATEGORY_OVERRIDES, true,
                "Should soups and stews from vanilla Minecraft grant additional effects, like meals from this mod?");
        rabbitStewJumpBoost = config.getBoolean("rabbitStewJumpBoost", CATEGORY_OVERRIDES, true,
                "Should Rabbit Stew grant users the jumping prowess of a rabbit when eaten?");
        dispenserUsesToolsOnCuttingBoard = config.getBoolean("dispenserUsesToolsOnCuttingBoard", CATEGORY_OVERRIDES, true,
                "Should the Dispenser be able to operate a Cutting Board in front of it? (Kept for upstream parity; dispenser cutting is not present in this 1.12.2 port.)");

        enableStackableSoupItems = config.getBoolean("enableStackableSoupItems", CATEGORY_OVERRIDES_STACK_SIZE, true,
                "Should BowlFoodItems in the following list become stackable to 16, much like Farmer's Delight's meals?");
        soupItemList = config.getStringList("soupItemList", CATEGORY_OVERRIDES_STACK_SIZE, DEFAULT_SOUP_ITEMS,
                "List of BowlFoodItems. They must be vanilla ItemSoup items or return a bowl container to be affected.");

        generateFDChestLoot = config.getBoolean("generateFDChestLoot", CATEGORY_WORLD, true,
                "Should this mod add some of its items as extra chest loot across Minecraft? (Kept for upstream parity; chest-loot injection is not present in this 1.12.2 port.)");
        generateVillageCompostHeaps = config.getBoolean("genVillageCompostHeaps", CATEGORY_WORLD, true,
                "Should FD generate Compost Heaps across village biomes when the Village Names compatibility path is loaded?");
        generateVillageFarmFDCrops = config.getBoolean("genFDCropsOnVillageFarms", CATEGORY_WORLD, true,
                "Should FD crops show up planted randomly in various village farms? (Kept for upstream parity.)");
        chanceWildCabbages = getWorldChance("wild_cabbages", 30);
        chanceWildBeetroots = getWorldChance("wild_beetroots", 30);
        chanceWildPotatoes = getWorldChance("wild_potatoes", 100);
        chanceWildCarrots = getWorldChance("wild_carrots", 120);
        chanceWildOnions = getWorldChance("wild_onions", 120);
        chanceWildTomatoes = getWorldChance("wild_tomatoes", 100);
        chanceWildRice = getWorldChance("wild_rice", 20);
        generateBrownMushroomColonies = config.getBoolean("genBrownMushroomColony", CATEGORY_WORLD + ".brown_mushroom_colonies", true,
                "Generate brown mushroom colonies on mushroom fields.");
        chanceBrownMushroomColonies = getWorldChance("brown_mushroom_colonies", 15);
        generateRedMushroomColonies = config.getBoolean("genRedMushroomColony", CATEGORY_WORLD + ".red_mushroom_colonies", true,
                "Generate red mushroom colonies on mushroom fields.");
        chanceRedMushroomColonies = getWorldChance("red_mushroom_colonies", 15);

        nourishmentHungerOverlay = config.getBoolean("nourishmentHungerOverlay", CATEGORY_CLIENT, true,
                "Should the hunger bar have a gilded overlay when the player has the Nourishment effect?");
        comfortHealthOverlay = config.getBoolean("comfortHealthOverlay", CATEGORY_CLIENT, true,
                "Should the health bar have a silver sheen when the player has the Comfort effect?");
        foodEffectTooltip = config.getBoolean("foodEffectTooltip", CATEGORY_CLIENT, true,
                "Should meal and drink tooltips display which effects they provide?");

        if (config.hasChanged()) {
            config.save();
        }
    }

    private static int getWorldChance(String categoryPath, int defaultValue) {
        return config.getInt("chance", CATEGORY_WORLD + "." + categoryPath, defaultValue, 0, Integer.MAX_VALUE,
                "Chance of generating clusters. Smaller value = more frequent. Set to 0 to disable this generator in 1.12.2.");
    }

    public static void applyRuntimeOverrides() {
        applyStackSizeOverrides();
    }

    private static void applyStackSizeOverrides() {
        for (Item item : STACK_SIZE_OVERRIDDEN_SOUPS) {
            if (item != null && isBowlFoodItem(item)) {
                item.setMaxStackSize(1);
            }
        }
        STACK_SIZE_OVERRIDDEN_SOUPS.clear();

        Set<String> configuredItems = new HashSet<>(Arrays.asList(soupItemList));
        for (String itemName : configuredItems) {
            Item item = getItem(itemName);
            if (item == null || !isBowlFoodItem(item)) {
                continue;
            }
            item.setMaxStackSize(enableStackableSoupItems ? 16 : 1);
            if (enableStackableSoupItems) {
                STACK_SIZE_OVERRIDDEN_SOUPS.add(item);
            }
        }
    }

    private static boolean isBowlFoodItem(Item item) {
        if (item instanceof ItemSoup) {
            return true;
        }
        ItemStack stack = new ItemStack(item);
        return item.hasContainerItem(stack) && item.getContainerItem(stack).getItem() == Items.BOWL;
    }

    public static boolean isCanvasSignDarkBackground(String colorName) {
        if (colorName == null || colorName.isEmpty()) {
            return false;
        }
        String normalizedColor = colorName.toLowerCase(Locale.ROOT);
        for (String configuredColor : canvasSignDarkBackgroundList) {
            if (normalizedColor.equals(configuredColor.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    public static Block getDefaultTomatoVineRopeBlock() {
        Block block = getBlock(defaultTomatoVineRope);
        if (block != null && block != Blocks.AIR) {
            return block;
        }
        return getBlock("farmersdelight:rope");
    }

    public static boolean isTomatoVineClimbableRope(Block block) {
        if (block == null || block == Blocks.AIR) {
            return false;
        }
        Block defaultRopeBlock = getDefaultTomatoVineRopeBlock();
        if (block == defaultRopeBlock) {
            return true;
        }
        return enableTomatoVineClimbingTaggedRopes && hasRopeOreDictionaryEntry(block);
    }

    private static boolean hasRopeOreDictionaryEntry(Block block) {
        Item item = Item.getItemFromBlock(block);
        if (item == null || item == Items.AIR) {
            return false;
        }
        ItemStack stack = new ItemStack(item);
        int[] oreIds = OreDictionary.getOreIDs(stack);
        for (int oreId : oreIds) {
            String oreName = OreDictionary.getOreName(oreId);
            for (String acceptedName : ROPE_ORE_NAMES) {
                if (acceptedName.equals(oreName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Item getItem(String itemName) {
        ResourceLocation itemId = getResourceLocation(itemName, "minecraft");
        Item item = itemId == null ? null : ForgeRegistries.ITEMS.getValue(itemId);
        return item == Items.AIR ? null : item;
    }

    private static Block getBlock(String blockName) {
        ResourceLocation blockId = getResourceLocation(blockName, FarmersDelightLegacy.MOD_ID);
        Block block = blockId == null ? null : ForgeRegistries.BLOCKS.getValue(blockId);
        return block == Blocks.AIR ? null : block;
    }

    private static ResourceLocation getResourceLocation(String path, String defaultNamespace) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        return path.contains(":") ? new ResourceLocation(path) : new ResourceLocation(defaultNamespace, path);
    }

    @Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID)
    public static final class EventHandler {

        private EventHandler() {
        }

        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (!FarmersDelightLegacy.MOD_ID.equals(event.getModID())) {
                return;
            }
            sync();
            applyRuntimeOverrides();
        }
    }
}

