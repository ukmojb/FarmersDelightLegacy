package com.wdcftgg.farmersdelightlegacy.common.item;

import com.wdcftgg.farmersdelightlegacy.common.block.sign.CanvasSignInteractionHelper;
import com.wdcftgg.farmersdelightlegacy.common.compat.FluidloggingCompat;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCanvasSign;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ItemCanvasSign extends ItemBlock {

    private final Block wallBlock;

    public ItemCanvasSign(Block standingBlock, Block wallBlock) {
        super(standingBlock);
        this.wallBlock = wallBlock;
        this.maxStackSize = 16;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (facing == EnumFacing.DOWN) {
            return EnumActionResult.FAIL;
        }

        IBlockState targetState = worldIn.getBlockState(pos);
        boolean canReplace = targetState.getBlock().isReplaceable(worldIn, pos);
        BlockPos placePos = canReplace ? pos : pos.offset(facing);

        if (!player.canPlayerEdit(placePos, facing, stack)) {
            return EnumActionResult.FAIL;
        }

        IBlockState placedState;
        if (facing == EnumFacing.UP) {
            int rotation = MathHelper.floor((player.rotationYaw + 180.0F) * 16.0F / 360.0F + 0.5D) & 15;
            placedState = this.block.getDefaultState().withProperty(BlockStandingSign.ROTATION, rotation);
        } else {
            placedState = wallBlock.getDefaultState().withProperty(BlockWallSign.FACING, facing);
        }

        if (!worldIn.mayPlace(placedState.getBlock(), placePos, false, facing, player)) {
            return EnumActionResult.FAIL;
        }

        if (worldIn.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        if (!FluidloggingCompat.placeBlock(worldIn, placePos, placedState, 11)) {
            return EnumActionResult.FAIL;
        }
        TileEntity tileEntity = worldIn.getTileEntity(placePos);
        if (tileEntity instanceof TileEntityCanvasSign && !setTileEntityNBT(worldIn, player, placePos, stack)) {
            CanvasSignInteractionHelper.openEditor(worldIn, placePos, player);
        }

        SoundType soundType = placedState.getBlock().getSoundType(placedState, worldIn, placePos, player);
        worldIn.playSound(null, placePos, soundType.getPlaceSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        return EnumActionResult.SUCCESS;
    }
}

