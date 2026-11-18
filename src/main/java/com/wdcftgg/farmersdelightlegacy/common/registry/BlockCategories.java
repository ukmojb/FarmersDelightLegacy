package com.wdcftgg.farmersdelightlegacy.common.registry;

import com.wdcftgg.farmersdelightlegacy.common.block.BlockCabinet;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockFeast;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockMushroomColony;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockPie;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockWildCrop;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockWildRice;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class BlockCategories {
    private static final Map<String, Set<IBlockState>> additions = new HashMap<>();

    private BlockCategories() {
    }

    public static void register(String category, Block block) {
        additions.computeIfAbsent(category, key -> new HashSet<>()).addAll(block.getBlockState().getValidStates());
    }

    public static void register(String category, IBlockState state) {
        additions.computeIfAbsent(category, key -> new HashSet<>()).add(state);
    }

    public static boolean contains(String category, IBlockState state) {
        Set<IBlockState> states = additions.get(category);
        if (states != null && states.contains(state) || containsDefault(category, state)) {
            return true;
        }
        Item item = Item.getItemFromBlock(state.getBlock());
        return item != Items.AIR && ItemCategories.contains(category, new ItemStack(item, 1, state.getBlock().damageDropped(state)));
    }

    private static boolean containsDefault(String category, IBlockState state) {
        Block block = state.getBlock();
        switch (category) {
            case "fdDirt":
                return block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.MYCELIUM || block == ModBlocks.RICH_SOIL
                        || hasId(block, "farmers_future_delight:rooted_dirt", "farmers_future_delight:moss_block");
            case "fdSand":
                return block == Blocks.SAND;
            case "fdTerrain":
                return contains("fdDirt", state) || contains("fdSand", state);
            case "fdStrawBlocks":
                return hasId(block, "farmersdelight:rope", "farmersdelight:safety_net", "farmersdelight:canvas_rug",
                        "farmersdelight:tatami", "farmersdelight:full_tatami_mat", "farmersdelight:half_tatami_mat");
            case "fdMineableKnife":
                return block == Blocks.CACTUS || block == Blocks.MELON_BLOCK || block == Blocks.PUMPKIN || block == Blocks.LIT_PUMPKIN
                        || block == Blocks.WEB || block == Blocks.CAKE || block == Blocks.WOOL || block == Blocks.CARPET
                        || block instanceof BlockPie || hasId(block, "farmersdelight:rice_bag")
                        || contains("fdStrawBlocks", state) || contains("fdDropsCakeSlice", state);
            case "fdDropsCakeSlice":
                ResourceLocation id = block.getRegistryName();
                return id != null && "farmers_future_delight".equals(id.getNamespace())
                        && (id.getPath().equals("candle_cake") || id.getPath().endsWith("_candle_cake"));
            case "fdTrayHeatSources":
                return block == Blocks.FIRE || block == Blocks.LAVA || block == Blocks.FLOWING_LAVA
                        || contains("fdCampfires", state);
            case "fdHeatSources":
                return contains("fdTrayHeatSources", state);
            case "fdCompostActivators":
                return contains("fdMushroomColonies", state);
            case "fdUnaffectedByRichSoil":
                return contains("fdMushroomColonies", state) || contains("fdWildCrops", state);
            case "fdCampfires":
                return hasId(block, "futuremc:campfire", "futuremc:soul_campfire", "farmers_future_delight:campfire",
                        "farmers_future_delight:soul_campfire", "campfire:campfire", "campfire:brazier", "nb:soul_campfire");
            case "fdCampfireSignalSmoke":
                return hasId(block, "farmersdelight:straw_bale", "farmersdelight:rice_bale");
            case "fdFeasts":
                return block instanceof BlockFeast;
            case "fdPies":
                return block instanceof BlockPie;
            case "fdCabinets":
            case "fdCabinetsWooden":
                return block instanceof BlockCabinet;
            case "fdMushroomColonies":
                return block instanceof BlockMushroomColony;
            case "fdWildCrops":
                return block instanceof BlockWildCrop || block instanceof BlockWildRice;
            default:
                return false;
        }
    }

    private static boolean hasId(Block block, String... names) {
        ResourceLocation id = block.getRegistryName();
        if (id == null) {
            return false;
        }
        for (String name : names) {
            if (id.toString().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
