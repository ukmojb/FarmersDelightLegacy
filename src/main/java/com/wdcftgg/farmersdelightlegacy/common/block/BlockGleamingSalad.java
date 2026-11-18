package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.client.particle.ParticleSparkle;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public final class BlockGleamingSalad extends BlockFeast {
    private static final FoodShape SHAPE = new FoodShape(
            FoodShape.box(3, 0, 3, 13, 2, 13), FoodShape.box(2, 2, 2, 14, 6, 14));

    public BlockGleamingSalad() {
        super(4, "gleaming_salad", "minecraft:bowl", true);
        setHardness(0.5F);
    }

    @Override
    protected FoodShape getShape(IBlockState state) {
        return SHAPE;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return getActualState(state, world, pos).getValue(SERVINGS) * 3;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        int servings = getActualState(state, world, pos).getValue(SERVINGS);
        if (random.nextFloat() < 0.05F * servings) {
            ParticleSparkle.spawn(world, pos.getX() + 0.25D + random.nextDouble() * 0.5D,
                    pos.getY() + 0.5D, pos.getZ() + 0.25D + random.nextDouble() * 0.5D);
        }
    }
}
