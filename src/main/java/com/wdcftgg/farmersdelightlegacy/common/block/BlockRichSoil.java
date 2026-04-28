package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

import java.util.Random;

public class BlockRichSoil extends Block {
    public BlockRichSoil() {
        super(Material.GROUND);
        this.setHardness(0.7F);
        this.setResistance(2.7F);
        this.setSoundType(SoundType.GROUND);
        this.setTickRandomly(true);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = playerIn.getHeldItem(hand);
        if (heldItem.isEmpty() || !(heldItem.getItem() instanceof ItemHoe)) {
            return false;
        }
        if (facing == EnumFacing.DOWN || !worldIn.isAirBlock(pos.up())) {
            return false;
        }

        if (worldIn.isRemote) {
            return true;
        }

        IBlockState farmlandState = ModBlocks.RICH_SOIL_FARMLAND.getDefaultState()
                .withProperty(BlockFarmland.MOISTURE, 0);
        worldIn.setBlockState(pos, farmlandState, 11);
        SoundType soundType = farmlandState.getBlock().getSoundType(farmlandState, worldIn, pos, playerIn);
        worldIn.playSound(null, pos, soundType.getPlaceSound(), SoundCategory.BLOCKS,
                (soundType.getVolume() + 1.0F) * 0.5F, soundType.getPitch() * 0.8F);
        heldItem.damageItem(1, playerIn);
        return true;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (worldIn.isRemote || Configuration.richSoilBoostChance <= 0.0D || rand.nextFloat() > Configuration.richSoilBoostChance) {
            return;
        }

        BlockPos abovePos = pos.up();
        IBlockState aboveState = worldIn.getBlockState(abovePos);
        Block aboveBlock = aboveState.getBlock();
        if (aboveBlock == Blocks.BROWN_MUSHROOM) {
            worldIn.setBlockState(abovePos, ModBlocks.BROWN_MUSHROOM_COLONY.getDefaultState(), 3);
            return;
        }
        if (aboveBlock == Blocks.RED_MUSHROOM) {
            worldIn.setBlockState(abovePos, ModBlocks.RED_MUSHROOM_COLONY.getDefaultState(), 3);
            return;
        }
        if (!(aboveBlock instanceof IGrowable)) {
            return;
        }

        IGrowable growable = (IGrowable) aboveBlock;
        if (growable.canGrow(worldIn, abovePos, aboveState, false) && growable.canUseBonemeal(worldIn, rand, abovePos, aboveState)) {
            growable.grow(worldIn, rand, abovePos, aboveState);
        }
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable) {
        if (direction != EnumFacing.UP) {
            return false;
        }

        EnumPlantType plantType = plantable.getPlantType(world, pos.up());
        return plantType != EnumPlantType.Crop && plantType != EnumPlantType.Nether && plantType != EnumPlantType.Water;
    }
}
