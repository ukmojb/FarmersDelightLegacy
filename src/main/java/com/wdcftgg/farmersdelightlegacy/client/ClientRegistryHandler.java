package com.wdcftgg.farmersdelightlegacy.client;

import com.wdcftgg.farmersdelight.Tags;
import com.wdcftgg.farmersdelightlegacy.client.render.TileEntityCuttingBoardRenderer;
import com.wdcftgg.farmersdelightlegacy.client.render.TileEntitySkilletRenderer;
import com.wdcftgg.farmersdelightlegacy.client.render.TileEntityStoveRenderer;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModItems;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCuttingBoard;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntitySkillet;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityStove;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class ClientRegistryHandler {

    private static boolean tileRenderersBound;

    private ClientRegistryHandler() {
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        if (!tileRenderersBound) {
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCuttingBoard.class, new TileEntityCuttingBoardRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySkillet.class, new TileEntitySkilletRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityStove.class, new TileEntityStoveRenderer());
            tileRenderersBound = true;
        }

        for (Item item : ModItems.ITEMS.values()) {
            if (item.getRegistryName() == null) {
                continue;
            }
            ResourceLocation location = item.getRegistryName();
            ModelLoader.setCustomModelResourceLocation(item, 0, new net.minecraft.client.renderer.block.model.ModelResourceLocation(location, "inventory"));
        }
    }
}

