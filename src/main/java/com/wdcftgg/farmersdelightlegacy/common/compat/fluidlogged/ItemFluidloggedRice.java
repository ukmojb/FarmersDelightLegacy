package com.wdcftgg.farmersdelightlegacy.common.compat.fluidlogged;

import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fluids.FluidRegistry;

public class ItemFluidloggedRice extends Item implements IPlantable {

    private final Block cropBlock;

    public ItemFluidloggedRice(Block cropBlock) {
        this.cropBlock = cropBlock;
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
        return ((IPlantable) cropBlock).getPlantType(world, pos);
    }

    @Override
    public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
        return cropBlock.getDefaultState();
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing,
                                      float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        BlockPos placePos = pos.up();
        if (facing != EnumFacing.UP || !player.canPlayerEdit(placePos, facing, stack)) {
            return EnumActionResult.FAIL;
        }

        IBlockState targetState = worldIn.getBlockState(pos);
        FluidState placeFluidState = FluidloggedUtils.getFluidState(worldIn, placePos);
        boolean hasWater = !placeFluidState.isEmpty() && FluidloggedUtils.isCompatibleFluid(FluidRegistry.WATER, placeFluidState.getFluid());
        if (this.isValidRiceSoil(targetState) && hasWater && worldIn.mayPlace(this.cropBlock, placePos, false, facing, player)
                && this.cropBlock.canPlaceBlockAt(worldIn, placePos)) {
            worldIn.setBlockState(placePos, this.cropBlock.getDefaultState(), 11);
            IBlockState placedState = worldIn.getBlockState(placePos);
            FluidloggedUtils.setFluidState(worldIn, placePos, placedState, FluidState.of(FluidRegistry.WATER), true);
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
            return EnumActionResult.SUCCESS;
        }

        if (!worldIn.isRemote && this.isValidRiceSoil(targetState)) {
            player.sendStatusMessage(new TextComponentTranslation("farmersdelight.block.rice.invalid_placement"), true);
        }
        return EnumActionResult.FAIL;
    }

    private boolean isValidRiceSoil(IBlockState state) {
        Block soil = state.getBlock();
        return soil instanceof BlockFarmland || soil instanceof BlockDirt || soil == ModBlocks.RICH_SOIL_FARMLAND;
    }
}
