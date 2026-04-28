package com.wdcftgg.farmersdelightlegacy.client.render;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockCanvasHangingSign;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockCanvasStandingSign;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockCanvasWallHangingSign;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockCanvasWallSign;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCanvasSign;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

public class TileEntityCanvasSignRenderer extends TileEntitySpecialRenderer<TileEntityCanvasSign> {

    private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(FarmersDelightLegacy.MOD_ID, "textures/entity/signs/canvas.png");
    private static final float HANGING_TEXT_SCALE = 0.0125F;
    private static final float STANDING_TEXT_SCALE = 0.010416667F;
    private final ModelSign signModel = new ModelSign();
    private final ModelCanvasHangingSign hangingSignModel = new ModelCanvasHangingSign();

    @Override
    public void render(TileEntityCanvasSign te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        IBlockState state = te.hasWorld() ? te.getWorld().getBlockState(te.getPos()) : null;
        Block block = state != null ? state.getBlock() : null;
        boolean hangingSign = block instanceof BlockCanvasHangingSign;
        boolean wallHangingSign = block instanceof BlockCanvasWallHangingSign;
        boolean anyHangingSign = hangingSign || wallHangingSign;

        GlStateManager.pushMatrix();
        float rotation = 0.0F;
        if (block instanceof BlockCanvasStandingSign || state == null) {
            if (state != null) {
                rotation = -state.getValue(BlockStandingSign.ROTATION) * 360.0F / 16.0F;
            }
            signModel.signStick.showModel = true;
            GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
        } else if (hangingSign) {
            rotation = -state.getValue(BlockCanvasHangingSign.ROTATION) * 360.0F / 16.0F;
            GlStateManager.translate(x + 0.5D, y + 0.9375D, z + 0.5D);
            GlStateManager.translate(0.0F, -0.3125F, 0.0F);
        } else if (wallHangingSign) {
            EnumFacing facing = state.getValue(BlockCanvasWallHangingSign.FACING);
            EnumFacing attachedFace = facing.getOpposite();
            rotation = -attachedFace.getHorizontalAngle() - 90.0F;
            GlStateManager.translate(x + 0.5D, y + 0.9375D, z + 0.5D);
            GlStateManager.translate(0.0F, -0.3125F, 0.0F);
        } else {
            if (block instanceof BlockCanvasWallSign) {
                switch (state.getValue(BlockWallSign.FACING)) {
                    case NORTH:
                        rotation = 180.0F;
                        GlStateManager.translate(0, 0, (float) 14 / 16);
                        break;
                    case WEST:
                        rotation = -90.0F;
                        GlStateManager.translate((float) 7 / 16, 0, (float) 7 / 16);
                        break;
                    case EAST:
                        rotation = 90.0F;
                        GlStateManager.translate((float) -7 / 16, 0, (float) 7 / 16);
                        break;
                    default:
                        rotation = 0.0F;
                        break;
                }
            }
            signModel.signStick.showModel = false;
            GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
            GlStateManager.translate(0.0F, -0.3125F, -0.4375F);
        }

        GlStateManager.rotate(rotation, 0.0F, 1.0F, 0.0F);
        bindTexture(resolveTexture(block));

        GlStateManager.pushMatrix();
        if (anyHangingSign) {
            GlStateManager.scale(1.0F, -1.0F, -1.0F);
            if (wallHangingSign) {
                hangingSignModel.renderWall(0.0625F);
            } else {
                hangingSignModel.renderCeiling(false, 0.0625F);
            }
        } else {
            GlStateManager.scale(0.6666667F, -0.6666667F, -0.6666667F);
            signModel.renderSign();
        }
        GlStateManager.popMatrix();

        renderSignText(te, anyHangingSign, block);
        GlStateManager.popMatrix();
    }

    private ResourceLocation resolveTexture(Block block) {
        if (block instanceof BlockCanvasStandingSign) {
            return ((BlockCanvasStandingSign) block).getTextureLocation();
        }
        if (block instanceof BlockCanvasWallSign) {
            return ((BlockCanvasWallSign) block).getTextureLocation();
        }
        if (block instanceof BlockCanvasHangingSign) {
            return ((BlockCanvasHangingSign) block).getTextureLocation();
        }
        if (block instanceof BlockCanvasWallHangingSign) {
            return ((BlockCanvasWallHangingSign) block).getTextureLocation();
        }
        return DEFAULT_TEXTURE;
    }

    private void renderSignText(TileEntityCanvasSign te, boolean hangingSign, Block block) {
        FontRenderer fontRenderer = this.getFontRenderer();
        if (hangingSign && te.isHangingTextOnBack()) {
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        }
        if (hangingSign) {
            GlStateManager.translate(0.0F, -0.32F, 0.073F);
            GlStateManager.scale(HANGING_TEXT_SCALE, -HANGING_TEXT_SCALE, HANGING_TEXT_SCALE);
            GlStateManager.glNormal3f(0.0F, 0.0F, -HANGING_TEXT_SCALE);
        } else {
            GlStateManager.translate(0.0F, 0.33333334F, 0.046666667F);
            GlStateManager.scale(STANDING_TEXT_SCALE, -STANDING_TEXT_SCALE, STANDING_TEXT_SCALE);
            GlStateManager.glNormal3f(0.0F, 0.0F, -STANDING_TEXT_SCALE);
        }
        GlStateManager.depthMask(false);

        for (int line = 0; line < te.signText.length; ++line) {
            ITextComponent textComponent = te.signText[line];
            String lineText = textComponent == null ? "" : textComponent.getUnformattedText();
            if (line == te.lineBeingEdited) {
                lineText = "> " + lineText + " <";
            }
            int centeredX = -fontRenderer.getStringWidth(lineText) / 2;
            fontRenderer.drawString(lineText, centeredX, line * 10 - te.signText.length * 5, getTextColor(block));
        }

        GlStateManager.depthMask(true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glNormal3f(0.0F, 0.0F, -1.0F);
    }

    private int getTextColor(Block block) {
        return Configuration.isCanvasSignDarkBackground(getCanvasColorName(block)) ? 0xFFFFFF : 0;
    }

    private String getCanvasColorName(Block block) {
        if (block == null || block.getRegistryName() == null) {
            return "";
        }

        String path = block.getRegistryName().getPath();
        String[] baseNames = new String[]{"canvas_sign", "canvas_wall_sign", "hanging_canvas_sign", "wall_hanging_canvas_sign"};
        for (String baseName : baseNames) {
            if (baseName.equals(path)) {
                return "";
            }
        }

        String[] suffixes = new String[]{"_canvas_sign", "_canvas_wall_sign", "_hanging_canvas_sign", "_wall_hanging_canvas_sign"};
        for (String suffix : suffixes) {
            if (path.endsWith(suffix)) {
                return path.substring(0, path.length() - suffix.length());
            }
        }
        return "";
    }
}
