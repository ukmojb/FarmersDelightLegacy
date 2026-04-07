package com.wdcftgg.farmersdelightlegacy.common.tile;

import com.wdcftgg.farmersdelightlegacy.common.block.BlockSkillet;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CampfireCookingRecipe;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CampfireCookingRecipeManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class TileEntitySkillet extends TileEntity implements IInventory, ITickable {

    private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(1, ItemStack.EMPTY);
    private ItemStack skilletStack = ItemStack.EMPTY;
    private int fireAspectLevel;
    private int cookingTime;
    private int cookingTimeTotal;

    @Override
    public void update() {
        if (this.world == null || this.world.isRemote) {
            return;
        }

        if (isHeated()) {
            ItemStack stored = getStoredStack();
            if (stored.isEmpty()) {
                this.cookingTime = 0;
                return;
            }

            this.cookingTime++;
            if (this.cookingTime >= this.cookingTimeTotal) {
                CampfireCookingRecipe recipe = CampfireCookingRecipeManager.findRecipe(stored);
                if (recipe != null) {
                    ItemStack result = recipe.getResultStack();
                    EnumFacing direction = this.world.getBlockState(this.pos).getValue(BlockSkillet.FACING).rotateY();
                    EntityItem drop = new EntityItem(this.world,
                            this.pos.getX() + 0.5D,
                            this.pos.getY() + 0.3D,
                            this.pos.getZ() + 0.5D,
                            result.copy());
                    drop.motionX = direction.getXOffset() * 0.08D;
                    drop.motionY = 0.25D;
                    drop.motionZ = direction.getZOffset() * 0.08D;
                    this.world.spawnEntity(drop);
                }
                this.cookingTime = 0;
                this.itemStacks.get(0).shrink(1);
                if (this.itemStacks.get(0).getCount() <= 0) {
                    this.itemStacks.set(0, ItemStack.EMPTY);
                }
                markDirty();
            }
        } else if (this.cookingTime > 0) {
            this.cookingTime = Math.max(0, this.cookingTime - 2);
        }
    }

    public ItemStack addItemToCook(ItemStack addedStack, EntityPlayer player) {
        CampfireCookingRecipe recipe = CampfireCookingRecipeManager.findRecipe(addedStack);
        if (recipe == null) {
            player.sendStatusMessage(new TextComponentTranslation("farmersdelight.block.skillet.invalid_item"), true);
            return addedStack;
        }

        this.cookingTimeTotal = BlockSkillet.getSkilletCookingTime(recipe.getCookingTime(), fireAspectLevel);
        ItemStack stored = this.itemStacks.get(0);
        if (!stored.isEmpty() && !ItemStack.areItemsEqual(stored, addedStack)) {
            return addedStack;
        }

        ItemStack remainder = addedStack.copy();
        if (stored.isEmpty()) {
            ItemStack placed = remainder.splitStack(1);
            this.itemStacks.set(0, placed);
            this.cookingTime = 0;
            markDirty();
            return remainder;
        }

        if (stored.getCount() < stored.getMaxStackSize()) {
            stored.grow(1);
            remainder.shrink(1);
            markDirty();
        }
        return remainder;
    }

    public ItemStack removeItem() {
        ItemStack stored = this.itemStacks.get(0);
        this.itemStacks.set(0, ItemStack.EMPTY);
        this.cookingTime = 0;
        this.cookingTimeTotal = 0;
        markDirty();
        return stored;
    }

    public ItemStack getStoredStack() {
        return this.itemStacks.get(0);
    }

    public void setSkilletItem(ItemStack stack) {
        this.skilletStack = stack.copy();
        this.skilletStack.setCount(1);
        this.fireAspectLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, this.skilletStack);
        markDirty();
    }

    public ItemStack getSkilletItem() {
        if (this.skilletStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return this.skilletStack.copy();
    }

    public boolean isCooking() {
        return isHeated() && !getStoredStack().isEmpty();
    }

    private boolean isHeated() {
        if (this.world == null) {
            return false;
        }
        if (!this.world.isBlockLoaded(this.pos.down())) {
            return false;
        }
        return this.world.getBlockState(this.pos).getValue(BlockSkillet.SUPPORT);
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.itemStacks.get(0).isEmpty();
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
            markDirty();
            return current;
        }

        ItemStack split = current.splitStack(count);
        if (current.getCount() <= 0) {
            this.itemStacks.set(index, ItemStack.EMPTY);
        }
        markDirty();
        return split;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack current = this.itemStacks.get(index);
        this.itemStacks.set(index, ItemStack.EMPTY);
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
        return 64;
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
        return CampfireCookingRecipeManager.findRecipe(stack) != null;
    }

    @Override
    public int getField(int id) {
        if (id == 0) {
            return this.cookingTime;
        }
        if (id == 1) {
            return this.cookingTimeTotal;
        }
        return 0;
    }

    @Override
    public void setField(int id, int value) {
        if (id == 0) {
            this.cookingTime = value;
        } else if (id == 1) {
            this.cookingTimeTotal = value;
        }
    }

    @Override
    public int getFieldCount() {
        return 2;
    }

    @Override
    public void clear() {
        this.itemStacks.set(0, ItemStack.EMPTY);
        this.cookingTime = 0;
        this.cookingTimeTotal = 0;
    }

    @Override
    public String getName() {
        return "container.farmersdelight.skillet";
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
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagList itemList = new NBTTagList();
        ItemStack stack = this.itemStacks.get(0);
        if (!stack.isEmpty()) {
            NBTTagCompound stackTag = new NBTTagCompound();
            stackTag.setByte("Slot", (byte) 0);
            stack.writeToNBT(stackTag);
            itemList.appendTag(stackTag);
        }
        compound.setTag("Items", itemList);
        compound.setInteger("CookTime", this.cookingTime);
        compound.setInteger("CookTimeTotal", this.cookingTimeTotal);
        if (!this.skilletStack.isEmpty()) {
            compound.setTag("Skillet", this.skilletStack.writeToNBT(new NBTTagCompound()));
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
            if (slot == 0) {
                this.itemStacks.set(0, new ItemStack(stackTag));
            }
        }
        this.cookingTime = compound.getInteger("CookTime");
        this.cookingTimeTotal = compound.getInteger("CookTimeTotal");
        if (compound.hasKey("Skillet", 10)) {
            this.skilletStack = new ItemStack(compound.getCompoundTag("Skillet"));
            this.fireAspectLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, this.skilletStack);
        } else {
            this.skilletStack = ItemStack.EMPTY;
            this.fireAspectLevel = 0;
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
            net.minecraft.block.state.IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }
}
