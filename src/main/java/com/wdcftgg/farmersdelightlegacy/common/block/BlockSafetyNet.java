package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.common.compat.FluidloggingCompat;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSafetyNet extends Block {

    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.0D, 7.0D / 16.0D, 0.0D, 1.0D, 9.0D / 16.0D, 1.0D);

    public BlockSafetyNet() {
        super(Material.CARPET);
        this.setHardness(0.2F);
        this.setResistance(0.2F);
        this.setSoundType(SoundType.CLOTH);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return FluidloggingCompat.createBlockState(this);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
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
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        if (entityIn.isSneaking()) {
            super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
            return;
        }
        entityIn.fall(fallDistance, 0.0F);
    }

    @Override
    public void onLanded(World worldIn, Entity entityIn) {
        if (entityIn.isSneaking()) {
            super.onLanded(worldIn, entityIn);
            return;
        }
        bounceEntity(entityIn);
    }

    private void bounceEntity(Entity entityIn) {
        if (entityIn.motionY < 0.0D) {
            double weightOffset = entityIn instanceof EntityLivingBase ? 0.6D : 0.8D;
            entityIn.motionY = -entityIn.motionY * weightOffset;
        }
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
