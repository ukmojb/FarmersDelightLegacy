package com.wdcftgg.farmersdelightlegacy.common.item;

import com.wdcftgg.farmersdelightlegacy.common.block.sign.BlockCanvasHangingSign;
import com.wdcftgg.farmersdelightlegacy.common.block.sign.BlockCanvasWallHangingSign;
import com.wdcftgg.farmersdelightlegacy.common.block.sign.CanvasSignInteractionHelper;
import com.wdcftgg.farmersdelightlegacy.common.compat.FluidloggingCompat;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCanvasSign;
import net.minecraft.block.Block;
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

public class ItemCanvasHangingSign extends ItemBlock {

    private final Block wallBlock;

    public ItemCanvasHangingSign(Block hangingBlock, Block wallBlock) {
        super(hangingBlock);
        this.wallBlock = wallBlock;
        this.maxStackSize = 16;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (facing == EnumFacing.UP) {
            return EnumActionResult.FAIL;
        }

        IBlockState targetState = worldIn.getBlockState(pos);
        boolean canReplace = targetState.getBlock().isReplaceable(worldIn, pos);
        BlockPos placePos = canReplace ? pos : pos.offset(facing);

        if (!player.canPlayerEdit(placePos, facing, stack)) {
            return EnumActionResult.FAIL;
        }

        PlacementSelection placementSelection = selectPlacement(worldIn, placePos, facing, player);
        if (placementSelection == null) {
            return EnumActionResult.FAIL;
        }

        if (worldIn.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        if (!worldIn.mayPlace(placementSelection.block, placePos, false, facing, null)) {
            return EnumActionResult.FAIL;
        }

        if (!FluidloggingCompat.placeBlock(worldIn, placePos, placementSelection.state, 11)) {
            return EnumActionResult.FAIL;
        }
        TileEntity tileEntity = worldIn.getTileEntity(placePos);
        if (tileEntity instanceof TileEntityCanvasSign && !setTileEntityNBT(worldIn, player, placePos, stack)) {
            CanvasSignInteractionHelper.openEditor(worldIn, placePos, player);
        }

        SoundType soundType = placementSelection.state.getBlock().getSoundType(placementSelection.state, worldIn, placePos, player);
        worldIn.playSound(null, placePos, soundType.getPlaceSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        return EnumActionResult.SUCCESS;
    }

    private PlacementSelection selectPlacement(World worldIn, BlockPos placePos, EnumFacing clickedFace, EntityPlayer player) {
        if (clickedFace == EnumFacing.DOWN) {
            return createCeilingPlacement(player);
        }
        return createWallPlacement(worldIn, placePos, clickedFace, player);
    }

    private PlacementSelection createCeilingPlacement(EntityPlayer player) {
        if (!(this.block instanceof BlockCanvasHangingSign)) {
            return null;
        }

        int rotation = MathHelper.floor((player.rotationYaw + 180.0F) * 16.0F / 360.0F + 0.5D) & 15;
        IBlockState placedState = this.block.getDefaultState().withProperty(BlockCanvasHangingSign.ROTATION, rotation);
        return new PlacementSelection(this.block, placedState);
    }

    private PlacementSelection createWallPlacement(World worldIn, BlockPos placePos, EnumFacing clickedFace, EntityPlayer player) {
        if (!(wallBlock instanceof BlockCanvasWallHangingSign)) {
            return null;
        }

        if (!clickedFace.getAxis().isHorizontal()) {
            return null;
        }

        BlockCanvasWallHangingSign hangingWallBlock = (BlockCanvasWallHangingSign) wallBlock;
        EnumFacing placementFacing = hangingWallBlock.resolvePlacementFacing(worldIn, placePos, clickedFace,
                getNearestHorizontalDirections(player));
        if (placementFacing == null) {
            return null;
        }
        IBlockState placedState = hangingWallBlock.getDefaultState().withProperty(BlockCanvasWallHangingSign.FACING, placementFacing);
        return new PlacementSelection(hangingWallBlock, placedState);
    }

    private EnumFacing[] getNearestHorizontalDirections(EntityPlayer player) {
        EnumFacing playerFacing = player.getHorizontalFacing();
        return new EnumFacing[]{
                playerFacing,
                playerFacing.rotateY(),
                playerFacing.rotateYCCW(),
                playerFacing.getOpposite()
        };
    }

    private static class PlacementSelection {
        private final Block block;
        private final IBlockState state;

        private PlacementSelection(Block block, IBlockState state) {
            this.block = block;
            this.state = state;
        }
    }
}
