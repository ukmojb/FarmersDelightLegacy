package com.wdcftgg.farmersdelightlegacy.common.network;

import com.wdcftgg.farmersdelightlegacy.client.particle.ParticleStar;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
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
            Minecraft.getMinecraft().addScheduledTask(() -> {
                World world = Minecraft.getMinecraft().world;
                if (world != null) {
                    for (int i = 0; i < 5; i++) {
                        ParticleStar.spawn(world,
                                message.x + (world.rand.nextDouble() * 2.0D - 1.0D) * message.width,
                                message.y + world.rand.nextDouble() * message.height + 0.5D,
                                message.z + (world.rand.nextDouble() * 2.0D - 1.0D) * message.width);
                    }
                }
            });
            return null;
        }
    }
}
