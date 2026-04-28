package com.wdcftgg.farmersdelightlegacy.common.item;

import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
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
    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        super.onFoodEaten(stack, worldIn, player);
        if (worldIn.isRemote) {
            return;
        }
        for (PotionEffect effect : FEEDING_EFFECTS) {
            player.addPotionEffect(new PotionEffect(effect));
        }
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
        if (!(target instanceof EntityWolf)) {
            return false;
        }

        EntityWolf wolf = (EntityWolf) target;
        if (!wolf.isEntityAlive() || !wolf.isTamed()) {
            return false;
        }

        if (playerIn.world.isRemote) {
            return true;
        }

        wolf.setHealth(wolf.getMaxHealth());
        for (PotionEffect effect : FEEDING_EFFECTS) {
            wolf.addPotionEffect(new PotionEffect(effect));
        }

        if (!playerIn.capabilities.isCreativeMode) {
            stack.shrink(1);
            ItemStack bowlStack = new ItemStack(Items.BOWL);
            if (!playerIn.inventory.addItemStackToInventory(bowlStack)) {
                playerIn.dropItem(bowlStack, false);
            }
        }

        World world = wolf.world;
        world.playSound(null, wolf.posX, wolf.posY, wolf.posZ, SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.PLAYERS, 0.8F, 0.8F);
        for (int i = 0; i < 5; ++i) {
            world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,
                    wolf.posX + (world.rand.nextDouble() - 0.5D),
                    wolf.posY + 0.5D + world.rand.nextDouble() * 0.5D,
                    wolf.posZ + (world.rand.nextDouble() - 0.5D),
                    0.0D,
                    0.02D,
                    0.0D);
        }

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
            TextComponentTranslation effectTooltip = new TextComponentTranslation("farmersdelight.tooltip.food.effect",
                    effectName, duration);
            effectTooltip.getStyle().setColor(TextFormatting.BLUE);
            tooltip.add(effectTooltip.getFormattedText());
        }
    }
}

