package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class BlockHangingTomato extends BlockTomatoVine {
    public BlockHangingTomato() {
        super(true);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, getAgeProperty());
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return getAge(state);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return withAge(meta & 3);
    }

    @Override
    protected boolean hasRope(IBlockState state) {
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
        if (Configuration.enableTomatoRopePermanence && !world.isRemote
                && world.getBlockState(pos).getBlock() != this
                && world.getBlockState(pos).getBlock() != Configuration.getDefaultTomatoVineRopeBlock()) {
            placeRope(world, pos);
        }
    }
}
