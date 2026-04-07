package com.wdcftgg.farmersdelightlegacy.common;

import com.wdcftgg.farmersdelight.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class ModCreativeTab {

    public static final CreativeTabs TAB = new CreativeTabs(Tags.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            Item tomatoItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(Tags.MOD_ID, "tomato"));
            if (tomatoItem != null) {
                return new ItemStack(tomatoItem);
            }
            return ItemStack.EMPTY;
        }
    };

    private ModCreativeTab() {
    }
}

