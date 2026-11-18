package com.wdcftgg.farmersdelightlegacy.client.particle;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.registry.BlockCategories;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayDeque;

@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID, value = Side.CLIENT)
public final class CampfireSmokeCompat {
    private CampfireSmokeCompat() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (event.phase != TickEvent.Phase.END || minecraft.isGamePaused()
                || minecraft.world == null || minecraft.effectRenderer == null) {
            return;
        }
        boolean futureMc = Loader.isModLoaded("futuremc");
        boolean futureDelight = Loader.isModLoaded("farmers_future_delight");
        if (!futureMc && !futureDelight) {
            return;
        }
        for (ArrayDeque<Particle> particles : minecraft.effectRenderer.fxLayers[1]) {
            for (Particle particle : particles) {
                if (particle.particleAge == 0) {
                    if (futureMc && FutureMcSmoke.matches(particle)) {
                        updateSmoke(minecraft.world, particle, "futuremc", false);
                    } else if (futureDelight && FutureDelightSmoke.matches(particle)) {
                        updateSmoke(minecraft.world, particle, "farmers_future_delight", true);
                    }
                }
            }
        }
    }

    static void updateSmoke(World world, Particle particle, String namespace, boolean adjustAlpha) {
        AxisAlignedBB bounds = particle.getBoundingBox();
        BlockPos origin = new BlockPos((bounds.minX + bounds.maxX) / 2, bounds.minY,
                (bounds.minZ + bounds.maxZ) / 2);
        int sources = 0;
        boolean signal = false;
        for (int offset = 0; offset <= 1; offset++) {
            BlockPos campfire = origin.down(offset);
            ResourceLocation id = world.getBlockState(campfire).getBlock().getRegistryName();
            if (id != null && namespace.equals(id.getNamespace())
                    && ("campfire".equals(id.getPath()) || "soul_campfire".equals(id.getPath()))) {
                sources++;
                signal |= world.getBlockState(campfire.down()).getBlock() == Blocks.HAY_BLOCK
                        || BlockCategories.contains("fdCampfireSignalSmoke", world.getBlockState(campfire.down()));
            }
        }
        if (sources == 0) {
            return;
        }
        boolean wasSignal = particle.particleMaxAge >= 280;
        if (sources > 1 || !"futuremc".equals(namespace)) {
            signal |= wasSignal;
        }
        if (signal != wasSignal) {
            particle.particleMaxAge += signal ? 200 : -200;
        }
        if (adjustAlpha) {
            particle.setAlphaF(signal ? 0.95F : 0.9F);
        }
    }

    private static final class FutureMcSmoke {
        private static boolean matches(Particle particle) {
            return particle instanceof thedarkcolour.futuremc.client.particle.CampfireParticle;
        }
    }

    private static final class FutureDelightSmoke {
        private static boolean matches(Particle particle) {
            return particle instanceof xy177.farmersfuturedelight.client.particle.ParticleCampfireSmoke;
        }
    }
}
