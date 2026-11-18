package com.wdcftgg.farmersdelightlegacy.common.compat;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class BellCompat {
    private static final ResourceLocation FUTURE_MC = new ResourceLocation("futuremc", "bell");
    private static final ResourceLocation FFD = new ResourceLocation("farmers_future_delight", "bell");

    private BellCompat() {
    }

    public static boolean isBell(IBlockState state) {
        ResourceLocation id = state.getBlock().getRegistryName();
        return FUTURE_MC.equals(id) || FFD.equals(id);
    }

    public static boolean ring(World world, BlockPos pos, IBlockState state) {
        if (!isBell(state)) {
            return false;
        }
        if (world.isRemote) {
            return true;
        }
        EnumFacing direction = EnumFacing.NORTH;
        for (IProperty<?> property : state.getPropertyKeys()) {
            if ("facing".equals(property.getName()) && state.getValue(property) instanceof EnumFacing) {
                direction = ((EnumFacing) state.getValue(property)).rotateY();
                break;
            }
        }
        return FFD.equals(state.getBlock().getRegistryName())
                ? FutureDelightBell.ring(world, pos, state, direction)
                : FutureMcBell.ring(world, pos, direction);
    }

    private static final class FutureDelightBell {
        private static boolean ring(World world, BlockPos pos, IBlockState state, EnumFacing direction) {
            if (!(state.getBlock() instanceof xy177.farmersfuturedelight.common.block.BlockBell)
                    || !(world.getTileEntity(pos) instanceof xy177.farmersfuturedelight.common.tile.TileEntityBell)) {
                return false;
            }
            ((xy177.farmersfuturedelight.common.block.BlockBell) state.getBlock()).ring(world, pos, direction);
            return true;
        }
    }

    private static final class FutureMcBell {
        private static boolean ring(World world, BlockPos pos, EnumFacing direction) {
            TileEntity tile = world.getTileEntity(pos);
            if (!(tile instanceof thedarkcolour.futuremc.tile.BellTileEntity)) {
                return false;
            }
            ((thedarkcolour.futuremc.tile.BellTileEntity) tile).ring(direction);
            world.playSound(null, pos, thedarkcolour.futuremc.registry.FSounds.INSTANCE.getBELL_RING(),
                    SoundCategory.BLOCKS, 2.0F, 1.0F);
            return true;
        }
    }
}
