package com.wdcftgg.farmersdelightlegacy.client.render;

import com.wdcftgg.farmersdelightlegacy.common.block.BlockSkillet;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntitySkillet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import java.util.Random;

public class TileEntitySkilletRenderer extends TileEntitySpecialRenderer<TileEntitySkillet> {

    private final Random random = new Random();

    @Override
    public void render(TileEntitySkillet te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        ItemStack stack = te.getStoredStack();
        if (stack.isEmpty()) {
            return;
        }

        EnumFacing facing = EnumFacing.NORTH;
        if (te.getWorld().getBlockState(te.getPos()).getBlock() instanceof BlockSkillet) {
            facing = te.getWorld().getBlockState(te.getPos()).getValue(BlockSkillet.FACING);
        }

        int seed = Item.getIdFromItem(stack.getItem()) + stack.getMetadata();
        this.random.setSeed(stack.isEmpty() ? 187L : seed);

        int modelCount = getModelCount(stack);
        for (int i = 0; i < modelCount; i++) {
            GlStateManager.pushMatrix();
            float xOffset = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
            float zOffset = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
            GlStateManager.translate(x + 0.5D + xOffset, y + 0.1D + 0.03D * (i + 1), z + 0.5D + zOffset);
            GlStateManager.rotate(-facing.getHorizontalAngle(), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
            GlStateManager.popMatrix();
        }
    }

    private int getModelCount(ItemStack stack) {
        if (stack.getCount() > 48) {
            return 5;
        }
        if (stack.getCount() > 32) {
            return 4;
        }
        if (stack.getCount() > 16) {
            return 3;
        }
        if (stack.getCount() > 1) {
            return 2;
        }
        return 1;
    }
}

