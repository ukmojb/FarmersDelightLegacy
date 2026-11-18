package com.wdcftgg.farmersdelightlegacy.common;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import com.wdcftgg.farmersdelightlegacy.common.registry.BackportContent;
import com.wdcftgg.farmersdelightlegacy.common.compat.WanderingTradersBackportCompat;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
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

    private static final String[] DEFAULT_HORSE_FEED_USERS = {
            "minecraft:horse", "minecraft:skeleton_horse", "minecraft:zombie_horse",
            "minecraft:donkey", "minecraft:mule", "minecraft:llama", "farmers_future_delight:trader_llama"
    };
    private static final String[] DEFAULT_DOG_FOOD_USERS = {"minecraft:wolf"};
    private static final String[] DEFAULT_HORSE_FEED_TEMPTED = {"minecraft:horse", "minecraft:donkey", "minecraft:mule"};
    private static final String[] DEFAULT_PLANTED_FROM_BELOW = {
            "farmers_future_delight:cave_vines", "farmers_future_delight:cave_vines_plant"
    };
    private static final String[] DEFAULT_UNAFFECTED_BY_RICH_SOIL = {
            "minecraft:grass", "minecraft:tallgrass", "minecraft:double_plant@0", "minecraft:double_plant@1",
            "minecraft:double_plant@4", "minecraft:double_plant@5",
            "farmersdelight:sandy_shrub", "farmersdelight:brown_mushroom_colony", "farmersdelight:red_mushroom_colony",
            "farmersdelight:wild_cabbages", "farmersdelight:wild_onions", "farmersdelight:wild_tomatoes",
            "farmersdelight:wild_carrots", "farmersdelight:wild_potatoes", "farmersdelight:wild_beetroots", "farmersdelight:wild_rice",
            "farmers_future_delight:moss_block", "farmers_future_delight:crimson_nylium", "farmers_future_delight:warped_nylium",
            "farmers_future_delight:twisting_vines", "farmers_future_delight:twisting_vines_plant",
            "farmers_future_delight:big_dripleaf", "farmers_future_delight:big_dripleaf_waterlogged", "farmers_future_delight:big_dripleaf_stem",
            "suikecherry:pink_petals"
    };
    private static final String[] DEFAULT_COLONY_GROWABLE_ON = {"farmersdelight:rich_soil"};

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
    public static String[] horseFeedUsers = DEFAULT_HORSE_FEED_USERS.clone();
    public static String[] horseFeedTempted = DEFAULT_HORSE_FEED_TEMPTED.clone();
    public static String[] dogFoodUsers = DEFAULT_DOG_FOOD_USERS.clone();

    public static final String CATEGORY_FARMING = "farming";
    public static String defaultTomatoVineRope = "farmersdelight:rope";
    public static boolean enableTomatoVineClimbingTaggedRopes = true;
    public static boolean enableTomatoRopePermanence = true;
    public static String[] plantedFromBelow = DEFAULT_PLANTED_FROM_BELOW.clone();
    public static String[] unaffectedByRichSoil = DEFAULT_UNAFFECTED_BY_RICH_SOIL.clone();
    public static String[] mushroomColonyGrowableOn = DEFAULT_COLONY_GROWABLE_ON.clone();

    public static final String CATEGORY_RECIPE_BOOK = "recipe_book";
    public static boolean enableRecipeBookCookingPot = true;

    public static final String CATEGORY_OVERRIDES = "overrides";
    public static boolean vanillaSoupExtraEffects = true;
    public static boolean enableRabbitStewBuff = true;
    public static boolean dispenserUsesToolsOnCuttingBoard = true;
    public static boolean enableCanvasSignEditing = true;
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
    private static ItemFood overriddenRabbitStew;
    private static int originalRabbitNutrition;
    private static float originalRabbitSaturation;
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
        BackportContent.load(config);
        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void sync() {
        if (config == null) {
            return;
        }

        config.load();
        BackportContent.load(config);

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
        horseFeedUsers = config.getStringList("horseFeedUsers", CATEGORY_SETTINGS, DEFAULT_HORSE_FEED_USERS,
                "接受马饲料的实体注册 ID；马类实体必须已驯服。");
        horseFeedTempted = config.getStringList("horseFeedTempted", CATEGORY_SETTINGS, DEFAULT_HORSE_FEED_TEMPTED,
                "手持马饲料时跟随玩家的实体注册 ID；实体必须使用地面寻路。");
        dogFoodUsers = config.getStringList("dogFoodUsers", CATEGORY_SETTINGS, DEFAULT_DOG_FOOD_USERS,
                "接受狗粮的实体注册 ID；可驯服实体必须已驯服。");
        defaultTomatoVineRope = config.getString("defaultTomatoVineRope", CATEGORY_FARMING, "farmersdelight:rope",
                "Which rope should Tomato Vines leave behind when mined by hand?");
        enableTomatoVineClimbingTaggedRopes = config.getBoolean("enableTomatoVineClimbingTaggedRopes", CATEGORY_FARMING, true,
                "Should tomato vines be able to climb ropes registered through 1.12.2 OreDictionary entries fdRopes, rope or blockRope?");
        enableTomatoRopePermanence = config.getBoolean("enableTomatoRopePermanence", CATEGORY_FARMING, true,
                "破坏或替换绳上番茄时保留绳索。关闭后允许命令和区域编辑直接替换；玩家采挖仍保留绳索。");
        plantedFromBelow = config.getStringList("plantedFromBelow", CATEGORY_FARMING, DEFAULT_PLANTED_FROM_BELOW,
                "从上方方块底面生长的植物，沃土仅从下方促生此类植物。填写方块注册 ID，可用 @元数据 限定状态。");
        unaffectedByRichSoil = config.getStringList("unaffectedByRichSoil", CATEGORY_FARMING, DEFAULT_UNAFFECTED_BY_RICH_SOIL,
                "不接受沃土骨粉促生的方块注册 ID，可用 @元数据 限定状态。");
        mushroomColonyGrowableOn = config.getStringList("mushroomColonyGrowableOn", CATEGORY_FARMING, DEFAULT_COLONY_GROWABLE_ON,
                "允许蘑菇群落自然生长的地面方块注册 ID，可用 @元数据 限定状态。");

        enableRecipeBookCookingPot = config.getBoolean("enableRecipeBookCookingPot", CATEGORY_RECIPE_BOOK, true,
                "Should the Cooking Pot have a Recipe Book available on its interface? (Kept for upstream parity; 1.12.2 GUI has no recipe book widget.)");

        vanillaSoupExtraEffects = config.getBoolean("vanillaSoupExtraEffects", CATEGORY_OVERRIDES, true,
                "Should soups and stews from vanilla Minecraft grant additional effects, like meals from this mod?");
        enableRabbitStewBuff = config.getBoolean("enableRabbitStewBuff", CATEGORY_OVERRIDES, true,
                "启用后，兔肉煲恢复 14 点饥饿值、21 点饱和度，并提供 5 分钟滋养。");
        config.getCategory(CATEGORY_OVERRIDES).remove("rabbitStewJumpBoost");
        dispenserUsesToolsOnCuttingBoard = config.getBoolean("dispenserUsesToolsOnCuttingBoard", CATEGORY_OVERRIDES, true,
                "Should the Dispenser be able to operate a Cutting Board in front of it?");
        enableCanvasSignEditing = config.getBoolean("enableCanvasSignEditing", CATEGORY_OVERRIDES, true,
                "If enabled, right-clicking a Canvas Sign opens its text editor.");
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

        configureGuiProperties();
        if (config.hasChanged()) {
            config.save();
        }
    }

    private static void configureGuiProperties() {
        for (String name : config.getCategoryNames()) {
            ConfigCategory category = config.getCategory(name);
            category.setLanguageKey("farmersdelight.configuration.category." + name);
            for (Property property : category.values()) {
                property.setLanguageKey("farmersdelight.configuration." + property.getName());
            }
        }
        config.getCategory(CATEGORY_RECIPE_BOOK).setShowInGui(false);
        config.getCategory(CATEGORY_SETTINGS).get("enableVanillaCropCrates").setRequiresMcRestart(true);
        config.getCategory(CATEGORY_WORLD).setRequiresWorldRestart(true);
        config.getCategory(CATEGORY_WORLD).get("genVillageCompostHeaps").setRequiresMcRestart(true);
        for (String path : wildCropGenerationCategoryPaths) {
            config.getCategory(CATEGORY_WORLD + "." + path).get("biomesInitialized").setShowInGui(false);
        }
    }

    public static List<ConfigCategory> getConfigCategories() {
        List<ConfigCategory> categories = new ArrayList<>();
        for (String name : config.getCategoryNames()) {
            ConfigCategory category = config.getCategory(name);
            if (!category.isChild() && category.showInGui()) {
                categories.add(category);
            }
        }
        return categories;
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
            biomeProperty.setDefaultValues(defaultBiomes);
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
        applyRabbitStewBuff();
    }

    private static void applyRabbitStewBuff() {
        if (enableRabbitStewBuff) {
            if (overriddenRabbitStew == null) {
                overriddenRabbitStew = (ItemFood) Items.RABBIT_STEW;
                originalRabbitNutrition = overriddenRabbitStew.healAmount;
                originalRabbitSaturation = overriddenRabbitStew.saturationModifier;
            }
            overriddenRabbitStew.healAmount = 14;
            overriddenRabbitStew.saturationModifier = 0.75F;
        } else if (overriddenRabbitStew != null) {
            overriddenRabbitStew.healAmount = originalRabbitNutrition;
            overriddenRabbitStew.saturationModifier = originalRabbitSaturation;
            overriddenRabbitStew = null;
        }
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
        if ("farmersdelight:rope".equals(defaultTomatoVineRope)) {
            return ModBlocks.ROPE;
        }
        Block block = getBlock(defaultTomatoVineRope);
        if (block != null && block != Blocks.AIR) {
            return block;
        }
        FarmersDelightLegacy.LOGGER.warn("番茄返还绳索配置 {} 未注册有效方块，使用 farmersdelight:rope", defaultTomatoVineRope);
        return ModBlocks.ROPE;
    }

    public static boolean isTomatoVineClimbableRope(Block block) {
        if (block == null || block == Blocks.AIR) {
            return false;
        }
        if (block == ModBlocks.ROPE) {
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
            if (config != null) {
                config.save();
            }
            syncAfterBiomeRegistration();
            applyRuntimeOverrides();
            if (configDirectory != null) {
                WanderingTradersBackportCompat.syncTradeTable(configDirectory);
            }
        }
    }
}

