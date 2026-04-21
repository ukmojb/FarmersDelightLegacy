package com.wdcftgg.farmersdelightlegacy.common.effect;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PotionNourishment extends Potion {

    private static final ResourceLocation ICON_TEXTURE = new ResourceLocation(FarmersDelightLegacy.MOD_ID,
            "textures/mob_effect/nourishment.png");

    public PotionNourishment() {
        super(false, 0xF7D560);
        this.setPotionName("effect.farmersdelight.nourishment");
        this.setBeneficial();
    }

    @Override
    public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier) {
        if (!(entityLivingBaseIn instanceof EntityPlayer) || entityLivingBaseIn.world.isRemote) {
            return;
        }

        EntityPlayer player = (EntityPlayer) entityLivingBaseIn;
        FoodStats foodStats = player.getFoodStats();
        boolean naturalRegen = player.world.getGameRules().getBoolean("naturalRegeneration");
        boolean playerHealingWithHunger = naturalRegen && player.shouldHeal() && foodStats.getFoodLevel() >= 18;
        if (!playerHealingWithHunger) {
            reduceExhaustion(foodStats);
        }
    }

    private static void reduceExhaustion(FoodStats foodStats) {
        try {
            float exhaustion = ObfuscationReflectionHelper.getPrivateValue(FoodStats.class, foodStats,
                    "foodExhaustionLevel", "field_75126_c");
            if (exhaustion > 0.0F) {
                // 与上游一致，只抵消现有消耗，不让 exhaustion 变成负值。
                ObfuscationReflectionHelper.setPrivateValue(FoodStats.class, foodStats,
                        Math.max(0.0F, exhaustion - 4.0F), "foodExhaustionLevel", "field_75126_c");
            }
        } catch (RuntimeException ignored) {
            foodStats.addExhaustion(-4.0F);
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
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
