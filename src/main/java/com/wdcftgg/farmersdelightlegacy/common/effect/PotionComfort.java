package com.wdcftgg.farmersdelightlegacy.common.effect;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PotionComfort extends Potion {

    private static final ResourceLocation ICON_TEXTURE = new ResourceLocation(FarmersDelightLegacy.MOD_ID,
            "textures/mob_effect/comfort.png");

    public PotionComfort() {
        super(false, 0xF2E8C9);
        this.setPotionName("effect.farmersdelight.comfort");
        this.setBeneficial();
    }

    @Override
    public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier) {
        if (entityLivingBaseIn.isPotionActive(MobEffects.REGENERATION)) {
            return;
        }

        if (entityLivingBaseIn instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityLivingBaseIn;
            if (player.getFoodStats().getSaturationLevel() > 0.0F) {
                return;
            }
        }

        if (entityLivingBaseIn.getHealth() < entityLivingBaseIn.getMaxHealth()) {
            entityLivingBaseIn.heal(1.0F);
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration % 80 == 0;
    }

    @Override
    public boolean hasStatusIcon() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(PotionEffect effect, Gui gui, int x, int y, float z) {
        renderEffectIcon(gui, x + 6, y + 7, 1.0F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderHUDEffect(PotionEffect effect, Gui gui, int x, int y, float z, float alpha) {
        renderEffectIcon(gui, x + 3, y + 3, alpha);
    }

    @SideOnly(Side.CLIENT)
    private static void renderEffectIcon(Gui gui, int x, int y, float alpha) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(ICON_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 18, 18, 18.0F, 18.0F);
    }
}
