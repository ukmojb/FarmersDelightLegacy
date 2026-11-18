package com.wdcftgg.farmersdelightlegacy.common.tile;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockBasket;
import com.wdcftgg.farmersdelightlegacy.common.inventory.ContainerBasket;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class TileEntityBasket extends TileEntityLockableLoot implements ITickable {

    private static final int SLOT_COUNT = 27;

    private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int transferCooldown = -1;

    @Override
    protected IItemHandler createUnSidedHandler() {
        return new InvWrapper(this) {
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (simulate) {
                    return super.insertItem(slot, stack, true);
                }
                boolean wasEmpty = TileEntityBasket.this.isEmpty();
                int originalCount = stack.getCount();
                ItemStack remainder = super.insertItem(slot, stack, false);
                if (wasEmpty && originalCount > remainder.getCount() && !mayTransfer()) {
                    setTransferCooldown(8);
                }
                return remainder;
            }
        };
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

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
        this.fillWithLoot(null);
        for (ItemStack itemStack : this.itemStacks) {
            if (!itemStack.isEmpty()) {
                return false;
            }
        }
        return true;
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
        if (this.world != null && !this.world.isRemote && !player.isSpectator()) {
            this.fillWithLoot(player);
        }
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.itemStacks;
    }

    @Override
    public String getGuiID() {
        return "minecraft:chest";
    }

    @Override
    public Container createContainer(InventoryPlayer inventory, EntityPlayer player) {
        this.fillWithLoot(player);
        return new ContainerBasket(inventory, this);
    }

    @Override
    public String getName() {
        return hasCustomName() ? this.customName : FarmersDelightLegacy.MOD_ID + ".container.basket";
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        if (hasCustomName()) {
            tag.setString("CustomName", this.customName);
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.customName = tag.hasKey("CustomName", 8) ? tag.getString("CustomName") : null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (!this.checkLootAndWrite(compound)) {
            ItemStackHelper.saveAllItems(compound, this.itemStacks);
        }
        compound.setInteger("TransferCooldown", this.transferCooldown);
        if (this.hasCustomName()) {
            compound.setString("CustomName", this.customName);
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.itemStacks.clear();
        this.lootTable = null;
        this.lootTableSeed = 0L;
        if (!this.checkLootAndRead(compound)) {
            ItemStackHelper.loadAllItems(compound, this.itemStacks);
        }
        this.transferCooldown = compound.getInteger("TransferCooldown");
        this.customName = compound.hasKey("CustomName", 8) ? compound.getString("CustomName") : null;
    }
}

