package com.wdcftgg.farmersdelightlegacy.common.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class ItemPopsicle extends ItemFoodTooltip {
    public ItemPopsicle() {
        super(3, 0.2F, false);
        setAlwaysEdible();
        setUseDuration(16);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase consumer) {
        consumer.extinguish();
        return super.onItemUseFinish(stack, world, consumer);
    }
}
