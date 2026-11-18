package com.wdcftgg.farmersdelightlegacy.common.network;

import com.wdcftgg.farmersdelightlegacy.common.item.SkilletFlip;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class PacketFlipSkillet implements IMessage {
    @Override
    public void fromBytes(ByteBuf buffer) {
    }

    @Override
    public void toBytes(ByteBuf buffer) {
    }

    public static final class Handler implements IMessageHandler<PacketFlipSkillet, IMessage> {
        @Override
        public IMessage onMessage(PacketFlipSkillet message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> SkilletFlip.start(player));
            return null;
        }
    }
}
