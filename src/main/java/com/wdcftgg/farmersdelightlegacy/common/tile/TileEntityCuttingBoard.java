package com.wdcftgg.farmersdelightlegacy.common.tile;

import com.wdcftgg.farmersdelightlegacy.common.recipe.CuttingBoardRecipeManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.util.Collections;
import java.util.List;

public class TileEntityCuttingBoard extends TileEntity {

    private ItemStack storedItem = ItemStack.EMPTY;
    private boolean itemCarvingBoard;

    public boolean isEmpty() {
        return this.storedItem.isEmpty();
    }

    public ItemStack getStoredItem() {
        return this.storedItem.copy();
    }

    public boolean setStoredItem(ItemStack stack) {
        if (!this.storedItem.isEmpty() || stack.isEmpty()) {
            return false;
        }

        ItemStack placed = stack.copy();
        placed.setCount(1);
        this.storedItem = placed;
        this.itemCarvingBoard = false;
        markDirty();
        return true;
    }

    public boolean carveToolOnBoard(ItemStack stack) {
        if (!setStoredItem(stack)) {
            return false;
        }
        this.itemCarvingBoard = true;
        markDirty();
        return true;
    }

    public ItemStack removeStoredItem() {
        if (this.storedItem.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack removed = this.storedItem.copy();
        this.storedItem = ItemStack.EMPTY;
        this.itemCarvingBoard = false;
        markDirty();
        return removed;
    }

    public List<ItemStack> processStoredItem(ItemStack toolStack) {
        if (this.storedItem.isEmpty() || toolStack.isEmpty()) {
            return Collections.emptyList();
        }
        if (this.itemCarvingBoard) {
            return Collections.emptyList();
        }

        if (!CuttingBoardRecipeManager.hasRecipe(this.storedItem, toolStack)) {
            return Collections.emptyList();
        }

        List<ItemStack> results = CuttingBoardRecipeManager.getProcessedResults(this.storedItem, toolStack, this.world == null ? null : this.world.rand);
        this.storedItem.shrink(1);
        if (this.storedItem.getCount() <= 0) {
            this.storedItem = ItemStack.EMPTY;
            this.itemCarvingBoard = false;
        }
        markDirty();
        return results;
    }

    public boolean isItemCarvingBoard() {
        return this.itemCarvingBoard;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (!this.storedItem.isEmpty()) {
            compound.setTag("StoredItem", this.storedItem.writeToNBT(new NBTTagCompound()));
        }
        compound.setBoolean("IsItemCarved", this.itemCarvingBoard);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("StoredItem", 10)) {
            this.storedItem = new ItemStack(compound.getCompoundTag("StoredItem"));
        } else {
            this.storedItem = ItemStack.EMPTY;
        }
        this.itemCarvingBoard = compound.getBoolean("IsItemCarved");
        if (this.storedItem.isEmpty()) {
            this.itemCarvingBoard = false;
        }
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        this.readFromNBT(tag);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.world != null) {
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }
}

