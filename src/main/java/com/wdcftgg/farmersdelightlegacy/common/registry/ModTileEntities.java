package com.wdcftgg.farmersdelightlegacy.common.registry;

import com.wdcftgg.farmersdelight.Tags;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityBasket;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCabinet;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCookingPot;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCuttingBoard;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntitySkillet;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityStove;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class ModTileEntities {

    private ModTileEntities() {
    }

    public static void registerAll() {
        GameRegistry.registerTileEntity(TileEntityCookingPot.class, new ResourceLocation(Tags.MOD_ID, "cooking_pot"));
        GameRegistry.registerTileEntity(TileEntityCuttingBoard.class, new ResourceLocation(Tags.MOD_ID, "cutting_board"));
        GameRegistry.registerTileEntity(TileEntityCabinet.class, new ResourceLocation(Tags.MOD_ID, "cabinet"));
        GameRegistry.registerTileEntity(TileEntityBasket.class, new ResourceLocation(Tags.MOD_ID, "basket"));
        GameRegistry.registerTileEntity(TileEntityStove.class, new ResourceLocation(Tags.MOD_ID, "stove"));
        GameRegistry.registerTileEntity(TileEntitySkillet.class, new ResourceLocation(Tags.MOD_ID, "skillet"));
    }
}

