package com.wdcftgg.farmersdelightlegacy.common.item;

import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public class ItemFoodTooltip extends ItemFood {

    protected final int foodAmount;
    protected final float saturationAmount;
    @Nullable
    protected final ResourceLocation effectId;
    protected final int effectDuration;
    protected final int effectAmplifier;
    protected final float effectChance;
    protected final String[] extraTooltipKeys;

    public ItemFoodTooltip(int amount, float saturation, boolean isWolfFood) {
        this(amount, saturation, isWolfFood, null, 0, 0, 0.0F);
    }

    public ItemFoodTooltip(int amount, float saturation, boolean isWolfFood, @Nullable ResourceLocation effectId, int effectDuration,
                           int effectAmplifier, float effectChance, String... extraTooltipKeys) {
        super(amount, saturation, isWolfFood);
        this.foodAmount = amount;
        this.saturationAmount = saturation;
        this.effectId = effectId;
        this.effectDuration = effectDuration;
        this.effectAmplifier = effectAmplifier;
        this.effectChance = effectChance;
        this.extraTooltipKeys = extraTooltipKeys == null ? new String[0] : extraTooltipKeys;
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        super.onFoodEaten(stack, worldIn, player);
        if (worldIn.isRemote || effectId == null || worldIn.rand.nextFloat() > effectChance) {
            return;
        }

        Potion potion = ForgeRegistries.POTIONS.getValue(effectId);
        if (potion != null) {
            player.addPotionEffect(new PotionEffect(potion, effectDuration, effectAmplifier));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (!Configuration.foodEffectTooltip) {
            return;
        }

//        tooltip.add(TextFormatting.GRAY + new TextComponentTranslation("farmersdelight.tooltip.food.hunger",
//                String.valueOf(this.foodAmount)).getFormattedText());
//        float restoredSaturation = this.saturationAmount * this.foodAmount * 2.0F;
//        tooltip.add(TextFormatting.GRAY + new TextComponentTranslation("farmersdelight.tooltip.food.saturation",
//                String.format(Locale.ROOT, "%.1f", restoredSaturation)).getFormattedText());

        if (effectId != null) {
            Potion potion = ForgeRegistries.POTIONS.getValue(effectId);
            if (potion != null) {
                PotionEffect effect = new PotionEffect(potion, effectDuration, effectAmplifier);
                String duration = Potion.getPotionDurationString(effect, 1.0F);
                String effectName = new TextComponentTranslation(effect.getEffectName()).getFormattedText();
                TextComponentTranslation effectTooltip = new TextComponentTranslation("farmersdelight.tooltip.food.effect",
                        effectName, duration);
                effectTooltip.getStyle().setColor(TextFormatting.BLUE);
                tooltip.add(effectTooltip.getFormattedText());
                if (effectChance < 0.999F) {
                    TextComponentTranslation translation = new TextComponentTranslation("farmersdelight.tooltip.food.effect_chance",
                            Math.round(effectChance * 100.0F));
                    translation.getStyle().setColor(TextFormatting.BLUE);
                    tooltip.add(translation.getFormattedText());
                }
            }
        }

        for (String key : extraTooltipKeys) {
            tooltip.add(TextFormatting.DARK_PURPLE + new TextComponentTranslation(key).getFormattedText());
        }
    }
}
