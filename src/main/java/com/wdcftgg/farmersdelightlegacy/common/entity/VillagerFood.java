package com.wdcftgg.farmersdelightlegacy.common.entity;

import com.wdcftgg.farmersdelightlegacy.common.registry.ModItems;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.ForgeEventFactory;

public final class VillagerFood {
    private VillagerFood() {
    }

    public static int points(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.BREAD) {
            return 4;
        }
        if (item == ModItems.get("rice")) {
            return 2;
        }
        if (item == Items.CARROT || item == Items.POTATO || item == Items.BEETROOT
                || item == ModItems.get("cabbage") || item == ModItems.get("tomato") || item == ModItems.get("onion")) {
            return 1;
        }
        return 0;
    }

    public static int inventoryPoints(EntityVillager villager) {
        int points = 0;
        InventoryBasic inventory = villager.getVillagerInventory();
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            points += points(stack) * stack.getCount();
        }
        return points;
    }

    public static boolean wantsMoreFood(EntityVillager villager) {
        return villager.wantsMoreFood() && inventoryPoints(villager) < (villager.getProfession() == 0 ? 60 : 12);
    }

    public static boolean makeWilling(EntityVillager villager) {
        if (villager.getIsWillingToMate(true)) {
            return true;
        }
        if (inventoryPoints(villager) < 12) {
            return false;
        }
        int remaining = 12;
        InventoryBasic inventory = villager.getVillagerInventory();
        for (int slot = 0; slot < inventory.getSizeInventory() && remaining > 0; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            int value = points(stack);
            if (value > 0) {
                int count = Math.min(stack.getCount(), (remaining + value - 1) / value);
                inventory.decrStackSize(slot, count);
                remaining -= count * value;
            }
        }
        villager.setIsWillingToMate(true);
        villager.world.setEntityState(villager, (byte) 18);
        return true;
    }

    public static void pickUpFood(EntityVillager villager) {
        if (!villager.canPickUpLoot() || !ForgeEventFactory.getMobGriefingEvent(villager.world, villager)) {
            return;
        }
        for (EntityItem item : villager.world.getEntitiesWithinAABB(EntityItem.class,
                villager.getEntityBoundingBox().grow(1, 0, 1))) {
            ItemStack stack = item.getItem();
            if (item.isDead || item.cannotPickup() || stack.isEmpty() || !isWanted(stack.getItem())) {
                continue;
            }
            ItemStack remainder = villager.getVillagerInventory().addItem(stack);
            if (remainder.isEmpty()) {
                item.setDead();
            } else {
                item.setItem(remainder);
            }
        }
    }

    private static boolean isWanted(Item item) {
        return item == ModItems.get("cabbage") || item == ModItems.get("tomato")
                || item == ModItems.get("onion") || item == ModItems.get("rice")
                || item == ModItems.get("cabbage_seeds") || item == ModItems.get("tomato_seeds")
                || item == ModItems.get("rice_panicle");
    }
}
