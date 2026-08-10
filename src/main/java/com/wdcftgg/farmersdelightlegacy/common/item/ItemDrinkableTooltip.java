package com.wdcftgg.farmersdelightlegacy.common.item;

import com.wdcftgg.farmersdelightlegacy.common.compat.UniversalTweaksMilkCompat;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemDrinkableTooltip extends ItemFoodTooltip {

    private final DrinkEffect drinkEffect;
    private final ItemStack containerItem;

    public ItemDrinkableTooltip(int amount, float saturation, boolean alwaysEdible, @Nullable ResourceLocation effectId,
                                int effectDuration, int effectAmplifier, float effectChance, DrinkEffect drinkEffect,
                                String... extraTooltipKeys) {
        this(amount, saturation, alwaysEdible, effectId, effectDuration, effectAmplifier, effectChance, drinkEffect,
                new ItemStack(Items.GLASS_BOTTLE), extraTooltipKeys);
    }

    public ItemDrinkableTooltip(int amount, float saturation, boolean alwaysEdible, @Nullable ResourceLocation effectId,
                                int effectDuration, int effectAmplifier, float effectChance, DrinkEffect drinkEffect,
                                ItemStack containerItem, String... extraTooltipKeys) {
        super(amount, saturation, false, effectId, effectDuration, effectAmplifier, effectChance, extraTooltipKeys);
        this.drinkEffect = drinkEffect;
        this.containerItem = containerItem == null || containerItem.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : containerItem.copy();
        this.setAlwaysEdible();
    }

    public ItemDrinkableTooltip(int amount, float saturation, boolean alwaysEdible, List<FoodEffectEntry> foodEffects,
                                DrinkEffect drinkEffect, ItemStack containerItem, String... extraTooltipKeys) {
        super(amount, saturation, false, foodEffects, extraTooltipKeys);
        this.drinkEffect = drinkEffect;
        this.containerItem = containerItem == null || containerItem.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : containerItem.copy();
        this.setAlwaysEdible();
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.DRINK;
    }

    @Override
    public void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        super.onFoodEaten(stack, worldIn, player);
        if (!worldIn.isRemote) {
            this.drinkEffect.apply(worldIn, player);
        }
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return !this.containerItem.isEmpty();
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        return this.containerItem.copy();
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        if (entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityLiving;
            player.getFoodStats().addStats(this, stack);
            this.onFoodEaten(stack, worldIn, player);
            player.addStat(StatList.getObjectUseStats(this));

            if (player instanceof EntityPlayerMP) {
                CriteriaTriggers.CONSUME_ITEM.trigger((EntityPlayerMP) player, stack);
            }
        }

        stack.shrink(1);
        if (!(entityLiving instanceof EntityPlayer)) {
            return stack;
        }

        EntityPlayer player = (EntityPlayer) entityLiving;
        if (player.capabilities.isCreativeMode) {
            return stack;
        }

        ItemStack bottle = this.getContainerItem(stack);
        if (stack.isEmpty()) {
            return bottle;
        }

        if (!player.inventory.addItemStackToInventory(bottle)) {
            player.dropItem(bottle, false);
        }
        return stack;
    }

    public enum DrinkEffect {
        NONE {
            @Override
            void apply(World worldIn, EntityPlayer player) {
            }
        },
        CLEAR_ONE {
            @Override
            void apply(World worldIn, EntityPlayer player) {
                clearRandomEffect(player, false, true, worldIn);
            }
        },
        CLEAR_ONE_HARMFUL {
            @Override
            void apply(World worldIn, EntityPlayer player) {
                clearRandomEffect(player, true, false, worldIn);
            }
        },
        HEAL_MINOR {
            @Override
            void apply(World worldIn, EntityPlayer player) {
                player.heal(2.0F);
            }
        };

        abstract void apply(World worldIn, EntityPlayer player);

        static void clearRandomEffect(EntityPlayer player, boolean harmfulOnly, boolean respectUniversalTweaks, World worldIn) {
            List<Potion> compatibleEffects = new ArrayList<>();
            for (PotionEffect effect : player.getActivePotionEffects()) {
                if (harmfulOnly && !effect.getPotion().isBadEffect()) {
                    continue;
                }
                boolean curableByMilk = effect.getCurativeItems().stream().anyMatch(curative -> curative.getItem() == Items.MILK_BUCKET);
                if (curableByMilk && (!respectUniversalTweaks || UniversalTweaksMilkCompat.canMilkCure(effect))) {
                    compatibleEffects.add(effect.getPotion());
                }
            }

            if (!compatibleEffects.isEmpty()) {
                Potion selectedPotion = compatibleEffects.get(worldIn.rand.nextInt(compatibleEffects.size()));
                player.removePotionEffect(selectedPotion);
            }
        }
    }
}
