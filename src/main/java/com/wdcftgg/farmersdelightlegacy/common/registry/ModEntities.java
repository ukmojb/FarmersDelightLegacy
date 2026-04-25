package com.wdcftgg.farmersdelightlegacy.common.registry;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.entity.EntityRottenTomato;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public final class ModEntities {

    private static final int ROTTEN_TOMATO_ID = 1;

    private ModEntities() {
    }

    public static void registerAll() {
        EntityRegistry.registerModEntity(new ResourceLocation(FarmersDelightLegacy.MOD_ID, "rotten_tomato"),
                EntityRottenTomato.class,
                "rotten_tomato",
                ROTTEN_TOMATO_ID,
                FarmersDelightLegacy.INSTANCE,
                64,
                10,
                true);
    }
}

