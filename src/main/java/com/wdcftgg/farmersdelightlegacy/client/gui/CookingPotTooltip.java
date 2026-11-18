package com.wdcftgg.farmersdelightlegacy.client.gui;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.item.ItemCookingPot;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCookingPot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;

@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID, value = Side.CLIENT)
public final class CookingPotTooltip {
    private CookingPotTooltip() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void addMealContainer(ItemTooltipEvent event) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen instanceof GuiCookingPot) {
            ((GuiCookingPot) screen).addMealTooltip(event.getItemStack(), event.getToolTip());
        }
    }

    @SubscribeEvent
    public static void renderMeal(RenderTooltipEvent.PostText event) {
        if (!(event.getStack().getItem() instanceof ItemCookingPot)) {
            return;
        }
        ItemStack meal = TileEntityCookingPot.getMealFromItem(event.getStack());
        if (meal.isEmpty()) {
            return;
        }
        int line = findMealLine(event.getLines());
        if (line >= 0) {
            int textY = event.getY() + line * 10 + 2;
            int iconY = textY + Math.floorDiv(event.getFontRenderer().FONT_HEIGHT - 16, 2);
            RenderItem renderer = Minecraft.getMinecraft().getRenderItem();
            float previousDepth = renderer.zLevel;
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.enableDepth();
            renderer.zLevel = 300;
            renderer.renderItemAndEffectIntoGUI(meal, event.getX(), iconY);
            renderer.zLevel = previousDepth;
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.popMatrix();
        }
    }

    static int findMealLine(List<String> lines) {
        String indent = TextFormatting.getTextWithoutFormattingCodes(ItemCookingPot.MEAL_TOOLTIP_PREFIX);
        for (int line = 1; line < lines.size(); line++) {
            if (TextFormatting.getTextWithoutFormattingCodes(lines.get(line)).startsWith(indent)) {
                return line;
            }
        }
        return -1;
    }
}
