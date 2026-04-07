package com.wdcftgg.farmersdelightlegacy.common.tile;

import com.wdcftgg.farmersdelightlegacy.common.block.BlockStove;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CampfireCookingRecipe;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CampfireCookingRecipeManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityStove extends TileEntity implements IInventory, ITickable {

    private static final int SLOT_COUNT = 6;
    private static final float[][] SLOT_OFFSETS = new float[][]{
            {0.3F, 0.2F},
            {0.0F, 0.2F},
            {-0.3F, 0.2F},
            {0.3F, -0.2F},
            {0.0F, -0.2F},
            {-0.3F, -0.2F}
    };

    private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final int[] cookingTimes = new int[SLOT_COUNT];
    private final int[] cookingTimesTotal = new int[SLOT_COUNT];

    @Override
    public void update() {
        if (this.world == null) {
            return;
        }

        if (this.world.isRemote) {
            animationTick();
            return;
        }

        IBlockState state = this.world.getBlockState(this.pos);
        boolean lit = state.getBlock() instanceof BlockStove && state.getValue(BlockStove.LIT);

        if (isStoveBlockedAbove()) {
            if (!isEmpty()) {
                dropAllItems();
                markDirty();
            }
            return;
        }

        if (lit) {
            cookAndOutputItems();
            return;
        }

        for (int i = 0; i < SLOT_COUNT; i++) {
            if (this.cookingTimes[i] > 0) {
                this.cookingTimes[i] = Math.max(0, this.cookingTimes[i] - 2);
            }
        }
    }

    private void animationTick() {
        IBlockState state = this.world.getBlockState(this.pos);
        if (!(state.getBlock() instanceof BlockStove)) {
            return;
        }

        EnumFacing facing = state.getValue(BlockStove.FACING);
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (this.itemStacks.get(i).isEmpty() || this.world.rand.nextFloat() >= 0.2F) {
                continue;
            }

            float[] baseOffset = SLOT_OFFSETS[i];
            float xOffset = baseOffset[0];
            float zOffset = baseOffset[1];
            if (facing.getAxis() == EnumFacing.Axis.Z) {
                float tmp = xOffset;
                xOffset = zOffset;
                zOffset = tmp;
            }

            double x = this.pos.getX() + 0.5D - facing.getXOffset() * zOffset + facing.rotateY().getXOffset() * xOffset;
            double y = this.pos.getY() + 1.0D;
            double z = this.pos.getZ() + 0.5D - facing.getZOffset() * zOffset + facing.rotateY().getZOffset() * xOffset;
            for (int k = 0; k < 3; k++) {
                this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0D, 5.0E-4D, 0.0D);
            }
        }
    }

    private void cookAndOutputItems() {
        boolean changed = false;
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = this.itemStacks.get(i);
            if (stack.isEmpty()) {
                continue;
            }

            this.cookingTimes[i]++;
            if (this.cookingTimes[i] < this.cookingTimesTotal[i]) {
                continue;
            }

            CampfireCookingRecipe recipe = CampfireCookingRecipeManager.findRecipe(stack);
            if (recipe != null) {
                ItemStack result = recipe.getResultStack();
                if (!result.isEmpty()) {
                    EntityItem drop = new EntityItem(this.world,
                            this.pos.getX() + 0.5D,
                            this.pos.getY() + 1.0D,
                            this.pos.getZ() + 0.5D,
                            result.copy());
                    drop.motionX = this.world.rand.nextGaussian() * 0.01D;
                    drop.motionY = 0.1D;
                    drop.motionZ = this.world.rand.nextGaussian() * 0.01D;
                    this.world.spawnEntity(drop);
                }
            }

            this.itemStacks.set(i, ItemStack.EMPTY);
            this.cookingTimes[i] = 0;
            this.cookingTimesTotal[i] = 0;
            changed = true;
        }

        if (changed) {
            markDirty();
        }
    }

    public int getNextEmptySlot() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (this.itemStacks.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    public boolean addItem(ItemStack itemStackIn, CampfireCookingRecipe recipe, int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return false;
        }
        if (!this.itemStacks.get(slot).isEmpty()) {
            return false;
        }

        this.cookingTimes[slot] = 0;
        this.cookingTimesTotal[slot] = recipe.getCookingTime();
        this.itemStacks.set(slot, itemStackIn.splitStack(1));
        markDirty();
        return true;
    }

    public boolean isStoveBlockedAbove() {
        if (this.world == null) {
            return false;
        }
        IBlockState above = this.world.getBlockState(this.pos.up());
        AxisAlignedBB aboveBox = above.getCollisionBoundingBox(this.world, this.pos.up());
        if (aboveBox == null || aboveBox == net.minecraft.block.Block.NULL_AABB) {
            return false;
        }

        AxisAlignedBB grillArea = new AxisAlignedBB(
                this.pos.getX() + 3.0D / 16.0D,
                this.pos.getY() + 1.0D,
                this.pos.getZ() + 3.0D / 16.0D,
                this.pos.getX() + 13.0D / 16.0D,
                this.pos.getY() + 17.0D / 16.0D,
                this.pos.getZ() + 13.0D / 16.0D
        );
        AxisAlignedBB movedAbove = aboveBox.offset(this.pos.up());
        return aboveBox.intersects(grillArea) || movedAbove.intersects(grillArea);
    }

    private void dropAllItems() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = this.itemStacks.get(i);
            if (!stack.isEmpty()) {
                EntityItem drop = new EntityItem(this.world,
                        this.pos.getX() + 0.5D,
                        this.pos.getY() + 0.5D,
                        this.pos.getZ() + 0.5D,
                        stack.copy());
                this.world.spawnEntity(drop);
                this.itemStacks.set(i, ItemStack.EMPTY);
                this.cookingTimes[i] = 0;
                this.cookingTimesTotal[i] = 0;
            }
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
            this.cookingTimes[index] = 0;
            this.cookingTimesTotal[index] = 0;
            markDirty();
            return current;
        }

        ItemStack split = current.splitStack(count);
        if (current.getCount() <= 0) {
            this.itemStacks.set(index, ItemStack.EMPTY);
            this.cookingTimes[index] = 0;
            this.cookingTimesTotal[index] = 0;
        }
        markDirty();
        return split;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack current = this.itemStacks.get(index);
        this.itemStacks.set(index, ItemStack.EMPTY);
        this.cookingTimes[index] = 0;
        this.cookingTimesTotal[index] = 0;
        markDirty();
        return current;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.itemStacks.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }
        markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUsableByPlayer(net.minecraft.entity.player.EntityPlayer player) {
        return this.world != null
                && this.world.getTileEntity(this.pos) == this
                && player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(net.minecraft.entity.player.EntityPlayer player) {
    }

    @Override
    public void closeInventory(net.minecraft.entity.player.EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return CampfireCookingRecipeManager.findRecipe(stack) != null;
    }

    @Override
    public int getField(int id) {
        if (id >= 0 && id < SLOT_COUNT) {
            return this.cookingTimes[id];
        }
        if (id >= SLOT_COUNT && id < SLOT_COUNT * 2) {
            return this.cookingTimesTotal[id - SLOT_COUNT];
        }
        return 0;
    }

    @Override
    public void setField(int id, int value) {
        if (id >= 0 && id < SLOT_COUNT) {
            this.cookingTimes[id] = value;
            return;
        }
        if (id >= SLOT_COUNT && id < SLOT_COUNT * 2) {
            this.cookingTimesTotal[id - SLOT_COUNT] = value;
        }
    }

    @Override
    public int getFieldCount() {
        return SLOT_COUNT * 2;
    }

    @Override
    public void clear() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            this.itemStacks.set(i, ItemStack.EMPTY);
            this.cookingTimes[i] = 0;
            this.cookingTimesTotal[i] = 0;
        }
    }

    @Override
    public String getName() {
        return "container.farmersdelight.stove";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public net.minecraft.util.text.ITextComponent getDisplayName() {
        return new net.minecraft.util.text.TextComponentTranslation(this.getName());
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
        compound.setIntArray("CookingTimes", this.cookingTimes);
        compound.setIntArray("CookingTotalTimes", this.cookingTimesTotal);
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
            if (slot >= 0 && slot < SLOT_COUNT) {
                this.itemStacks.set(slot, new ItemStack(stackTag));
            }
        }

        int[] loadedCookingTimes = compound.getIntArray("CookingTimes");
        int[] loadedCookingTimesTotal = compound.getIntArray("CookingTotalTimes");
        System.arraycopy(loadedCookingTimes, 0, this.cookingTimes, 0, Math.min(SLOT_COUNT, loadedCookingTimes.length));
        System.arraycopy(loadedCookingTimesTotal, 0, this.cookingTimesTotal, 0, Math.min(SLOT_COUNT, loadedCookingTimesTotal.length));
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
