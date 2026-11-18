package com.wdcftgg.farmersdelightlegacy.client.gui;

import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCanvasSign;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT)
public final class CanvasSignEditorOpener {

    private static BlockPos pendingPos;
    private static boolean pendingFront;
    private static int remainingTicks;

    private CanvasSignEditorOpener() {
    }

    public static void openWhenReady(BlockPos pos) {
        openWhenReady(pos, true);
    }

    public static void openWhenReady(BlockPos pos, boolean front) {
        pendingPos = pos;
        pendingFront = front;
        remainingTicks = 40;
        tryOpenPendingEditor();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || pendingPos == null) {
            return;
        }
        tryOpenPendingEditor();
    }

    private static void tryOpenPendingEditor() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null || minecraft.player == null || pendingPos == null) {
            return;
        }

        TileEntity tileEntity = minecraft.world.getTileEntity(pendingPos);
        if (tileEntity instanceof TileEntityCanvasSign) {
            minecraft.displayGuiScreen(new GuiEditCanvasSign((TileEntityCanvasSign) tileEntity, pendingFront));
            pendingPos = null;
            remainingTicks = 0;
            return;
        }

        --remainingTicks;
        if (remainingTicks <= 0) {
            pendingPos = null;
        }
    }
}
