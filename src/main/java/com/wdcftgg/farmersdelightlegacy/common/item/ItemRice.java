package com.wdcftgg.farmersdelightlegacy.common.item;

import com.wdcftgg.farmersdelightlegacy.common.compat.FluidloggingCompat;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
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

public class ItemRice extends Item implements IPlantable {

    private final Block cropBlock;

    public ItemRice(Block cropBlock) {
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
        IBlockState crop = cropBlock.getDefaultState();
        boolean aquatic = FluidloggingCompat.usesFutureDelightWaterlogging(crop);
        boolean suitableSoil = aquatic
                ? targetState.getBlock().canSustainPlant(targetState, worldIn, pos, facing, (IPlantable) cropBlock)
                : isValidRiceSoil(targetState);
        if ((aquatic || worldIn.isAirBlock(placePos)) && suitableSoil
                && worldIn.mayPlace(cropBlock, placePos, false, facing, player)
                && FluidloggingCompat.placeBlock(worldIn, placePos, crop, 11)) {
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
            return EnumActionResult.SUCCESS;
        }

        if (!worldIn.isRemote && suitableSoil) {
            player.sendStatusMessage(new TextComponentTranslation(aquatic ? "farmersdelight.block.rice.invalid_placement"
                    : "farmersdelight.block.rice.invalid_placement.legacy"), true);
        }
        return EnumActionResult.FAIL;
    }

    private boolean isValidRiceSoil(IBlockState state) {
        Block soil = state.getBlock();
        return soil instanceof BlockFarmland || soil instanceof BlockDirt || soil == ModBlocks.RICH_SOIL_FARMLAND;
    }
}
