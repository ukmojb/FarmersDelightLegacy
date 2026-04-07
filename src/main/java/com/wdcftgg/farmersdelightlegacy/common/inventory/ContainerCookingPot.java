package com.wdcftgg.farmersdelightlegacy.common.inventory;

import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCookingPot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerCookingPot extends Container {

    private static final int INPUT_SLOT_COUNT = 6;
    private static final int MEAL_DISPLAY_SLOT = 6;
    private static final int CONTAINER_SLOT = 7;
    private static final int OUTPUT_SLOT = 8;
    private static final int INPUT_START_X = 30;
    private static final int INPUT_START_Y = 17;
    private static final int SLOT_SPACING = 18;
    private static final int MEAL_DISPLAY_X = 124;
    private static final int MEAL_DISPLAY_Y = 26;
    private static final int CONTAINER_X = 92;
    private static final int CONTAINER_Y = 55;
    private static final int OUTPUT_X = 124;
    private static final int OUTPUT_Y = 55;

    private final TileEntityCookingPot tileEntityCookingPot;
    private int lastCookTime;
    private int lastCookTimeTotal;

    public ContainerCookingPot(InventoryPlayer playerInventory, TileEntityCookingPot tileEntityCookingPot) {
        this.tileEntityCookingPot = tileEntityCookingPot;

        int index = 0;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlotToContainer(new Slot(tileEntityCookingPot, index++, INPUT_START_X + col * SLOT_SPACING, INPUT_START_Y + row * SLOT_SPACING));
            }
        }

        this.addSlotToContainer(new Slot(tileEntityCookingPot, MEAL_DISPLAY_SLOT, MEAL_DISPLAY_X, MEAL_DISPLAY_Y) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            @Override
            public boolean canTakeStack(EntityPlayer playerIn) {
                return false;
            }
        });

        this.addSlotToContainer(new Slot(tileEntityCookingPot, CONTAINER_SLOT, CONTAINER_X, CONTAINER_Y) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return tileEntityCookingPot.isServingContainer(stack);
            }
        });

        this.addSlotToContainer(new Slot(tileEntityCookingPot, OUTPUT_SLOT, OUTPUT_X, OUTPUT_Y) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }
        });

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
        return tileEntityCookingPot.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            itemStack = slotStack.copy();

            int tileSlotCount = OUTPUT_SLOT + 1;
            if (index == OUTPUT_SLOT) {
                if (!this.mergeItemStack(slotStack, tileSlotCount, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(slotStack, itemStack);
            } else if (index < tileSlotCount) {
                if (!this.mergeItemStack(slotStack, tileSlotCount, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                boolean isValidContainer = tileEntityCookingPot.isServingContainer(slotStack);
                if (isValidContainer && !this.mergeItemStack(slotStack, CONTAINER_SLOT, CONTAINER_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
                if (!slotStack.isEmpty() && !this.mergeItemStack(slotStack, 0, INPUT_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemStack;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, this.tileEntityCookingPot.getField(0));
        listener.sendWindowProperty(this, 1, this.tileEntityCookingPot.getField(1));
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (IContainerListener listener : this.listeners) {
            if (this.lastCookTime != this.tileEntityCookingPot.getField(0)) {
                listener.sendWindowProperty(this, 0, this.tileEntityCookingPot.getField(0));
            }
            if (this.lastCookTimeTotal != this.tileEntityCookingPot.getField(1)) {
                listener.sendWindowProperty(this, 1, this.tileEntityCookingPot.getField(1));
            }
        }

        this.lastCookTime = this.tileEntityCookingPot.getField(0);
        this.lastCookTimeTotal = this.tileEntityCookingPot.getField(1);
    }

    @Override
    public void updateProgressBar(int id, int data) {
        this.tileEntityCookingPot.setField(id, data);
    }
}

