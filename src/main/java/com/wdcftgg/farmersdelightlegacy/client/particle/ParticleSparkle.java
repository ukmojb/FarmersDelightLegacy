package com.wdcftgg.farmersdelightlegacy.client.particle;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID, value = Side.CLIENT)
public final class ParticleSparkle extends Particle {
    private static final ResourceLocation[] FRAMES = {
            new ResourceLocation("farmersdelight", "particle/sparkle_0"),
            new ResourceLocation("farmersdelight", "particle/sparkle_1"),
            new ResourceLocation("farmersdelight", "particle/sparkle_2"),
            new ResourceLocation("farmersdelight", "particle/sparkle_3")
    };

    private ParticleSparkle(World world, double x, double y, double z) {
        super(world, x, y, z);
        particleMaxAge = FRAMES.length;
        particleScale *= 0.75F;
        setFrame(0);
    }

    @SubscribeEvent
    public static void registerTextures(TextureStitchEvent.Pre event) {
        for (ResourceLocation frame : FRAMES) {
            event.getMap().registerSprite(frame);
        }
    }

    public static void spawn(World world, double x, double y, double z) {
        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleSparkle(world, x, y, z));
    }

    private void setFrame(int frame) {
        TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
        setParticleTexture(map.getAtlasSprite(FRAMES[frame].toString()));
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (++particleAge >= particleMaxAge) {
            setExpired();
        } else {
            setFrame(particleAge);
        }
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    @Override
    public int getBrightnessForRender(float partialTick) {
        return 15728880;
    }
}
