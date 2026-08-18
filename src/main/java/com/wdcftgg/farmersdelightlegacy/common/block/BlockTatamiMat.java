package com.wdcftgg.farmersdelightlegacy.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTatamiMat extends BlockHorizontal {

    public enum EnumMatPart implements IStringSerializable {
        FOOT("foot"),
        HEAD("head");

        private final String name;

        EnumMatPart(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    public static final PropertyEnum<EnumMatPart> PART = PropertyEnum.create("part", EnumMatPart.class);
    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 2.0D / 16.0D, 1.0D);

    public BlockTatamiMat() {
        super(Material.CARPET);
        this.setHardness(0.3F);
        this.setResistance(0.3F);
        this.setSoundType(SoundType.CLOTH);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(FACING, net.minecraft.util.EnumFacing.NORTH)
                .withProperty(PART, EnumMatPart.FOOT));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return SHAPE;
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
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        net.minecraft.util.EnumFacing placementFacing = placer.getHorizontalFacing();
        BlockPos pairPos = pos.offset(placementFacing);

        IBlockState pairState = worldIn.getBlockState(pairPos);
        if (!pairState.getBlock().isReplaceable(worldIn, pairPos)) {
            net.minecraft.util.EnumFacing opposite = placementFacing.getOpposite();
            BlockPos oppositePairPos = pos.offset(opposite);
            IBlockState oppositePairState = worldIn.getBlockState(oppositePairPos);
            if (oppositePairState.getBlock().isReplaceable(worldIn, oppositePairPos)) {
                placementFacing = opposite;
            }
        }

        return this.getDefaultState()
                .withProperty(FACING, placementFacing)
                .withProperty(PART, EnumMatPart.FOOT);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos)
                && worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), net.minecraft.util.EnumFacing.UP);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (worldIn.isRemote) {
            return;
        }

        BlockPos pairPos = pos.offset(state.getValue(FACING));
        IBlockState pairTargetState = worldIn.getBlockState(pairPos);
        if (!pairTargetState.getBlock().isReplaceable(worldIn, pairPos)
                || !worldIn.getBlockState(pairPos.down()).isSideSolid(worldIn, pairPos.down(), net.minecraft.util.EnumFacing.UP)) {
            worldIn.destroyBlock(pos, true);
            return;
        }

        worldIn.setBlockState(pairPos, state.withProperty(PART, EnumMatPart.HEAD), 3);
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (!worldIn.isRemote && player.capabilities.isCreativeMode && state.getValue(PART) == EnumMatPart.FOOT) {
            BlockPos pairPos = pos.offset(state.getValue(FACING));
            IBlockState pairState = worldIn.getBlockState(pairPos);
            if (pairState.getBlock() == this && pairState.getValue(PART) == EnumMatPart.HEAD) {
                worldIn.setBlockToAir(pairPos);
            }
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        net.minecraft.util.EnumFacing toPair = state.getValue(PART) == EnumMatPart.FOOT ? state.getValue(FACING) : state.getValue(FACING).getOpposite();
        BlockPos pairPos = pos.offset(toPair);

        IBlockState pairState = worldIn.getBlockState(pairPos);
        boolean pairValid = pairState.getBlock() == this && pairState.getValue(PART) != state.getValue(PART);
        boolean hasSupport = worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), net.minecraft.util.EnumFacing.UP);

        if (!pairValid) {
            worldIn.setBlockToAir(pos);
            return;
        }

        if (!hasSupport) {
            if (!worldIn.isRemote) {
                BlockPos footPos = state.getValue(PART) == EnumMatPart.FOOT ? pos : pairPos;
                BlockPos headPos = state.getValue(PART) == EnumMatPart.FOOT ? pairPos : pos;
                IBlockState footState = worldIn.getBlockState(footPos);
                IBlockState headState = worldIn.getBlockState(headPos);
                if (footState.getBlock() == this) {
                    worldIn.destroyBlock(footPos, true);
                }
                if (headState.getBlock() == this) {
                    worldIn.setBlockToAir(headPos);
                }
            }
            return;
        }

        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{FACING, PART});
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(FACING).getHorizontalIndex();
        if (state.getValue(PART) == EnumMatPart.HEAD) {
            meta |= 4;
        }
        return meta;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        net.minecraft.util.EnumFacing facing = net.minecraft.util.EnumFacing.byHorizontalIndex(meta & 3);
        EnumMatPart part = (meta & 4) != 0 ? EnumMatPart.HEAD : EnumMatPart.FOOT;
        return this.getDefaultState().withProperty(FACING, facing).withProperty(PART, part);
    }
}
