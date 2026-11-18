package com.wdcftgg.farmersdelightlegacy.common.entity;

import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public final class VillagerShareTask extends EntityAIWatchClosest2 {
    private final EntityVillager villager;
    private int delay;

    public VillagerShareTask(EntityVillager villager) {
        super(villager, EntityVillager.class, 3, 0.02F);
        this.villager = villager;
    }

    @Override
    public void startExecuting() {
        super.startExecuting();
        delay = (villager.canAbondonItems() || VillagerFood.inventoryPoints(villager) >= 24)
                && closestEntity instanceof EntityVillager
                && VillagerFood.wantsMoreFood((EntityVillager) closestEntity) ? 10 : 0;
    }

    @Override
    public void updateTask() {
        super.updateTask();
        if (delay <= 0 || --delay > 0) {
            return;
        }
        InventoryBasic inventory = villager.getVillagerInventory();
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            ItemStack gift = ItemStack.EMPTY;
            int consumed = 0;
            if (VillagerFood.points(stack) > 0 && stack.getCount() > 3) {
                consumed = stack.getCount() / 2;
                gift = stack.copy();
                gift.setCount(consumed);
            } else if (stack.getItem() == Items.WHEAT && stack.getCount() > 5) {
                consumed = stack.getCount() / 2 / 3 * 3;
                gift = new ItemStack(Items.BREAD, consumed / 3);
            }
            if (!gift.isEmpty()) {
                EntityItem item = new EntityItem(villager.world, villager.posX,
                        villager.posY - 0.3D + villager.getEyeHeight(), villager.posZ, gift);
                float yaw = villager.rotationYawHead * 0.017453292F;
                float pitch = villager.rotationPitch * 0.017453292F;
                item.motionX = -MathHelper.sin(yaw) * MathHelper.cos(pitch) * 0.3F;
                item.motionZ = MathHelper.cos(yaw) * MathHelper.cos(pitch) * 0.3F;
                item.motionY = -MathHelper.sin(pitch) * 0.3F + 0.1F;
                item.setDefaultPickupDelay();
                if (villager.world.spawnEntity(item)) {
                    inventory.decrStackSize(slot, consumed);
                }
                return;
            }
        }
    }
}
