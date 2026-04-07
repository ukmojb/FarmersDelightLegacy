package com.wdcftgg.farmersdelightlegacy;

import com.wdcftgg.farmersdelight.Tags;
import com.wdcftgg.farmersdelightlegacy.common.gui.ModGuiHandler;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModTileEntities;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class FarmersDelightLegacy {

    @Mod.Instance(Tags.MOD_ID)
    public static FarmersDelightLegacy INSTANCE;

    public static FarmersDelightLegacy getInstance() {
        return INSTANCE;
    }

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModTileEntities.registerAll();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new ModGuiHandler());
        LOGGER.info("{} preInit 完成，准备注册内容。", Tags.MOD_NAME);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("{} init 完成。", Tags.MOD_NAME);
    }

}
