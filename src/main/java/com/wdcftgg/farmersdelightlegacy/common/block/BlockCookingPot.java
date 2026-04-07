package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.gui.ModGuiHandler;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModSounds;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCookingPot;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockCookingPot extends BlockContainer {

    private static final AxisAlignedBB POT_SHAPE = new AxisAlignedBB(2.0D / 16.0D, 0.0D, 2.0D / 16.0D, 14.0D / 16.0D, 10.0D / 16.0D, 14.0D / 16.0D);

    public BlockCookingPot() {
        super(Material.IRON);
        this.setHardness(2.0F);
        this.setResistance(4.0F);
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
                worldIn.playSound(null, pos, net.minecraft.init.SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return true;
            }
        }

        playerIn.openGui(FarmersDelightLegacy.getInstance(), ModGuiHandler.COOKING_POT_GUI_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntityCookingPot) {
            InventoryHelper.dropInventoryItems(worldIn, pos, (TileEntityCookingPot) tileEntity);
        }
        super.breakBlock(worldIn, pos, state);
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
        return POT_SHAPE;
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
                    pos.getY(),
                    pos.getZ() + 0.5D,
                    cookingPot.hasCookedMeal() ? ModSounds.COOKING_POT_BOIL_SOUP : ModSounds.COOKING_POT_BOIL,
                    SoundCategory.BLOCKS,
                    0.5F,
                    rand.nextFloat() * 0.2F + 0.9F,
                    false
            );
        }
    }
}

