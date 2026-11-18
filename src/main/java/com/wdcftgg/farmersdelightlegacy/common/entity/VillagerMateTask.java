package com.wdcftgg.farmersdelightlegacy.common.entity;

import net.minecraft.entity.ai.EntityAIVillagerMate;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.math.BlockPos;

public final class VillagerMateTask extends EntityAIVillagerMate {
    private final EntityVillager owner;

    public VillagerMateTask(EntityVillager villager) {
        super(villager);
        owner = villager;
    }

    @Override
    public boolean shouldExecute() {
        if (owner.getGrowingAge() != 0 || owner.getRNG().nextInt(500) != 0) {
            return false;
        }
        village = owner.world.getVillageCollection().getNearestVillage(new BlockPos(owner), 0);
        if (village == null || !village.isMatingSeason()
                || village.getNumVillagers() >= (int) (village.getNumVillageDoors() * 0.35D)
                || !VillagerFood.makeWilling(owner)) {
            return false;
        }
        mate = owner.world.findNearestEntityWithinAABB(EntityVillager.class,
                owner.getEntityBoundingBox().grow(8, 3, 8), owner);
        return mate != null && mate.getGrowingAge() == 0 && VillagerFood.makeWilling(mate);
    }
}
