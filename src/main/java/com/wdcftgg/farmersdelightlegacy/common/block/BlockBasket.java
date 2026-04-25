package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.gui.ModGuiHandler;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityBasket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class BlockBasket extends BlockDirectional implements ITileEntityProvider {

    public static final PropertyBool ENABLED = PropertyBool.create("enabled");
    private static final AxisAlignedBB RENDER_SHAPE = new AxisAlignedBB(0.0625D, 0.0625D, 0.0625D, 0.9375D, 0.9375D, 0.9375D);
    private static final Map<EnumFacing, AxisAlignedBB[]> COLLISION_SHAPE_BY_FACING = new EnumMap<>(EnumFacing.class);

    static {
        COLLISION_SHAPE_BY_FACING.put(EnumFacing.UP, new AxisAlignedBB[]{
                new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D),
                new AxisAlignedBB(0.0D, 0.125D, 0.0D, 0.125D, 1.0D, 1.0D),
                new AxisAlignedBB(0.875D, 0.125D, 0.0D, 1.0D, 1.0D, 1.0D),
                new AxisAlignedBB(0.125D, 0.125D, 0.0D, 0.875D, 1.0D, 0.125D),
                new AxisAlignedBB(0.125D, 0.125D, 0.875D, 0.875D, 1.0D, 1.0D)
        });
        COLLISION_SHAPE_BY_FACING.put(EnumFacing.DOWN, new AxisAlignedBB[]{
                new AxisAlignedBB(0.0D, 0.875D, 0.0D, 1.0D, 1.0D, 1.0D),
                new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.125D, 0.875D, 1.0D),
                new AxisAlignedBB(0.875D, 0.0D, 0.0D, 1.0D, 0.875D, 1.0D),
                new AxisAlignedBB(0.125D, 0.0D, 0.0D, 0.875D, 0.875D, 0.125D),
                new AxisAlignedBB(0.125D, 0.0D, 0.875D, 0.875D, 0.875D, 1.0D)
        });
        COLLISION_SHAPE_BY_FACING.put(EnumFacing.NORTH, new AxisAlignedBB[]{
                new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D),
                new AxisAlignedBB(0.0D, 0.875D, 0.0D, 1.0D, 1.0D, 1.0D),
                new AxisAlignedBB(0.0D, 0.125D, 0.0D, 0.125D, 0.875D, 1.0D),
                new AxisAlignedBB(0.875D, 0.125D, 0.0D, 1.0D, 0.875D, 1.0D),
                new AxisAlignedBB(0.125D, 0.125D, 0.875D, 0.875D, 0.875D, 1.0D)
        });
        COLLISION_SHAPE_BY_FACING.put(EnumFacing.SOUTH, new AxisAlignedBB[]{
                new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D),
                new AxisAlignedBB(0.0D, 0.875D, 0.0D, 1.0D, 1.0D, 1.0D),
                new AxisAlignedBB(0.0D, 0.125D, 0.0D, 0.125D, 0.875D, 1.0D),
                new AxisAlignedBB(0.875D, 0.125D, 0.0D, 1.0D, 0.875D, 1.0D),
                new AxisAlignedBB(0.125D, 0.125D, 0.0D, 0.875D, 0.875D, 0.125D)
        });
        COLLISION_SHAPE_BY_FACING.put(EnumFacing.WEST, new AxisAlignedBB[]{
                new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D),
                new AxisAlignedBB(0.0D, 0.875D, 0.0D, 1.0D, 1.0D, 1.0D),
                new AxisAlignedBB(0.0D, 0.125D, 0.0D, 1.0D, 0.875D, 0.125D),
                new AxisAlignedBB(0.0D, 0.125D, 0.875D, 1.0D, 0.875D, 1.0D),
                new AxisAlignedBB(0.875D, 0.125D, 0.125D, 1.0D, 0.875D, 0.875D)
        });
        COLLISION_SHAPE_BY_FACING.put(EnumFacing.EAST, new AxisAlignedBB[]{
                new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D),
                new AxisAlignedBB(0.0D, 0.875D, 0.0D, 1.0D, 1.0D, 1.0D),
                new AxisAlignedBB(0.0D, 0.125D, 0.0D, 1.0D, 0.875D, 0.125D),
                new AxisAlignedBB(0.0D, 0.125D, 0.875D, 1.0D, 0.875D, 1.0D),
                new AxisAlignedBB(0.0D, 0.125D, 0.125D, 0.125D, 0.875D, 0.875D)
        });
    }

    public BlockBasket() {
        super(Material.WOOD);
        this.setHardness(1.5F);
        this.setResistance(1.5F);
        this.setSoundType(SoundType.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.UP).withProperty(ENABLED, true));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityBasket();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return RENDER_SHAPE;
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
        return FULL_BLOCK_AABB.offset(pos);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState) {
        AxisAlignedBB[] collisionBoxes = COLLISION_SHAPE_BY_FACING.get(state.getValue(FACING));
        if (collisionBoxes == null) {
            return;
        }
        for (AxisAlignedBB collisionBox : collisionBoxes) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, collisionBox);
        }
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        return rayTrace(pos, start, end, FULL_BLOCK_AABB);
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
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        EnumFacing placedFacing = EnumFacing.getDirectionFromEntityLiving(pos, placer);
        return this.getDefaultState().withProperty(FACING, placedFacing).withProperty(ENABLED, !worldIn.isBlockPowered(pos));
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        boolean enabled = !worldIn.isBlockPowered(pos);
        updateEnabledState(worldIn, pos, state, enabled);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (stack.hasDisplayName()) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity instanceof TileEntityBasket) {
                ((TileEntityBasket) tileEntity).setCustomName(stack.getDisplayName());
            }
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return true;
        }

        playerIn.openGui(FarmersDelightLegacy.INSTANCE, ModGuiHandler.BASKET_GUI_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(FACING).getIndex();
        if (!state.getValue(ENABLED)) {
            meta |= 8;
        }
        return meta;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byIndex(meta & 7);
        if (facing == null) {
            facing = EnumFacing.UP;
        }
        boolean enabled = (meta & 8) == 0;
        return this.getDefaultState().withProperty(FACING, facing).withProperty(ENABLED, enabled);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, ENABLED);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rotation) {
        return state.withProperty(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        boolean enabled = !worldIn.isBlockPowered(pos);
        updateEnabledState(worldIn, pos, state, enabled);
    }

    private void updateEnabledState(World worldIn, BlockPos pos, IBlockState state, boolean enabled) {
        if (enabled == state.getValue(ENABLED)) {
            return;
        }

        TileEntity previousTileEntity = worldIn.getTileEntity(pos);
        NBTTagCompound previousData = null;
        if (previousTileEntity != null) {
            previousData = previousTileEntity.writeToNBT(new NBTTagCompound());
        }

        worldIn.setBlockState(pos, state.withProperty(ENABLED, enabled), 2);

        if (previousData == null) {
            return;
        }

        TileEntity currentTileEntity = worldIn.getTileEntity(pos);
        if (currentTileEntity != null && currentTileEntity != previousTileEntity) {
            currentTileEntity.readFromNBT(previousData);
            currentTileEntity.markDirty();
        }
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntityBasket) {
            ((TileEntityBasket) tileEntity).onEntityCollision(entityIn);
        }
        super.onEntityCollision(worldIn, pos, state, entityIn);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IInventory) {
            InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory) tileEntity);
            worldIn.updateComparatorOutputLevel(pos, this);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World worldIn, BlockPos pos) {
        return net.minecraft.inventory.Container.calcRedstone(worldIn.getTileEntity(pos));
    }
}

