package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import com.wdcftgg.farmersdelightlegacy.common.registry.BlockCategories;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.IGrowable;
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
import net.minecraftforge.common.ForgeHooks;
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
        if (worldIn.isRemote) {
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
        tryBoostingPlantsAboveAndBelow(worldIn, pos, rand);
    }

    static void tryBoostingPlantsAboveAndBelow(World world, BlockPos pos, Random random) {
        if (world.isRemote || Configuration.richSoilBoostChance <= 0.0D
                || random.nextFloat() > Configuration.richSoilBoostChance) {
            return;
        }
        BlockPos above = pos.up();
        IBlockState aboveState = world.getBlockState(above);
        if (!isPlantedFromBelow(aboveState) && boostPlant(world, above, aboveState, random)) {
            return;
        }
        BlockPos below = pos.down();
        IBlockState belowState = world.getBlockState(below);
        if (isPlantedFromBelow(belowState)) {
            boostPlant(world, below, belowState, random);
        }
    }

    private static boolean boostPlant(World world, BlockPos pos, IBlockState state, Random random) {
        if (matchesBlockList(state, Configuration.unaffectedByRichSoil)
                || BlockCategories.contains("fdUnaffectedByRichSoil", state) || !(state.getBlock() instanceof IGrowable)) {
            return false;
        }
        IGrowable plant = (IGrowable) state.getBlock();
        if (!plant.canGrow(world, pos, state, false) || !ForgeHooks.onCropsGrowPre(world, pos, state, true)) {
            return false;
        }
        plant.grow(world, random, pos, state);
        world.playEvent(2005, pos, 15);
        ForgeHooks.onCropsGrowPost(world, pos, state, world.getBlockState(pos));
        return true;
    }

    private static boolean isPlantedFromBelow(IBlockState state) {
        return matchesBlockList(state, Configuration.plantedFromBelow) || BlockCategories.contains("fdPlantedFromBelow", state);
    }

    static boolean matchesBlockList(IBlockState state, String[] entries) {
        String id = String.valueOf(state.getBlock().getRegistryName());
        String variant = id + "@" + state.getBlock().getMetaFromState(state);
        for (String entry : entries) {
            if (entry.equals(id) || entry.equals(variant)) {
                return true;
            }
        }
        return false;
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
