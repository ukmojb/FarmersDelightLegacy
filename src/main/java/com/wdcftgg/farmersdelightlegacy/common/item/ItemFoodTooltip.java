package com.wdcftgg.farmersdelightlegacy.common.item;

import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModEffects;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemFoodTooltip extends ItemFood {

    protected final int foodAmount;
    protected final float saturationAmount;
    protected final List<FoodEffectEntry> foodEffects;
    protected final String[] extraTooltipKeys;
    private int useDuration = 32;

    public ItemFoodTooltip(int amount, float saturation, boolean isWolfFood) {
        this(amount, saturation, isWolfFood, null, 0, 0, 0.0F);
    }

    public ItemFoodTooltip(int amount, float saturation, boolean isWolfFood, @Nullable ResourceLocation effectId, int effectDuration,
                           int effectAmplifier, float effectChance, String... extraTooltipKeys) {
        this(amount, saturation, isWolfFood, createSingleEffectList(effectId, effectDuration, effectAmplifier, effectChance),
                extraTooltipKeys);
    }

    public ItemFoodTooltip(int amount, float saturation, boolean isWolfFood, List<FoodEffectEntry> foodEffects,
                           String... extraTooltipKeys) {
        super(amount, saturation, isWolfFood);
        this.foodAmount = amount;
        this.saturationAmount = saturation;
        this.foodEffects = foodEffects == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(foodEffects));
        this.extraTooltipKeys = extraTooltipKeys == null ? new String[0] : extraTooltipKeys;
    }

    public ItemFoodTooltip setUseDuration(int ticks) {
        if (ticks <= 0) {
            throw new IllegalArgumentException("Food use duration must be positive");
        }
        this.useDuration = ticks;
        return this;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return useDuration;
    }

    @Override
    public void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        if (worldIn.isRemote) {
            return;
        }

        for (FoodEffectEntry effectEntry : this.foodEffects) {
            effectEntry.apply(worldIn, player);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (!Configuration.foodEffectTooltip) {
            return;
        }

        for (FoodEffectEntry effectEntry : this.foodEffects) {
            effectEntry.addTooltip(tooltip);
        }

        for (String key : extraTooltipKeys) {
            tooltip.add(TextFormatting.DARK_PURPLE + new TextComponentTranslation(key).getFormattedText());
        }
    }
    private static List<FoodEffectEntry> createSingleEffectList(@Nullable ResourceLocation effectId, int effectDuration,
                                                                int effectAmplifier, float effectChance) {
        if (effectId == null) {
            return Collections.emptyList();
        }
        List<FoodEffectEntry> effects = new ArrayList<>();
        effects.add(new FoodEffectEntry(effectId, effectDuration, effectAmplifier, effectChance));
        return effects;
    }

    public static final class FoodEffectEntry {
        private final ResourceLocation effectId;
        private final int duration;
        private final int amplifier;
        private final float chance;

        public FoodEffectEntry(ResourceLocation effectId, int duration, int amplifier, float chance) {
            this.effectId = effectId;
            this.duration = Math.max(0, duration);
            this.amplifier = Math.max(0, amplifier);
            this.chance = Math.max(0.0F, Math.min(1.0F, chance));
        }

        public ResourceLocation getEffectId() {
            return this.effectId;
        }

        public int getDuration() {
            return this.duration;
        }

        public int getAmplifier() {
            return this.amplifier;
        }

        public float getChance() {
            return this.chance;
        }

        private void apply(World worldIn, EntityPlayer player) {
            if (this.effectId == null || worldIn.rand.nextFloat() > this.chance) {
                return;
            }

            Potion potion = ForgeRegistries.POTIONS.getValue(this.effectId);
            if (potion != null) {
                player.addPotionEffect(new PotionEffect(potion, this.duration, this.amplifier, false,
                        potion != ModEffects.NOURISHMENT));
            }
        }

        @SideOnly(Side.CLIENT)
        private void addTooltip(List<String> tooltip) {
            if (this.effectId == null) {
                return;
            }

            Potion potion = ForgeRegistries.POTIONS.getValue(this.effectId);
            if (potion == null) {
                return;
            }

            PotionEffect effect = new PotionEffect(potion, this.duration, this.amplifier);
            String durationText = Potion.getPotionDurationString(effect, 1.0F);
            String effectName = new TextComponentTranslation(effect.getEffectName()).getFormattedText();
            TextComponentTranslation effectTooltip = new TextComponentTranslation("farmersdelight.tooltip.food.effect",
                    effectName, durationText);
            effectTooltip.getStyle().setColor(TextFormatting.BLUE);
            tooltip.add(effectTooltip.getFormattedText());
            if (this.chance < 0.999F) {
                TextComponentTranslation translation = new TextComponentTranslation("farmersdelight.tooltip.food.effect_chance",
                        Math.round(this.chance * 100.0F));
                translation.getStyle().setColor(TextFormatting.BLUE);
                tooltip.add(translation.getFormattedText());
            }
        }
    }

}
