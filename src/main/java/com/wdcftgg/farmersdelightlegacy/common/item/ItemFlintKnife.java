package com.wdcftgg.farmersdelightlegacy.common.item;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public final class ItemFlintKnife extends ItemKnife {
    public ItemFlintKnife() {
        super(ToolMaterial.STONE, 1.5D);
    }

    @Override
    public boolean getIsRepairable(ItemStack tool, ItemStack material) {
        return material.getItem() == Items.FLINT;
    }

    @Override
    public String getToolMaterialName() {
        return "FLINT";
    }
}
