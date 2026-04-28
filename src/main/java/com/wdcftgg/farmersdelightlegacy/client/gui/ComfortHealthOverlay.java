package com.wdcftgg.farmersdelightlegacy.client.gui;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Random;

@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID, value = Side.CLIENT)
public final class ComfortHealthOverlay {

    private static final ResourceLocation MOD_ICONS_TEXTURE = new ResourceLocation(FarmersDelightLegacy.MOD_ID,
            "textures/gui/fd_icons.png");

    private ComfortHealthOverlay() {
    }

    @SubscribeEvent
    public static void onRenderHealthPost(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.HEALTH) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = minecraft.player;
        if (!Configuration.comfortHealthOverlay || player == null || minecraft.gameSettings.hideGUI
                || !minecraft.playerController.shouldDrawHUD() || ModEffects.COMFORT == null
                || !player.isPotionActive(ModEffects.COMFORT)) {
            return;
        }

        FoodStats foodStats = player.getFoodStats();
        boolean canComfortHeal = foodStats.getSaturationLevel() <= 0.0F
                && player.shouldHeal()
                && !player.isPotionActive(MobEffects.REGENERATION);
        if (!canComfortHeal) {
            return;
        }

        renderComfortOverlay(minecraft, player, event.getResolution());
    }

    private static void renderComfortOverlay(Minecraft minecraft, EntityPlayer player, ScaledResolution resolution) {
        int top = resolution.getScaledHeight() - GuiIngameForge.left_height + 10;
        int left = resolution.getScaledWidth() / 2 - 91;
        int ticks = minecraft.ingameGUI.getUpdateCounter();
        Random random = new Random(ticks * 312871L);

        int health = MathHelper.ceil(player.getHealth());
        float absorption = MathHelper.ceil(player.getAbsorptionAmount());
        float maxHealth = player.getMaxHealth();
        int healthRows = MathHelper.ceil((maxHealth + absorption) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);
        int bottomRowOffset = (healthRows - 1) * rowHeight;
        int comfortSheen = ticks % 50;
        int comfortFrame = comfortSheen % 2;
        int[] textureWidths = new int[]{5, 9};
        int visibleHearts = MathHelper.ceil(Math.min(maxHealth, 20.0F) / 2.0F);

        GlStateManager.enableBlend();
        minecraft.getTextureManager().bindTexture(MOD_ICONS_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        for (int index = 0; index < visibleHearts; ++index) {
            int column = index % 10;
            int x = left + column * 8;
            int y = top + bottomRowOffset;
            if (health <= 4) {
                y += random.nextInt(2);
            }

            if (column == comfortSheen / 2) {
                Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 9.0F, textureWidths[comfortFrame], 9,
                        256.0F, 256.0F);
            }
            if (column == (comfortSheen / 2) - 1 && comfortFrame == 0) {
                Gui.drawModalRectWithCustomSizedTexture(x + 5, y, 5.0F, 9.0F, 4, 9,
                        256.0F, 256.0F);
            }
        }

        minecraft.getTextureManager().bindTexture(Gui.ICONS);
        GlStateManager.disableBlend();
    }
}
