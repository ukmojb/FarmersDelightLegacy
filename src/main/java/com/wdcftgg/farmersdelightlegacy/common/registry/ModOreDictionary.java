package com.wdcftgg.farmersdelightlegacy.common.registry;

import com.wdcftgg.farmersdelightlegacy.api.knife.IKnifeItem;
import com.wdcftgg.farmersdelightlegacy.common.compat.BackportIngredients;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModOreDictionary {

    private static final Map<String, String> TAG_TO_OREDICT = buildTagToOreDictMap();
    private static boolean registered;
    private static boolean compatRegistered;

    private ModOreDictionary() {
    }

    public static void registerAll() {
        if (registered) {
            return;
        }

        registerFromModItems();
        registerVanillaFallbacks();
        ItemCategories.registerAll();

        registered = true;
    }

    public static Map<String, String> getTagToOreDictMap() {
        return TAG_TO_OREDICT;
    }

    public static void registerCompatOres() {
        if (compatRegistered) {
            return;
        }

        registerTrapdoorOreGroups();
        registerCherryPlankOreGroup();
        registerCampfireOreGroup();
        BackportIngredients.registerAll();
        ItemCategories.registerCompat();

        compatRegistered = true;
    }

    private static void registerFromModItems() {
        registerOre("cropCabbage", "cabbage");
        registerOre("cropCabbage", "cabbage_leaf");
        registerOre("cropTomato", "tomato");
        registerOre("cropOnion", "onion");
        registerOre("cropRice", "rice");

        registerOre("foodDough", "wheat_dough");
        registerOre("foodPasta", "raw_pasta");
        registerOre("pastaOrDough", "raw_pasta");
        registerOre("pastaOrDough", "wheat_dough");

        registerOre("pumpkinOrSlice", "pumpkin_slice");
        registerOre("dumplingFilling", "minced_beef");
        registerOre("rawOrMincedBeef", "minced_beef");
        registerOre("tomatoOrSauce", "tomato");
        registerOre("tomatoOrSauce", "tomato_sauce");

        registerOre("listAllbeefraw", "minced_beef");
        registerOre("listAllchickenraw", "chicken_cuts");
        registerOre("listAllmuttonraw", "mutton_chops");
        registerOre("listAllfishraw", "cod_slice");
        registerOre("listAllfishraw", "salmon_slice");
        registerOre("listAllporkraw", "bacon");

        registerOre("cabbageRoolIngredients", "chicken_cuts");
        registerOre("cabbageRoolIngredients", "mutton_chops");

        registerOre("dogPrey", "bacon");

        registerOre("cooked_eggs", "fried_egg");

        registerOre("listAllveggie", "tomato");
        registerOre("listAllveggie", "onion");

        registerOre("listAllmilk", "milk_bottle");

        for (Map.Entry<String, Item> entry : ModItems.ITEMS.entrySet()) {
            if (entry.getKey().endsWith("_knife") || entry.getValue() instanceof IKnifeItem) {
                OreDictionary.registerOre("toolKnife", new ItemStack(entry.getValue(), 1, OreDictionary.WILDCARD_VALUE));
            }
            if (entry.getKey().endsWith("_axe")) {
                OreDictionary.registerOre("toolAxe", new ItemStack(entry.getValue(), 1, OreDictionary.WILDCARD_VALUE));
            }
        }

        registerOreStack("fdRopes", new ItemStack(ModBlocks.ROPE));
        registerOreStack("rope", new ItemStack(ModBlocks.ROPE));
        registerOreStack("blockRope", new ItemStack(ModBlocks.ROPE));

        registerCanvasSignOreGroups();
    }

    private static void registerCherryPlankOreGroup() {
        registerOptionalOre("cherryPlank", "sakura", "plank_sakura");
        registerOptionalOre("cherryPlank", "suikecherry", "cherry_planks");
    }

    private static void registerCampfireOreGroup() {
        registerOptionalOre("campfire", "futuremc", "campfire");
    }

    private static void registerTrapdoorOreGroups() {
        String[][] trapdoorOreEntries = new String[][]{
                {"oakTrapdoor", "oak"},
                {"spruceTrapdoor", "spruce"},
                {"birchTrapdoor", "birch"},
                {"jungleTrapdoor", "jungle"},
                {"acaciaTrapdoor", "acacia"},
                {"darkOakTrapdoor", "dark_oak"},
                {"crimsonTrapdoor", "crimson"},
                {"warpedTrapdoor", "warped"}
        };
        for (String[] trapdoorOreEntry : trapdoorOreEntries) {
            String oreName = trapdoorOreEntry[0];
            String woodName = trapdoorOreEntry[1];
            boolean registeredVariant = registerOptionalOre(oreName, "futuremc", woodName + "_trapdoor");
            registeredVariant = registerOptionalOre(oreName, "quark", woodName + "_trapdoor") || registeredVariant;
            registeredVariant = registerOptionalOre(oreName, "nb", woodName + "_trapdoor") || registeredVariant;
            registeredVariant = registerOptionalOre(oreName, "farmers_future_delight", woodName + "_trapdoor") || registeredVariant;
            if ("oak".equals(woodName) || !registeredVariant) {
                OreDictionary.registerOre(oreName, new ItemStack(Item.getItemFromBlock(Blocks.TRAPDOOR)));
            }
        }
    }

    private static void registerCanvasSignOreGroups() {
        String[] canvasSigns = new String[]{
                "canvas_sign", "black_canvas_sign", "blue_canvas_sign", "brown_canvas_sign", "cyan_canvas_sign",
                "gray_canvas_sign", "green_canvas_sign", "light_blue_canvas_sign", "light_gray_canvas_sign",
                "lime_canvas_sign", "magenta_canvas_sign", "orange_canvas_sign", "pink_canvas_sign",
                "purple_canvas_sign", "red_canvas_sign", "white_canvas_sign", "yellow_canvas_sign"
        };
        String[] hangingCanvasSigns = new String[]{
                "hanging_canvas_sign", "black_hanging_canvas_sign", "blue_hanging_canvas_sign", "brown_hanging_canvas_sign",
                "cyan_hanging_canvas_sign", "gray_hanging_canvas_sign", "green_hanging_canvas_sign",
                "light_blue_hanging_canvas_sign", "light_gray_hanging_canvas_sign", "lime_hanging_canvas_sign",
                "magenta_hanging_canvas_sign", "orange_hanging_canvas_sign", "pink_hanging_canvas_sign",
                "purple_hanging_canvas_sign", "red_hanging_canvas_sign", "white_hanging_canvas_sign", "yellow_hanging_canvas_sign"
        };

        for (String itemName : canvasSigns) {
            registerOre("fdCanvasSigns", itemName);
        }
        for (String itemName : hangingCanvasSigns) {
            registerOre("fdHangingCanvasSigns", itemName);
        }
    }

    private static void registerVanillaFallbacks() {
        registerToolOre("toolAxe", Items.WOODEN_AXE);
        registerToolOre("toolAxe", Items.STONE_AXE);
        registerToolOre("toolAxe", Items.IRON_AXE);
        registerToolOre("toolAxe", Items.GOLDEN_AXE);
        registerToolOre("toolAxe", Items.DIAMOND_AXE);

        registerToolOre("toolPickaxe", Items.WOODEN_PICKAXE);
        registerToolOre("toolPickaxe", Items.STONE_PICKAXE);
        registerToolOre("toolPickaxe", Items.IRON_PICKAXE);
        registerToolOre("toolPickaxe", Items.GOLDEN_PICKAXE);
        registerToolOre("toolPickaxe", Items.DIAMOND_PICKAXE);

        registerToolOre("toolShovel", Items.WOODEN_SHOVEL);
        registerToolOre("toolShovel", Items.STONE_SHOVEL);
        registerToolOre("toolShovel", Items.IRON_SHOVEL);
        registerToolOre("toolShovel", Items.GOLDEN_SHOVEL);
        registerToolOre("toolShovel", Items.DIAMOND_SHOVEL);

        registerToolOre("toolShears", Items.SHEARS);
        registerToolOre("toolHoe", Items.WOODEN_HOE);
        registerToolOre("toolHoe", Items.STONE_HOE);
        registerToolOre("toolHoe", Items.IRON_HOE);
        registerToolOre("toolHoe", Items.GOLDEN_HOE);
        registerToolOre("toolHoe", Items.DIAMOND_HOE);
        registerOreStack("dyeWhite", new ItemStack(Items.DYE, 1, 15));
        registerOreStack("dyeBrown", new ItemStack(Items.DYE, 1, 3));
        registerOreStack("dyeBlack", new ItemStack(Items.DYE, 1, 0));
        registerOreStack("foodBread", new ItemStack(Items.BREAD));
        registerOreStack("cropBeetroot", new ItemStack(Items.BEETROOT));

        OreDictionary.registerOre("listAllbeefraw", new ItemStack(Items.BEEF));
        OreDictionary.registerOre("rawOrMincedBeef", new ItemStack(Items.BEEF));
        OreDictionary.registerOre("listAllchickenraw", new ItemStack(Items.CHICKEN));
        OreDictionary.registerOre("listAllmuttonraw", new ItemStack(Items.MUTTON));
        OreDictionary.registerOre("listAllporkraw", new ItemStack(Items.PORKCHOP));
        OreDictionary.registerOre("listAllfishraw", new ItemStack(Items.FISH, 1, 2));
        OreDictionary.registerOre("listAllfishraw", new ItemStack(Items.FISH, 1, 1));
        OreDictionary.registerOre("rawOrSlicedCod", new ItemStack(Items.FISH, 1, 0));
        registerOreStack("rawOrSlicedCod", itemStackOf("cod_slice"));
        OreDictionary.registerOre("rawOrSlicedSalmon", new ItemStack(Items.FISH, 1, 1));
        registerOreStack("rawOrSlicedSalmon", itemStackOf("salmon_slice"));
        OreDictionary.registerOre("pumpkinOrSlice", new ItemStack(Item.getItemFromBlock(Blocks.PUMPKIN)));
        OreDictionary.registerOre("pastaOrDough", new ItemStack(Items.WHEAT));
        OreDictionary.registerOre("potatoOrBaked", new ItemStack(Items.POTATO));
        OreDictionary.registerOre("potatoOrBaked", new ItemStack(Items.BAKED_POTATO));
        OreDictionary.registerOre("dumplingFilling", new ItemStack(Items.BEEF));
        OreDictionary.registerOre("dumplingFilling", new ItemStack(Items.CHICKEN));
        OreDictionary.registerOre("dumplingFilling", new ItemStack(Items.PORKCHOP));
        OreDictionary.registerOre("dumplingFilling", new ItemStack(Item.getItemFromBlock(Blocks.BROWN_MUSHROOM)));

        OreDictionary.registerOre("listAllEgg", new ItemStack(Items.EGG));
        OreDictionary.registerOre("listAllmilk", new ItemStack(Items.MILK_BUCKET));
        OreDictionary.registerOre("listAllveggie", new ItemStack(Items.CARROT));
        OreDictionary.registerOre("listAllveggie", new ItemStack(Items.POTATO));
        OreDictionary.registerOre("listAllveggie", new ItemStack(Items.BEETROOT));

        OreDictionary.registerOre("cabbageRoolIngredients", new ItemStack(Items.BEEF));
        OreDictionary.registerOre("cabbageRoolIngredients", new ItemStack(Items.CHICKEN));
        OreDictionary.registerOre("cabbageRoolIngredients", new ItemStack(Items.MUTTON));
        OreDictionary.registerOre("cabbageRoolIngredients", new ItemStack(Items.PORKCHOP));
        OreDictionary.registerOre("cabbageRoolIngredients", new ItemStack(Items.FISH, 1, 2));
        OreDictionary.registerOre("cabbageRoolIngredients", new ItemStack(Items.FISH, 1, 1));
        OreDictionary.registerOre("cabbageRoolIngredients", new ItemStack(Items.FISH, 1, 0));
        OreDictionary.registerOre("cabbageRoolIngredients", new ItemStack(Items.CARROT));
        OreDictionary.registerOre("cabbageRoolIngredients", new ItemStack(Items.POTATO));
        OreDictionary.registerOre("cabbageRoolIngredients", new ItemStack(Items.BEETROOT));
        OreDictionary.registerOre("cabbageRoolIngredients", new ItemStack(Item.getItemFromBlock(Blocks.BROWN_MUSHROOM)));
        OreDictionary.registerOre("cabbageRoolIngredients", new ItemStack(Item.getItemFromBlock(Blocks.RED_MUSHROOM)));

        OreDictionary.registerOre("mushroomRiceIngredients", new ItemStack(Items.CARROT));
        OreDictionary.registerOre("mushroomRiceIngredients", new ItemStack(Items.POTATO));

        OreDictionary.registerOre("dogPrey", new ItemStack(Items.CHICKEN));
        OreDictionary.registerOre("dogPrey", new ItemStack(Items.MUTTON));
        OreDictionary.registerOre("dogPrey", new ItemStack(Items.RABBIT));

        OreDictionary.registerOre("listAllmushroom", new ItemStack(Item.getItemFromBlock(Blocks.BROWN_MUSHROOM)));
        OreDictionary.registerOre("listAllmushroom", new ItemStack(Item.getItemFromBlock(Blocks.RED_MUSHROOM)));

        OreDictionary.registerOre("bone", new ItemStack(Items.BONE));
        OreDictionary.registerOre("listAllmeatraw", new ItemStack(Items.BEEF));
        OreDictionary.registerOre("listAllmeatraw", new ItemStack(Items.CHICKEN));
        OreDictionary.registerOre("listAllmeatraw", new ItemStack(Items.MUTTON));
        OreDictionary.registerOre("listAllmeatraw", new ItemStack(Items.PORKCHOP));
    }

    private static boolean registerOptionalOre(String oreName, String modId, String path) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(modId, path));
        if (item == null) {
            return false;
        }
        OreDictionary.registerOre(oreName, new ItemStack(item));
        return true;
    }

    private static void registerToolOre(String oreName, Item item) {
        OreDictionary.registerOre(oreName, new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE));
    }

    private static void registerOre(String oreName, String itemName) {
        Item item = ModItems.ITEMS.get(itemName);
        if (item != null) {
            OreDictionary.registerOre(oreName, item);
        }
    }

    private static ItemStack itemStackOf(String itemName) {
        Item item = ModItems.ITEMS.get(itemName);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    private static void registerOreStack(String oreName, ItemStack stack) {
        if (!stack.isEmpty()) {
            OreDictionary.registerOre(oreName, stack);
        }
    }

    private static Map<String, String> buildTagToOreDictMap() {
        Map<String, String> tagMap = new LinkedHashMap<>();
        tagMap.put("forge:tools/knives", "toolKnife");
        tagMap.put("forge:tools/axes", "toolAxe");
        tagMap.put("forge:tools/pickaxes", "toolPickaxe");
        tagMap.put("forge:tools/shovels", "toolShovel");
        tagMap.put("forge:tools/shears", "toolShears");
        tagMap.put("forge:crops/cabbage", "cropCabbage");
        tagMap.put("forge:crops/onion", "cropOnion");
        tagMap.put("forge:crops/rice", "cropRice");
        tagMap.put("forge:crops/tomato", "cropTomato");
        tagMap.put("forge:raw_beef", "listAllbeefraw");
        tagMap.put("forge:raw_chicken", "listAllchickenraw");
        tagMap.put("forge:raw_mutton", "listAllmuttonraw");
        tagMap.put("forge:raw_pork", "listAllporkraw");
        tagMap.put("forge:mushrooms", "listAllmushroom");
        tagMap.put("forge:berries", "listAllberry");
        tagMap.put("forge:vegetables", "listAllveggie");
        tagMap.put("forge:eggs", "listAllEgg");
        tagMap.put("forge:cooked_eggs", "cooked_eggs");
        tagMap.put("forge:milk", "listAllmilk");
        tagMap.put("forge:dough", "foodDough");
        tagMap.put("forge:pasta", "foodPasta");
        tagMap.put("forge:bones", "bone");
        tagMap.put("forge:salad_ingredients", "foodLeafyGreen");
        tagMap.put("farmersdelight:cabbage_roll_ingredients", "fdCabbageRollIngredients");
        tagMap.put("farmersdelight:wolf_prey", "listAllmeatraw");
        tagMap.put("farmersdelight:canvas_signs", "fdCanvasSigns");
        tagMap.put("farmersdelight:hanging_canvas_signs", "fdHangingCanvasSigns");
        tagMap.putAll(ItemCategories.tagMappings());
        return Collections.unmodifiableMap(tagMap);
    }
}
