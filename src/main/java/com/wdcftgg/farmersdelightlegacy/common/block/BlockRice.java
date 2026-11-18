package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.common.compat.FluidloggingCompat;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModItems;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import java.util.Random;

public class BlockRice extends BlockBush implements IGrowable {

    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);
    public static final PropertyBool SUPPORTING = PropertyBool.create("supporting");
    private static final AxisAlignedBB[] SHAPE_BY_AGE = new AxisAlignedBB[]{
            new AxisAlignedBB(3.0D / 16.0D, 0.0D, 3.0D / 16.0D, 13.0D / 16.0D, 8.0D / 16.0D, 13.0D / 16.0D),
            new AxisAlignedBB(3.0D / 16.0D, 0.0D, 3.0D / 16.0D, 13.0D / 16.0D, 10.0D / 16.0D, 13.0D / 16.0D),
            new AxisAlignedBB(2.0D / 16.0D, 0.0D, 2.0D / 16.0D, 14.0D / 16.0D, 12.0D / 16.0D, 14.0D / 16.0D),
            new AxisAlignedBB(1.0D / 16.0D, 0.0D, 1.0D / 16.0D, 15.0D / 16.0D, 1.0D, 15.0D / 16.0D)
    };

    public BlockRice() {
        super(Material.PLANTS);
        this.setTickRandomly(true);
        this.setHardness(0.0F);
        this.setSoundType(SoundType.PLANT);
        this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, 0).withProperty(SUPPORTING, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{AGE, SUPPORTING});
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int age = Math.max(0, Math.min(3, state.getValue(AGE)));
        return age | (state.getValue(SUPPORTING) ? 4 : 0);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(AGE, meta & 3)
                .withProperty(SUPPORTING, (meta & 4) != 0);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE_BY_AGE[Math.max(0, Math.min(this.getMaxAge(), this.getAge(state)))];
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        Block soil = state.getBlock();
        if (soil == ModBlocks.RICH_SOIL_FARMLAND) {
            return true;
        }
        if (soil instanceof BlockFarmland) {
            return true;
        }
        if (soil == Blocks.DIRT) {
            return true;
        }
        if (soil == Blocks.GRASS) {
            return true;
        }
        if (soil == Blocks.GRASS_PATH) {
            return true;
        }
        if (soil instanceof BlockDirt) {
            return true;
        }
        return super.canSustainBush(state);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos) && FluidloggingCompat.hasAdjacentWater(worldIn, pos.down());
    }

    @Override
    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
        IBlockState soilState = worldIn.getBlockState(pos.down());
        return this.canSustainBush(soilState) && FluidloggingCompat.hasAdjacentWater(worldIn, pos.down());
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isAreaLoaded(pos, 1)) {
            return;
        }

        if (removeIfUnsupported(worldIn, pos, state)) {
            return;
        }

        if (worldIn.getLightFromNeighbors(pos.up()) >= 6) {
            int age = this.getAge(state);
            boolean shouldGrow = age <= this.getMaxAge() && rand.nextInt((int) (25.0F / 10.0F) + 1) == 0;
            if (ForgeHooks.onCropsGrowPre(worldIn, pos, state, shouldGrow)) {
                if (age == this.getMaxAge()) {
                    if (this.tryGrowPanicles(worldIn, pos, 0)) {
                        ForgeHooks.onCropsGrowPost(worldIn, pos, state, worldIn.getBlockState(pos));
                    }
                } else {
                    IBlockState grownState = state.withProperty(AGE, age + 1);
                    worldIn.setBlockState(pos, grownState, 2);
                    ForgeHooks.onCropsGrowPost(worldIn, pos, state, grownState);
                }
            }
        }

        this.updateSupportingState(worldIn, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!removeIfUnsupported(worldIn, pos, state)) {
            this.updateSupportingState(worldIn, pos);
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ModItems.ITEMS.get("rice");
    }

    protected final boolean removeIfUnsupported(World world, BlockPos pos, IBlockState state) {
        if (canBlockStay(world, pos, state)) {
            return false;
        }
        world.playEvent(2001, pos, Block.getStateId(state));
        dropBlockAsItem(world, pos, state, 0);
        FluidloggingCompat.removeBlock(world, pos);
        return true;
    }

    @Override
    public int quantityDropped(Random random) {
        return 1;
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        IBlockState upperState = worldIn.getBlockState(pos.up());
        if (upperState.getBlock() instanceof BlockRicePanicles) {
            return !((BlockRicePanicles) upperState.getBlock()).isMaxAge(upperState);
        }
        return true;
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        int ageGrowth = Math.min(this.getAge(state) + this.getBonemealAgeIncrease(worldIn), 7);
        if (ageGrowth <= this.getMaxAge()) {
            worldIn.setBlockState(pos, state.withProperty(AGE, ageGrowth), 2);
            return;
        }

        IBlockState topState = worldIn.getBlockState(pos.up());
        if (topState.getBlock() instanceof BlockRicePanicles) {
            BlockRicePanicles panicles = (BlockRicePanicles) topState.getBlock();
            if (panicles.canGrow(worldIn, pos.up(), topState, false)) {
                panicles.grow(worldIn, rand, pos.up(), topState);
            }
            return;
        }

        int remainingGrowth = ageGrowth - this.getMaxAge() - 1;
        if (this.tryGrowPanicles(worldIn, pos, remainingGrowth)) {
            worldIn.setBlockState(pos, worldIn.getBlockState(pos).withProperty(AGE, this.getMaxAge()), 2);
        }
    }

    public int getMaxAge() {
        return 3;
    }

    protected int getAge(IBlockState state) {
        return state.getValue(AGE);
    }

    protected int getBonemealAgeIncrease(World worldIn) {
        return 1 + worldIn.rand.nextInt(4);
    }

    private boolean tryGrowPanicles(World worldIn, BlockPos pos, int panicleAge) {
        BlockPos abovePos = pos.up();
        if (!worldIn.isAirBlock(abovePos)) {
            return false;
        }

        BlockRicePanicles paniclesBlock = (BlockRicePanicles) ModBlocks.RICE_PANICLES;
        IBlockState paniclesState = ModBlocks.RICE_PANICLES.getDefaultState()
                .withProperty(BlockRicePanicles.AGE, Math.max(0, Math.min(3, panicleAge)));
        if (!paniclesBlock.canPlaceBlockAt(worldIn, abovePos) || !paniclesBlock.canBlockStay(worldIn, abovePos, paniclesState)) {
            return false;
        }

        worldIn.setBlockState(abovePos, paniclesState, 2);
        this.updateSupportingState(worldIn, pos);
        return true;
    }

    private void updateSupportingState(World worldIn, BlockPos pos) {
        IBlockState current = worldIn.getBlockState(pos);
        if (current.getBlock() != this) {
            return;
        }

        boolean supporting = worldIn.getBlockState(pos.up()).getBlock() == ModBlocks.RICE_PANICLES;
        if (current.getValue(SUPPORTING) != supporting) {
            worldIn.setBlockState(pos, current.withProperty(SUPPORTING, supporting), 2);
        }
    }
}
