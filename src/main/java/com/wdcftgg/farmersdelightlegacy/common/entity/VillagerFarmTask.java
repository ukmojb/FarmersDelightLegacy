package com.wdcftgg.farmersdelightlegacy.common.entity;

import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIMoveToBlock;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.ForgeEventFactory;

public final class VillagerFarmTask extends EntityAIMoveToBlock {
    private final EntityVillager villager;
    private boolean harvest;
    private boolean active;

    public VillagerFarmTask(EntityVillager villager) {
        super(villager, 0.6D, 16);
        this.villager = villager;
    }

    @Override
    public boolean shouldExecute() {
        if (!ForgeEventFactory.getMobGriefingEvent(villager.world, villager)) {
            return false;
        }
        if (runDelay <= 0) {
            active = false;
        }
        return super.shouldExecute();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return active && ForgeEventFactory.getMobGriefingEvent(villager.world, villager)
                && super.shouldContinueExecuting();
    }

    @Override
    protected boolean shouldMoveTo(World world, BlockPos pos) {
        if (!(world.getBlockState(pos).getBlock() instanceof BlockFarmland)) {
            return false;
        }
        IBlockState state = world.getBlockState(pos.up());
        boolean mature = state.getBlock() instanceof BlockCrops && ((BlockCrops) state.getBlock()).isMaxAge(state);
        if (mature && VillagerFood.wantsMoreFood(villager) && (!active || harvest)) {
            harvest = true;
            active = true;
            return true;
        }
        if (world.isAirBlock(pos.up()) && (!active || !harvest) && findSeed(world, pos) >= 0) {
            harvest = false;
            active = true;
            return true;
        }
        return false;
    }

    @Override
    public void updateTask() {
        super.updateTask();
        villager.getLookHelper().setLookPosition(destinationBlock.getX() + 0.5D,
                destinationBlock.getY() + 1, destinationBlock.getZ() + 0.5D, 10,
                villager.getVerticalFaceSpeed());
        if (!getIsAboveDestination() || !ForgeEventFactory.getMobGriefingEvent(villager.world, villager)) {
            return;
        }
        BlockPos pos = destinationBlock.up();
        IBlockState state = villager.world.getBlockState(pos);
        if (harvest && state.getBlock() instanceof BlockCrops && ((BlockCrops) state.getBlock()).isMaxAge(state)) {
            villager.world.destroyBlock(pos, true);
        } else if (!harvest && villager.world.isAirBlock(pos)) {
            int seedSlot = findSeed(villager.world, destinationBlock);
            if (seedSlot >= 0) {
                InventoryBasic inventory = villager.getVillagerInventory();
                IPlantable seed = (IPlantable) inventory.getStackInSlot(seedSlot).getItem();
                if (villager.world.setBlockState(pos, seed.getPlant(villager.world, pos), 3)) {
                    inventory.decrStackSize(seedSlot, 1);
                }
            }
        }
        active = false;
        runDelay = 10;
    }

    private int findSeed(World world, BlockPos soilPos) {
        IBlockState soil = world.getBlockState(soilPos);
        InventoryBasic inventory = villager.getVillagerInventory();
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof IPlantable) {
                IPlantable seed = (IPlantable) stack.getItem();
                if (seed.getPlantType(world, soilPos.up()) == EnumPlantType.Crop
                        && soil.getBlock().canSustainPlant(soil, world, soilPos, EnumFacing.UP, seed)
                        && seed.getPlant(world, soilPos.up()).getBlock().canPlaceBlockAt(world, soilPos.up())) {
                    return slot;
                }
            }
        }
        return -1;
    }
}
