package com.wdcftgg.farmersdelightlegacy.common.world;

import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Random;

public final class VillageCropRules {
    private VillageCropRules() {
    }

    public static void replace(World world, List<BlockPos> positions) {
        for (BlockPos pos : positions) {
            IBlockState source = world.getBlockState(pos);
            IBlockState target = replacement(source, world.getBiome(pos), new Random(MathHelper.getPositionRandom(pos)));
            if (source != target && ((BlockBush) target.getBlock()).canBlockStay(world, pos, target)) {
                world.setBlockState(pos, target, 2);
            }
        }
    }

    public static IBlockState replacement(IBlockState source, Biome biome, Random random) {
        boolean cold = biome.isSnowyBiome();
        boolean arid = !cold && biome.getDefaultTemperature() >= 1.0F && biome.getRainfall() < 0.4F;
        float chance;
        if (source.getBlock() == Blocks.WHEAT) {
            chance = 0.3F;
        } else if (cold && source.getBlock() == Blocks.POTATOES) {
            chance = 0.2F;
        } else {
            return source;
        }
        Block[] crops = cold ? new Block[]{ModBlocks.CABBAGES, ModBlocks.ONIONS}
                : arid ? new Block[]{ModBlocks.CABBAGES, ModBlocks.BUDDING_TOMATOES}
                : new Block[]{ModBlocks.CABBAGES, ModBlocks.BUDDING_TOMATOES, ModBlocks.ONIONS};
        for (Block crop : crops) {
            if (random.nextFloat() < chance) {
                return crop.getDefaultState();
            }
        }
        return source;
    }
}
