package com.wdcftgg.farmersdelightlegacy.common.tile;

import com.wdcftgg.farmersdelight.Tags;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockBasket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.List;

public class TileEntityBasket extends TileEntity implements IInventory, ITickable {

    private static final int SLOT_COUNT = 27;

    private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private String customName;
    private int transferCooldown = -1;

    @Override
    public void update() {
        if (this.world == null || this.world.isRemote) {
            return;
        }

        this.transferCooldown--;

        if (!isOnTransferCooldown()) {
            setTransferCooldown(0);
            int facing = EnumFacing.UP.getIndex();
            if (this.world.getBlockState(this.pos).getBlock() instanceof BlockBasket) {
                facing = this.world.getBlockState(this.pos).getValue(BlockBasket.FACING).getIndex();
            }
            int facingIndex = facing;
            updateTransfer(() -> pullItems(this.world, this, facingIndex));
        }
    }

    private interface TransferAction {
        boolean run();
    }

    private void updateTransfer(TransferAction action) {
        if (this.world == null || this.world.isRemote) {
            return;
        }

        if (!(this.world.getBlockState(this.pos).getBlock() instanceof BlockBasket)) {
            return;
        }

        if (!this.world.getBlockState(this.pos).getValue(BlockBasket.ENABLED) || isOnTransferCooldown()) {
            return;
        }

        if (!isFull() && action.run()) {
            setTransferCooldown(8);
            markDirty();
        }
    }

    private void setTransferCooldown(int ticks) {
        this.transferCooldown = ticks;
    }

    private boolean isOnTransferCooldown() {
        return this.transferCooldown > 0;
    }

    public boolean mayTransfer() {
        return this.transferCooldown > 8;
    }

    public static boolean pullItems(World world, TileEntityBasket basket, int facingIndex) {
        for (EntityItem entityItem : getCaptureItems(world, basket, facingIndex)) {
            if (captureItem(basket, entityItem)) {
                return true;
            }
        }
        return false;
    }

    private static List<EntityItem> getCaptureItems(World world, TileEntityBasket basket, int facingIndex) {
        EnumFacing facing = EnumFacing.byIndex(facingIndex);
        if (facing == null) {
            facing = EnumFacing.UP;
        }
        AxisAlignedBB captureBox = basket.getCaptureBox(facing);
        return world.getEntitiesWithinAABB(EntityItem.class, captureBox, EntityItem::isEntityAlive);
    }

    private AxisAlignedBB getCaptureBox(EnumFacing facing) {
        BlockPos pos = this.getPos();

        switch (facing) {
            case DOWN:
                return new AxisAlignedBB(pos.getX(), pos.getY() - 1, pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            case UP:
                return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
            case NORTH:
                return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ() - 1, pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            case SOUTH:
                return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 2);
            case WEST:
                return new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            case EAST:
            default:
                return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 2, pos.getY() + 1, pos.getZ() + 1);
        }
    }

    public static boolean captureItem(IInventory inventory, EntityItem itemEntity) {
        ItemStack sourceStack = itemEntity.getItem().copy();
        ItemStack remainder = putStackInInventoryAllSlots(inventory, sourceStack);
        if (remainder.isEmpty()) {
            itemEntity.setDead();
            return true;
        }

        itemEntity.setItem(remainder);
        return false;
    }

    private static ItemStack putStackInInventoryAllSlots(IInventory destination, ItemStack stack) {
        for (int i = 0; i < destination.getSizeInventory() && !stack.isEmpty(); i++) {
            stack = insertStack(destination, stack, i);
        }
        return stack;
    }

    private static boolean canCombine(ItemStack stack1, ItemStack stack2) {
        return stack1.getCount() <= stack1.getMaxStackSize()
                && ItemStack.areItemsEqual(stack1, stack2)
                && ItemStack.areItemStackTagsEqual(stack1, stack2);
    }

    private static ItemStack insertStack(IInventory destination, ItemStack stack, int index) {
        ItemStack slotStack = destination.getStackInSlot(index);
        if (!destination.isItemValidForSlot(index, stack)) {
            return stack;
        }

        boolean changed = false;
        boolean destinationWasEmpty = destination.isEmpty();

        if (slotStack.isEmpty()) {
            destination.setInventorySlotContents(index, stack);
            stack = ItemStack.EMPTY;
            changed = true;
        } else if (canCombine(slotStack, stack)) {
            int maxSize = Math.min(destination.getInventoryStackLimit(), slotStack.getMaxStackSize());
            int canMove = Math.min(stack.getCount(), maxSize - slotStack.getCount());
            if (canMove > 0) {
                stack.shrink(canMove);
                slotStack.grow(canMove);
                changed = true;
            }
        }

        if (changed) {
            if (destinationWasEmpty && destination instanceof TileEntityBasket) {
                TileEntityBasket basket = (TileEntityBasket) destination;
                if (!basket.mayTransfer()) {
                    basket.setTransferCooldown(8);
                }
            }
            destination.markDirty();
        }

        return stack;
    }

    public void onEntityCollision(Entity entity) {
        if (!(entity instanceof EntityItem) || this.world == null || this.world.isRemote) {
            return;
        }

        EnumFacing facing = EnumFacing.UP;
        if (this.world.getBlockState(this.pos).getBlock() instanceof BlockBasket) {
            facing = this.world.getBlockState(this.pos).getValue(BlockBasket.FACING);
        }
        AxisAlignedBB captureBox = getCaptureBox(facing);
        if (captureBox.intersects(entity.getEntityBoundingBox())) {
            EntityItem entityItem = (EntityItem) entity;
            updateTransfer(() -> captureItem(this, entityItem));
        }
    }

    private boolean isFull() {
        for (ItemStack itemStack : this.itemStacks) {
            if (itemStack.isEmpty() || itemStack.getCount() < itemStack.getMaxStackSize()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getSizeInventory() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.itemStacks) {
            if (!itemStack.isEmpty()) {
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
    }

    @Override
    public void closeInventory(EntityPlayer player) {
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
        return hasCustomName() ? this.customName : Tags.MOD_ID + ".container.basket";
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
        compound.setInteger("TransferCooldown", this.transferCooldown);
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
        this.transferCooldown = compound.getInteger("TransferCooldown");
        this.customName = compound.hasKey("CustomName", 8) ? compound.getString("CustomName") : null;
    }
}

