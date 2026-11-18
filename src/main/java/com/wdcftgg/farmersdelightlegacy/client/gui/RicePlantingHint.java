package com.wdcftgg.farmersdelightlegacy.client.gui;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID, value = Side.CLIENT)
public final class RicePlantingHint {
    private RicePlantingHint() {
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        GuiIngame gui = Minecraft.getMinecraft().ingameGUI;
        if (gui.overlayMessageTime != 1 || (1 - event.getPartialTicks()) * 256 / 20 >= 4) {
            return;
        }
        if (gui.overlayMessage.equals(new TextComponentTranslation("farmersdelight.block.rice.invalid_placement").getFormattedText())
                || gui.overlayMessage.equals(new TextComponentTranslation("farmersdelight.block.rice.invalid_placement.legacy").getFormattedText())) {
            gui.overlayMessageTime = 0;
        }
    }
}
