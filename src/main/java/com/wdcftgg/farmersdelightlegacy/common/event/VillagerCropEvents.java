package com.wdcftgg.farmersdelightlegacy.common.event;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.entity.VillagerFarmTask;
import com.wdcftgg.farmersdelightlegacy.common.entity.VillagerFood;
import com.wdcftgg.farmersdelightlegacy.common.entity.VillagerMateTask;
import com.wdcftgg.farmersdelightlegacy.common.entity.VillagerShareTask;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHarvestFarmland;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAIVillagerInteract;
import net.minecraft.entity.ai.EntityAIVillagerMate;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID)
public final class VillagerCropEvents {
    private VillagerCropEvents() {
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinWorldEvent event) {
        if (!event.getWorld().isRemote && event.getEntity() instanceof EntityVillager) {
            updateTasks((EntityVillager) event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityVillager) || event.getEntityLiving().world.isRemote) {
            return;
        }
        EntityVillager villager = (EntityVillager) event.getEntityLiving();
        if (!villager.isEntityAlive() || villager.isAIDisabled()) {
            return;
        }
        if (villager.ticksExisted % 20 == 0) {
            updateTasks(villager);
        }
        VillagerFood.pickUpFood(villager);
    }

    private static void updateTasks(EntityVillager villager) {
        for (EntityAITasks.EntityAITaskEntry entry : new ArrayList<>(villager.tasks.taskEntries)) {
            EntityAIBase replacement;
            if (entry.action.getClass() == EntityAIHarvestFarmland.class) {
                replacement = new VillagerFarmTask(villager);
            } else if (entry.action.getClass() == EntityAIVillagerMate.class) {
                replacement = new VillagerMateTask(villager);
            } else if (entry.action.getClass() == EntityAIVillagerInteract.class) {
                replacement = new VillagerShareTask(villager);
            } else {
                continue;
            }
            villager.tasks.removeTask(entry.action);
            villager.tasks.addTask(entry.priority, replacement);
        }
    }
}
