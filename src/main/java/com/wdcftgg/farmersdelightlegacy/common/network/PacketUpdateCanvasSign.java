package com.wdcftgg.farmersdelightlegacy.common.network;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCanvasSign;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class PacketUpdateCanvasSign implements IMessage {
    private BlockPos pos;
    private boolean front;
    private final String[] lines = new String[4];

    public PacketUpdateCanvasSign() {
    }

    public PacketUpdateCanvasSign(BlockPos pos, boolean front, String[] lines) {
        this.pos = pos;
        this.front = front;
        System.arraycopy(lines, 0, this.lines, 0, 4);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        PacketBuffer packet = new PacketBuffer(buffer);
        pos = packet.readBlockPos();
        front = packet.readBoolean();
        for (int index = 0; index < lines.length; index++) {
            lines[index] = packet.readString(384);
        }
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        PacketBuffer packet = new PacketBuffer(buffer);
        packet.writeBlockPos(pos);
        packet.writeBoolean(front);
        for (String line : lines) {
            packet.writeString(line);
        }
    }

    public void apply(EntityPlayerMP player) {
        WorldServer world = player.getServerWorld();
        if (!world.isBlockLoaded(pos) || player.getDistanceSq(pos) > 64.0D) {
            return;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityCanvasSign && !((TileEntityCanvasSign) tile).applyText(player, front, lines)) {
            FarmersDelightLegacy.LOGGER.warn("Player {} attempted to update a canvas sign without an active editor at {}", player.getName(), pos);
        }
    }

    public static final class Handler implements IMessageHandler<PacketUpdateCanvasSign, IMessage> {
        @Override
        public IMessage onMessage(PacketUpdateCanvasSign message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> message.apply(player));
            return null;
        }
    }
}
