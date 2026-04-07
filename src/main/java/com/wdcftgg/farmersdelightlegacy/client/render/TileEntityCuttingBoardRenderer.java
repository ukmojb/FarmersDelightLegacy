package com.wdcftgg.farmersdelightlegacy.client.render;

import com.wdcftgg.farmersdelightlegacy.common.block.BlockCuttingBoard;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCuttingBoard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.EnumFacing;

public class TileEntityCuttingBoardRenderer extends TileEntitySpecialRenderer<TileEntityCuttingBoard> {

    @Override
    public void render(TileEntityCuttingBoard te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        ItemStack stack = te.getStoredItem();
        if (stack.isEmpty()) {
            return;
        }

        EnumFacing facing = EnumFacing.NORTH;
        if (te.getWorld().getBlockState(te.getPos()).getBlock() instanceof BlockCuttingBoard) {
            facing = te.getWorld().getBlockState(te.getPos()).getValue(BlockCuttingBoard.FACING);
        }

        IBakedModel bakedModel = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, te.getWorld(), null);
        boolean isBlockItem = stack.getItem() instanceof ItemBlock;
        boolean isGui3d = bakedModel != null && bakedModel.isGui3d();

        GlStateManager.pushMatrix();
        if (te.isItemCarvingBoard()) {
            renderCarvedItemPose(x, y, z, facing, stack);
        } else if (isBlockItem && isGui3d) {
            renderBlockPose(x, y, z, facing);
        } else {
            renderLayingItemPose(x, y, z, facing);
        }
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
        GlStateManager.popMatrix();
    }

    private void renderLayingItemPose(double x, double y, double z, EnumFacing facing) {
        GlStateManager.translate(x + 0.5D, y + 0.08D, z + 0.5D);
        GlStateManager.rotate(-facing.getOpposite().getHorizontalAngle(), 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.6F, 0.6F, 0.6F);
    }

    private void renderBlockPose(double x, double y, double z, EnumFacing facing) {
        GlStateManager.translate(x + 0.5D, y + 0.27D, z + 0.5D);
        GlStateManager.rotate(-facing.getOpposite().getHorizontalAngle(), 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(0.8F, 0.8F, 0.8F);
    }

    private void renderCarvedItemPose(double x, double y, double z, EnumFacing facing, ItemStack stack) {
        GlStateManager.translate(x + 0.5D, y + 0.23D, z + 0.5D);
        GlStateManager.rotate(-facing.getOpposite().getHorizontalAngle() + 180.0F, 0.0F, 1.0F, 0.0F);

        float poseAngle;
        if (stack.getItem() instanceof ItemTool) {
            poseAngle = 225.0F;
        } else if (stack.getItem() instanceof ItemSword) {
            poseAngle = 180.0F;
        } else {
            poseAngle = 180.0F;
        }
        GlStateManager.rotate(poseAngle, 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(0.6F, 0.6F, 0.6F);
    }
}

