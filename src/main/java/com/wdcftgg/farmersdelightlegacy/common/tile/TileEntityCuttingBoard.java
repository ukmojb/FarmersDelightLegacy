package com.wdcftgg.farmersdelightlegacy.common.tile;

import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.CuttingBoardRecipeManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Collections;
import java.util.List;

public class TileEntityCuttingBoard extends TileEntity implements ISidedInventory {

    private static final int INPUT_SLOT = 0;
    private static final int[] INPUT_SLOTS = new int[]{INPUT_SLOT};

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
        if (this.storedItem.isEmpty()) {
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
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return index == INPUT_SLOT ? this.storedItem : ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (index != INPUT_SLOT || count <= 0 || this.storedItem.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return removeStoredItem();
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return index == INPUT_SLOT ? removeStoredItem() : ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index != INPUT_SLOT) {
            return;
        }

        if (stack.isEmpty()) {
            this.storedItem = ItemStack.EMPTY;
        } else {
            this.storedItem = stack.copy();
            this.storedItem.setCount(1);
        }
        this.itemCarvingBoard = false;
        markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return this.world != null
                && this.world.getTileEntity(this.pos) == this
                && player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index == INPUT_SLOT
                && !stack.isEmpty()
                && !CuttingBoardRecipeManager.isUsedAsRecipeTool(stack);
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        if (!this.storedItem.isEmpty() || this.itemCarvingBoard) {
            this.storedItem = ItemStack.EMPTY;
            this.itemCarvingBoard = false;
            markDirty();
        }
    }

    @Override
    public String getName() {
        return "tile.farmersdelight.cutting_board.name";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(this.getName());
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return INPUT_SLOTS;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return this.storedItem.isEmpty() && this.isItemValidForSlot(index, itemStackIn);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return false;
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

