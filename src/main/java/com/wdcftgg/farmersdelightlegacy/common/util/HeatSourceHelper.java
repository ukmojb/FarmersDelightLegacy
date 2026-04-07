package com.wdcftgg.farmersdelightlegacy.common.util;

import com.wdcftgg.farmersdelightlegacy.common.block.BlockStove;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class HeatSourceHelper {

    private HeatSourceHelper() {
    }

    public static boolean isDirectHeatSource(World world, BlockPos pos) {
        if (world == null || pos == null || !world.isBlockLoaded(pos)) {
            return false;
        }

        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof BlockStove) {
            return state.getValue(BlockStove.LIT);
        }

        if (block == Blocks.FIRE || block == Blocks.MAGMA || block == Blocks.LIT_FURNACE || block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) {
            return true;
        }

        Material material = state.getMaterial();
        return material == Material.FIRE || material == Material.LAVA;
    }

    public static boolean hasHeatSourceNearby(World world, BlockPos center, int radius) {
        if (world == null || center == null || radius < 0) {
            return false;
        }

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    mutablePos.setPos(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (isDirectHeatSource(world, mutablePos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

