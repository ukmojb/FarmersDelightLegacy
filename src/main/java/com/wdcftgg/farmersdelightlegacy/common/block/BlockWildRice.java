package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.common.registry.BlockCategories;
import com.wdcftgg.farmersdelightlegacy.common.compat.FluidloggingCompat;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockWildRice extends BlockBush implements IGrowable {
    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 1.0D, 0.875D);
    public static final PropertyEnum<BlockDoublePlant.EnumBlockHalf> HALF = PropertyEnum.create("half", BlockDoublePlant.EnumBlockHalf.class);

    public BlockWildRice() {
        super(Material.PLANTS);
        this.setHardness(0.0F);
        this.setSoundType(SoundType.PLANT);
        this.setTickRandomly(true);
        this.setDefaultState(this.blockState.getBaseState().withProperty(HALF, BlockDoublePlant.EnumBlockHalf.LOWER));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        return BlockCategories.contains("fdTerrain", state);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos)
                && canSustainBush(worldIn.getBlockState(pos.down()))
                && worldIn.isAirBlock(pos.up())
                && this.hasNearbyWater(worldIn, pos.down());
    }

    @Override
    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
        BlockDoublePlant.EnumBlockHalf half = state.getValue(HALF);
        if (half == BlockDoublePlant.EnumBlockHalf.UPPER) {
            IBlockState belowState = worldIn.getBlockState(pos.down());
            return belowState.getBlock() == this && belowState.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.LOWER;
        }

        IBlockState soilState = worldIn.getBlockState(pos.down());
        return this.canSustainBush(soilState) && this.hasNearbyWater(worldIn, pos.down()) && worldIn.getBlockState(pos.up()).getBlock() == this;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote && state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.LOWER && worldIn.isAirBlock(pos.up())) {
            worldIn.setBlockState(pos.up(), this.getDefaultState().withProperty(HALF, BlockDoublePlant.EnumBlockHalf.UPPER), 2);
        }
        super.onBlockAdded(worldIn, pos, state);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        checkAndDropBlock(worldIn, pos, state);
    }

    @Override
    protected void checkAndDropBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!this.canBlockStay(worldIn, pos, state)) {
            if (!worldIn.isRemote) {
                this.spawnUpstreamDrop(worldIn, pos, state, ItemStack.EMPTY);
            }
            FluidloggingCompat.removeBlock(worldIn, pos);
            if (state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.LOWER && worldIn.getBlockState(pos.up()).getBlock() == this) {
                FluidloggingCompat.removeBlock(worldIn, pos.up());
            }
            if (state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER && worldIn.getBlockState(pos.down()).getBlock() == this) {
                FluidloggingCompat.removeBlock(worldIn, pos.down());
            }
        }
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (!worldIn.isRemote && !player.capabilities.isCreativeMode && this.hasCounterpart(worldIn, pos, state)) {
            this.spawnUpstreamDrop(worldIn, pos, state, player.getHeldItemMainhand());
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(FarmersDelightLegacy.MOD_ID, "rice"));
        return item != null ? item : Item.getItemFromBlock(Blocks.AIR);
    }

    @Override
    public int quantityDropped(Random random) {
        return 1;
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state,
                             @Nullable TileEntity te, ItemStack stack) {
        player.addStat(StatList.getBlockStats(this));
        player.addExhaustion(0.005F);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if (!hasCounterpart(world, pos, state)) {
            return;
        }

        Item riceItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(FarmersDelightLegacy.MOD_ID, "rice"));
        if (riceItem != null) {
            drops.add(new ItemStack(riceItem));
        }
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return rand.nextFloat() < 0.3F;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        if (state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER) {
            pos = pos.down();
            state = worldIn.getBlockState(pos);
        }

        if (state.getBlock() != this || state.getValue(HALF) != BlockDoublePlant.EnumBlockHalf.LOWER) {
            return;
        }

        Block.spawnAsEntity(worldIn, pos, new net.minecraft.item.ItemStack(this));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(HALF, (meta & 1) == 1
                ? BlockDoublePlant.EnumBlockHalf.UPPER
                : BlockDoublePlant.EnumBlockHalf.LOWER);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER ? 1 : 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, HALF);
    }

    @Override
    public IBlockState withRotation(IBlockState state, net.minecraft.util.Rotation rot) {
        return state;
    }

    @Override
    public IBlockState withMirror(IBlockState state, net.minecraft.util.Mirror mirrorIn) {
        return state;
    }

    private boolean hasNearbyWater(World worldIn, BlockPos soilPos) {
        return FluidloggingCompat.hasAdjacentWater(worldIn, soilPos) || FluidloggingCompat.containsWater(worldIn, soilPos);
    }

    private boolean hasCounterpart(IBlockAccess world, BlockPos pos, IBlockState state) {
        BlockDoublePlant.EnumBlockHalf half = state.getValue(HALF);
        BlockPos counterpartPos = half == BlockDoublePlant.EnumBlockHalf.LOWER ? pos.up() : pos.down();
        IBlockState counterpartState = world.getBlockState(counterpartPos);
        return counterpartState.getBlock() == this && counterpartState.getValue(HALF) != half;
    }

    private void spawnUpstreamDrop(World worldIn, BlockPos pos, IBlockState state, ItemStack toolStack) {
        if (!hasCounterpart(worldIn, pos, state)) {
            return;
        }

        boolean usingShears = !toolStack.isEmpty() && toolStack.getItem() == Items.SHEARS;
        ResourceLocation dropId = new ResourceLocation(FarmersDelightLegacy.MOD_ID, usingShears ? "wild_rice" : "rice");
        Item dropItem = ForgeRegistries.ITEMS.getValue(dropId);
        if (dropItem != null) {
            spawnAsEntity(worldIn, pos, new ItemStack(dropItem));
        }
    }
}

