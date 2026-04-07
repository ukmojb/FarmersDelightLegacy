package com.wdcftgg.farmersdelightlegacy.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;

public class BlockFeast extends Block {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyInteger SERVINGS = PropertyInteger.create("servings", 0, 8);

    private static final AxisAlignedBB FEAST_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D);

    private final int maxServings;
    private final ResourceLocation servingItemId;
    @Nullable
    private final ResourceLocation requiredContainerId;

    public BlockFeast(int maxServings, String servingItemPath, @Nullable String requiredContainerPath) {
        super(Material.CAKE);
        this.maxServings = maxServings;
        this.servingItemId = new ResourceLocation("farmersdelight", servingItemPath);
        this.requiredContainerId = requiredContainerPath == null ? null : new ResourceLocation(requiredContainerPath);

        this.setHardness(0.8F);
        this.setResistance(1.0F);
        this.setSoundType(SoundType.WOOD);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(SERVINGS, maxServings));
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
                                            float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(SERVINGS, this.maxServings);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hand != EnumHand.MAIN_HAND) {
            return true;
        }

        int servings = state.getValue(SERVINGS);
        if (servings <= 0) {
            return false;
        }

        Item servingItem = ForgeRegistries.ITEMS.getValue(this.servingItemId);
        if (servingItem == null) {
            return false;
        }

        ItemStack heldStack = playerIn.getHeldItem(hand);
        if (this.requiredContainerId != null) {
            Item containerItem = ForgeRegistries.ITEMS.getValue(this.requiredContainerId);
            if (containerItem == null || heldStack.isEmpty() || heldStack.getItem() != containerItem) {
                return false;
            }
            if (!playerIn.capabilities.isCreativeMode) {
                heldStack.shrink(1);
            }
        }

        if (!worldIn.isRemote) {
            ItemStack result = new ItemStack(servingItem);
            if (!playerIn.addItemStackToInventory(result)) {
                playerIn.dropItem(result, false);
            }

            worldIn.setBlockState(pos, state.withProperty(SERVINGS, servings - 1), 3);
        }

        return true;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int facingMeta = state.getValue(FACING).getHorizontalIndex();
        int servingsMeta = state.getValue(SERVINGS);
        return (servingsMeta << 2) | facingMeta;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        int facingMeta = meta & 3;
        int servingsMeta = (meta >> 2) & 15;
        if (servingsMeta > this.maxServings) {
            servingsMeta = this.maxServings;
        }
        return this.getDefaultState()
                .withProperty(FACING, EnumFacing.byHorizontalIndex(facingMeta))
                .withProperty(SERVINGS, servingsMeta);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{FACING, SERVINGS});
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return FEAST_AABB;
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
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }
}

