package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.common.compat.FluidloggingCompat;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.FarmlandWaterManager;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockRichSoilFarmland extends BlockFarmland {
    public BlockRichSoilFarmland() {
        this.setHardness(0.7F);
        this.setResistance(2.7F);
        this.setSoundType(SoundType.GROUND);
        this.setDefaultState(this.blockState.getBaseState().withProperty(MOISTURE, 0));
        this.setTickRandomly(true);
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (worldIn.isRemote) {
            return;
        }
        int moisture = state.getValue(MOISTURE);
        if (!this.hasWater(worldIn, pos) && !worldIn.isRainingAt(pos.up())) {
            if (moisture > 0) {
                worldIn.setBlockState(pos, state.withProperty(MOISTURE, moisture - 1), 2);
            }
        } else if (moisture < 7) {
            worldIn.setBlockState(pos, state.withProperty(MOISTURE, 7), 2);
        } else {
            BlockRichSoil.tryBoostingPlantsAboveAndBelow(worldIn, pos, rand);
        }
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote && !canRemainFarmland(worldIn, pos)) {
            turnToRichSoil(worldIn, pos);
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!worldIn.isRemote && !canRemainFarmland(worldIn, pos)) {
            worldIn.scheduleUpdate(pos, this, 1);
        }
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote && !canRemainFarmland(worldIn, pos)) {
            turnToRichSoil(worldIn, pos);
        }
    }

    @Override
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        entityIn.fall(fallDistance, 1.0F);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(ModBlocks.RICH_SOIL);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(ModBlocks.RICH_SOIL);
    }

    private boolean hasWater(World worldIn, BlockPos pos) {
        for (BlockPos blockPos : BlockPos.getAllInBoxMutable(pos.add(-4, 0, -4), pos.add(4, 1, 4))) {
            if (FluidloggingCompat.containsWater(worldIn, blockPos)) {
                return true;
            }
        }
        return FarmlandWaterManager.hasBlockWaterTicket(worldIn, pos);
    }

    private boolean canRemainFarmland(World world, BlockPos pos) {
        IBlockState above = world.getBlockState(pos.up());
        Block block = above.getBlock();
        return !above.getMaterial().isSolid() || block instanceof BlockFenceGate || block instanceof BlockTrapDoor
                || block == Blocks.MELON_BLOCK || block == Blocks.PUMPKIN;
    }

    private void turnToRichSoil(World worldIn, BlockPos pos) {
        worldIn.setBlockState(pos, ModBlocks.RICH_SOIL.getDefaultState());
        AxisAlignedBB soilBox = new AxisAlignedBB(0.0D, 0.9375D, 0.0D, 1.0D, 1.0D, 1.0D).offset(pos);

        for (Entity entity : worldIn.getEntitiesWithinAABBExcludingEntity(null, soilBox)) {
            double offset = Math.min(soilBox.maxY - soilBox.minY, soilBox.maxY - entity.getEntityBoundingBox().minY);
            entity.setPositionAndUpdate(entity.posX, entity.posY + offset + 0.001D, entity.posZ);
        }
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, net.minecraft.util.EnumFacing direction, IPlantable plantable) {
        if (direction != EnumFacing.UP) {
            return false;
        }

        IBlockState plant = plantable.getPlant(world, pos.up());
        Block plantBlock = plant.getBlock();
        if (plantable.getPlantType(world, pos.up()) == EnumPlantType.Crop) {
            return true;
        }
        if (plantBlock == Blocks.MELON_STEM || plantBlock == Blocks.PUMPKIN_STEM) {
            return true;
        }
        return super.canSustainPlant(state, world, pos, direction, plantable);
    }

    @Override
    public boolean isFertile(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock() == this && state.getValue(MOISTURE) > 0;
    }
}
