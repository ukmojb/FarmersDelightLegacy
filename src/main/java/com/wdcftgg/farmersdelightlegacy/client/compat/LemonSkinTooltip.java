package com.wdcftgg.farmersdelightlegacy.client.compat;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import ua.myxazaur.lemonskin.LemonSkin;
import ua.myxazaur.lemonskin.ModConfig;
import ua.myxazaur.lemonskin.helpers.AppleCoreHelper;
import ua.myxazaur.lemonskin.helpers.BetterWithModsHelper;
import ua.myxazaur.lemonskin.helpers.FoodHelper;
import ua.myxazaur.lemonskin.helpers.FoodHelper.BasicFoodValues;
import ua.myxazaur.lemonskin.helpers.TooltipHelper;

import java.util.List;

public final class LemonSkinTooltip {
    private static final String HUNGER_ROW = "\u00a0\u00a7r\u00a70\u00a7r\u00a71\u00a7r";
    private static final String SATURATION_ROW = "\u00a0\u00a7r\u00a70\u00a7r\u00a72\u00a7r";
    private static boolean missingRowReported;

    private LemonSkinTooltip() {
    }

    public static void reserveFoodTooltipSpace(List<String> tooltip, ItemStack stack) {
        if (!TooltipHelper.shouldShowFoodTooltip(stack)) {
            return;
        }
        EntityPlayer player = Minecraft.getMinecraft().player;
        BasicFoodValues base = FoodHelper.getDefaultFoodValues(stack);
        BasicFoodValues actual = player == null ? base : FoodHelper.getModifiedFoodValues(stack, player);
        if (LemonSkin.hasAppleCore && player != null) {
            actual = AppleCoreHelper.getFoodValuesForDisplay(actual, player);
        }
        actual = BetterWithModsHelper.getFoodValuesForDisplay(actual);
        if (actual.hunger > 0) {
            tooltip.add(HUNGER_ROW);
        }
        if (actual.getSaturationIncrement() > 0) {
            tooltip.add(SATURATION_ROW);
        }
    }

    public static boolean canSkipTooltip(BasicFoodValues base, BasicFoodValues actual) {
        return base.equals(actual)
                && (!ModConfig.CLIENT.USE_MODERN_TOOLTIP || actual.getSaturationIncrement() <= 0);
    }

    public static void renderModern(RenderTooltipEvent.PostText event, ItemStack stack,
                                    BasicFoodValues base, BasicFoodValues actual) {
        Minecraft minecraft = Minecraft.getMinecraft();
        GuiScreen screen = minecraft.currentScreen;
        if (screen == null) {
            return;
        }
        if (actual.hunger > 0) {
            int row = findRow(event.getLines(), HUNGER_ROW);
            if (row >= 0) {
                renderHunger(minecraft, screen, event, stack, base, actual, event.getY() + row * 10 + 2);
            }
        }
        if (actual.getSaturationIncrement() > 0) {
            int row = findRow(event.getLines(), SATURATION_ROW);
            if (row >= 0) {
                renderSaturation(minecraft, screen, event, base, actual, event.getY() + row * 10 + 2);
            }
        }
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
    }

    private static int findRow(List<String> lines, String marker) {
        for (int index = 0; index < lines.size(); index++) {
            if (lines.get(index).endsWith(marker)) {
                return index;
            }
        }
        if (!missingRowReported) {
            FarmersDelightLegacy.LOGGER.warn("LemonSkin 食物提示行标记丢失，已跳过对应图标绘制：{}", lines);
            missingRowReported = true;
        }
        return -1;
    }

    private static void renderHunger(Minecraft minecraft, GuiScreen screen, RenderTooltipEvent.PostText event,
                                     ItemStack stack, BasicFoodValues base, BasicFoodValues actual, int y) {
        int bars = (int) Math.ceil(Math.max(base.hunger, actual.hunger) / 2.0F);
        String count = bars > 10 ? "x" + bars : null;
        if (count != null) {
            bars = 1;
        }
        minecraft.getTextureManager().bindTexture(Gui.ICONS);
        boolean rotten = FoodHelper.isRotten(stack);
        int iconOffset = rotten ? 36 : 0;
        for (int value = 0; value < bars * 2; value += 2) {
            int textureX;
            if (actual.hunger > base.hunger && base.hunger <= value) {
                textureX = 133 + iconOffset;
            } else if (actual.hunger > value + 1 || base.hunger == actual.hunger) {
                textureX = 16 + (rotten ? 13 : 0) * 9;
            } else if (actual.hunger == value + 1) {
                textureX = 124;
            } else {
                textureX = 34;
            }
            int x = event.getX() + value / 2 * 9;
            screen.drawTexturedModalRect(x, y, textureX, 27, 9, 9);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.25F);
            screen.drawTexturedModalRect(x, y, base.hunger - 1 == value ? 115 : 106, 27, 9, 9);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            if (actual.hunger > value) {
                screen.drawTexturedModalRect(x, y,
                        actual.hunger - 1 == value ? 61 + iconOffset : 52 + iconOffset, 27, 9, 9);
            }
        }
        if (count != null) {
            renderCount(event.getFontRenderer(), count, event.getX() + bars * 9 + 2, y, 2);
        }
    }

    private static void renderSaturation(Minecraft minecraft, GuiScreen screen, RenderTooltipEvent.PostText event,
                                         BasicFoodValues base, BasicFoodValues actual, int y) {
        float saturation = actual.getSaturationIncrement();
        int bars = (int) Math.ceil(Math.max(base.getSaturationIncrement(), saturation) / 2.0F);
        String count = bars > 10 ? "x" + bars : null;
        if (count != null) {
            bars = 1;
        }
        ModConfig.RGB color = ModConfig.CLIENT.getColor();
        minecraft.getTextureManager().bindTexture(ModConfig.CLIENT.getIcons());
        for (int value = 0; value < bars * 2; value += 2) {
            float fill = (saturation - value) / 2.0F;
            int textureX = fill >= 1 ? 21 : fill > 0.5F ? 14 : fill > 0.25F ? 7 : fill > 0 ? 0 : 28;
            if (saturation <= value) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
            } else {
                GlStateManager.color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F);
            }
            screen.drawTexturedModalRect(event.getX() + value / 2 * 7, y, textureX, 27, 7, 7);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
        if (count != null) {
            renderCount(event.getFontRenderer(), count, event.getX() + bars * 7 + 2, y, 1);
        }
    }

    private static void renderCount(FontRenderer font, String count, int x, int y, int yOffset) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.75F, 0.75F, 0.75F);
        font.drawStringWithShadow(count, x * 4.0F / 3.0F, y * 4.0F / 3.0F + yOffset, 0xFFDDDDDD);
        GlStateManager.popMatrix();
    }
}
