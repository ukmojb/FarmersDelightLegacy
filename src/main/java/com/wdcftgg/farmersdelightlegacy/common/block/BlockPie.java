package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.item.ItemFoodTooltip;
import com.wdcftgg.farmersdelightlegacy.common.item.ItemKnife;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Random;
import java.util.Objects;
import java.util.List;
import javax.annotation.Nullable;

public class BlockPie extends Block {
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyInteger BITES = PropertyInteger.create("bites", 0, 3);
    private static final FoodShape[][] SHAPES = {
            new FoodShape(FoodShape.box(2, 0, 2, 14, 4, 14)).rotated(),
            new FoodShape(FoodShape.box(2, 0, 8, 8, 4, 14), FoodShape.box(2, 0, 2, 14, 4, 8)).rotated(),
            new FoodShape(FoodShape.box(2, 0, 2, 14, 4, 8)).rotated(),
            new FoodShape(FoodShape.box(8, 0, 2, 14, 4, 8)).rotated()
    };

    private final ResourceLocation sliceItemId;

    public BlockPie(String sliceItemPath) {
        super(Material.CAKE);
        this.sliceItemId = new ResourceLocation(FarmersDelightLegacy.MOD_ID, sliceItemPath);
        this.setHardness(0.5F);
        this.setResistance(0.5F);
        this.setSoundType(SoundType.CLOTH);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(BITES, 0));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return getShape(state).getBounds();
    }

    private FoodShape getShape(IBlockState state) {
        return SHAPES[state.getValue(BITES)][state.getValue(FACING).getHorizontalIndex()];
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox,
                                      List<AxisAlignedBB> boxes, @Nullable Entity entity, boolean actualState) {
        getShape(state).addCollisionBoxes(pos, entityBox, boxes);
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
        return getShape(state).rayTrace(pos, start, end);
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos)
                && world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (fromPos.equals(pos.down()) && !this.canPlaceBlockAt(world, pos)) {
            world.setBlockToAir(pos);
            return;
        }
        super.neighborChanged(state, world, pos, block, fromPos);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack heldStack = player.getHeldItem(hand);
        if (ItemKnife.isKnife(heldStack)) {
            if (!world.isRemote) {
                cutSlice(world, pos, state, player, heldStack);
            }
            return true;
        }

        return consumeBite(world, pos, state, player);
    }

    private boolean consumeBite(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (!player.canEat(false)) {
            return false;
        }
        if (world.isRemote) {
            return true;
        }

        ItemStack sliceStack = getPieSlice();
        Item sliceItem = sliceStack.getItem();
        if (sliceItem instanceof ItemFood) {
            ItemFood sliceFood = (ItemFood) sliceItem;
            player.getFoodStats().addStats(sliceFood, sliceStack);
            if (sliceFood instanceof ItemFoodTooltip) {
                ((ItemFoodTooltip) sliceFood).onFoodEaten(sliceStack, world, player);
            }
        }

        removeSlice(world, pos, state);
        world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.PLAYERS, 0.8F, 0.8F);
        spawnPieParticles(world, pos, state);
        return true;
    }

    private void cutSlice(World world, BlockPos pos, IBlockState state, EntityPlayer player, ItemStack knifeStack) {
        removeSlice(world, pos, state);

        EnumFacing dropFacing = player.getHorizontalFacing().getOpposite();
        ItemStack sliceStack = getPieSlice();
        EntityItem sliceEntity = new EntityItem(world,
                pos.getX() + 0.5D, pos.getY() + 0.3D, pos.getZ() + 0.5D, sliceStack);
        sliceEntity.motionX = dropFacing.getXOffset() * 0.15D;
        sliceEntity.motionY = 0.05D;
        sliceEntity.motionZ = dropFacing.getZOffset() * 0.15D;
        sliceEntity.setDefaultPickupDelay();
        world.spawnEntity(sliceEntity);

        world.playSound(null, pos, ModSounds.foodSlice, SoundCategory.PLAYERS, 0.8F, 0.8F);
        spawnPieParticles(world, pos, state);
        StatBase stat = StatList.getObjectUseStats(knifeStack.getItem());
        if (stat != null) {
            player.addStat(stat);
        }
    }

    public ItemStack getPieSlice() {
        return new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(sliceItemId), "未注册派切片: " + sliceItemId));
    }

    private void removeSlice(World world, BlockPos pos, IBlockState state) {
        int bites = state.getValue(BITES);
        if (bites >= 3) {
            world.setBlockToAir(pos);
        } else {
            world.setBlockState(pos, state.withProperty(BITES, bites + 1), 3);
        }
    }

    private void spawnPieParticles(World world, BlockPos pos, IBlockState state) {
        if (world instanceof WorldServer) {
            ((WorldServer) world).spawnParticle(EnumParticleTypes.BLOCK_CRACK,
                    pos.getX() + 0.5D, pos.getY() + 0.3D, pos.getZ() + 0.5D,
                    3, 0.1D, 0.1D, 0.1D, 0.001D, Block.getStateId(state));
        }
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        return 4 - state.getValue(BITES);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
                                            float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{FACING, BITES});
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int facingBits = state.getValue(FACING).getHorizontalIndex();
        int biteBits = state.getValue(BITES) << 2;
        return facingBits | biteBits;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        int facingIndex = meta & 3;
        int bites = (meta >> 2) & 3;
        return this.getDefaultState()
                .withProperty(FACING, EnumFacing.byHorizontalIndex(facingIndex))
                .withProperty(BITES, bites);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        if (this == ModBlocks.pumpkinPie) {
            return new ItemStack(Items.PUMPKIN_PIE);
        }
        return super.getPickBlock(state, target, world, pos, player);
    }
}

