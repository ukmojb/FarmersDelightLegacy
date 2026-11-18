package com.wdcftgg.farmersdelightlegacy.common.util;

import com.wdcftgg.farmersdelightlegacy.api.heat.HeatSourceApi;
import com.wdcftgg.farmersdelightlegacy.api.heat.HeatSourceOffsetApi;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockStove;
import com.wdcftgg.farmersdelightlegacy.common.registry.BlockCategories;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityHopper;
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
        if (HeatSourceApi.isRegisteredAsDirectHeatSource(world, pos, state)) {
            return true;
        }

        if (HeatSourceApi.isRemovedAsDirectHeatSource(world, pos, state)) {
            return false;
        }

        if (BlockCategories.contains("fdHeatSources", state)) {
            IProperty<?> lit = state.getBlock().getBlockState().getProperty("lit");
            IProperty<?> waterlogged = state.getBlock().getBlockState().getProperty("waterlogged");
            return (lit == null || Boolean.TRUE.equals(state.getProperties().get(lit)))
                    && (waterlogged == null || !Boolean.TRUE.equals(state.getProperties().get(waterlogged)));
        }
        return isBuiltInDirectHeatSource(state);
    }

    public static boolean isVisualSupportHeatSource(World world, BlockPos pos) {
        if (world == null || pos == null || !world.isBlockLoaded(pos)) {
            return false;
        }

        IBlockState state = world.getBlockState(pos);
        if (BlockCategories.contains("fdTrayHeatSources", state)) {
            return true;
        }

        Material material = state.getMaterial();
        return material == Material.FIRE || material == Material.LAVA;
    }

    public static boolean isCookwareHeated(World world, BlockPos cookwarePos) {
        BlockPos heatPos = resolveCookwareHeatSourcePos(world, cookwarePos);
        return isDirectHeatSource(world, heatPos);
    }

    public static boolean hasVisualSupportForCookware(World world, BlockPos cookwarePos) {
        BlockPos supportPos = cookwarePos == null ? null : cookwarePos.down();
        return isVisualSupportHeatSource(world, supportPos);
    }

    public static BlockPos resolveCookwareHeatSourcePos(World world, BlockPos cookwarePos) {
        if (world == null || cookwarePos == null) {
            return cookwarePos;
        }

        BlockPos firstBelowPos = cookwarePos.down();
        if (!world.isBlockLoaded(firstBelowPos)) {
            return firstBelowPos;
        }

        IBlockState firstBelowState = world.getBlockState(firstBelowPos);
        if (!isDirectHeatSource(world, firstBelowPos) && shouldOffsetCookwareHeatCheck(world, firstBelowPos, firstBelowState)) {
            return firstBelowPos.down();
        }

        return firstBelowPos;
    }

    public static boolean shouldOffsetCookwareHeatCheck(World world, BlockPos pos, IBlockState state) {
        if (world == null || pos == null || state == null) {
            return false;
        }

        if (state.getBlock() == Blocks.HOPPER || world.getTileEntity(pos) instanceof TileEntityHopper
                || BlockCategories.contains("fdHeatConductors", state)) {
            return true;
        }

        return HeatSourceOffsetApi.shouldOffsetDown(world, pos, state);
    }

    private static boolean isBuiltInDirectHeatSource(IBlockState state) {
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

