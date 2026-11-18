package com.wdcftgg.farmersdelightlegacy.client.particle;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID, value = Side.CLIENT)
public final class ParticleStar extends Particle {
    private static final ResourceLocation TEXTURE = new ResourceLocation("farmersdelight", "particle/star");
    private final float fullScale;

    private ParticleStar(World world, double x, double y, double z) {
        super(world, x, y + 0.3D, z, 0.0D, 0.0D, 0.0D);
        motionX *= 0.01D;
        motionY = motionY * 0.01D + 0.1D;
        motionZ *= 0.01D;
        particleScale *= 1.5F;
        fullScale = particleScale;
        particleMaxAge = 16;
        canCollide = false;
        setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(TEXTURE.toString()));
    }

    @SubscribeEvent
    public static void registerTexture(TextureStitchEvent.Pre event) {
        event.getMap().registerSprite(TEXTURE);
    }

    public static void spawn(World world, double x, double y, double z) {
        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleStar(world, x, y, z));
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity viewer, float partialTicks,
                               float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        particleScale = fullScale * MathHelper.clamp((particleAge + partialTicks) / particleMaxAge * 32.0F, 0.0F, 1.0F);
        super.renderParticle(buffer, viewer, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (particleAge++ >= particleMaxAge) {
            setExpired();
            return;
        }
        move(motionX, motionY, motionZ);
        if (posY == prevPosY) {
            motionX *= 1.1D;
            motionZ *= 1.1D;
        }
        motionX *= 0.86D;
        motionY *= 0.86D;
        motionZ *= 0.86D;
        if (onGround) {
            motionX *= 0.7D;
            motionZ *= 0.7D;
        }
    }

    @Override
    public int getFXLayer() {
        return 1;
    }
}
