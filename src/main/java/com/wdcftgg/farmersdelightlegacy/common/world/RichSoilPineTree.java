package com.wdcftgg.farmersdelightlegacy.common.world;

import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeTaiga;
import net.minecraft.world.gen.feature.WorldGenMegaPineTree;

public final class RichSoilPineTree extends WorldGenMegaPineTree {
    private RichSoilPineTree(boolean spruce) {
        super(false, spruce);
    }

    public static void registerAll() {
        BiomeTaiga.MEGA_PINE_GENERATOR = new RichSoilPineTree(false);
        BiomeTaiga.MEGA_SPRUCE_GENERATOR = new RichSoilPineTree(true);
    }

    @Override
    protected void setBlockAndNotifyAdequately(World world, BlockPos pos, IBlockState state) {
        if (state.getBlock() == Blocks.DIRT && state.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.PODZOL
                && world.getBlockState(pos).getBlock() == ModBlocks.RICH_SOIL) {
            return;
        }
        super.setBlockAndNotifyAdequately(world, pos, state);
    }
}
