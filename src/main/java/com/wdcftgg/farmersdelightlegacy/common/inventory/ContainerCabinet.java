package com.wdcftgg.farmersdelightlegacy.common.inventory;

import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCabinet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerCabinet extends Container {

    private static final int CABINET_SLOT_COUNT = 27;

    private final TileEntityCabinet tileEntityCabinet;

    public ContainerCabinet(InventoryPlayer playerInventory, TileEntityCabinet tileEntityCabinet) {
        this.tileEntityCabinet = tileEntityCabinet;
        this.tileEntityCabinet.openInventory(playerInventory.player);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlotToContainer(new Slot(tileEntityCabinet, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlotToContainer(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.tileEntityCabinet.isUsableByPlayer(playerIn);
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        this.tileEntityCabinet.closeInventory(playerIn);
    }

    public TileEntityCabinet getTileEntityCabinet() {
        return this.tileEntityCabinet;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            itemStack = slotStack.copy();

            if (index < CABINET_SLOT_COUNT) {
                if (!this.mergeItemStack(slotStack, CABINET_SLOT_COUNT, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(slotStack, 0, CABINET_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemStack;
    }
}

