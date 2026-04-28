package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
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
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        int moisture = state.getValue(MOISTURE);
        if (!this.hasWater(worldIn, pos) && !worldIn.isRainingAt(pos.up())) {
            if (moisture > 0) {
                setRichSoilFarmlandMoisture(worldIn, pos, moisture - 1);
            }
        } else if (moisture < 7) {
            setRichSoilFarmlandMoisture(worldIn, pos, 7);
        }

        this.tryBoostCropGrowth(worldIn, pos, state, rand);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
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
            if (worldIn.getBlockState(blockPos).getMaterial() == net.minecraft.block.material.Material.WATER) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCrops(World worldIn, BlockPos pos) {
        IBlockState cropState = worldIn.getBlockState(pos.up());
        Block block = cropState.getBlock();
        if (!(block instanceof IPlantable)) {
            return false;
        }
        return this.canSustainPlant(worldIn.getBlockState(pos), worldIn, pos, net.minecraft.util.EnumFacing.UP, (IPlantable) block);
    }

    private void tryBoostCropGrowth(World worldIn, BlockPos farmlandPos, IBlockState farmlandState, Random rand) {
        if (worldIn.isRemote) {
            return;
        }

        if (farmlandState.getBlock() != this) {
            return;
        }

        if (farmlandState.getValue(MOISTURE) <= 0) {
            return;
        }

        BlockPos cropPos = farmlandPos.up();
        IBlockState cropState = worldIn.getBlockState(cropPos);
        Block cropBlock = cropState.getBlock();
        if (!(cropBlock instanceof net.minecraft.block.IGrowable)) {
            return;
        }

        net.minecraft.block.IGrowable growable = (net.minecraft.block.IGrowable) cropBlock;
        if (!growable.canGrow(worldIn, cropPos, cropState, false)) {
            return;
        }

        double boostChance = Configuration.richSoilBoostChance;
        if (boostChance <= 0.0D) {
            return;
        }
        if (this.hasAdjacentRichSoil(worldIn, farmlandPos)) {
            boostChance = Math.min(1.0D, boostChance * 2.0D);
        }
        if (rand.nextFloat() > boostChance) {
            return;
        }

        if (cropBlock instanceof BlockCrops) {
            ((BlockCrops) cropBlock).grow(worldIn, rand, cropPos, cropState);
            return;
        }

        if (growable.canUseBonemeal(worldIn, rand, cropPos, cropState)) {
            growable.grow(worldIn, rand, cropPos, cropState);
        }
    }

    private boolean hasAdjacentRichSoil(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.north()).getBlock() == ModBlocks.RICH_SOIL
                || worldIn.getBlockState(pos.south()).getBlock() == ModBlocks.RICH_SOIL
                || worldIn.getBlockState(pos.west()).getBlock() == ModBlocks.RICH_SOIL
                || worldIn.getBlockState(pos.east()).getBlock() == ModBlocks.RICH_SOIL;
    }

    private void turnToRichSoil(World worldIn, BlockPos pos) {
        worldIn.setBlockState(pos, ModBlocks.RICH_SOIL.getDefaultState());
        AxisAlignedBB soilBox = FULL_BLOCK_AABB.offset(pos);

        for (Entity entity : worldIn.getEntitiesWithinAABBExcludingEntity(null, soilBox)) {
            entity.setPosition(entity.posX, entity.posY + 1.0D, entity.posZ);
        }
    }

    private void setRichSoilFarmlandMoisture(World worldIn, BlockPos pos, int moisture) {
        int clampedMoisture = Math.max(0, Math.min(7, moisture));
        IBlockState richFarmlandState = ModBlocks.RICH_SOIL_FARMLAND.getDefaultState().withProperty(MOISTURE, clampedMoisture);
        worldIn.setBlockState(pos, richFarmlandState, 2);
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
        return true;
    }
}
