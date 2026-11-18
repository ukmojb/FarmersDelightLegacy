package com.wdcftgg.farmersdelightlegacy.client.render;

import com.wdcftgg.farmersdelightlegacy.common.item.ItemSkillet;
import com.wdcftgg.farmersdelightlegacy.common.item.SkilletFlip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public final class SkilletItemRenderer extends TileEntityItemStackRenderer {
    private final IBakedModel skillet;
    private ItemCameraTransforms.TransformType transform = ItemCameraTransforms.TransformType.NONE;

    public SkilletItemRenderer(IBakedModel skillet) {
        this.skillet = skillet;
    }

    public void setTransform(ItemCameraTransforms.TransformType transform) {
        this.transform = transform;
    }

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        Minecraft minecraft = Minecraft.getMinecraft();
        RenderItem renderer = minecraft.getRenderItem();
        float progress = minecraft.world == null ? 0
                : SkilletFlip.progress(stack, minecraft.world.getTotalWorldTime(), minecraft.getRenderPartialTicks());
        ItemStack ingredient = ItemSkillet.getCookingStackForRender(stack);
        if (!ingredient.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.5F, 0.1F + 0.4F * MathHelper.sin(progress * (float) Math.PI), 0.5F);
            GlStateManager.rotate(180 * (progress + (SkilletFlip.isFlipped(stack) ? 1 : 0)) + 90, 1, 0, 0);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            renderer.renderItem(ingredient, ItemCameraTransforms.TransformType.FIXED);
            GlStateManager.popMatrix();
        }
        GlStateManager.pushMatrix();
        if (transform == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND
                || transform == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND) {
            GlStateManager.translate(0.5F, 0.5F, 1.5F);
            GlStateManager.rotate(-MathHelper.sin(progress * (float) Math.PI * 2) * 15, 1, 0, 0);
            GlStateManager.translate(-0.5F, -0.5F, -1.5F - MathHelper.sin(progress * (float) Math.PI) * 0.2F);
        }
        minecraft.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        renderer.renderItem(stack, skillet);
        GlStateManager.popMatrix();
    }
}
