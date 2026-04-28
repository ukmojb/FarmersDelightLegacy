package com.wdcftgg.farmersdelightlegacy.common.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ItemBowlFoodTooltip extends ItemFoodTooltip {

    public ItemBowlFoodTooltip(int amount, float saturation, boolean isWolfFood) {
        this(amount, saturation, isWolfFood, null, 0, 0, 0.0F);
    }

    public ItemBowlFoodTooltip(int amount, float saturation, boolean isWolfFood, @Nullable ResourceLocation effectId,
                               int effectDuration, int effectAmplifier, float effectChance, String... extraTooltipKeys) {
        super(amount, saturation, isWolfFood, effectId, effectDuration, effectAmplifier, effectChance, extraTooltipKeys);
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        return new ItemStack(Items.BOWL);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        ItemStack result = super.onItemUseFinish(stack, worldIn, entityLiving);
        if (!(entityLiving instanceof EntityPlayer)) {
            return result;
        }

        EntityPlayer player = (EntityPlayer) entityLiving;
        if (player.capabilities.isCreativeMode) {
            return result;
        }

        ItemStack bowl = new ItemStack(Items.BOWL);
        if (result.isEmpty()) {
            return bowl;
        }

        if (!player.inventory.addItemStackToInventory(bowl)) {
            player.dropItem(bowl, false);
        }
        return result;
    }
}
