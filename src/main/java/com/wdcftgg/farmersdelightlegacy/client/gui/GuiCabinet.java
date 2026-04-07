package com.wdcftgg.farmersdelightlegacy.client.gui;

import com.wdcftgg.farmersdelightlegacy.common.inventory.ContainerCabinet;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCabinet;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiCabinet extends GuiContainer {

    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");

    private final InventoryPlayer playerInventory;
    private final TileEntityCabinet tileEntityCabinet;

    public GuiCabinet(InventoryPlayer playerInventory, TileEntityCabinet tileEntityCabinet) {
        super(new ContainerCabinet(playerInventory, tileEntityCabinet));
        this.playerInventory = playerInventory;
        this.tileEntityCabinet = tileEntityCabinet;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(this.tileEntityCabinet.getDisplayName().getUnformattedText(), 8, 6, 4210752);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, 72, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
    }
}

