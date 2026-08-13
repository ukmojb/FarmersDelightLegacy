package com.wdcftgg.farmersdelightlegacy.common;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.compat.WanderingTradersBackportCompat;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.io.File;
import java.util.*;

public final class Configuration {

    private static final int[] defaultWildCropDimensions = new int[]{0};
    private static final String[] emptyWildCropBiomes = new String[0];
    private static final String[] wildCropGenerationCategoryPaths = new String[]{
            "wild_cabbages", "wild_beetroots", "wild_potatoes", "wild_carrots",
            "wild_onions", "wild_tomatoes", "wild_rice"
    };

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
    public static boolean enablePumpkinPieSneakToPlace = false;
    public static boolean enablePumpkinPieDirectEating = false;

    public static final String CATEGORY_OVERRIDES_STACK_SIZE = CATEGORY_OVERRIDES + ".stack_size";
    public static boolean enableStackableSoupItems = true;
    public static String[] soupItemList = new String[]{"minecraft:mushroom_stew", "minecraft:beetroot_soup", "minecraft:rabbit_stew"};

    public static final String CATEGORY_WORLD = "world";
    public static boolean generateFDChestLoot = true;
    public static boolean useReducedRopeChestLoot = false;
    public static boolean generateVillageCompostHeaps = true;
    public static boolean generateVillageFarmFDCrops = true;
    public static int chanceWildCabbages = 30;
    public static int chanceWildBeetroots = 30;
    public static int chanceWildPotatoes = 100;
    public static int chanceWildCarrots = 120;
    public static int chanceWildOnions = 120;
    public static int chanceWildTomatoes = 100;
    public static int chanceWildRice = 20;
    public static WildCropGenerationSettings wildCabbagesGeneration = WildCropGenerationSettings.createDefault(30);
    public static WildCropGenerationSettings wildBeetrootsGeneration = WildCropGenerationSettings.createDefault(30);
    public static WildCropGenerationSettings wildPotatoesGeneration = WildCropGenerationSettings.createDefault(100);
    public static WildCropGenerationSettings wildCarrotsGeneration = WildCropGenerationSettings.createDefault(120);
    public static WildCropGenerationSettings wildOnionsGeneration = WildCropGenerationSettings.createDefault(120);
    public static WildCropGenerationSettings wildTomatoesGeneration = WildCropGenerationSettings.createDefault(100);
    public static WildCropGenerationSettings wildRiceGeneration = WildCropGenerationSettings.createDefault(20);
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
    private static File configDirectory;
    private static boolean wildCropBiomeDefaultInitializationReady;

    private Configuration() {
    }

    public static void load(File configFile) {
        config = new net.minecraftforge.common.config.Configuration(configFile);
        configDirectory = configFile.getParentFile();
        wildCropBiomeDefaultInitializationReady = false;
        sync();
    }

    public static void syncAfterBiomeRegistration() {
        wildCropBiomeDefaultInitializationReady = true;
        sync();
    }

    public static void loadSettingsOnly(File configFile) {
        config = new net.minecraftforge.common.config.Configuration(configFile);
        configDirectory = configFile.getParentFile();
        syncSettingsOnly();
    }

    private static void syncSettingsOnly() {
        if (config == null) {
            return;
        }

        config.load();
        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void sync() {
        if (config == null) {
            return;
        }

        config.load();

        enableVanillaCropCrates = config.getBoolean("enableVanillaCropCrates", CATEGORY_SETTINGS, true,
                "Farmer's Delight adds crates (3x3) for vanilla crops, similar to Quark and Thermal Cultivation. Should they be craftable?");
        farmersBuyFDCrops = config.getBoolean("farmersBuyFDCrops", CATEGORY_SETTINGS, true,
                "Should Novice and Apprentice Farmers buy this mod's crops?");
        wanderingTraderSellsFDItems = config.getBoolean("wanderingTraderSellsFDItems", CATEGORY_SETTINGS, true,
                "Should the Wandering Trader sell some of this mod's items when Wandering Traders Backport is loaded?");
        richSoilBoostChance = config.getFloat("richSoilBoostChance", CATEGORY_SETTINGS, 0.2F, 0.0F, 1.0F,
                "How often should Rich Soil Farmland boost a plant's growth at each random tick? Set it to 0.0 to disable this.");
        cuttingBoardFortuneBonus = config.getFloat("cuttingBoardFortuneBonus", CATEGORY_SETTINGS, 0.1F, 0.0F, 1.0F,
                "How much of a bonus should each level of Fortune grant to Cutting Board chances? Set it to 0.0 to disable this.");
        enableRopeReeling = config.getBoolean("enableRopeReeling", CATEGORY_SETTINGS, true,
                "Should players be able to reel back rope, bottom to top, when sneak-using with an empty hand on them?");
        canvasSignDarkBackgroundList = config.getStringList("canvasSignDarkBackgroundList", CATEGORY_SETTINGS, DEFAULT_DARK_CANVAS_BACKGROUNDS,
                "Dye colors that make Canvas Sign text render white by default. Valid values: white, orange, magenta, light_blue, yellow, lime, pink, gray, light_gray, cyan, purple, blue, brown, green, red, black.");
        toolAxeUsesItemAxeCheck = config.getBoolean("toolAxeUsesItemAxeCheck", CATEGORY_SETTINGS, true,
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
        enablePumpkinPieSneakToPlace = config.getBoolean("enablePumpkinPieSneakToPlace", CATEGORY_OVERRIDES, false,
                "If enabled, Pumpkin Pie will require the user to sneak to place it down as a block.");
        enablePumpkinPieDirectEating = config.getBoolean("enablePumpkinPieDirectEating", CATEGORY_OVERRIDES, false,
                "If enabled, Pumpkin Pie can be eaten directly from the player's hand. Disable this to make Pumpkin Pie placement-only while keeping placed pie slices edible.");

        enableStackableSoupItems = config.getBoolean("enableStackableSoupItems", CATEGORY_OVERRIDES_STACK_SIZE, true,
                "Should BowlFoodItems in the following list become stackable to 16, much like Farmer's Delight's meals?");
        soupItemList = config.getStringList("soupItemList", CATEGORY_OVERRIDES_STACK_SIZE, DEFAULT_SOUP_ITEMS,
                "List of BowlFoodItems. They must be vanilla ItemSoup items or return a bowl container to be affected.");

        generateFDChestLoot = config.getBoolean("generateFDChestLoot", CATEGORY_WORLD, true,
                "Should this mod add some of its items as extra chest loot across Minecraft?");
        useReducedRopeChestLoot = config.getBoolean("useReducedRopeChestLoot", CATEGORY_WORLD, true,
                "Should abandoned mineshaft and simple dungeon chests use one rope loot roll instead of the original three rolls? Disable this to restore the original, more abundant rope loot.");
        generateVillageCompostHeaps = config.getBoolean("genVillageCompostHeaps", CATEGORY_WORLD, true,
                "Should FD generate Compost Heaps across village biomes when the Village Names compatibility path is loaded?");
        generateVillageFarmFDCrops = config.getBoolean("genFDCropsOnVillageFarms", CATEGORY_WORLD, true,
                "Should FD crops show up planted randomly in various village farms? (Kept for upstream parity.)");
        wildCabbagesGeneration = getWildCropGenerationSettings("wild_cabbages", 30);
        wildBeetrootsGeneration = getWildCropGenerationSettings("wild_beetroots", 30);
        wildPotatoesGeneration = getWildCropGenerationSettings("wild_potatoes", 100);
        wildCarrotsGeneration = getWildCropGenerationSettings("wild_carrots", 120);
        wildOnionsGeneration = getWildCropGenerationSettings("wild_onions", 120);
        wildTomatoesGeneration = getWildCropGenerationSettings("wild_tomatoes", 100);
        wildRiceGeneration = getWildCropGenerationSettings("wild_rice", 20);
        chanceWildCabbages = wildCabbagesGeneration.getChance();
        chanceWildBeetroots = wildBeetrootsGeneration.getChance();
        chanceWildPotatoes = wildPotatoesGeneration.getChance();
        chanceWildCarrots = wildCarrotsGeneration.getChance();
        chanceWildOnions = wildOnionsGeneration.getChance();
        chanceWildTomatoes = wildTomatoesGeneration.getChance();
        chanceWildRice = wildRiceGeneration.getChance();
        generateBrownMushroomColonies = config.getBoolean("genBrownMushroomColony", CATEGORY_WORLD + ".brown_mushroom_colonies", true,
                "Generate brown mushroom colonies on mushroom fields.");
        chanceBrownMushroomColonies = getWorldChance("brown_mushroom_colonies", 15);
        generateRedMushroomColonies = config.getBoolean("genRedMushroomColony", CATEGORY_WORLD + ".red_mushroom_colonies", true,
                "Generate red mushroom colonies on mushroom fields.");
        chanceRedMushroomColonies = getWorldChance("red_mushroom_colonies", 15);
        removeGeneratedDefaultBiomesProperties();

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
                "Chance of generating clusters. Smaller value = more frequent. Set to 0 to disable this generator.");
    }

    private static WildCropGenerationSettings getWildCropGenerationSettings(String categoryPath, int defaultChance) {
        String category = CATEGORY_WORLD + "." + categoryPath;
        int chance = getWorldChance(categoryPath, defaultChance);
        int[] dimensions = config.get(category, "dimensions", defaultWildCropDimensions,
                "Dimension ids used by this wild crop generator. When dimensionsAreWhitelist is true, the crop only generates in these dimensions. When false, the crop skips these dimensions.").getIntList();
        boolean dimensionsAreWhitelist = config.getBoolean("dimensionsAreWhitelist", category, true,
                "If true, dimensions is a whitelist. If false, dimensions is a blacklist.");
        String[] defaultBiomes = getDefaultWildCropBiomes(categoryPath);
        Property biomesInitializedProperty = config.get(category, "biomesInitialized", false,
                "Internal marker used to decide whether this wild crop's biome list has already received its generated default values.");
        boolean biomesInitialized = biomesInitializedProperty.getBoolean(false);
        boolean biomePropertyExists = config.hasKey(category, "biomes");
        boolean shouldInitializeBiomes = wildCropBiomeDefaultInitializationReady && !biomesInitialized;
        Property biomeProperty = null;
        if (shouldInitializeBiomes || biomePropertyExists) {
            String[] biomeFallbackValues = shouldInitializeBiomes ? defaultBiomes : emptyWildCropBiomes;
            biomeProperty = config.get(category, "biomes", biomeFallbackValues,
                    "Biome registry ids used by this wild crop generator. When biomesInitialized is false, this list is regenerated from the built-in BiomeDictionary and temperature rules after biome registration is complete.");
        }
        Property biomesAreWhitelistProperty = config.get(category, "biomesAreWhitelist", true,
                "If true, biomes is a whitelist. If false, biomes is a blacklist.");
        boolean biomesAreWhitelist = biomesAreWhitelistProperty.getBoolean();
        if (shouldInitializeBiomes) {
            biomeProperty.set(defaultBiomes);
            biomesAreWhitelist = true;
            biomesAreWhitelistProperty.set(true);
            biomesInitializedProperty.set(true);
        }
        String[] biomes = biomeProperty == null ? emptyWildCropBiomes : biomeProperty.getStringList();
        return new WildCropGenerationSettings(chance, dimensions, dimensionsAreWhitelist, normalizeConfiguredStringList(biomes), biomesAreWhitelist);
    }

    public static String[] getWildCropGenerationCategoryPaths() {
        return wildCropGenerationCategoryPaths.clone();
    }

    public static int resetAllWildCropBiomesInitialized() {
        return resetWildCropBiomesInitialized(wildCropGenerationCategoryPaths);
    }

    public static boolean resetWildCropBiomesInitialized(String categoryPath) {
        String normalizedCategoryPath = normalizeWildCropGenerationCategoryPath(categoryPath);
        if (normalizedCategoryPath.isEmpty()) {
            return false;
        }
        return resetWildCropBiomesInitialized(new String[]{normalizedCategoryPath}) == 1;
    }

    private static int resetWildCropBiomesInitialized(String[] categoryPaths) {
        if (config == null) {
            return 0;
        }

        config.load();
        int resetCount = 0;
        for (String categoryPath : categoryPaths) {
            String normalizedCategoryPath = normalizeWildCropGenerationCategoryPath(categoryPath);
            if (normalizedCategoryPath.isEmpty()) {
                continue;
            }
            String category = CATEGORY_WORLD + "." + normalizedCategoryPath;
            Property biomesInitializedProperty = config.get(category, "biomesInitialized", false,
                    "Internal marker used to decide whether this wild crop's biome list has already received its generated default values.");
            biomesInitializedProperty.set(false);
            resetCount++;
        }

        if (resetCount > 0) {
            config.save();
            sync();
        }
        return resetCount;
    }

    private static String normalizeWildCropGenerationCategoryPath(String categoryPath) {
        if (categoryPath == null) {
            return "";
        }
        String normalizedCategoryPath = categoryPath.trim().toLowerCase(Locale.ROOT);
        for (String wildCropGenerationCategoryPath : wildCropGenerationCategoryPaths) {
            if (wildCropGenerationCategoryPath.equals(normalizedCategoryPath)) {
                return wildCropGenerationCategoryPath;
            }
        }
        return "";
    }

    private static String[] getDefaultWildCropBiomes(String categoryPath) {
        Set<String> biomeIds = new LinkedHashSet<>();
        for (Biome biome : ForgeRegistries.BIOMES.getValuesCollection()) {
            ResourceLocation biomeId = biome.getRegistryName();
            if (biomeId != null && matchesDefaultWildCropBiome(categoryPath, biome)) {
                biomeIds.add(biomeId.toString().toLowerCase(Locale.ROOT));
            }
        }
        return biomeIds.toArray(new String[0]);
    }

    private static boolean matchesDefaultWildCropBiome(String categoryPath, Biome biome) {
        if ("wild_cabbages".equals(categoryPath) || "wild_beetroots".equals(categoryPath)) {
            return BiomeDictionary.hasType(biome, BiomeDictionary.Type.BEACH);
        }
        if ("wild_onions".equals(categoryPath) || "wild_carrots".equals(categoryPath)) {
            return !BiomeDictionary.hasType(biome, BiomeDictionary.Type.MUSHROOM)
                    && isBiomeTemperatureBetween(biome, 0.4F, 0.9F);
        }
        if ("wild_tomatoes".equals(categoryPath)) {
            return BiomeDictionary.hasType(biome, BiomeDictionary.Type.HOT)
                    && !BiomeDictionary.hasType(biome, BiomeDictionary.Type.WET);
        }
        if ("wild_potatoes".equals(categoryPath)) {
            return isBiomeTemperatureBetween(biome, 0.1F, 0.3F);
        }
        if ("wild_rice".equals(categoryPath)) {
            return BiomeDictionary.hasType(biome, BiomeDictionary.Type.WET);
        }
        return false;
    }

    private static boolean isBiomeTemperatureBetween(Biome biome, float minimumTemperature, float maximumTemperature) {
        float temperature = biome.getDefaultTemperature();
        return temperature >= minimumTemperature && temperature <= maximumTemperature;
    }

    private static String[] normalizeConfiguredStringList(String[] values) {
        Set<String> normalizedValues = new LinkedHashSet<>();
        for (String value : values) {
            if (value == null) {
                continue;
            }
            String normalizedValue = value.trim().toLowerCase(Locale.ROOT);
            if (!normalizedValue.isEmpty()) {
                normalizedValues.add(normalizedValue);
            }
        }
        return normalizedValues.toArray(new String[0]);
    }

    private static void removeGeneratedDefaultBiomesProperties() {
        for (String categoryPath : wildCropGenerationCategoryPaths) {
            ConfigCategory category = config.getCategory(CATEGORY_WORLD + "." + categoryPath);
            if (category.containsKey("generatedDefaultBiomes")) {
                category.remove("generatedDefaultBiomes");
            }
        }
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

    public static final class WildCropGenerationSettings {
        private final int chance;
        private final Set<Integer> dimensions;
        private final boolean dimensionsAreWhitelist;
        private final Set<String> biomes;
        private final boolean biomesAreWhitelist;

        private WildCropGenerationSettings(int chance, int[] dimensions, boolean dimensionsAreWhitelist, String[] biomes, boolean biomesAreWhitelist) {
            this.chance = chance;
            this.dimensions = createDimensionSet(dimensions);
            this.dimensionsAreWhitelist = dimensionsAreWhitelist;
            this.biomes = createBiomeSet(biomes);
            this.biomesAreWhitelist = biomesAreWhitelist;
        }

        private static WildCropGenerationSettings createDefault(int chance) {
            return new WildCropGenerationSettings(chance, defaultWildCropDimensions, true, emptyWildCropBiomes, true);
        }

        private static Set<Integer> createDimensionSet(int[] dimensions) {
            Set<Integer> dimensionSet = new HashSet<>();
            for (int dimension : dimensions) {
                dimensionSet.add(dimension);
            }
            return dimensionSet;
        }

        private static Set<String> createBiomeSet(String[] biomes) {
            return new HashSet<>(Arrays.asList(biomes));
        }

        public int getChance() {
            return this.chance;
        }

        public boolean canGenerateInDimension(int dimension) {
            boolean containsDimension = this.dimensions.contains(dimension);
            return this.dimensionsAreWhitelist == containsDimension;
        }

        public boolean canGenerateInBiome(ResourceLocation biomeId) {
            String configuredBiomeId = biomeId == null ? "" : biomeId.toString().toLowerCase(Locale.ROOT);
            boolean containsBiome = this.biomes.contains(configuredBiomeId);
            return this.biomesAreWhitelist == containsBiome;
        }

        public boolean usesBiomeWhitelist() {
            return this.biomesAreWhitelist;
        }
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
            syncAfterBiomeRegistration();
            applyRuntimeOverrides();
            if (configDirectory != null) {
                WanderingTradersBackportCompat.syncTradeTable(configDirectory);
            }
        }
    }
}

