package com.wdcftgg.farmersdelightlegacy.common.item;

import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.network.PacketStarParticles;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class ItemDogFood extends ItemBowlFoodTooltip {

    private static final List<PotionEffect> FEEDING_EFFECTS = Arrays.asList(
            new PotionEffect(MobEffects.SPEED, 6000, 0),
            new PotionEffect(MobEffects.STRENGTH, 6000, 0),
            new PotionEffect(MobEffects.RESISTANCE, 6000, 0)
    );

    public ItemDogFood(int amount, float saturation) {
        super(amount, saturation, false);
        this.setAlwaysEdible();
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
        ResourceLocation entityId = EntityList.getKey(target);
        if (stack.isEmpty() || playerIn.isSpectator() || playerIn.getCooldownTracker().hasCooldown(this)
                || entityId == null || !Arrays.asList(Configuration.dogFoodUsers).contains(entityId.toString())
                || !target.isEntityAlive() || target instanceof EntityTameable && !((EntityTameable) target).isTamed()) {
            return false;
        }
        if (target.world.isRemote) {
            return true;
        }
        target.setHealth(target.getMaxHealth());
        for (PotionEffect effect : FEEDING_EFFECTS) {
            target.addPotionEffect(new PotionEffect(effect));
        }
        if (!playerIn.capabilities.isCreativeMode) {
            ItemStack bowl = getContainerItem(stack);
            stack.shrink(1);
            if (!playerIn.inventory.addItemStackToInventory(bowl)) {
                playerIn.dropItem(bowl, false);
            }
        }
        target.world.playSound(null, target.posX, target.posY, target.posZ,
                SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.PLAYERS, 0.8F, 0.8F);
        PacketStarParticles.send(target);
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (!Configuration.foodEffectTooltip) {
            return;
        }
        tooltip.add(TextFormatting.GRAY + new TextComponentTranslation("farmersdelight.tooltip.dog_food.when_feeding").getFormattedText());
        for (PotionEffect effect : FEEDING_EFFECTS) {
            String duration = Potion.getPotionDurationString(effect, 1.0F);
            String effectName = new TextComponentTranslation(effect.getEffectName()).getFormattedText();
            if (effect.getAmplifier() > 0) {
                effectName += " " + new TextComponentTranslation("potion.potency." + effect.getAmplifier()).getFormattedText();
            }
            TextComponentTranslation effectTooltip = new TextComponentTranslation("farmersdelight.tooltip.food.effect",
                    effectName, duration);
            effectTooltip.getStyle().setColor(TextFormatting.BLUE);
            tooltip.add(effectTooltip.getFormattedText());
        }
    }
}

