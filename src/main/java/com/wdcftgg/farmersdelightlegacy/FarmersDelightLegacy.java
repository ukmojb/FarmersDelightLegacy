package com.wdcftgg.farmersdelightlegacy;

import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.advancement.ModAdvancements;
import com.wdcftgg.farmersdelightlegacy.common.compat.CampfireHeatSourceCompat;
import com.wdcftgg.farmersdelightlegacy.common.compat.FutureMcSmithingCompat;
import com.wdcftgg.farmersdelightlegacy.common.compat.VillageNamesVillageCompat;
import com.wdcftgg.farmersdelightlegacy.common.event.HeatSourceExample;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModDispenserBehaviors;
import com.wdcftgg.farmersdelightlegacy.common.event.ModVillagerTradeHandler;
import com.wdcftgg.farmersdelightlegacy.common.gui.ModGuiHandler;
import com.wdcftgg.farmersdelightlegacy.common.recipe.LegacyHeatingRecipe;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModEntities;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModTileEntities;
import com.wdcftgg.farmersdelightlegacy.common.world.WildCropWorldGenerator;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = FarmersDelightLegacy.MOD_ID,
        name = FarmersDelightLegacy.MOD_NAME,
        version = Tags.VERSION,
        dependencies = "after:oe",
        customProperties = {
                @Mod.CustomProperty(k = "iconItem", v = "farmersdelight:stove"),
                @Mod.CustomProperty(k = "license", v = "MIT License"),
                @Mod.CustomProperty(k = "issueTrackerUrl", v = "https://github.com/ukmojb/FarmersDelightLegacy/issues")
        }
)
public class FarmersDelightLegacy {

    public static final Logger LOGGER = LogManager.getLogger(FarmersDelightLegacy.MOD_NAME);
    public static final String MOD_ID = Tags.MOD_ID;
    public static final String MOD_NAME = Tags.MOD_NAME;

    @Mod.Instance(FarmersDelightLegacy.MOD_ID)
    public static FarmersDelightLegacy INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Configuration.load(event.getSuggestedConfigurationFile());
        Configuration.applyRuntimeOverrides();
        ModAdvancements.registerAll();
        ModEntities.registerAll();
        ModTileEntities.registerAll();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new ModGuiHandler());
        GameRegistry.registerWorldGenerator(new WildCropWorldGenerator(), 0);
        HeatSourceExample.registerHeatSourceExample();
        LOGGER.info("{} preInit 完成，准备注册内容。", FarmersDelightLegacy.MOD_NAME);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        CampfireHeatSourceCompat.registerAll();
        FutureMcSmithingCompat.registerAll();
        ModVillagerTradeHandler.registerAll();
        if (Loader.isModLoaded("villagenames")) {
            VillageNamesVillageCompat.registerAll();
        }
        LOGGER.info("{} init 完成。", FarmersDelightLegacy.MOD_NAME);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LegacyHeatingRecipe.registerSmeltingRecipes();
        ModDispenserBehaviors.registerAll();
    }

}
