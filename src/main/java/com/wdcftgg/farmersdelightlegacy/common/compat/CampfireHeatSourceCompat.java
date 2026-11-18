package com.wdcftgg.farmersdelightlegacy.common.compat;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.api.heat.HeatSourceApi;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class CampfireHeatSourceCompat {
    private CampfireHeatSourceCompat() {
    }

    public static void registerAll() {
        for (String name : new String[]{"campfire:campfire", "campfire:brazier", "futuremc:campfire",
                "futuremc:soul_campfire", "nb:soul_campfire"}) {
            ResourceLocation id = new ResourceLocation(name);
            if (!ForgeRegistries.BLOCKS.containsKey(id)) {
                continue;
            }
            Block block = ForgeRegistries.BLOCKS.getValue(id);
            IProperty<?> lit = block.getBlockState().getProperty("lit");
            if (lit == null || lit.getValueClass() != Boolean.class) {
                FarmersDelightLegacy.LOGGER.error("Cannot register campfire heat source without a boolean lit property: {}", id);
                continue;
            }
            HeatSourceApi.registerDirectHeatSourcePredicate(name,
                    (world, pos, state) -> state.getBlock() == block && isLit(state));
        }
    }

    static boolean isLit(IBlockState state) {
        IProperty<?> lit = state.getBlock().getBlockState().getProperty("lit");
        return lit != null && Boolean.TRUE.equals(state.getProperties().get(lit));
    }
}
