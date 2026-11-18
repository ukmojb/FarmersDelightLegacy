package com.wdcftgg.farmersdelightlegacy.common.compat;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockTomatoVine;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID)
public final class PamsHarvestCraftTomatoHarvestCompat {
    private static final String PAMS_HARVESTCRAFT_MOD_ID = "harvestcraft";

    private PamsHarvestCraftTomatoHarvestCompat() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickTomatoesBeforePamsHarvestCraft(PlayerInteractEvent.RightClickBlock event) {
        if (!Loader.isModLoaded(PAMS_HARVESTCRAFT_MOD_ID)) {
            return;
        }

        IBlockState state = event.getWorld().getBlockState(event.getPos());
        Block block = state.getBlock();
        EntityPlayer player = event.getEntityPlayer();
        if (block instanceof BlockTomatoVine) {
            BlockTomatoVine tomatoBlock = (BlockTomatoVine) block;
            if (state.getValue(BlockTomatoVine.AGE) < tomatoBlock.getMaxAge()) {
                return;
            }
            BlockTomatoVine.harvestMatureTomatoes(event.getWorld(), event.getPos(), state, player);
            player.swingArm(event.getHand());
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.SUCCESS);
        }
    }
}
