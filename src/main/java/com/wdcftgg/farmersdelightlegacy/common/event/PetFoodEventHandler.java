package com.wdcftgg.farmersdelightlegacy.common.event;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.item.ItemDogFood;
import com.wdcftgg.farmersdelightlegacy.common.item.ItemHorseFeed;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID)
public final class PetFoodEventHandler {
    private PetFoodEventHandler() {
    }

    @SubscribeEvent
    public static void onFeedPet(PlayerInteractEvent.EntityInteract event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = event.getItemStack();
        if (event.isCanceled() || player.isSpectator() || stack.isEmpty()
                || !(event.getTarget() instanceof EntityLivingBase)) {
            return;
        }
        Item item = stack.getItem();
        if ((item instanceof ItemHorseFeed || item instanceof ItemDogFood)
                && item.itemInteractionForEntity(stack, player, (EntityLivingBase) event.getTarget(), event.getHand())) {
            event.setCancellationResult(EnumActionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
}
