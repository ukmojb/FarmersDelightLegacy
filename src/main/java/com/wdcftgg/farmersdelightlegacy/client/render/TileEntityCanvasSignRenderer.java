package com.wdcftgg.farmersdelightlegacy.client.render;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.block.sign.BlockCanvasHangingSign;
import com.wdcftgg.farmersdelightlegacy.common.block.sign.BlockCanvasStandingSign;
import com.wdcftgg.farmersdelightlegacy.common.block.sign.BlockCanvasWallHangingSign;
import com.wdcftgg.farmersdelightlegacy.common.block.sign.BlockCanvasWallSign;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCanvasSign;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

public class TileEntityCanvasSignRenderer extends TileEntitySpecialRenderer<TileEntityCanvasSign> {

    private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(FarmersDelightLegacy.MOD_ID, "textures/entity/signs/canvas.png");
    private static final float HANGING_TEXT_SCALE = 0.0125F;
    private static final float STANDING_TEXT_SCALE = 0.010416667F;
    private static final float OUTLINE_RADIUS = 1.0F;
    private static final float OUTLINE_SAMPLE_STEP = 0.5F;
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
            rotation = -attachedFace.getHorizontalAngle();
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
        if (destroyStage >= 0) {
            bindTexture(DESTROY_STAGES[destroyStage]);
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 2.0F, 1.0F);
            GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        } else {
            bindTexture(resolveTexture(block));
        }

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

        if (destroyStage >= 0) {
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        } else {
            renderSignText(te, anyHangingSign, true);
            renderSignText(te, anyHangingSign, false);
        }
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

    private void renderSignText(TileEntityCanvasSign te, boolean hangingSign, boolean front) {
        FontRenderer fontRenderer = this.getFontRenderer();
        GlStateManager.pushMatrix();
        if (!front) {
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

        ITextComponent[] text = te.getText(front);
        int baseColor = getTextColor(te.getColor(front));
        boolean glowing = te.isGlowing(front);
        int textColor = glowing ? baseColor : darken(baseColor, 0.6D);
        int outlineColor = baseColor == 0 ? 0xF0EBCC : darken(baseColor, 0.4D);
        net.minecraft.entity.Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
        boolean outline = glowing && (baseColor == 0 || camera != null && camera.getDistanceSq(te.getPos()) < 256.0D);
        float previousX = OpenGlHelper.lastBrightnessX;
        float previousY = OpenGlHelper.lastBrightnessY;
        GlStateManager.disableLighting();
        if (glowing) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        }
        for (int line = 0; line < text.length; ++line) {
            String lineText = fontRenderer.trimStringToWidth(text[line].getUnformattedText(), hangingSign ? 60 : 90);
            int centeredX = -fontRenderer.getStringWidth(lineText) / 2;
            int lineY = line * 9 - (hangingSign ? 18 : 17);
            if (outline) {
                renderTextOutline(fontRenderer, lineText, centeredX, lineY, outlineColor);
                GlStateManager.translate(0.0F, 0.0F, -0.01F);
            }
            fontRenderer.drawString(lineText, centeredX, lineY, textColor);
            if (outline) {
                GlStateManager.translate(0.0F, 0.0F, 0.01F);
            }
        }
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, previousX, previousY);
        GlStateManager.enableLighting();

        GlStateManager.depthMask(true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glNormal3f(0.0F, 0.0F, -1.0F);
        GlStateManager.popMatrix();
    }

    private static void renderTextOutline(FontRenderer font, String text, int x, int y, int color) {
        if (text.isEmpty()) {
            return;
        }
        for (float offsetX = -OUTLINE_RADIUS; offsetX <= OUTLINE_RADIUS; offsetX += OUTLINE_SAMPLE_STEP) {
            for (float offsetY = -OUTLINE_RADIUS; offsetY <= OUTLINE_RADIUS; offsetY += OUTLINE_SAMPLE_STEP) {
                if (offsetX != 0.0F || offsetY != 0.0F) {
                    font.drawString(text, x + offsetX, y + offsetY, color, false);
                }
            }
        }
    }

    public static int getTextColor(EnumDyeColor color) {
        switch (color) {
            case WHITE: return 0xFFFFFF;
            case ORANGE: return 0xFF681F;
            case MAGENTA: return 0xFF00FF;
            case LIGHT_BLUE: return 0x9AC0CD;
            case YELLOW: return 0xFFFF00;
            case LIME: return 0xBFFF00;
            case PINK: return 0xFF69B4;
            case GRAY: return 0x808080;
            case SILVER: return 0xD3D3D3;
            case CYAN: return 0x00FFFF;
            case PURPLE: return 0xA020F0;
            case BLUE: return 0x0000FF;
            case BROWN: return 0x8B4513;
            case GREEN: return 0x00FF00;
            case RED: return 0xFF0000;
            default: return 0;
        }
    }

    private static int darken(int color, double brightness) {
        return (int) ((color >> 16 & 255) * brightness) << 16
                | (int) ((color >> 8 & 255) * brightness) << 8
                | (int) ((color & 255) * brightness);
    }
}
