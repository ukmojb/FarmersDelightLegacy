package com.wdcftgg.farmersdelightlegacy.common.inventory;

import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCookingPot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.wrapper.InvWrapper;

public final class CookingPotItemHandler extends InvWrapper {
    private final TileEntityCookingPot cookingPot;
    private final EnumFacing side;

    public CookingPotItemHandler(TileEntityCookingPot cookingPot, EnumFacing side) {
        super(cookingPot);
        this.cookingPot = cookingPot;
        this.side = side;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return cookingPot.canInsertItem(slot, stack, side) ? super.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return cookingPot.canExtractItem(slot, getStackInSlot(slot), side)
                ? super.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
    }
}
