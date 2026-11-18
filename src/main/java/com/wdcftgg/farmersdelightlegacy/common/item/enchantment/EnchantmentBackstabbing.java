package com.wdcftgg.farmersdelightlegacy.common.item.enchantment;

import com.wdcftgg.farmersdelightlegacy.common.item.ItemKnife;
import com.wdcftgg.farmersdelightlegacy.common.registry.ItemCategories;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class EnchantmentBackstabbing extends Enchantment {

    public EnchantmentBackstabbing() {
        super(Rarity.UNCOMMON, EnumEnchantmentType.WEAPON, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND});
        this.setName("backstabbing");
    }

    @Override
    public int getMinLevel() {
        return 1;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinEnchantability(int enchantmentLevel) {
        return 15 + (enchantmentLevel - 1) * 9;
    }

    @Override
    public int getMaxEnchantability(int enchantmentLevel) {
        return this.getMinEnchantability(enchantmentLevel) + 50;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return ItemKnife.isKnife(stack) || ItemCategories.contains("fdEnchantableKnife", stack);
    }

    @Override
    public boolean canApply(ItemStack stack) {
        return this.canApplyAtEnchantingTable(stack);
    }

    public static boolean isLookingBehindTarget(EntityLivingBase target, net.minecraft.util.math.Vec3d attackerLocation) {
        if (attackerLocation == null) {
            return false;
        }
        net.minecraft.util.math.Vec3d look = target.getLookVec().normalize();
        net.minecraft.util.math.Vec3d attackDirection = attackerLocation.subtract(target.getPositionVector()).normalize();
        attackDirection = new net.minecraft.util.math.Vec3d(attackDirection.x, 0.0D, attackDirection.z);
        return attackDirection.dotProduct(look) < -0.5D;
    }

    public static float getBackstabbingDamagePerLevel(float amount, int level) {
        return amount * ((level * 0.2F) + 1.2F);
    }
}
