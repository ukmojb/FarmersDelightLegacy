package com.wdcftgg.farmersdelightlegacy.common.registry;

import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockCuttingBoard;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CuttingBoardRecipeManager;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCuttingBoard;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

public final class ModDispenserBehaviors {
    private static final Set<Item> WRAPPED_ITEMS = new HashSet<>();

    private ModDispenserBehaviors() {
    }

    public static void registerAll() {
        for (Item item : ForgeRegistries.ITEMS) {
            if (item == null || WRAPPED_ITEMS.contains(item)) {
                continue;
            }
            IBehaviorDispenseItem originalBehavior = BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(item);
            if (originalBehavior instanceof CuttingBoardDispenserBehavior) {
                WRAPPED_ITEMS.add(item);
                continue;
            }
            BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(item, new CuttingBoardDispenserBehavior(originalBehavior));
            WRAPPED_ITEMS.add(item);
        }
    }

    private static final class CuttingBoardDispenserBehavior implements IBehaviorDispenseItem {
        private final IBehaviorDispenseItem fallbackBehavior;

        private CuttingBoardDispenserBehavior(IBehaviorDispenseItem fallbackBehavior) {
            this.fallbackBehavior = fallbackBehavior == null ? IBehaviorDispenseItem.DEFAULT_BEHAVIOR : fallbackBehavior;
        }

        @Override
        public ItemStack dispense(IBlockSource source, ItemStack stack) {
            if (Configuration.dispenserUsesToolsOnCuttingBoard && tryProcessCuttingBoard(source, stack)) {
                source.getWorld().playEvent(1000, source.getBlockPos(), 0);
                return stack;
            }
            return this.fallbackBehavior.dispense(source, stack);
        }

        private boolean tryProcessCuttingBoard(IBlockSource source, ItemStack stack) {
            if (stack.isEmpty() || !CuttingBoardRecipeManager.isUsedAsRecipeTool(stack)) {
                return false;
            }

            World world = source.getWorld();
            EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
            BlockPos cuttingBoardPos = source.getBlockPos().offset(facing);
            IBlockState cuttingBoardState = world.getBlockState(cuttingBoardPos);
            if (!(cuttingBoardState.getBlock() instanceof BlockCuttingBoard)) {
                return false;
            }

            TileEntity tileEntity = world.getTileEntity(cuttingBoardPos);
            if (!(tileEntity instanceof TileEntityCuttingBoard)) {
                return false;
            }

            TileEntityCuttingBoard cuttingBoard = (TileEntityCuttingBoard) tileEntity;
            if (cuttingBoard.isEmpty() || !CuttingBoardRecipeManager.hasRecipe(cuttingBoard.getStoredItem(), stack)) {
                return false;
            }

            if (!world.isRemote) {
                BlockCuttingBoard.processStoredItemFromDispenser(world, cuttingBoardPos, cuttingBoardState, cuttingBoard, stack);
            }
            return true;
        }
    }
}
