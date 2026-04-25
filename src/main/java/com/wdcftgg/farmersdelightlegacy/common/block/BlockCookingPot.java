package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.gui.ModGuiHandler;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModSounds;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCookingPot;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class BlockCookingPot extends BlockContainer {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyBool SUPPORT = PropertyBool.create("support");
    private static final AxisAlignedBB POT_SHAPE = new AxisAlignedBB(2.0D / 16.0D, 0.0D, 2.0D / 16.0D, 14.0D / 16.0D, 10.0D / 16.0D, 14.0D / 16.0D);
    private static final AxisAlignedBB POT_SHAPE_WITH_TRAY = new AxisAlignedBB(0.0D, -1.0D / 16.0D, 0.0D, 1.0D, 10.0D / 16.0D, 1.0D);
    private static final Set<Long> CREATIVE_EMPTY_BREAKS = new HashSet<>();

    public BlockCookingPot() {
        super(Material.IRON);
        this.setHardness(2.0F);
        this.setResistance(4.0F);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(SUPPORT, false));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityCookingPot();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return true;
        }

        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntityCookingPot) {
            TileEntityCookingPot cookingPot = (TileEntityCookingPot) tileEntity;
            ItemStack servingStack = cookingPot.useHeldItemOnMeal(playerIn.getHeldItem(hand));
            if (!servingStack.isEmpty()) {
                if (!playerIn.inventory.addItemStackToInventory(servingStack)) {
                    playerIn.dropItem(servingStack, false);
                }
                cookingPot.awardExperience(playerIn, 1);
                worldIn.playSound(null, pos, net.minecraft.init.SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return true;
            }
        }

        playerIn.openGui(FarmersDelightLegacy.INSTANCE, ModGuiHandler.COOKING_POT_GUI_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        boolean suppressPotDrop = CREATIVE_EMPTY_BREAKS.remove(pos.toLong());
        if (!worldIn.isRemote && worldIn.getGameRules().getBoolean("doTileDrops")) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity instanceof TileEntityCookingPot) {
                TileEntityCookingPot cookingPot = (TileEntityCookingPot) tileEntity;
                if (!suppressPotDrop) {
                    ItemStack potDrop = new ItemStack(Item.getItemFromBlock(this));
                    ItemStack meal = cookingPot.getStoredMealStack();
                    TileEntityCookingPot.writeMealToItem(potDrop, meal, cookingPot.getContainer(), cookingPot.isUsingDefaultMealContainer());
                    InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), potDrop);
                }

                for (ItemStack stack : cookingPot.getDroppableInventoryWithoutMeal()) {
                    InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
                }
            } else if (!suppressPotDrop) {
                InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Item.getItemFromBlock(this)));
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return POT_SHAPE;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return state.getValue(SUPPORT) ? POT_SHAPE_WITH_TRAY : POT_SHAPE;
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState()
                .withProperty(FACING, placer.getHorizontalFacing().getOpposite())
                .withProperty(SUPPORT, false);
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (!worldIn.isRemote && player.capabilities.isCreativeMode) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity instanceof TileEntityCookingPot && ((TileEntityCookingPot) tileEntity).isCompletelyEmpty()) {
                CREATIVE_EMPTY_BREAKS.add(pos.toLong());
            }
        }
        super.onBlockHarvested(worldIn, pos, state, player);
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
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (!(tileEntity instanceof TileEntityCookingPot)) {
            return;
        }

        TileEntityCookingPot cookingPot = (TileEntityCookingPot) tileEntity;
        if (!cookingPot.isHeated()) {
            return;
        }

        if (rand.nextInt(10) == 0) {
            worldIn.playSound(
                    pos.getX() + 0.5D,
                    pos.getY() + 1,
                    pos.getZ() + 0.5D,
                    cookingPot.hasCookedMeal() ? ModSounds.COOKING_POT_BOIL_SOUP : ModSounds.COOKING_POT_BOIL,
                    SoundCategory.BLOCKS,
                    0.5F,
                    rand.nextFloat() * 0.2F + 0.9F,
                    false
            );
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{FACING, SUPPORT});
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byHorizontalIndex((meta >> 1) & 3);
        return this.getDefaultState()
                .withProperty(FACING, facing)
                .withProperty(SUPPORT, (meta & 1) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(FACING).getHorizontalIndex() << 1;
        if (state.getValue(SUPPORT)) {
            meta |= 1;
        }
        return meta;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

}

