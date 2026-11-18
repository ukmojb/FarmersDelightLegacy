package com.wdcftgg.farmersdelightlegacy.common.compat;

import com.wdcftgg.farmersdelightlegacy.common.block.BlockRice;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockWildRice;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.api.IWaterloggableBlock;
import xy177.farmersfuturedelight.common.FFDConfig;

import java.util.Arrays;
import java.util.Random;

public final class FarmersFutureDelightWaterlogging {
    private FarmersFutureDelightWaterlogging() {
    }

    public static BlockRice createRice() {
        return FFDConfig.isWaterloggingEnabled() ? new WaterloggedRice() : new BlockRice();
    }

    public static BlockWildRice createWildRice() {
        return FFDConfig.isWaterloggingEnabled() ? new WaterloggedWildRice() : new BlockWildRice();
    }

    private static boolean canPlantInWater(World world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial() == Material.WATER
                && FFDConfig.isFluidAllowed(world, FluidRegistry.WATER)
                && WaterloggedBlockApi.isWaterSource(world, pos);
    }

    private static final class WaterloggedRice extends BlockRice implements IWaterloggableBlock {
        @Override
        public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
            super.updateTick(world, pos, state, random);
        }

        @Override
        public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
            if (!isWaterloggedState(state)) {
                super.updateTick(world, pos, state, random);
            } else if (world.isAreaLoaded(pos, 1)) {
                removeIfUnsupported(world, pos, state);
            }
        }

        @Override
        protected BlockStateContainer createBlockState() {
            return new BlockStateContainer(this, AGE, SUPPORTING, WaterloggedBlockApi.UNIVERSAL_WATERLOGGED);
        }

        @Override
        public boolean canPlaceBlockAt(World world, BlockPos pos) {
            if (world.isAirBlock(pos)) {
                return super.canPlaceBlockAt(world, pos);
            }
            return canPlantInWater(world, pos) && canSustainBush(world.getBlockState(pos.down()));
        }

        @Override
        public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
            return isWaterloggedState(state) ? canSustainBush(world.getBlockState(pos.down()))
                    : super.canBlockStay(world, pos, state);
        }

        @Override
        public boolean isWaterloggedState(IBlockState state) {
            return state.getValue(WaterloggedBlockApi.UNIVERSAL_WATERLOGGED);
        }

        @Override
        public IBlockState setWaterloggedState(IBlockState state, boolean waterlogged) {
            return waterlogged && isWaterloggedState(state) ? state : null;
        }
    }

    private static final class WaterloggedWildRice extends BlockWildRice implements IWaterloggableBlock {
        @Override
        protected BlockStateContainer createBlockState() {
            return new BlockStateContainer(this, HALF, WaterloggedBlockApi.UNIVERSAL_WATERLOGGED);
        }

        @Override
        public boolean canPlaceBlockAt(World world, BlockPos pos) {
            if (world.isAirBlock(pos)) {
                return super.canPlaceBlockAt(world, pos);
            }
            return canPlantInWater(world, pos) && pos.getY() + 1 < world.getHeight() && world.isAirBlock(pos.up())
                    && canSustainBush(world.getBlockState(pos.down()));
        }

        @Override
        public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
            if (!isWaterloggedState(state)) {
                return super.canBlockStay(world, pos, state);
            }
            IBlockState upper = world.getBlockState(pos.up());
            return upper.getBlock() == this && upper.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER
                    && canSustainBush(world.getBlockState(pos.down())) && WaterloggedBlockApi.isWaterSource(world, pos);
        }

        @Override
        public boolean isWaterloggedState(IBlockState state) {
            return state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.LOWER
                    && state.getValue(WaterloggedBlockApi.UNIVERSAL_WATERLOGGED);
        }

        @Override
        public IBlockState setWaterloggedState(IBlockState state, boolean waterlogged) {
            return state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.LOWER
                    ? state.withProperty(WaterloggedBlockApi.UNIVERSAL_WATERLOGGED, waterlogged) : null;
        }
    }

    static void registerAll() {
        MinecraftForge.EVENT_BUS.register(FarmersFutureDelightWaterlogging.class);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        WaterloggedBlockApi.prepareUniversalStateCarriers(event.getRegistry());
    }

    static BlockStateContainer createBlockState(Block block, IProperty<?>[] properties) {
        if (!FFDConfig.isWaterloggingEnabled()) {
            return new BlockStateContainer(block, properties);
        }
        IProperty<?>[] expanded = Arrays.copyOf(properties, properties.length + 1);
        expanded[properties.length] = WaterloggedBlockApi.UNIVERSAL_WATERLOGGED;
        return new BlockStateContainer(block, expanded);
    }

    static boolean supportsState(IBlockState state) {
        return FFDConfig.isWaterloggingEnabled() && WaterloggedBlockApi.hasUniversalWaterloggedProperty(state);
    }

    static boolean containsWater(IBlockAccess world, BlockPos pos) {
        return WaterloggedBlockApi.containsWater(world, pos);
    }

    static boolean canUseBucket(World world, BlockPos pos, ItemStack stack) {
        return stack.getItem() == Items.WATER_BUCKET && FFDConfig.isFluidAllowed(world, FluidRegistry.WATER)
                || stack.getItem() == Items.BUCKET && WaterloggedBlockApi.isWaterlogged(world, pos)
                && WaterloggedBlockApi.isWaterSource(world, pos);
    }

    static boolean placeBlock(World world, BlockPos pos, IBlockState state, int flags) {
        boolean waterlogged = supportsState(state) && FFDConfig.isFluidAllowed(world, FluidRegistry.WATER)
                && WaterloggedBlockApi.isWaterSource(world, pos);
        IBlockState placed = waterlogged
                ? state.withProperty(WaterloggedBlockApi.UNIVERSAL_WATERLOGGED, true) : state;
        if (!world.setBlockState(pos, placed, flags)) {
            return false;
        }
        if (waterlogged && !world.isRemote) {
            WaterloggedBlockApi.recordContainedFluid(world, pos, FluidRegistry.WATER);
            world.scheduleUpdate(pos, placed.getBlock(), WaterloggedBlockApi.fluidTickRate(world, FluidRegistry.WATER));
        }
        return true;
    }

    static boolean removeBlock(World world, BlockPos pos) {
        return WaterloggedBlockApi.restoreFluid(world, pos, world.getBlockState(pos), 3);
    }
}
