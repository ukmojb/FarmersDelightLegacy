package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.common.registry.ModSounds;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntitySkillet;
import com.wdcftgg.farmersdelightlegacy.common.util.HeatSourceHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockSkillet extends BlockHorizontal implements ITileEntityProvider {

    public static final int MINIMUM_COOKING_TIME = 60;
    public static final PropertyBool SUPPORT = PropertyBool.create("support");

    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(1.0D / 16.0D, 0.0D, 1.0D / 16.0D, 15.0D / 16.0D, 4.0D / 16.0D, 15.0D / 16.0D);
    private static final AxisAlignedBB SHAPE_WITH_TRAY = new AxisAlignedBB(0.0D, -1.0D / 16.0D, 0.0D, 1.0D, 4.0D / 16.0D, 1.0D);

    public BlockSkillet() {
        super(Material.IRON);
        this.setHardness(2.0F);
        this.setResistance(4.0F);
        this.setSoundType(SoundType.METAL);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(SUPPORT, false));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntitySkillet();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return state.getValue(SUPPORT) ? SHAPE_WITH_TRAY : SHAPE;
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
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, net.minecraft.entity.EntityLivingBase placer) {
        return this.getDefaultState()
                .withProperty(FACING, placer.getHorizontalFacing())
                .withProperty(SUPPORT, getTrayState(worldIn, pos));
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (fromPos.getY() == pos.getY() - 1 || fromPos.getY() == pos.getY() + 1) {
            worldIn.setBlockState(pos, state.withProperty(SUPPORT, getTrayState(worldIn, pos)), 2);
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (!(tileEntity instanceof TileEntitySkillet)) {
            return false;
        }

        TileEntitySkillet skillet = (TileEntitySkillet) tileEntity;
        ItemStack heldStack = playerIn.getHeldItem(hand);
        if (!worldIn.isRemote) {
            if (heldStack.isEmpty()) {
                ItemStack extracted = skillet.removeItem();
                if (!extracted.isEmpty()) {
                    if (!playerIn.inventory.addItemStackToInventory(extracted)) {
                        playerIn.dropItem(extracted, false);
                    }
                }
            } else {
                ItemStack remainder = skillet.addItemToCook(heldStack, playerIn);
                if (remainder.getCount() != heldStack.getCount()) {
                    if (!playerIn.capabilities.isCreativeMode) {
                        playerIn.setHeldItem(hand, remainder);
                    }
                    worldIn.playSound(null, pos, net.minecraft.init.SoundEvents.BLOCK_METAL_PLACE, SoundCategory.BLOCKS, 0.7F, 1.0F);
                }
            }
        }
        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntitySkillet) {
            ItemStack stored = ((TileEntitySkillet) tileEntity).getStoredStack();
            if (!stored.isEmpty()) {
                net.minecraft.inventory.InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stored);
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntitySkillet) {
            ItemStack skilletStack = ((TileEntitySkillet) tileEntity).getSkilletItem();
            if (!skilletStack.isEmpty()) {
                drops.add(skilletStack);
                return;
            }
        }
        drops.add(new ItemStack(this));
    }

    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (!(tileEntity instanceof TileEntitySkillet)) {
            return;
        }

        TileEntitySkillet skillet = (TileEntitySkillet) tileEntity;
        if (!skillet.isCooking()) {
            return;
        }

        double x = pos.getX() + 0.5D;
        double y = pos.getY();
        double z = pos.getZ() + 0.5D;
        if (rand.nextInt(10) == 0) {
            worldIn.playSound(x, y, z, ModSounds.SKILLET_SIZZLE, SoundCategory.BLOCKS, 0.4F, rand.nextFloat() * 0.2F + 0.9F, false);
        }
        if (rand.nextFloat() < 0.2F) {
            worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                    x + (rand.nextDouble() * 0.4D - 0.2D),
                    pos.getY() + 0.1D,
                    z + (rand.nextDouble() * 0.4D - 0.2D),
                    0.0D, rand.nextBoolean() ? 0.015D : 0.005D, 0.0D);
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(FACING).getHorizontalIndex();
        if (state.getValue(SUPPORT)) {
            meta |= 4;
        }
        return meta;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3))
                .withProperty(SUPPORT, (meta & 4) != 0);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, SUPPORT);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    private boolean getTrayState(World world, BlockPos pos) {
        return HeatSourceHelper.isDirectHeatSource(world, pos.down());
    }

    public static int getSkilletCookingTime(int originalCookingTime, int fireAspectLevel) {
        int cookingTime = originalCookingTime > 0 ? originalCookingTime : 600;
        int cookingSeconds = cookingTime / 20;
        float cookingTimeReduction = 0.2F - (fireAspectLevel * 0.05F);
        int result = (int) (cookingSeconds * cookingTimeReduction) * 20;
        return Math.max(MINIMUM_COOKING_TIME, Math.min(result, originalCookingTime));
    }
}
