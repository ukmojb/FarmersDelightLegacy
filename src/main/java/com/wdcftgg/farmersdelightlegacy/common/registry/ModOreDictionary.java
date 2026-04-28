package com.wdcftgg.farmersdelightlegacy.common.registry;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.OreDictionary;
import thedarkcolour.futuremc.registry.FItems;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModOreDictionary {

    private static final Map<String, String> TAG_TO_OREDICT = buildTagToOreDictMap();
    private static boolean registered;

    private ModOreDictionary() {
    }

    public static void registerAll() {
        if (registered) {
            return;
        }

        registerFromModItems();
        registerVanillaFallbacks();

        registered = true;
    }

    public static Map<String, String> getTagToOreDictMap() {
        return TAG_TO_OREDICT;
    }

    private static void registerFromModItems() {
        registerOre("cropCabbage", "cabbage");
        registerOre("cropCabbage", "cabbage_leaf");
        registerOre("cropTomato", "tomato");
        registerOre("cropOnion", "onion");
        registerOre("cropRice", "rice");
        registerOre("cropRice", "rice_panicle");
//        registerOre("cropSweetBerry", "sweet_berry_cookie");

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

        registerOre("dogPrey", "salmon_slice");
        registerOre("dogPrey", "bacon");

        registerOre("listAllveggie", "cabbage");
        registerOre("listAllveggie", "tomato");
        registerOre("listAllveggie", "onion");

//        registerOre("listAllberry", "<ore:seedSweetBerry>");
//        registerOre("listAllberry", "sweet_berry_cookie");
        registerOre("listAllmilk", "milk_bottle");

        for (Map.Entry<String, Item> entry : ModItems.ITEMS.entrySet()) {
            if (entry.getKey().endsWith("_knife")) {
                OreDictionary.registerOre("toolKnife", new ItemStack(entry.getValue(), 1, OreDictionary.WILDCARD_VALUE));
            }
        }

        registerOreStack("fdRopes", new ItemStack(ModBlocks.ROPE));
        registerOreStack("rope", new ItemStack(ModBlocks.ROPE));
        registerOreStack("blockRope", new ItemStack(ModBlocks.ROPE));

        registerCanvasSignOreGroups();
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
        // 1.12.2 需要手动补齐常用工具矿辞，供 cutting_board 的 tool 匹配使用。
        registerOreStack("toolAxe", new ItemStack(Items.WOODEN_AXE));
        registerOreStack("toolAxe", new ItemStack(Items.STONE_AXE));
        registerOreStack("toolAxe", new ItemStack(Items.IRON_AXE));
        registerOreStack("toolAxe", new ItemStack(Items.GOLDEN_AXE));
        registerOreStack("toolAxe", new ItemStack(Items.DIAMOND_AXE));

        registerOreStack("toolPickaxe", new ItemStack(Items.WOODEN_PICKAXE));
        registerOreStack("toolPickaxe", new ItemStack(Items.STONE_PICKAXE));
        registerOreStack("toolPickaxe", new ItemStack(Items.IRON_PICKAXE));
        registerOreStack("toolPickaxe", new ItemStack(Items.GOLDEN_PICKAXE));
        registerOreStack("toolPickaxe", new ItemStack(Items.DIAMOND_PICKAXE));

        registerOreStack("toolShovel", new ItemStack(Items.WOODEN_SHOVEL));
        registerOreStack("toolShovel", new ItemStack(Items.STONE_SHOVEL));
        registerOreStack("toolShovel", new ItemStack(Items.IRON_SHOVEL));
        registerOreStack("toolShovel", new ItemStack(Items.GOLDEN_SHOVEL));
        registerOreStack("toolShovel", new ItemStack(Items.DIAMOND_SHOVEL));

        registerOreStack("toolShears", new ItemStack(Items.SHEARS));

        if (!Loader.isModLoaded("futuremc")) registerOreStack("cropSweetBerry", new ItemStack(Items.APPLE));



        // 1.12 中骨粉/可可豆/墨囊均归属染料物品，通过矿辞区分元数据。
        registerOreStack("dyeWhite", new ItemStack(Items.DYE, 1, 15));
        registerOreStack("dyeBrown", new ItemStack(Items.DYE, 1, 3));
        registerOreStack("dyeBlack", new ItemStack(Items.DYE, 1, 0));

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
        OreDictionary.registerOre("dumplingFilling", new ItemStack(Items.MUTTON));
        OreDictionary.registerOre("dumplingFilling", new ItemStack(Items.PORKCHOP));
        OreDictionary.registerOre("dumplingFilling", new ItemStack(Items.RABBIT));
        OreDictionary.registerOre("dumplingFilling", new ItemStack(Item.getItemFromBlock(Blocks.BROWN_MUSHROOM)));
        OreDictionary.registerOre("dumplingFilling", new ItemStack(Item.getItemFromBlock(Blocks.RED_MUSHROOM)));

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


        if (Loader.isModLoaded("oe"))
            OreDictionary.registerOre("mayBeKelp", Item.getByNameOrId("oe:dried_kelp"));
        else
            OreDictionary.registerOre("mayBeKelp", new ItemStack(Items.REEDS));


        OreDictionary.registerOre("listAllmushroom", new ItemStack(Item.getItemFromBlock(Blocks.BROWN_MUSHROOM)));
        OreDictionary.registerOre("listAllmushroom", new ItemStack(Item.getItemFromBlock(Blocks.RED_MUSHROOM)));

        OreDictionary.registerOre("bone", new ItemStack(Items.BONE));
        OreDictionary.registerOre("listAllmeatraw", new ItemStack(Items.BEEF));
        OreDictionary.registerOre("listAllmeatraw", new ItemStack(Items.CHICKEN));
        OreDictionary.registerOre("listAllmeatraw", new ItemStack(Items.MUTTON));
        OreDictionary.registerOre("listAllmeatraw", new ItemStack(Items.PORKCHOP));
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
        tagMap.put("forge:cooked_eggs", "listAllEgg");
        tagMap.put("forge:milk", "listAllmilk");
        tagMap.put("forge:dough", "foodDough");
        tagMap.put("forge:pasta", "foodPasta");
        tagMap.put("forge:bones", "bone");
        tagMap.put("forge:salad_ingredients", "listAllveggie");
        tagMap.put("farmersdelight:cabbage_roll_ingredients", "listAllveggie");
        tagMap.put("farmersdelight:wolf_prey", "listAllmeatraw");
        tagMap.put("farmersdelight:canvas_signs", "fdCanvasSigns");
        tagMap.put("farmersdelight:hanging_canvas_signs", "fdHangingCanvasSigns");
        return Collections.unmodifiableMap(tagMap);
    }
}

