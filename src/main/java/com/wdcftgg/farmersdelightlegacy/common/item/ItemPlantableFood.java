package com.wdcftgg.farmersdelightlegacy.common.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ItemPlantableFood extends ItemFoodTooltip implements IPlantable {

    private final Block cropBlock;
    private final Set<Block> validSoils;

    public ItemPlantableFood(int amount, float saturation, Block cropBlock, Block... validSoils) {
        super(amount, saturation, false);
        this.cropBlock = cropBlock;
        this.validSoils = new HashSet<>(Arrays.asList(validSoils));
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
        return EnumPlantType.Crop;
    }

    @Override
    public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
        return cropBlock.getDefaultState();
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing,
                                      float hitX, float hitY, float hitZ) {
        if (facing != EnumFacing.UP) {
            return EnumActionResult.FAIL;
        }

        ItemStack stack = player.getHeldItem(hand);
        BlockPos plantPos = pos.up();
        if (!player.canPlayerEdit(plantPos, facing, stack)) {
            return EnumActionResult.FAIL;
        }

        if (!this.canPlantOnSoil(worldIn, pos)) {
            return EnumActionResult.FAIL;
        }
        if (!worldIn.isAirBlock(plantPos) || !this.cropBlock.canPlaceBlockAt(worldIn, plantPos)) {
            return EnumActionResult.FAIL;
        }

        worldIn.setBlockState(plantPos, this.cropBlock.getDefaultState(), 11);
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        return EnumActionResult.SUCCESS;
    }

    private boolean canPlantOnSoil(World worldIn, BlockPos soilPos) {
        IBlockState soilState = worldIn.getBlockState(soilPos);
        if (this.cropBlock instanceof IPlantable) {
            return soilState.getBlock().canSustainPlant(soilState, worldIn, soilPos, EnumFacing.UP, (IPlantable) this.cropBlock);
        }
        return this.validSoils.contains(soilState.getBlock());
    }
}
