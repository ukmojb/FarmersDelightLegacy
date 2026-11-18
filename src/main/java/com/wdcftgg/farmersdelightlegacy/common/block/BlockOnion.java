package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.common.registry.ModItems;
import com.wdcftgg.farmersdelightlegacy.common.compat.SereneSeasonsCompat;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.fml.common.Loader;

import java.util.Random;

public class BlockOnion extends BlockCrops {

    private static final AxisAlignedBB[] SHAPE_BY_AGE = new AxisAlignedBB[]{
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 2.0D / 16.0D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 3.0D / 16.0D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 4.0D / 16.0D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 5.0D / 16.0D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 6.0D / 16.0D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 7.0D / 16.0D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 8.0D / 16.0D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 9.0D / 16.0D, 1.0D)
    };

    public BlockOnion() {
        this.setTickRandomly(true);
        this.setHardness(0.0F);
        this.setSoundType(net.minecraft.block.SoundType.PLANT);
    }

    @Override
    protected Item getSeed() {
        return ModItems.ITEMS.get("onion");
    }

    @Override
    protected Item getCrop() {
        return ModItems.ITEMS.get("onion");
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        int age = Math.max(0, Math.min(this.getMaxAge(), this.getAge(state)));
        return SHAPE_BY_AGE[age];
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!this.canBlockStay(worldIn, pos, state)) {
            worldIn.destroyBlock(pos, true);
            return;
        }
        if (isGrowingSeason(worldIn, pos)) {
            super.updateTick(worldIn, pos, state, rand);
        }
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return super.canGrow(world, pos, state, isClient) && isGrowingSeason(world, pos);
    }

    @Override
    public void grow(World world, BlockPos pos, IBlockState state) {
        if (isGrowingSeason(world, pos)) {
            super.grow(world, pos, state);
        }
    }

    private boolean isGrowingSeason(World world, BlockPos pos) {
        return !Loader.isModLoaded("sereneseasons") || SereneSeasonsCompat.canGrowOnion(world, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!this.canBlockStay(worldIn, pos, state)) {
            worldIn.destroyBlock(pos, true);
            return;
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
        return EnumPlantType.Crop;
    }
}
