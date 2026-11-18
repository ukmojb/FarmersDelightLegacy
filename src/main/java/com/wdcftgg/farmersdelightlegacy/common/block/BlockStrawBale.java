package com.wdcftgg.farmersdelightlegacy.common.block;

import net.minecraft.block.BlockHay;
import net.minecraft.block.SoundType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockStrawBale extends BlockHay {

    public BlockStrawBale() {
        super();
        this.setHardness(0.5F);
        this.setResistance(2.5F);
        this.setSoundType(SoundType.PLANT);
        this.setDefaultState(this.blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.Y));
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 60;
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 20;
    }
}
