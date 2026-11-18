package com.wdcftgg.farmersdelightlegacy.common.network;

import com.wdcftgg.farmersdelightlegacy.client.gui.CanvasSignEditorOpener;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketOpenCanvasSignEditor implements IMessage {

    private BlockPos pos;
    private boolean front;

    public PacketOpenCanvasSignEditor() {
    }

    public PacketOpenCanvasSignEditor(BlockPos pos) {
        this(pos, true);
    }

    public PacketOpenCanvasSignEditor(BlockPos pos, boolean front) {
        this.pos = pos;
        this.front = front;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.front = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        buf.writeBoolean(this.front);
    }

    public static class Handler implements IMessageHandler<PacketOpenCanvasSignEditor, IMessage> {

        @Override
        public IMessage onMessage(PacketOpenCanvasSignEditor message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> CanvasSignEditorOpener.openWhenReady(message.pos, message.front));
            return null;
        }
    }
}
