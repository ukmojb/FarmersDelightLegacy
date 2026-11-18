package com.wdcftgg.farmersdelightlegacy.common.item;

import com.wdcftgg.farmersdelightlegacy.common.entity.EntityFireproofItem;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public final class ItemNetheriteKnife extends ItemKnife {
    public ItemNetheriteKnife() {
        super(ToolMaterial.DIAMOND, 4.5D);
        setMaxDamage(2031);
    }

    @Override
    public int getItemEnchantability() {
        return 15;
    }

    @Override
    protected float getKnifeMiningSpeed() {
        return 9.0F;
    }

    @Override
    public String getToolMaterialName() {
        return "NETHERITE";
    }

    @Override
    public boolean getIsRepairable(ItemStack tool, ItemStack material) {
        for (int id : OreDictionary.getOreIDs(material)) {
            if ("ingotNetherite".equals(OreDictionary.getOreName(id))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    public Entity createEntity(World world, Entity original, ItemStack stack) {
        if (original instanceof EntityFireproofItem) {
            return null;
        }
        EntityFireproofItem entity = new EntityFireproofItem(world);
        entity.readFromNBT(original.writeToNBT(new NBTTagCompound()));
        entity.setItem(stack.copy());
        return entity;
    }
}
