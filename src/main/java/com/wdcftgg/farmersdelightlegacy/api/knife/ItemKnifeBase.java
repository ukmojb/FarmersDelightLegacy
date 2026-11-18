package com.wdcftgg.farmersdelightlegacy.api.knife;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.wdcftgg.farmersdelightlegacy.common.registry.BlockCategories;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

public class ItemKnifeBase extends ItemSword implements IKnifeItem {

    private static final float webSpeed = 15.0F;
    private static final Set<Enchantment> allowedEnchantments = Sets.newHashSet(
            Enchantments.SHARPNESS,
            Enchantments.SMITE,
            Enchantments.BANE_OF_ARTHROPODS,
            Enchantments.KNOCKBACK,
            Enchantments.FIRE_ASPECT,
            Enchantments.LOOTING,
            Enchantments.EFFICIENCY,
            Enchantments.FORTUNE,
            Enchantments.SILK_TOUCH
    );
    private static final Set<Enchantment> deniedEnchantments = Sets.newHashSet(Enchantments.SWEEPING);
    private final double attackDamage;
    private final double attackSpeed;
    private final float miningSpeed;

    public ItemKnifeBase(Item.ToolMaterial material, double attackDamage) {
        this(material, attackDamage, -2.0D);
    }

    public ItemKnifeBase(Item.ToolMaterial material, double attackDamage, double attackSpeed) {
        super(material);
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        this.miningSpeed = material.getEfficiency();
    }

    @Override
    public boolean canHarvestBlock(IBlockState blockIn) {
        return blockIn.getBlock() == Blocks.WEB;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        Block block = state.getBlock();
        Material material = state.getMaterial();
        if (block == Blocks.WEB) {
            return webSpeed;
        }

        if (BlockCategories.contains("fdMineableKnife", state)
                || block instanceof BlockCrops
                || block instanceof BlockBush
                || block == Blocks.MELON_BLOCK
                || block == Blocks.PUMPKIN
                || block == Blocks.LIT_PUMPKIN
                || material == Material.PLANTS
                || material == Material.VINE
                || material == Material.LEAVES
                || material == Material.GOURD
                || material == Material.CACTUS
                || material == Material.CLOTH
                || material == Material.CAKE
                || material == Material.CARPET) {
            return getKnifeMiningSpeed();
        }

        return super.getDestroySpeed(stack, state);
    }

    protected float getKnifeMiningSpeed() {
        return miningSpeed;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos,
                                    EntityLivingBase entityLiving) {
        if (state.getBlockHardness(worldIn, pos) != 0.0F) {
            stack.damageItem(1, entityLiving);
        }
        return true;
    }

    @Override
    public ItemStack getKnifeJeiInfoStack(ItemStack stack) {
        return stack.copy();
    }

    @Override
    public void onCuttingBoardRecipeProcessed(ItemStack stack, World world, EntityPlayer player) {
        if (player != null) {
            stack.damageItem(1, player);
            return;
        }
        if (stack.attemptDamageItem(1, world.rand, null)) {
            stack.shrink(1);
            stack.setItemDamage(0);
        }
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (allowedEnchantments.contains(enchantment)) {
            return true;
        }
        if (deniedEnchantments.contains(enchantment)) {
            return false;
        }
        return enchantment.type != null && enchantment.type.canEnchantItem(this);
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> attributes = HashMultimap.create(super.getItemAttributeModifiers(equipmentSlot));
        if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
            attributes.removeAll(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
            attributes.removeAll(SharedMonsterAttributes.ATTACK_SPEED.getName());
            attributes.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                    new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", this.attackDamage, 0));
            attributes.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", this.attackSpeed, 0));
        }
        return attributes;
    }
}
