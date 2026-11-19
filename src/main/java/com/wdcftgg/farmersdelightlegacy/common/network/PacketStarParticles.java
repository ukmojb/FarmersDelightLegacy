package com.wdcftgg.farmersdelightlegacy.common.network;

import com.wdcftgg.farmersdelightlegacy.client.particle.ParticleStar;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class PacketStarParticles implements IMessage {
    private double x;
    private double y;
    private double z;
    private float width;
    private float height;

    public PacketStarParticles() {
    }

    public PacketStarParticles(EntityLivingBase target) {
        x = target.posX;
        y = target.posY;
        z = target.posZ;
        width = target.width;
        height = target.height;
    }

    public static void send(EntityLivingBase target) {
        ModNetworkHandler.INSTANCE.sendToAllTracking(new PacketStarParticles(target), target);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        x = buffer.readDouble();
        y = buffer.readDouble();
        z = buffer.readDouble();
        width = buffer.readFloat();
        height = buffer.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeFloat(width);
        buffer.writeFloat(height);
    }

    public static final class Handler implements IMessageHandler<PacketStarParticles, IMessage> {
        @Override
        public IMessage onMessage(PacketStarParticles message, MessageContext context) {
            ParticleStar.scheduleBurst(message.x, message.y, message.z, message.width, message.height);
            return null;
        }
    }
}
