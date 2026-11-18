package com.wdcftgg.farmersdelightlegacy.client.event;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.item.ItemSkillet;
import com.wdcftgg.farmersdelightlegacy.common.item.SkilletFlip;
import com.wdcftgg.farmersdelightlegacy.common.network.ModNetworkHandler;
import com.wdcftgg.farmersdelightlegacy.common.network.PacketFlipSkillet;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID, value = Side.CLIENT)
public final class SkilletInput {
    private SkilletInput() {
    }

    @SubscribeEvent
    public static void onKeyboard(InputEvent.KeyInputEvent event) {
        flip();
    }

    @SubscribeEvent
    public static void onMouse(InputEvent.MouseInputEvent event) {
        flip();
    }

    private static void flip() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.currentScreen != null || minecraft.player == null || !minecraft.player.isHandActive()) {
            return;
        }
        ItemStack stack = minecraft.player.getActiveItemStack();
        if (!(stack.getItem() instanceof ItemSkillet)) {
            return;
        }
        boolean pressed = false;
        while (minecraft.gameSettings.keyBindAttack.isPressed()) {
            pressed = true;
        }
        if (pressed && !SkilletFlip.isFlipping(stack)) {
            ModNetworkHandler.INSTANCE.sendToServer(new PacketFlipSkillet());
        }
    }
}
