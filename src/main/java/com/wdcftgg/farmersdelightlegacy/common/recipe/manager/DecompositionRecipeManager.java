package com.wdcftgg.farmersdelightlegacy.common.recipe.manager;

import com.wdcftgg.farmersdelightlegacy.common.compat.FluidloggingCompat;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import com.wdcftgg.farmersdelightlegacy.common.registry.BlockCategories;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DecompositionRecipeManager {

    private static final Map<String, DecompositionCondition> BASE_CONDITIONS = new LinkedHashMap<>();
    private static final Map<String, DecompositionAccelerator> ACCELERATORS = new LinkedHashMap<>();
    private static final Map<String, ItemStack> ACCELERATOR_DISPLAY_STACKS = new LinkedHashMap<>();
    private static boolean defaultsRegistered;

    private DecompositionRecipeManager() {
    }

    public static void registerDefaults() {
        if (defaultsRegistered) {
            return;
        }
        defaultsRegistered = true;

        registerBaseCondition("farmersdelight:sky_light", (world, compostPos) -> getMaxNearbySkyLight(world, compostPos) > 12 ? 0.1F : 0.05F);
        registerBaseCondition("farmersdelight:water", (world, compostPos) -> hasNearbyWater(world, compostPos) ? 0.1F : 0.0F);

        registerAccelerator("farmersdelight:brown_mushroom", blockMatcher(Blocks.BROWN_MUSHROOM, 0.02F), new ItemStack(Blocks.BROWN_MUSHROOM));
        registerAccelerator("farmersdelight:red_mushroom", blockMatcher(Blocks.RED_MUSHROOM, 0.02F), new ItemStack(Blocks.RED_MUSHROOM));
        registerAccelerator("farmersdelight:mycelium", blockMatcher(Blocks.MYCELIUM, 0.02F), new ItemStack(Blocks.MYCELIUM));
        registerAccelerator("farmersdelight:podzol", (world, compostPos, testPos, state) -> {
            if (state.getBlock() == Blocks.DIRT && state.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.PODZOL) {
                return 0.02F;
            }
            return 0.0F;
        }, new ItemStack(Blocks.DIRT, 1, 2));
        registerAccelerator("farmersdelight:organic_compost", blockMatcher(ModBlocks.ORGANIC_COMPOST, 0.02F), new ItemStack(ModBlocks.ORGANIC_COMPOST));
        registerAccelerator("farmersdelight:rich_soil", blockMatcher(ModBlocks.RICH_SOIL, 0.02F), new ItemStack(ModBlocks.RICH_SOIL));
        registerAccelerator("farmersdelight:rich_soil_farmland", blockMatcher(ModBlocks.RICH_SOIL_FARMLAND, 0.02F), new ItemStack(ModBlocks.RICH_SOIL_FARMLAND));
        registerAccelerator("farmersdelight:brown_mushroom_colony", blockMatcher(ModBlocks.BROWN_MUSHROOM_COLONY, 0.02F), new ItemStack(ModBlocks.BROWN_MUSHROOM_COLONY));
        registerAccelerator("farmersdelight:red_mushroom_colony", blockMatcher(ModBlocks.RED_MUSHROOM_COLONY, 0.02F), new ItemStack(ModBlocks.RED_MUSHROOM_COLONY));
        registerAccelerator("farmersdelight:compost_activators", (world, compostPos, testPos, state) ->
                BlockCategories.contains("fdCompostActivators", state) && !isDefaultActivator(state) ? 0.02F : 0.0F, ItemStack.EMPTY);
    }

    private static boolean isDefaultActivator(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.BROWN_MUSHROOM || block == Blocks.RED_MUSHROOM || block == Blocks.MYCELIUM
                || block == Blocks.DIRT && state.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.PODZOL
                || block == ModBlocks.ORGANIC_COMPOST || block == ModBlocks.RICH_SOIL || block == ModBlocks.RICH_SOIL_FARMLAND
                || block == ModBlocks.BROWN_MUSHROOM_COLONY || block == ModBlocks.RED_MUSHROOM_COLONY;
    }

    public static void registerBaseCondition(String key, DecompositionCondition condition) {
        if (key == null || condition == null) {
            return;
        }
        synchronized (BASE_CONDITIONS) {
            BASE_CONDITIONS.put(key, condition);
        }
    }

    public static void unregisterBaseCondition(String key) {
        if (key == null) {
            return;
        }
        synchronized (BASE_CONDITIONS) {
            BASE_CONDITIONS.remove(key);
        }
    }

    public static void registerAccelerator(String key, DecompositionAccelerator accelerator, ItemStack displayStack) {
        if (key == null || accelerator == null) {
            return;
        }
        synchronized (ACCELERATORS) {
            ACCELERATORS.put(key, accelerator);
        }
        if (displayStack != null && !displayStack.isEmpty()) {
            synchronized (ACCELERATOR_DISPLAY_STACKS) {
                ACCELERATOR_DISPLAY_STACKS.put(key, displayStack.copy());
            }
        }
    }

    public static void unregisterAccelerator(String key) {
        if (key == null) {
            return;
        }
        synchronized (ACCELERATORS) {
            ACCELERATORS.remove(key);
        }
        synchronized (ACCELERATOR_DISPLAY_STACKS) {
            ACCELERATOR_DISPLAY_STACKS.remove(key);
        }
    }

    public static float getDecompositionChance(World world, BlockPos compostPos) {
        registerDefaults();
        float chance = 0.0F;
        DecompositionCondition[] baseConditions;
        DecompositionAccelerator[] accelerators;
        synchronized (BASE_CONDITIONS) {
            baseConditions = BASE_CONDITIONS.values().toArray(new DecompositionCondition[0]);
        }
        synchronized (ACCELERATORS) {
            accelerators = ACCELERATORS.values().toArray(new DecompositionAccelerator[0]);
        }

        for (DecompositionCondition condition : baseConditions) {
            chance += Math.max(0.0F, condition.getChanceBonus(world, compostPos));
        }
        for (BlockPos testPos : BlockPos.getAllInBoxMutable(compostPos.add(-1, -1, -1), compostPos.add(1, 1, 1))) {
            IBlockState state = world.getBlockState(testPos);
            for (DecompositionAccelerator accelerator : accelerators) {
                chance += Math.max(0.0F, accelerator.getChanceBonus(world, compostPos, testPos, state));
            }
        }
        return chance;
    }

    public static List<ItemStack> getAcceleratorDisplayStacks() {
        registerDefaults();
        List<ItemStack> stacks = new ArrayList<>();
        synchronized (ACCELERATOR_DISPLAY_STACKS) {
            for (ItemStack stack : ACCELERATOR_DISPLAY_STACKS.values()) {
                stacks.add(stack.copy());
            }
        }
        return stacks;
    }

    private static int getMaxNearbySkyLight(World world, BlockPos compostPos) {
        int maxSkyLight = 0;
        for (BlockPos testPos : BlockPos.getAllInBoxMutable(compostPos.add(-1, -1, -1), compostPos.add(1, 1, 1))) {
            int skyLight = world.getLightFor(EnumSkyBlock.SKY, testPos.up());
            if (skyLight > maxSkyLight) {
                maxSkyLight = skyLight;
            }
        }
        return maxSkyLight;
    }

    private static boolean hasNearbyWater(World world, BlockPos compostPos) {
        for (BlockPos testPos : BlockPos.getAllInBoxMutable(compostPos.add(-1, -1, -1), compostPos.add(1, 1, 1))) {
            if (FluidloggingCompat.containsWater(world, testPos)) {
                return true;
            }
        }
        return false;
    }

    private static DecompositionAccelerator blockMatcher(Block block, float chanceBonus) {
        return (world, compostPos, testPos, state) -> state.getBlock() == block ? chanceBonus : 0.0F;
    }

    @FunctionalInterface
    public interface DecompositionCondition {
        float getChanceBonus(World world, BlockPos compostPos);
    }

    @FunctionalInterface
    public interface DecompositionAccelerator {
        float getChanceBonus(World world, BlockPos compostPos, BlockPos testPos, IBlockState state);
    }
}
