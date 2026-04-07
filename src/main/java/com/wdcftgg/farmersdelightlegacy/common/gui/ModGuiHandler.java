package com.wdcftgg.farmersdelightlegacy.common.gui;

import com.wdcftgg.farmersdelightlegacy.client.gui.GuiCookingPot;
import com.wdcftgg.farmersdelightlegacy.common.inventory.ContainerBasket;
import com.wdcftgg.farmersdelightlegacy.common.inventory.ContainerCabinet;
import com.wdcftgg.farmersdelightlegacy.common.inventory.ContainerCookingPot;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityBasket;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCabinet;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCookingPot;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public final class ModGuiHandler implements IGuiHandler {

    public static final int COOKING_POT_GUI_ID = 1;
    public static final int CABINET_GUI_ID = 2;
    public static final int BASKET_GUI_ID = 3;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if (id == COOKING_POT_GUI_ID && tileEntity instanceof TileEntityCookingPot) {
            return new ContainerCookingPot(player.inventory, (TileEntityCookingPot) tileEntity);
        }
        if (id == CABINET_GUI_ID && tileEntity instanceof TileEntityCabinet) {
            return new ContainerCabinet(player.inventory, (TileEntityCabinet) tileEntity);
        }
        if (id == BASKET_GUI_ID && tileEntity instanceof TileEntityBasket) {
            return new ContainerBasket(player.inventory, (TileEntityBasket) tileEntity);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if (id == COOKING_POT_GUI_ID && tileEntity instanceof TileEntityCookingPot) {
            return new GuiCookingPot(player.inventory, (TileEntityCookingPot) tileEntity);
        }
        if (id == CABINET_GUI_ID && tileEntity instanceof TileEntityCabinet) {
            return new GuiChest(player.inventory, (TileEntityCabinet) tileEntity);
        }
        if (id == BASKET_GUI_ID && tileEntity instanceof TileEntityBasket) {
            return new GuiChest(player.inventory, (TileEntityBasket) tileEntity);
        }
        return null;
    }
}

