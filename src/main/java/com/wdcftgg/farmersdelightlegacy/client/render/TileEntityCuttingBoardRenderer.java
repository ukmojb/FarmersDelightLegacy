package com.wdcftgg.farmersdelightlegacy.client.render;

import com.wdcftgg.farmersdelightlegacy.common.block.BlockCuttingBoard;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCuttingBoard;
import com.wdcftgg.farmersdelightlegacy.common.registry.ItemCategories;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

public class TileEntityCuttingBoardRenderer extends TileEntitySpecialRenderer<TileEntityCuttingBoard> {
    private final Random random = new Random();

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
        boolean isGui3d = bakedModel != null && bakedModel.isGui3d();

        random.setSeed(Item.getIdFromItem(stack.getItem()) + stack.getItemDamage());
        int modelCount = stack.getCount() > 1
                ? 1 + MathHelper.ceil(stack.getCount() / (float) stack.getMaxStackSize() * 4) : 1;
        for (int index = 0; index < modelCount; index++) {
            double offsetX = modelCount == 1 ? 0 : (random.nextFloat() * 2 - 1) * 0.075;
            double offsetZ = modelCount == 1 ? 0 : (random.nextFloat() * 2 - 1) * 0.075;
            double offsetY = 0.03 * (index + 1);
            GlStateManager.pushMatrix();
            if (te.isItemCarvingBoard()) {
                renderCarvedItemPose(x, y, z, facing, stack);
            } else if (isGui3d && !ItemCategories.contains("fdFlatOnCuttingBoard", stack)) {
                renderBlockPose(x + offsetX, y + offsetY, z + offsetZ, facing);
            } else {
                renderLayingItemPose(x + offsetX, y + offsetY, z + offsetZ, facing);
            }
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
            GlStateManager.popMatrix();
        }
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
        if (stack.getItem() instanceof ItemPickaxe || stack.getItem() instanceof ItemHoe) {
            poseAngle = 225.0F;
        } else if (ItemCategories.contains("fdTrident", stack)) {
            poseAngle = 135.0F;
        } else {
            poseAngle = 180.0F;
        }
        GlStateManager.rotate(poseAngle, 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(0.6F, 0.6F, 0.6F);
    }
}

