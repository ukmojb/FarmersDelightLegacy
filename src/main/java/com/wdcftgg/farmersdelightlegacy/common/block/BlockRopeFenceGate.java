package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.common.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockWall;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockRopeFenceGate extends BlockFenceGate {
    private static final AxisAlignedBB SHAPE_Z = new AxisAlignedBB(0.0D, 0.0D, 7.0D / 16.0D, 1.0D, 14.0D / 16.0D, 9.0D / 16.0D);
    private static final AxisAlignedBB SHAPE_X = new AxisAlignedBB(7.0D / 16.0D, 0.0D, 0.0D, 9.0D / 16.0D, 14.0D / 16.0D, 1.0D);

    public BlockRopeFenceGate() {
        super(BlockPlanks.EnumType.OAK);
        setHardness(1.0F);
        setResistance(1.0F);
        setSoundType(SoundType.WOOD);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock().isReplaceable(world, pos);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        EnumFacing side = state.getValue(FACING).rotateY();
        return state.withProperty(IN_WALL, world.getBlockState(pos.offset(side)).getBlock() instanceof BlockWall
                && world.getBlockState(pos.offset(side.getOpposite())).getBlock() instanceof BlockWall);
    }

    @Override
    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing face) {
        if (!face.getAxis().isHorizontal() || world.getBlockState(pos).getValue(FACING).getAxis() == face.getAxis()) {
            return false;
        }
        Block neighbor = world.getBlockState(pos.offset(face)).getBlock();
        return neighbor instanceof BlockRopeFence || neighbor instanceof BlockWall;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(FACING).getAxis() == EnumFacing.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (state.getValue(OPEN)) {
            return NULL_AABB;
        }
        AxisAlignedBB box = getBoundingBox(state, world, pos);
        return new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, 1.5D, box.maxZ);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing face, float hitX, float hitY, float hitZ) {
        if (state.getValue(OPEN)) {
            state = state.withProperty(OPEN, false);
        } else {
            EnumFacing playerFacing = EnumFacing.fromAngle(player.rotationYaw);
            if (state.getValue(FACING) == playerFacing.getOpposite()) {
                state = state.withProperty(FACING, playerFacing);
            }
            state = state.withProperty(OPEN, true);
        }
        world.setBlockState(pos, state, 10);
        playGateSound(world, pos, state.getValue(OPEN), player);
        return true;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (world.isRemote) {
            return;
        }
        boolean powered = world.isBlockPowered(pos);
        if (state.getValue(POWERED) != powered) {
            world.setBlockState(pos, state.withProperty(POWERED, powered).withProperty(OPEN, powered), 2);
            if (state.getValue(OPEN) != powered) {
                playGateSound(world, pos, powered, null);
            }
        }
    }

    private static void playGateSound(World world, BlockPos pos, boolean open, @Nullable EntityPlayer player) {
        world.playSound(player, pos, open ? ModSounds.ropeFenceGateOpen : ModSounds.ropeFenceGateClose,
                SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 20;
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 5;
    }
}
