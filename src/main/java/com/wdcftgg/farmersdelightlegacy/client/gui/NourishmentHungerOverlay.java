package com.wdcftgg.farmersdelightlegacy.client.gui;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Random;

@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID, value = Side.CLIENT)
public final class NourishmentHungerOverlay {
    private static final ResourceLocation MOD_ICONS_TEXTURE = new ResourceLocation(FarmersDelightLegacy.MOD_ID,
            "textures/gui/fd_icons.png");

    private NourishmentHungerOverlay() {
    }

    @SubscribeEvent
    public static void onRenderFoodPost(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.FOOD) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = minecraft.player;
        if (!Configuration.nourishmentHungerOverlay || player == null || minecraft.gameSettings.hideGUI || player.getRidingEntity() != null
                || !minecraft.playerController.shouldDrawHUD() || ModEffects.NOURISHMENT == null
                || !player.isPotionActive(ModEffects.NOURISHMENT)) {
            return;
        }

        renderNourishmentOverlay(minecraft, player, event.getResolution());
    }

    private static void renderNourishmentOverlay(Minecraft minecraft, EntityPlayer player, ScaledResolution resolution) {
        FoodStats foodStats = player.getFoodStats();
        int top = resolution.getScaledHeight() - GuiIngameForge.right_height + 10;
        int left = resolution.getScaledWidth() / 2 + 91;
        boolean playerHealingWithHunger = player.world.getGameRules().getBoolean("naturalRegeneration")
                && player.shouldHeal()
                && foodStats.getFoodLevel() >= 18;

        drawNourishmentOverlay(foodStats, minecraft, left, top, playerHealingWithHunger);
    }

    private static void drawNourishmentOverlay(FoodStats foodStats, Minecraft minecraft, int left, int top,
                                               boolean naturalHealing) {
        int foodLevel = foodStats.getFoodLevel();
        float saturation = foodStats.getSaturationLevel();
        int ticks = minecraft.ingameGUI.getUpdateCounter();
        Random random = new Random(ticks * 312871L);

        GlStateManager.enableBlend();
        minecraft.getTextureManager().bindTexture(MOD_ICONS_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        for (int index = 0; index < 10; ++index) {
            int x = left - index * 8 - 9;
            int y = top;

            if (saturation <= 0.0F && ticks % (foodLevel * 3 + 1) == 0) {
                y = top + (random.nextInt(3) - 1);
            }

            Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 9, 9, 256.0F, 256.0F);

            float effectiveHungerOfBar = foodLevel / 2.0F - index;
            int naturalHealingOffset = naturalHealing ? 18 : 0;

            if (effectiveHungerOfBar >= 1.0F) {
                Gui.drawModalRectWithCustomSizedTexture(x, y, 18.0F + naturalHealingOffset, 0.0F, 9, 9,
                        256.0F, 256.0F);
            } else if (effectiveHungerOfBar >= 0.5F) {
                Gui.drawModalRectWithCustomSizedTexture(x, y, 9.0F + naturalHealingOffset, 0.0F, 9, 9,
                        256.0F, 256.0F);
            }
        }

        minecraft.getTextureManager().bindTexture(Gui.ICONS);
        GlStateManager.disableBlend();
    }
}
