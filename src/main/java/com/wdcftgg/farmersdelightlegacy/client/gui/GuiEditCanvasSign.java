package com.wdcftgg.farmersdelightlegacy.client.gui;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.client.render.ModelCanvasHangingSign;
import com.wdcftgg.farmersdelightlegacy.client.render.TileEntityCanvasSignRenderer;
import com.wdcftgg.farmersdelightlegacy.common.network.ModNetworkHandler;
import com.wdcftgg.farmersdelightlegacy.common.network.PacketUpdateCanvasSign;
import com.wdcftgg.farmersdelightlegacy.common.block.sign.BlockCanvasHangingSign;
import com.wdcftgg.farmersdelightlegacy.common.block.sign.BlockCanvasStandingSign;
import com.wdcftgg.farmersdelightlegacy.common.block.sign.BlockCanvasWallHangingSign;
import com.wdcftgg.farmersdelightlegacy.common.block.sign.BlockCanvasWallSign;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCanvasSign;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import java.util.Arrays;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiEditCanvasSign extends GuiScreen {

    private static final int MAX_LINES = 4;
    private static final int MAX_LINE_WIDTH = 90;
    private static final int MAX_LINE_WIDTH_HANGING = 60;
    private static final int TEXT_LINE_SPACING = 9;
    private static final int TEXT_PADDING = 10;
    private static final int SIGN_CENTER_Y = 87;
    private static final int HANGING_SIGN_CENTER_Y = 125;
    private static final float SIGN_MODEL_SCALE = (MAX_LINE_WIDTH + TEXT_PADDING * 2) / 1.5F;
    private static final float HANGING_SIGN_MODEL_SCALE = (MAX_LINE_WIDTH_HANGING + TEXT_PADDING * 2) / 0.875F;
    private final TileEntityCanvasSign tileSign;
    private final boolean front;
    private final String[] lines;
    private final ModelSign signModel = new ModelSign();
    private final ModelCanvasHangingSign hangingSignModel = new ModelCanvasHangingSign();
    private int updateCounter;
    private int editLine;
    private GuiButton doneButton;

    public GuiEditCanvasSign(TileEntityCanvasSign tileSign) {
        this(tileSign, true);
    }

    public GuiEditCanvasSign(TileEntityCanvasSign tileSign, boolean front) {
        this.tileSign = tileSign;
        this.front = front;
        this.lines = Arrays.stream(tileSign.getText(front)).map(net.minecraft.util.text.ITextComponent::getUnformattedText).toArray(String[]::new);
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        this.doneButton = this.addButton(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120, I18n.format("gui.done")));
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        if (this.mc.getConnection() != null) {
            ModNetworkHandler.INSTANCE.sendToServer(new PacketUpdateCanvasSign(this.tileSign.getPos(), front, lines));
        }
    }

    @Override
    public void updateScreen() {
        ++this.updateCounter;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.enabled && button.id == 0) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 200) {
            this.editLine = this.editLine - 1 & 3;
        }
        if (keyCode == 208 || keyCode == 28 || keyCode == 156) {
            this.editLine = this.editLine + 1 & 3;
        }

        String lineText = lines[this.editLine];
        if (keyCode == 14 && !lineText.isEmpty()) {
            lineText = lineText.substring(0, lineText.length() - 1);
        }
        if (ChatAllowedCharacters.isAllowedCharacter(typedChar) && this.fontRenderer.getStringWidth(lineText + typedChar) <= (isHangingSign() ? MAX_LINE_WIDTH_HANGING : MAX_LINE_WIDTH)) {
            lineText = lineText + typedChar;
        }
        lines[this.editLine] = lineText;

        if (keyCode == 1) {
            this.actionPerformed(this.doneButton);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, I18n.format(this.isHangingSign() ? "hanging_sign.edit" : "sign.edit"), this.width / 2, 40, 16777215);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        if (this.isHangingSign()) {
            this.drawHangingCanvasSign();
        } else {
            this.drawCanvasSign();
        }
        GlStateManager.popMatrix();
        this.drawEditorTextOverlay();
        this.prepareGuiControlRenderState();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void prepareGuiControlRenderState() {
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawCanvasSign() {
        GlStateManager.translate(this.width / 2.0F, SIGN_CENTER_Y + SIGN_MODEL_SCALE / 2.0F, 50.0F);
        GlStateManager.scale(SIGN_MODEL_SCALE, -SIGN_MODEL_SCALE, SIGN_MODEL_SCALE);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        if (!front) {
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        }
        this.mc.getTextureManager().bindTexture(this.getTextureLocation());
        this.signModel.signStick.showModel = this.tileSign.getBlockType() instanceof BlockCanvasStandingSign;
        this.signModel.renderSign();
    }

    private void drawHangingCanvasSign() {
        GlStateManager.translate(this.width / 2.0F, HANGING_SIGN_CENTER_Y, 50.0F);
        GlStateManager.scale(HANGING_SIGN_MODEL_SCALE, -HANGING_SIGN_MODEL_SCALE, HANGING_SIGN_MODEL_SCALE);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);

        if (!front) {
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        }
        GlStateManager.translate(0.0F, -0.3125F, 0.0F);
        this.mc.getTextureManager().bindTexture(this.getTextureLocation());
        this.hangingSignModel.renderWall(0.0625F);
    }

    private void drawEditorTextOverlay() {
        int centerX = this.width / 2;
        int centerY = this.isHangingSign() ? HANGING_SIGN_CENTER_Y : SIGN_CENTER_Y;
        int maxLineWidth = this.isHangingSign() ? MAX_LINE_WIDTH_HANGING : MAX_LINE_WIDTH;
        int startY = centerY - ((MAX_LINES - 1) * TEXT_LINE_SPACING + this.fontRenderer.FONT_HEIGHT) / 2;
        int textColor = this.getTextColor();
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        for (int line = 0; line < Math.min(MAX_LINES, lines.length); ++line) {
            String lineText = this.fontRenderer.trimStringToWidth(lines[line], maxLineWidth);
            int textWidth = this.fontRenderer.getStringWidth(lineText);
            int x = centerX - textWidth / 2;
            int y = startY + line * TEXT_LINE_SPACING;
            this.fontRenderer.drawString(lineText, x, y, textColor);
            if (line == this.editLine && this.updateCounter / 6 % 2 == 0) {
                int cursorX = x + textWidth + 1;
                drawRect(cursorX, y, cursorX + 1, y + this.fontRenderer.FONT_HEIGHT, 0xFF000000 | textColor);
            }
        }
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private boolean isHangingSign() {
        Block block = this.tileSign.getBlockType();
        return block instanceof BlockCanvasHangingSign || block instanceof BlockCanvasWallHangingSign;
    }

    private int getTextColor() {
        return TileEntityCanvasSignRenderer.getTextColor(tileSign.getColor(front));
    }

    private ResourceLocation getTextureLocation() {
        Block block = this.tileSign.getBlockType();
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
        return new ResourceLocation(FarmersDelightLegacy.MOD_ID, "textures/entity/signs/canvas.png");
    }

}
