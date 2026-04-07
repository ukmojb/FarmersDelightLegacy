package com.wdcftgg.farmersdelightlegacy.common.tile;

import com.wdcftgg.farmersdelight.Tags;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockCabinet;
import com.wdcftgg.farmersdelightlegacy.common.inventory.ContainerCabinet;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModSounds;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.EnumFacing;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.List;

public class TileEntityCabinet extends TileEntity implements IInventory {

    private static final int SLOT_COUNT = 27;

    private final List<ItemStack> itemStacks;
    private String customName;
    private int viewers;

    public TileEntityCabinet() {
        this.itemStacks = new ArrayList<>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            this.itemStacks.add(ItemStack.EMPTY);
        }
    }

    @Override
    public int getSizeInventory() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.itemStacks) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return this.itemStacks.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack current = this.itemStacks.get(index);
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (current.getCount() <= count) {
            this.itemStacks.set(index, ItemStack.EMPTY);
            this.markDirty();
            return current;
        }

        ItemStack split = current.splitStack(count);
        if (current.getCount() <= 0) {
            this.itemStacks.set(index, ItemStack.EMPTY);
        }
        this.markDirty();
        return split;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack current = this.itemStacks.get(index);
        this.itemStacks.set(index, ItemStack.EMPTY);
        this.markDirty();
        return current;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.itemStacks.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }
        this.markDirty();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.world != null && this.pos != null) {
            this.world.updateComparatorOutputLevel(this.pos, this.getBlockType());
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        if (this.world == null || this.world.getTileEntity(this.pos) != this) {
            return false;
        }
        return player.getDistanceSq((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player) {
        startOpen(player);
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        stopOpen(player);
    }

    public void startOpen(EntityPlayer player) {
        if (this.world == null || this.isInvalid() || player.isSpectator()) {
            return;
        }

        this.viewers++;
        if (this.viewers == 1) {
            updateOpenState(true);
        }
        scheduleRecheck();
        this.markDirty();
    }

    public void stopOpen(EntityPlayer player) {
        if (this.world == null || this.isInvalid() || player.isSpectator()) {
            return;
        }

        this.viewers = Math.max(0, this.viewers - 1);
        if (this.viewers <= 0) {
            updateOpenState(false);
        } else {
            scheduleRecheck();
        }
        this.markDirty();
    }

    public void recheckOpen() {
        if (this.world == null || this.isInvalid() || this.pos == null) {
            return;
        }

        int activeViewers = 0;
        for (EntityPlayer player : this.world.playerEntities) {
            if (!(player.openContainer instanceof ContainerCabinet)) {
                continue;
            }

            ContainerCabinet container = (ContainerCabinet) player.openContainer;
            if (container.getTileEntityCabinet() == this) {
                activeViewers++;
            }
        }

        this.viewers = activeViewers;
        updateOpenState(activeViewers > 0);

        if (activeViewers > 0) {
            scheduleRecheck();
        }
    }

    private void scheduleRecheck() {
        if (this.world != null && this.pos != null) {
            this.world.scheduleUpdate(this.pos, this.getBlockType(), 5);
        }
    }

    private void updateOpenState(boolean open) {
        if (this.world == null || this.pos == null) {
            return;
        }

        BlockPos blockPos = this.getPos();
        if (!(this.world.getBlockState(blockPos).getBlock() instanceof BlockCabinet)) {
            return;
        }

        boolean currentlyOpen = this.world.getBlockState(blockPos).getValue(BlockCabinet.OPEN);
        if (currentlyOpen == open) {
            return;
        }

        this.world.setBlockState(blockPos, this.world.getBlockState(blockPos).withProperty(BlockCabinet.OPEN, open), 3);

        EnumFacing facing = this.world.getBlockState(blockPos).getValue(BlockCabinet.FACING);
        double soundX = blockPos.getX() + 0.5D + facing.getXOffset() * 0.5D;
        double soundY = blockPos.getY() + 0.5D + facing.getYOffset() * 0.5D;
        double soundZ = blockPos.getZ() + 0.5D + facing.getZOffset() * 0.5D;

        this.world.playSound(null,
                soundX,
                soundY,
                soundZ,
                open ? ModSounds.CABINET_OPEN : ModSounds.CABINET_CLOSE,
                SoundCategory.BLOCKS,
                0.5F,
                this.world.rand.nextFloat() * 0.1F + 0.9F);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
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
        for (int i = 0; i < SLOT_COUNT; i++) {
            this.itemStacks.set(i, ItemStack.EMPTY);
        }
    }

    @Override
    public String getName() {
        return hasCustomName() ? this.customName : Tags.MOD_ID + ".container.cabinet";
    }

    @Override
    public boolean hasCustomName() {
        return this.customName != null && !this.customName.isEmpty();
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    @Override
    public ITextComponent getDisplayName() {
        return this.hasCustomName() ? new TextComponentString(this.customName) : new TextComponentTranslation(this.getName());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = this.itemStacks.get(i);
            if (!stack.isEmpty()) {
                NBTTagCompound stackTag = new NBTTagCompound();
                stackTag.setByte("Slot", (byte) i);
                stack.writeToNBT(stackTag);
                itemList.appendTag(stackTag);
            }
        }
        compound.setTag("Items", itemList);
        if (this.hasCustomName()) {
            compound.setString("CustomName", this.customName);
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.clear();
        NBTTagList itemList = compound.getTagList("Items", 10);
        for (int i = 0; i < itemList.tagCount(); i++) {
            NBTTagCompound stackTag = itemList.getCompoundTagAt(i);
            int slot = stackTag.getByte("Slot") & 255;
            if (slot < SLOT_COUNT) {
                this.itemStacks.set(slot, new ItemStack(stackTag));
            }
        }
        this.customName = compound.hasKey("CustomName", 8) ? compound.getString("CustomName") : null;
    }
}

