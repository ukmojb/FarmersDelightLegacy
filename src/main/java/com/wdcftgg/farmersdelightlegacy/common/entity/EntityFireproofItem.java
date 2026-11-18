package com.wdcftgg.farmersdelightlegacy.common.entity;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public final class EntityFireproofItem extends EntityItem {
    public EntityFireproofItem(World world) {
        super(world);
        isImmuneToFire = true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return !source.isFireDamage() && super.attackEntityFrom(source, amount);
    }
}
