package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.common.compat.FluidloggingCompat;
import net.minecraft.block.BlockFence;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockRopeFence extends BlockFence {
    private static final double EDGE_MIN = 7.0D / 16.0D;
    private static final double EDGE_MAX = 9.0D / 16.0D;

    public BlockRopeFence() {
        super(Material.WOOD, MapColor.WOOD);
        setHardness(1.0F);
        setResistance(1.0F);
        setSoundType(SoundType.WOOD);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return FluidloggingCompat.createBlockState(this, NORTH, EAST, SOUTH, WEST);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean canConnectTo(IBlockAccess world, BlockPos pos, EnumFacing face) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockRopeFence) {
            return true;
        }
        if (state.getBlock() instanceof BlockRopeFenceGate) {
            return face.getAxis().isHorizontal() && state.getValue(BlockRopeFenceGate.FACING).getAxis() != face.getAxis();
        }
        return !isExcepBlockForAttachWithPiston(state.getBlock())
                && state.getBlockFaceShape(world, pos, face) == BlockFaceShape.SOLID;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.withProperty(NORTH, canConnectTo(world, pos.north(), EnumFacing.SOUTH))
                .withProperty(EAST, canConnectTo(world, pos.east(), EnumFacing.WEST))
                .withProperty(SOUTH, canConnectTo(world, pos.south(), EnumFacing.NORTH))
                .withProperty(WEST, canConnectTo(world, pos.west(), EnumFacing.EAST));
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return face.getAxis().isHorizontal() ? BlockFaceShape.UNDEFINED : BlockFaceShape.CENTER;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        state = getActualState(state, world, pos);
        return new AxisAlignedBB(state.getValue(WEST) ? 0.0D : EDGE_MIN, 0.0D,
                state.getValue(NORTH) ? 0.0D : EDGE_MIN,
                state.getValue(EAST) ? 1.0D : EDGE_MAX, 1.0D,
                state.getValue(SOUTH) ? 1.0D : EDGE_MAX);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox,
                                      List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean isActualState) {
        if (!isActualState) {
            state = getActualState(state, world, pos);
        }
        for (AxisAlignedBB box : getShape(state, 1.5D)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, box);
        }
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
        RayTraceResult closest = null;
        for (AxisAlignedBB box : getShape(getActualState(state, world, pos), 1.0D)) {
            RayTraceResult hit = rayTrace(pos, start, end, box);
            if (hit != null && (closest == null || start.squareDistanceTo(hit.hitVec) < start.squareDistanceTo(closest.hitVec))) {
                closest = hit;
            }
        }
        return closest;
    }

    private static List<AxisAlignedBB> getShape(IBlockState state, double height) {
        List<AxisAlignedBB> boxes = new ArrayList<>(5);
        boxes.add(new AxisAlignedBB(EDGE_MIN, 0.0D, EDGE_MIN, EDGE_MAX, height, EDGE_MAX));
        if (state.getValue(NORTH)) {
            boxes.add(new AxisAlignedBB(EDGE_MIN, 0.0D, 0.0D, EDGE_MAX, height, EDGE_MIN));
        }
        if (state.getValue(SOUTH)) {
            boxes.add(new AxisAlignedBB(EDGE_MIN, 0.0D, EDGE_MAX, EDGE_MAX, height, 1.0D));
        }
        if (state.getValue(WEST)) {
            boxes.add(new AxisAlignedBB(0.0D, 0.0D, EDGE_MIN, EDGE_MIN, height, EDGE_MAX));
        }
        if (state.getValue(EAST)) {
            boxes.add(new AxisAlignedBB(EDGE_MAX, 0.0D, EDGE_MIN, 1.0D, height, EDGE_MAX));
        }
        return boxes;
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
