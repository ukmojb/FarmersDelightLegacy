package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.IGrowable;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.ForgeHooks;

import java.util.Random;

public class BlockBuddingTomato extends BlockBush implements IGrowable {

    public static final PropertyInteger AGE = BlockCrops.AGE;

    public BlockBuddingTomato() {
        setDefaultState(blockState.getBaseState().withProperty(AGE, 0));
        setCreativeTab(null);
        disableStats();
        this.setTickRandomly(true);
        this.setHardness(0.0F);
        this.setSoundType(net.minecraft.block.SoundType.PLANT);
    }

    public int getMaxAge() {
        return 3;
    }

    protected Item getSeed() {
        return ModItems.ITEMS.get("tomato_seeds");
    }

    protected Item getCrop() {
        return ModItems.ITEMS.get("tomato");
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!this.canBlockStay(worldIn, pos, state)) {
            worldIn.destroyBlock(pos, true);
            return;
        }
        if (getAge(state) > getMaxAge()) {
            worldIn.setBlockState(pos, ModBlocks.TOMATOES.getDefaultState(), 3);
        }
    }

    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (world.isRemote || !world.isAreaLoaded(pos, 1) || world.getLightFromNeighbors(pos) < 9) {
            return;
        }
        float speed = getGrowthChance(world, pos);
        if (ForgeHooks.onCropsGrowPre(world, pos, state, random.nextInt((int) (25.0F / speed) + 1) == 0)) {
            IBlockState grown = isMaxAge(state) ? ModBlocks.TOMATOES.getDefaultState() : withAge(getAge(state) + 1);
            world.setBlockState(pos, grown, 3);
            ForgeHooks.onCropsGrowPost(world, pos, state, world.getBlockState(pos));
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!this.canBlockStay(worldIn, pos, state)) {
            worldIn.destroyBlock(pos, true);
            return;
        }
        if (getAge(state) > getMaxAge()) {
            worldIn.setBlockState(pos, ModBlocks.TOMATOES.getDefaultState(), 3);
            return;
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        if (worldIn.isRemote) {
            return;
        }
        int maxAge = this.getMaxAge();
        int growth = Math.min(this.getAge(state) + this.getBonemealAgeIncrease(worldIn), 7);
        if (growth <= maxAge) {
            worldIn.setBlockState(pos, this.withAge(growth), 2);
        } else {
            int remainingGrowth = growth - maxAge - 1;
            IBlockState vineState = ModBlocks.TOMATOES.getDefaultState()
                    .withProperty(BlockTomatoVine.ROPELOGGED, false)
                    .withProperty(this.getAgeProperty(), Math.min(remainingGrowth, 3));
            worldIn.setBlockState(pos, vineState, 2);
        }
    }

    protected int getBonemealAgeIncrease(World world) {
        return 1 + world.rand.nextInt(4);
    }

    public void grow(World world, BlockPos pos, IBlockState state) {
        grow(world, world.rand, pos, state);
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) {
        return true;
    }

    public boolean isMaxAge(IBlockState state) {
        return getAge(state) >= getMaxAge();
    }

    protected PropertyInteger getAgeProperty() {
        return AGE;
    }

    protected int getAge(IBlockState state) {
        return state.getValue(AGE);
    }

    public IBlockState withAge(int age) {
        return getDefaultState().withProperty(AGE, age);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AGE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return withAge(meta & 7);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return getAge(state);
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        return state.getBlock() instanceof BlockFarmland;
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        IBlockState soil = world.getBlockState(pos.down());
        return (world.getLightFromNeighbors(pos) >= 8 || world.canSeeSky(pos))
                && soil.getBlock().canSustainPlant(soil, world, pos.down(), EnumFacing.UP, this);
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(getSeed());
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return getSeed();
    }

    private float getGrowthChance(World world, BlockPos pos) {
        float speed = 1.0F;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos soilPos = pos.add(x, -1, z);
                IBlockState soil = world.getBlockState(soilPos);
                if (soil.getBlock().canSustainPlant(soil, world, soilPos, EnumFacing.UP, this)) {
                    float bonus = soil.getBlock().isFertile(world, soilPos) ? 3.0F : 1.0F;
                    speed += x == 0 && z == 0 ? bonus : bonus / 4.0F;
                }
            }
        }
        boolean eastWest = world.getBlockState(pos.east()).getBlock() == this
                || world.getBlockState(pos.west()).getBlock() == this;
        boolean northSouth = world.getBlockState(pos.north()).getBlock() == this
                || world.getBlockState(pos.south()).getBlock() == this;
        boolean diagonal = world.getBlockState(pos.north().east()).getBlock() == this
                || world.getBlockState(pos.north().west()).getBlock() == this
                || world.getBlockState(pos.south().east()).getBlock() == this
                || world.getBlockState(pos.south().west()).getBlock() == this;
        return eastWest && northSouth || diagonal ? speed / 2.0F : speed;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return new AxisAlignedBB(0, 0, 0, 1, (2 + 4 * Math.min(getAge(state), getMaxAge())) / 16.0D, 1);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world,
                         BlockPos pos, IBlockState state, int fortune) {
        drops.add(new ItemStack(getSeed()));
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
        return EnumPlantType.Crop;
    }
}
