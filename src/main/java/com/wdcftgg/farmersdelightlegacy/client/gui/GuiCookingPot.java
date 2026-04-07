package com.wdcftgg.farmersdelightlegacy.client.gui;

import com.wdcftgg.farmersdelight.Tags;
import com.wdcftgg.farmersdelightlegacy.common.inventory.ContainerCookingPot;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCookingPot;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiCookingPot extends GuiContainer {

    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/gui/cooking_pot.png");
    private static final int HEAT_X = 47;
    private static final int HEAT_Y = 55;
    private static final int HEAT_W = 17;
    private static final int HEAT_H = 15;
    private static final int ARROW_X = 89;
    private static final int ARROW_Y = 25;
    private static final int ARROW_W = 24;
    private static final int ARROW_H = 17;

    private final InventoryPlayer playerInventory;
    private final TileEntityCookingPot tileEntityCookingPot;

    public GuiCookingPot(InventoryPlayer playerInventory, TileEntityCookingPot tileEntityCookingPot) {
        super(new ContainerCookingPot(playerInventory, tileEntityCookingPot));
        this.playerInventory = playerInventory;
        this.tileEntityCookingPot = tileEntityCookingPot;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(this.tileEntityCookingPot.getDisplayName().getUnformattedText(), 28, 6, 4210752);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, 72, 4210752);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);

        if (this.tileEntityCookingPot.isHeated()) {
            this.drawTexturedModalRect(this.guiLeft + HEAT_X, this.guiTop + HEAT_Y, 176, 0, HEAT_W, HEAT_H);
        }

        int progress = this.tileEntityCookingPot.getCookProgressionScaled(ARROW_W);
        this.drawTexturedModalRect(this.guiLeft + ARROW_X, this.guiTop + ARROW_Y, 176, 15, progress + 1, ARROW_H);
    }
}

