package com.wdcftgg.farmersdelightlegacy;

import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.advancement.ModAdvancements;
import com.wdcftgg.farmersdelightlegacy.common.command.CommandFarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.compat.*;
import com.wdcftgg.farmersdelightlegacy.common.compat.futuremc.FutureMcComposterCompat;
import com.wdcftgg.farmersdelightlegacy.common.compat.futuremc.FutureMcSmithingCompat;
import com.wdcftgg.farmersdelightlegacy.common.event.ModVillagerTradeHandler;
import com.wdcftgg.farmersdelightlegacy.common.example.HeatSourceExample;
import com.wdcftgg.farmersdelightlegacy.common.example.HuntingDropExample;
import com.wdcftgg.farmersdelightlegacy.common.gui.ModGuiHandler;
import com.wdcftgg.farmersdelightlegacy.common.network.ModNetworkHandler;
import com.wdcftgg.farmersdelightlegacy.common.recipe.LegacyHeatingRecipe;
import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.CuttingBoardRecipeManager;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModDispenserBehaviors;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModEntities;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModTileEntities;
import com.wdcftgg.farmersdelightlegacy.common.world.RichSoilPineTree;
import com.wdcftgg.farmersdelightlegacy.common.world.WildCropWorldGenerator;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = FarmersDelightLegacy.MOD_ID,
        name = FarmersDelightLegacy.MOD_NAME,
        version = Tags.VERSION,
        guiFactory = "com.wdcftgg.farmersdelightlegacy.client.gui.ConfigGuiFactory",
        dependencies = "after:oe;after:traders;after:futuremc;after:campfire;after:farmers_future_delight@[1.6.0,);after:sereneseasons;after:immersiveengineering",
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
    public void construct(FMLConstructionEvent event) {
        Configuration.loadSettingsOnly(new java.io.File(Loader.instance().getConfigDir(), FarmersDelightLegacy.MOD_ID + ".cfg"));
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Configuration.load(event.getSuggestedConfigurationFile());
        Configuration.applyRuntimeOverrides();
        WanderingTradersBackportCompat.syncTradeTable(event);
        ModAdvancements.registerAll();
        ModEntities.registerAll();
        ModTileEntities.registerAll();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new ModGuiHandler());
        ModNetworkHandler.registerAll();
        FluidloggingCompat.registerAll();
        GameRegistry.registerWorldGenerator(new WildCropWorldGenerator(), 0);
        HeatSourceExample.registerHeatSourceExample();
        HuntingDropExample.registerHuntingDropExample();
        LOGGER.info("{} preInit completed; preparing content registration.", FarmersDelightLegacy.MOD_NAME);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        RichSoilPineTree.registerAll();
        AnimalFoodCompat.registerAll();
        if (Loader.isModLoaded("sereneseasons")) {
            SereneSeasonsCompat.registerAll();
        }
        if (Loader.isModLoaded("immersiveengineering")) {
            ImmersiveEngineeringCompat.registerAll();
        }
        CampfireHeatSourceCompat.registerAll();
        if (Loader.isModLoaded("futuremc")) {
            FutureMcSmithingCompat.registerAll();
            FutureMcComposterCompat.registerAll();
        }
        if (Loader.isModLoaded(FarmersFutureDelightCompat.MOD_ID)) {
            FarmersFutureDelightCompat.registerAll();
        }
        ModVillagerTradeHandler.registerAll();
        com.wdcftgg.farmersdelightlegacy.common.world.VillageCompostPiece.registerAll();
        if (Loader.isModLoaded("villagenames")) {
            VillageNamesVillageCompat.registerAll();
        }
        LegacyHeatingRecipe.registerSmeltingRecipes();
        LOGGER.info("{} init completed.", FarmersDelightLegacy.MOD_NAME);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        Configuration.syncAfterBiomeRegistration();
        CuttingBoardRecipeManager.reloadBuiltInRecipes();
        CampfireCookingCompat.registerAll();
        ModDispenserBehaviors.registerAll();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandFarmersDelightLegacy());
    }

}
