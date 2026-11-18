package com.wdcftgg.farmersdelightlegacy.common.registry;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID)
public final class ItemCategories {
    private static final Map<String, List<String>> INCLUSIONS = inclusions();

    private ItemCategories() {
    }

    public static void registerAll() {
        String[][] groups = {
            {"fenceGate", "farmersdelight:rope_fence_gate"},
            {"fence", "farmersdelight:rope_fence"},
            {"fdFoods", "minecraft:baked_potato", "farmersdelight:tomato_sauce", "farmersdelight:pie_crust", "farmersdelight:pumpkin_slice", "farmersdelight:ham", "farmersdelight:smoked_ham", "farmersdelight:dog_food"},
            {"listAllfruit", "minecraft:apple", "minecraft:golden_apple@0", "minecraft:golden_apple@1", "minecraft:melon", "minecraft:chorus_fruit"},
            {"rope", "farmersdelight:rope"},
            {"listAllseed", "farmersdelight:cabbage_seeds", "farmersdelight:rice", "farmersdelight:tomato_seeds"},
            {"cropCabbage", "farmersdelight:cabbage", "farmersdelight:cabbage_leaf"},
            {"listAllgrain", "minecraft:wheat", "farmersdelight:rice"},
            {"cropOnion", "farmersdelight:onion"},
            {"cropRice", "farmersdelight:rice"},
            {"cropTomato", "farmersdelight:tomato"},
            {"listAllmilk", "farmersdelight:milk_bottle", "minecraft:milk_bucket"},
            {"foodCookedBacon", "farmersdelight:cooked_bacon"},
            {"listAllbeefcooked", "minecraft:cooked_beef", "farmersdelight:beef_patty"},
            {"listAllchickencooked", "minecraft:cooked_chicken", "farmersdelight:cooked_chicken_cuts"},
            {"foodCookedCod", "minecraft:cooked_fish@0", "farmersdelight:cooked_cod_slice"},
            {"cooked_eggs", "farmersdelight:fried_egg"},
            {"listAllmuttoncooked", "minecraft:cooked_mutton", "farmersdelight:cooked_mutton_chops"},
            {"listAllporkcooked", "minecraft:cooked_porkchop"},
            {"foodCookedSalmon", "minecraft:cooked_fish@1", "farmersdelight:cooked_salmon_slice"},
            {"fdFoodsCookie", "farmersdelight:honey_cookie", "farmersdelight:sweet_berry_cookie", "minecraft:cookie"},
            {"fdFoodsEdibleWhenPlaced", "farmersdelight:apple_pie", "farmersdelight:sweet_berry_cheesecake", "farmersdelight:chocolate_pie", "minecraft:cake"},
            {"fdFoodsFoodPoisoning", "farmersdelight:wheat_dough", "farmersdelight:raw_pasta", "farmersdelight:chicken_cuts", "farmersdelight:nether_salad"},
            {"foodPasta", "farmersdelight:raw_pasta"},
            {"fdFoodsPie", "farmersdelight:apple_pie_slice", "farmersdelight:sweet_berry_cheesecake_slice", "farmersdelight:chocolate_pie_slice", "farmersdelight:pumpkin_pie_slice", "minecraft:pumpkin_pie"},
            {"foodRawBacon", "farmersdelight:bacon"},
            {"listAllbeefraw", "minecraft:beef", "farmersdelight:minced_beef"},
            {"listAllchickenraw", "minecraft:chicken", "farmersdelight:chicken_cuts"},
            {"rawOrSlicedCod", "minecraft:fish@0", "farmersdelight:cod_slice"},
            {"listAllmuttonraw", "minecraft:mutton", "farmersdelight:mutton_chops"},
            {"listAllporkraw", "minecraft:porkchop"},
            {"rawOrSlicedSalmon", "minecraft:fish@1", "farmersdelight:salmon_slice"},
            {"fdFoodsSoup", "farmersdelight:bone_broth", "farmersdelight:beef_stew", "farmersdelight:vegetable_soup", "farmersdelight:chicken_soup", "farmersdelight:fish_stew", "farmersdelight:pumpkin_soup", "farmersdelight:baked_cod_stew", "farmersdelight:noodle_soup", "minecraft:mushroom_stew", "minecraft:beetroot_soup", "minecraft:rabbit_stew"},
            {"listAllveggie", "farmersdelight:onion", "farmersdelight:tomato", "minecraft:carrot", "minecraft:golden_carrot", "minecraft:potato", "minecraft:beetroot"},
            {"foodWheatDough", "farmersdelight:wheat_dough"},
            {"fdStorageBlocksBeetroot", "farmersdelight:beetroot_crate"},
            {"fdStorageBlocksCabbage", "farmersdelight:cabbage_crate"},
            {"fdStorageBlocksCarrot", "farmersdelight:carrot_crate"},
            {"fdStorageBlocksOnion", "farmersdelight:onion_crate"},
            {"fdStorageBlocksPotato", "farmersdelight:potato_crate"},
            {"fdStorageBlocksRicePanicle", "farmersdelight:rice_bale"},
            {"fdStorageBlocksRice", "farmersdelight:rice_bag"},
            {"fdStorageBlocksStraw", "farmersdelight:straw_bale"},
            {"fdStorageBlocksTomato", "farmersdelight:tomato_crate"},
            {"toolKnife", "farmersdelight:flint_knife", "farmersdelight:iron_knife", "farmersdelight:diamond_knife", "farmersdelight:golden_knife", "farmersdelight:netherite_knife"},
            {"fdDrinks", "farmersdelight:milk_bottle", "farmersdelight:apple_cider", "farmersdelight:melon_juice", "farmersdelight:hot_cocoa"},
            {"fdFeasts", "farmersdelight:roast_chicken_block", "farmersdelight:stuffed_pumpkin_block", "farmersdelight:shepherds_pie_block", "farmersdelight:honey_glazed_ham_block", "farmersdelight:gleaming_salad_block", "farmersdelight:rice_roll_medley_block"},
            {"fdMeals", "minecraft:mushroom_stew", "minecraft:beetroot_soup", "minecraft:rabbit_stew", "farmersdelight:mixed_salad", "farmersdelight:cooked_rice", "farmersdelight:bone_broth", "farmersdelight:beef_stew", "farmersdelight:vegetable_soup", "farmersdelight:fish_stew", "farmersdelight:chicken_soup", "farmersdelight:fried_rice", "farmersdelight:pumpkin_soup", "farmersdelight:baked_cod_stew", "farmersdelight:noodle_soup", "farmersdelight:onion_soup", "farmersdelight:bacon_and_eggs", "farmersdelight:ratatouille", "farmersdelight:steak_and_potatoes", "farmersdelight:pasta_with_meatballs", "farmersdelight:pasta_with_mutton_chop", "farmersdelight:mushroom_rice", "farmersdelight:roasted_mutton_chops", "farmersdelight:vegetable_noodles", "farmersdelight:squid_ink_pasta", "farmersdelight:grilled_salmon", "farmersdelight:roast_chicken", "farmersdelight:stuffed_pumpkin", "farmersdelight:honey_glazed_ham", "farmersdelight:shepherds_pie", "farmersdelight:gleaming_salad"},
            {"fdMushroomColonies", "farmersdelight:brown_mushroom_colony", "farmersdelight:red_mushroom_colony"},
            {"fdPies", "minecraft:pumpkin_pie", "farmersdelight:apple_pie", "farmersdelight:sweet_berry_cheesecake", "farmersdelight:chocolate_pie"},
            {"fdServingContainers", "minecraft:bowl", "minecraft:glass_bottle", "minecraft:bucket"},
            {"fdSnacks", "farmersdelight:barbecue_stick", "farmersdelight:egg_sandwich", "farmersdelight:chicken_sandwich", "farmersdelight:hamburger", "farmersdelight:bacon_sandwich", "farmersdelight:mutton_wrap", "farmersdelight:dumplings", "farmersdelight:stuffed_potato", "farmersdelight:cabbage_rolls", "farmersdelight:salmon_roll", "farmersdelight:cod_roll", "farmersdelight:kelp_roll", "farmersdelight:kelp_roll_slice"},
            {"fdSweets", "minecraft:cake", "minecraft:cookie", "farmersdelight:cake_slice", "farmersdelight:apple_pie_slice", "farmersdelight:sweet_berry_cheesecake_slice", "farmersdelight:chocolate_pie_slice", "farmersdelight:pumpkin_pie_slice", "farmersdelight:sweet_berry_cookie", "farmersdelight:honey_cookie", "farmersdelight:melon_popsicle", "farmersdelight:glow_berry_custard", "farmersdelight:fruit_salad"},
            {"fdWildCrops", "farmersdelight:wild_carrots", "farmersdelight:wild_potatoes", "farmersdelight:wild_beetroots", "farmersdelight:wild_cabbages", "farmersdelight:wild_tomatoes", "farmersdelight:wild_onions", "farmersdelight:wild_rice"},
            {"fdCabinetsWooden", "farmersdelight:oak_cabinet", "farmersdelight:spruce_cabinet", "farmersdelight:birch_cabinet", "farmersdelight:jungle_cabinet", "farmersdelight:acacia_cabinet", "farmersdelight:dark_oak_cabinet", "farmersdelight:mangrove_cabinet", "farmersdelight:cherry_cabinet", "farmersdelight:bamboo_cabinet", "farmersdelight:crimson_cabinet", "farmersdelight:warped_cabinet"},
            {"cropCarrot", "minecraft:carrot"},
            {"cropPotato", "minecraft:potato"},
            {"cropBeetroot", "minecraft:beetroot"},
            {"cropWheat", "minecraft:wheat"},
            {"foodBread", "minecraft:bread"},
            {"listAllEgg", "minecraft:egg"},
            {"bone", "minecraft:bone"},
            {"listAllfishraw", "minecraft:fish@0", "minecraft:fish@1", "minecraft:fish@2", "minecraft:fish@3"},
            {"listAllmeatraw", "minecraft:beef", "minecraft:porkchop", "minecraft:chicken", "minecraft:rabbit", "minecraft:mutton"},
            {"listAllmeatcooked", "minecraft:cooked_beef", "minecraft:cooked_porkchop", "minecraft:cooked_chicken", "minecraft:cooked_rabbit", "minecraft:cooked_mutton"},
            {"listAllmushroom", "minecraft:brown_mushroom", "minecraft:red_mushroom"},
            {"dumplingFilling", "minecraft:brown_mushroom"}
        };
        for (String[] group : groups) {
            for (int index = 1; index < group.length; index++) {
                registerItem(group[0], group[index]);
            }
        }
        for (Map.Entry<String, List<String>> inclusion : INCLUSIONS.entrySet()) {
            for (ItemStack stack : new ArrayList<>(OreDictionary.getOres(inclusion.getKey(), false))) {
                include(inclusion.getKey(), stack);
            }
        }
        registerFurniture();
    }

    public static void registerCompat() {
        registerFurniture();
        for (String namespace : new String[]{"oe", "futuremc", "farmers_future_delight"}) {
            registerItem("fdFlatOnCuttingBoard", namespace + ":trident@*");
            registerItem("fdTrident", namespace + ":trident@*");
        }
        registerItem("fdFlatOnCuttingBoard", "farmers_future_delight:spyglass");
    }

    private static void registerFurniture() {
        String[] woods = {"oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "crimson", "warped", "cherry", "bamboo", "mangrove"};
        for (String wood : woods) {
            String group = "fdFurniture" + wood;
            registerItem(group, "farmersdelight:" + wood + "_cabinet");
            for (String namespace : new String[]{"minecraft", "futuremc", "farmers_future_delight", "nb", "suikecherry", "sakura"}) {
                for (String furniture : new String[]{"door", "trapdoor", "sign", "hanging_sign", "fence", "fence_gate", "pressure_plate", "button", "boat", "raft"}) {
                    registerItem(group, namespace + ":" + wood + "_" + furniture);
                }
            }
        }
        for (String item : new String[]{"wooden_door", "trapdoor", "sign", "fence", "fence_gate", "wooden_pressure_plate", "wooden_button", "boat"}) {
            registerItem("fdFurnitureoak", "minecraft:" + item);
        }
        registerItem("fdFurniturebamboo", "sakura:bamboo_fence_sunburnt");
    }

    public static boolean contains(String group, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (ItemStack entry : OreDictionary.getOres(group, false)) {
            if (OreDictionary.itemMatches(entry, stack, false)) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onOreRegistered(OreDictionary.OreRegisterEvent event) {
        include(event.getName(), event.getOre());
    }

    private static void include(String group, ItemStack stack) {
        for (String parent : INCLUSIONS.getOrDefault(group, Collections.emptyList())) {
            register(parent, stack);
        }
    }

    private static void register(String group, ItemStack stack) {
        if ("fdCookingVegetable".equals(group) && stack.getItem() == Items.MELON) {
            return;
        }
        if ("fdSafeRawFish".equals(group) && stack.getItem() == Items.FISH) {
            if (stack.getMetadata() == 3) {
                return;
            }
            if (stack.getMetadata() == OreDictionary.WILDCARD_VALUE) {
                for (int metadata = 0; metadata < 3; metadata++) {
                    register(group, new ItemStack(Items.FISH, 1, metadata));
                }
                return;
            }
        }
        for (ItemStack entry : OreDictionary.getOres(group, false)) {
            if (ItemStack.areItemsEqual(entry, stack)) {
                return;
            }
        }
        OreDictionary.registerOre(group, stack);
        include(group, stack);
    }

    private static void registerItem(String group, String token) {
        String[] parts = token.split("@", 2);
        ResourceLocation id = new ResourceLocation(parts[0]);
        if (!ForgeRegistries.ITEMS.containsKey(id)) {
            return;
        }
        Item item = ForgeRegistries.ITEMS.getValue(id);
        int metadata = parts.length == 1 ? 0 : "*".equals(parts[1]) ? OreDictionary.WILDCARD_VALUE : Integer.parseInt(parts[1]);
        register(group, new ItemStack(item, 1, metadata));
    }

    private static Map<String, List<String>> inclusions() {
        Map<String, List<String>> groups = new LinkedHashMap<>();
        groups.put("listAllgrain", Arrays.asList("crop"));
        groups.put("cropCabbage", Arrays.asList("crop", "foodLeafyGreen"));
        groups.put("cropOnion", Arrays.asList("crop", "listAllveggie"));
        groups.put("cropRice", Arrays.asList("crop"));
        groups.put("cropTomato", Arrays.asList("crop", "listAllveggie"));
        groups.put("fdSnacks", Arrays.asList("fdFoods"));
        groups.put("fdMeals", Arrays.asList("fdFoods"));
        groups.put("fdSweets", Arrays.asList("fdFoods"));
        groups.put("listAllfruit", Arrays.asList("fdFoods"));
        groups.put("listAllberry", Arrays.asList("fdFoods"));
        groups.put("foodBread", Arrays.asList("fdFoods"));
        groups.put("listAllmeatcooked", Arrays.asList("fdFoods"));
        groups.put("listAllfishcooked", Arrays.asList("fdFoods"));
        groups.put("fdFoodsCookie", Arrays.asList("fdFoods"));
        groups.put("fdFoodsPie", Arrays.asList("fdFoods"));
        groups.put("fdFoodsSoup", Arrays.asList("fdFoods"));
        groups.put("fdFoodsEdibleWhenPlaced", Arrays.asList("fdFoods"));
        groups.put("fdFoodsFoodPoisoning", Arrays.asList("fdFoods"));
        groups.put("fdDrinks", Arrays.asList("fdCommonDrinks"));
        groups.put("listAllmilk", Arrays.asList("fdCommonDrinks"));
        groups.put("listAlljuice", Arrays.asList("fdCommonDrinks"));
        groups.put("foodHoney", Arrays.asList("fdFoods", "fdCommonDrinks"));
        groups.put("foodLeafyGreen", Arrays.asList("fdFoods"));
        groups.put("foodDough", Arrays.asList("fdFoods"));
        groups.put("foodPasta", Arrays.asList("fdFoods"));
        groups.put("cooked_eggs", Arrays.asList("fdFoods"));
        groups.put("fdStorageBlocksCarrot", Arrays.asList("fdStorageBlocks"));
        groups.put("fdStorageBlocksPotato", Arrays.asList("fdStorageBlocks"));
        groups.put("fdStorageBlocksBeetroot", Arrays.asList("fdStorageBlocks"));
        groups.put("fdStorageBlocksCabbage", Arrays.asList("fdStorageBlocks"));
        groups.put("fdStorageBlocksTomato", Arrays.asList("fdStorageBlocks"));
        groups.put("fdStorageBlocksOnion", Arrays.asList("fdStorageBlocks"));
        groups.put("fdStorageBlocksRice", Arrays.asList("fdStorageBlocks"));
        groups.put("fdStorageBlocksRicePanicle", Arrays.asList("fdStorageBlocks"));
        groups.put("fdStorageBlocksStraw", Arrays.asList("fdStorageBlocks"));
        groups.put("toolKnife", Arrays.asList("tool", "fdStrawHarvesters", "fdEnchantableKnife"));
        groups.put("foodCookedCod", Arrays.asList("listAllfishcooked"));
        groups.put("foodCookedSalmon", Arrays.asList("listAllfishcooked"));
        groups.put("listAllchickencooked", Arrays.asList("listAllmeatcooked"));
        groups.put("listAllporkcooked", Arrays.asList("listAllmeatcooked"));
        groups.put("listAllbeefcooked", Arrays.asList("listAllmeatcooked"));
        groups.put("listAllmuttoncooked", Arrays.asList("listAllmeatcooked"));
        groups.put("foodCookedBacon", Arrays.asList("listAllporkcooked"));
        groups.put("foodWheatDough", Arrays.asList("foodDough"));
        groups.put("fdFeasts", Arrays.asList("fdFoodsEdibleWhenPlaced"));
        groups.put("rawOrSlicedCod", Arrays.asList("listAllfishraw"));
        groups.put("rawOrSlicedSalmon", Arrays.asList("listAllfishraw"));
        groups.put("listAllchickenraw", Arrays.asList("listAllmeatraw", "dumplingFilling"));
        groups.put("listAllporkraw", Arrays.asList("listAllmeatraw", "dumplingFilling"));
        groups.put("listAllbeefraw", Arrays.asList("listAllmeatraw", "dumplingFilling"));
        groups.put("listAllmuttonraw", Arrays.asList("listAllmeatraw"));
        groups.put("foodRawBacon", Arrays.asList("listAllporkraw"));
        groups.put("listAllfishraw", Arrays.asList("fdSafeRawFish", "fdFoods"));
        groups.put("fdCabinetsWooden", Arrays.asList("fdCabinets"));
        groups.put("cropCarrot", Arrays.asList("crop"));
        groups.put("cropPotato", Arrays.asList("crop"));
        groups.put("cropBeetroot", Arrays.asList("crop"));
        groups.put("cropWheat", Arrays.asList("crop"));
        groups.put("cropLettuce", Arrays.asList("foodLeafyGreen"));
        groups.put("listAllgreenveggie", Arrays.asList("foodLeafyGreen"));
        groups.put("listAllmeatraw", Arrays.asList("fdCabbageRollIngredients", "fdFoods"));
        groups.put("fdSafeRawFish", Arrays.asList("fdCabbageRollIngredients"));
        groups.put("listAllveggie", Arrays.asList("fdCabbageRollIngredients", "fdCookingVegetable", "fdFoods"));
        groups.put("listAllmushroom", Arrays.asList("fdCabbageRollIngredients", "fdBoneBrothIngredients"));
        groups.put("cropGlowBerry", Arrays.asList("fdBoneBrothIngredients"));
        return Collections.unmodifiableMap(groups);
    }

    public static Map<String, String> tagMappings() {
        Map<String, String> tags = new LinkedHashMap<>();
        tags.put("c:crops", "crop");
        tags.put("c:crops/cabbage", "cropCabbage");
        tags.put("c:crops/carrot", "cropCarrot");
        tags.put("c:crops/potato", "cropPotato");
        tags.put("c:crops/beetroot", "cropBeetroot");
        tags.put("c:crops/wheat", "cropWheat");
        tags.put("c:crops/onion", "cropOnion");
        tags.put("c:crops/rice", "cropRice");
        tags.put("c:crops/tomato", "cropTomato");
        tags.put("c:crops/grain", "listAllgrain");
        tags.put("c:drinks/milk", "listAllmilk");
        tags.put("c:eggs", "listAllEgg");
        tags.put("c:bones", "bone");
        tags.put("c:mushrooms", "listAllmushroom");
        tags.put("c:foods/cabbage", "cropCabbage");
        tags.put("c:foods/onion", "cropOnion");
        tags.put("c:foods/tomato", "cropTomato");
        tags.put("c:foods/bread", "foodBread");
        tags.put("c:foods/berry", "listAllberry");
        tags.put("c:foods/fruit", "listAllfruit");
        tags.put("c:foods/dough", "foodDough");
        tags.put("c:foods/dough/wheat", "foodWheatDough");
        tags.put("c:foods/pasta", "foodPasta");
        tags.put("c:foods/leafy_green", "foodLeafyGreen");
        tags.put("c:foods/raw_beef", "listAllbeefraw");
        tags.put("c:foods/raw_chicken", "listAllchickenraw");
        tags.put("c:foods/raw_mutton", "listAllmuttonraw");
        tags.put("c:foods/raw_pork", "listAllporkraw");
        tags.put("c:foods/raw_bacon", "foodRawBacon");
        tags.put("c:foods/raw_cod", "rawOrSlicedCod");
        tags.put("c:foods/raw_salmon", "rawOrSlicedSalmon");
        tags.put("c:foods/raw_fish", "listAllfishraw");
        tags.put("c:foods/raw_meat", "listAllmeatraw");
        tags.put("c:foods/safe_raw_fish", "fdSafeRawFish");
        tags.put("c:foods/cooked_beef", "listAllbeefcooked");
        tags.put("c:foods/cooked_chicken", "listAllchickencooked");
        tags.put("c:foods/cooked_mutton", "listAllmuttoncooked");
        tags.put("c:foods/cooked_pork", "listAllporkcooked");
        tags.put("c:foods/cooked_bacon", "foodCookedBacon");
        tags.put("c:foods/cooked_cod", "foodCookedCod");
        tags.put("c:foods/cooked_salmon", "foodCookedSalmon");
        tags.put("c:foods/cooked_fish", "listAllfishcooked");
        tags.put("c:foods/cooked_meat", "listAllmeatcooked");
        tags.put("c:foods/cooked_egg", "cooked_eggs");
        tags.put("c:foods/vegetable", "listAllveggie");
        tags.put("c:fences", "fence");
        tags.put("c:fence_gates", "fenceGate");
        tags.put("c:ropes", "rope");
        tags.put("c:seeds", "listAllseed");
        tags.put("c:tools/knife", "toolKnife");
        tags.put("c:tools", "tool");
        tags.put("farmersdelight:tools/knives", "toolKnife");
        tags.put("farmersdelight:enchantable/knife", "fdEnchantableKnife");
        tags.put("c:drinks", "fdCommonDrinks");
        tags.put("c:drinks/juice", "listAlljuice");
        tags.put("c:drinks/honey", "foodHoney");
        tags.put("c:foods", "fdFoods");
        tags.put("c:storage_blocks", "fdStorageBlocks");
        tags.put("c:foods/cookie", "fdFoodsCookie");
        tags.put("c:foods/edible_when_placed", "fdFoodsEdibleWhenPlaced");
        tags.put("c:foods/food_poisoning", "fdFoodsFoodPoisoning");
        tags.put("c:foods/pie", "fdFoodsPie");
        tags.put("c:foods/soup", "fdFoodsSoup");
        tags.put("c:storage_blocks/beetroot", "fdStorageBlocksBeetroot");
        tags.put("c:storage_blocks/cabbage", "fdStorageBlocksCabbage");
        tags.put("c:storage_blocks/carrot", "fdStorageBlocksCarrot");
        tags.put("c:storage_blocks/onion", "fdStorageBlocksOnion");
        tags.put("c:storage_blocks/potato", "fdStorageBlocksPotato");
        tags.put("c:storage_blocks/rice_panicle", "fdStorageBlocksRicePanicle");
        tags.put("c:storage_blocks/rice", "fdStorageBlocksRice");
        tags.put("c:storage_blocks/straw", "fdStorageBlocksStraw");
        tags.put("c:storage_blocks/tomato", "fdStorageBlocksTomato");
        tags.put("farmersdelight:cabinets", "fdCabinets");
        tags.put("farmersdelight:drinks", "fdDrinks");
        tags.put("farmersdelight:feasts", "fdFeasts");
        tags.put("farmersdelight:flat_on_cutting_board", "fdFlatOnCuttingBoard");
        tags.put("farmersdelight:meals", "fdMeals");
        tags.put("farmersdelight:mushroom_colonies", "fdMushroomColonies");
        tags.put("farmersdelight:pies", "fdPies");
        tags.put("farmersdelight:serving_containers", "fdServingContainers");
        tags.put("farmersdelight:snacks", "fdSnacks");
        tags.put("farmersdelight:straw_harvesters", "fdStrawHarvesters");
        tags.put("farmersdelight:sweets", "fdSweets");
        tags.put("farmersdelight:wild_crops", "fdWildCrops");
        tags.put("farmersdelight:cabinets/wooden", "fdCabinetsWooden");
        return Collections.unmodifiableMap(tags);
    }
}

