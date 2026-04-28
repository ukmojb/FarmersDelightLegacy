package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.advancement.ModAdvancements;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.ForgeHooks;

import java.util.Random;

public class BlockTomatoVine extends BlockCrops {

    public static final PropertyBool ROPELOGGED = PropertyBool.create("ropelogged");
    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(2.0D / 16.0D, 0.0D, 2.0D / 16.0D, 14.0D / 16.0D, 1.0D, 14.0D / 16.0D);

    public BlockTomatoVine() {
        this.setTickRandomly(true);
        this.setHardness(0.0F);
        this.setSoundType(SoundType.PLANT);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(this.getAgeProperty(), 0)
                .withProperty(ROPELOGGED, false));
    }

    @Override
    public int getMaxAge() {
        return 3;
    }

    @Override
    protected Item getSeed() {
        return ModItems.ITEMS.get("tomato_seeds");
    }

    @Override
    protected Item getCrop() {
        return ModItems.ITEMS.get("tomato");
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{this.getAgeProperty(), ROPELOGGED});
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int age = Math.max(0, Math.min(3, this.getAge(state)));
        return age | (state.getValue(ROPELOGGED) ? 4 : 0);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        int age = meta & 3;
        boolean ropelogged = (meta & 4) != 0;
        return this.withAge(age).withProperty(ROPELOGGED, ropelogged);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (this.getAge(state) < this.getMaxAge()) {
            return false;
        }

        if (!worldIn.isRemote) {
            int quantity = 1 + worldIn.rand.nextInt(2);
            Item tomatoItem = ModItems.ITEMS.get("tomato");
            if (tomatoItem != null) {
                Block.spawnAsEntity(worldIn, pos, new ItemStack(tomatoItem, quantity));
            }

            if (worldIn.rand.nextFloat() < 0.05F) {
                Item rottenTomatoItem = ModItems.ITEMS.get("rotten_tomato");
                if (rottenTomatoItem != null) {
                    Block.spawnAsEntity(worldIn, pos, new ItemStack(rottenTomatoItem));
                }
            }

            worldIn.playSound(null, pos, SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.BLOCKS, 1.0F, 0.9F + worldIn.rand.nextFloat() * 0.2F);
            if (state.getValue(ROPELOGGED) && playerIn instanceof net.minecraft.entity.player.EntityPlayerMP) {
                ModAdvancements.HARVEST_ROPELOGGED_TOMATO.trigger((net.minecraft.entity.player.EntityPlayerMP) playerIn);
            }
            worldIn.setBlockState(pos, state.withProperty(this.getAgeProperty(), 0), 2);
        }

        return true;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!this.canBlockStay(worldIn, pos, state)) {
            boolean ropelogged = state.getValue(ROPELOGGED);
            worldIn.destroyBlock(pos, true);
            if (ropelogged) {
                worldIn.setBlockState(pos, getConfiguredRopeState(), 3);
            }
            return;
        }

        if (!worldIn.isAreaLoaded(pos, 1)) {
            return;
        }

        if (worldIn.getLightFromNeighbors(pos.up()) >= 9) {
            int age = this.getAge(state);
            if (age < this.getMaxAge()) {
                float growthChance = getGrowthChance(this, worldIn, pos);
                if (ForgeHooks.onCropsGrowPre(worldIn, pos, state, rand.nextInt((int) (25.0F / growthChance) + 1) == 0)) {
                    IBlockState grownState = state.withProperty(this.getAgeProperty(), age + 1);
                    worldIn.setBlockState(pos, grownState, 2);
                    ForgeHooks.onCropsGrowPost(worldIn, pos, state, grownState);
                    state = grownState;
                }
            }

            if (!worldIn.isRemote) {
                attemptRopeClimb(worldIn, pos, rand);
            }
        }
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        int newAge = Math.min(this.getAge(state) + Math.max(1, this.getBonemealAgeIncrease(worldIn) / 2), this.getMaxAge());
        worldIn.setBlockState(pos, state.withProperty(this.getAgeProperty(), newAge), 2);
        attemptRopeClimb(worldIn, pos, rand);
    }

    private void attemptRopeClimb(World worldIn, BlockPos pos, Random rand) {
        if (rand.nextFloat() >= 0.3F) {
            return;
        }

        BlockPos abovePos = pos.up();
        if (!Configuration.isTomatoVineClimbableRope(worldIn.getBlockState(abovePos).getBlock())) {
            return;
        }

        int height = 1;
        while (worldIn.getBlockState(pos.down(height)).getBlock() == this) {
            height++;
        }
        if (height < 3) {
            worldIn.setBlockState(abovePos, this.getDefaultState().withProperty(ROPELOGGED, true), 3);
        }
    }

    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        return state.getValue(ROPELOGGED);
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, net.minecraft.entity.Entity entityIn) {
        if (state.getValue(ROPELOGGED) && entityIn instanceof EntityLivingBase) {
            BlockRope.applyClimbingMotion((EntityLivingBase) entityIn);
        }
    }

    @Override
    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
        if (state.getValue(ROPELOGGED)) {
            IBlockState belowState = worldIn.getBlockState(pos.down());
            boolean belowIsTomato = belowState.getBlock() == this;
            boolean goodLight = worldIn.getLightFromNeighbors(pos) >= 8 || worldIn.canSeeSky(pos);
            return belowIsTomato && goodLight;
        }
        return super.canBlockStay(worldIn, pos, state);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!this.canBlockStay(worldIn, pos, state)) {
            if (!worldIn.isRemote) {
                worldIn.scheduleUpdate(pos, this, 1);
            }
            return;
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        boolean ropelogged = state.getValue(ROPELOGGED);
        super.breakBlock(worldIn, pos, state);
        if (!worldIn.isRemote && ropelogged && worldIn.isAirBlock(pos)) {
            worldIn.setBlockState(pos, getConfiguredRopeState(), 3);
        }
    }

    private static IBlockState getConfiguredRopeState() {
        Block ropeBlock = Configuration.getDefaultTomatoVineRopeBlock();
        if (ropeBlock == null) {
            ropeBlock = ModBlocks.ROPE;
        }
        return ropeBlock.getDefaultState();
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
        return EnumPlantType.Crop;
    }
}
