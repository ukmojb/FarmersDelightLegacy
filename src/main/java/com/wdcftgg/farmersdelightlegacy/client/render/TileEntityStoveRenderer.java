package com.wdcftgg.farmersdelightlegacy.client.render;

import com.wdcftgg.farmersdelightlegacy.common.block.BlockStove;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityStove;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class TileEntityStoveRenderer extends TileEntitySpecialRenderer<TileEntityStove> {

    private static final float[][] SLOT_OFFSETS = new float[][]{
            {0.3F, 0.2F},
            {0.0F, 0.2F},
            {-0.3F, 0.2F},
            {0.3F, -0.2F},
            {0.0F, -0.2F},
            {-0.3F, -0.2F}
    };

    @Override
    public void render(TileEntityStove te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        EnumFacing facing = EnumFacing.NORTH;
        if (te.getWorld().getBlockState(te.getPos()).getBlock() instanceof BlockStove) {
            facing = te.getWorld().getBlockState(te.getPos()).getValue(BlockStove.FACING).getOpposite();
        }

        for (int i = 0; i < 6; i++) {
            ItemStack stack = te.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5D, y + 1.02D, z + 0.5D);
            GlStateManager.rotate(-facing.getHorizontalAngle(), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            float[] baseOffset = SLOT_OFFSETS[i];
            GlStateManager.translate(baseOffset[0], baseOffset[1], 0.0D);
            GlStateManager.scale(0.375F, 0.375F, 0.375F);
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
            GlStateManager.popMatrix();
        }
    }
}

