package com.wdcftgg.farmersdelightlegacy.common.tile;

import com.wdcftgg.farmersdelight.Tags;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CookingPotRecipe;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CookingPotRecipeManager;
import com.wdcftgg.farmersdelightlegacy.common.util.HeatSourceHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TileEntityCookingPot extends TileEntity implements IInventory, ITickable {

    private static final int INPUT_SLOT_COUNT = 6;
    private static final int SLOT_COUNT = 9;
    private static final int MEAL_DISPLAY_SLOT = 6;
    private static final int CONTAINER_SLOT = 7;
    private static final int OUTPUT_SLOT = 8;
    private static final Set<String> DRINK_MEAL_ITEMS = new HashSet<>(Arrays.asList(
            "apple_cider",
            "melon_juice",
            "hot_cocoa",
            "milk_bottle"
    ));

    private final List<ItemStack> itemStacks;
    private int cookTime;
    private int cookTimeTotal = 200;
    private ItemStack mealContainerStack = ItemStack.EMPTY;
    private boolean useDefaultMealContainer = true;

    public TileEntityCookingPot() {
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
        if (stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }
        this.markDirty();
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
        if (index == MEAL_DISPLAY_SLOT || index == OUTPUT_SLOT) {
            return false;
        }
        if (index == CONTAINER_SLOT) {
            return isServingContainer(stack);
        }
        return true;
    }

    @Override
    public int getField(int id) {
        if (id == 0) {
            return this.cookTime;
        }
        if (id == 1) {
            return this.cookTimeTotal;
        }
        return 0;
    }

    @Override
    public void setField(int id, int value) {
        if (id == 0) {
            this.cookTime = value;
        } else if (id == 1) {
            this.cookTimeTotal = value;
        }
    }

    @Override
    public int getFieldCount() {
        return 2;
    }

    @Override
    public void clear() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            this.itemStacks.set(i, ItemStack.EMPTY);
        }
        this.mealContainerStack = ItemStack.EMPTY;
        this.useDefaultMealContainer = true;
        this.cookTime = 0;
        this.cookTimeTotal = 200;
    }

    @Override
    public String getName() {
        return Tags.MOD_ID + ".container.cooking_pot";
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
    public void update() {
        if (this.world == null || this.world.isRemote) {
            return;
        }

        boolean didInventoryChange = false;
        int previousCookTime = this.cookTime;

        CookingPotRecipe recipe = this.hasInput() ? CookingPotRecipeManager.findRecipe(getInputStacks()) : null;
        if (isHeated() && recipe != null && canCook(recipe)) {
            this.cookTimeTotal = Math.max(1, recipe.getCookTime());
            this.cookTime++;
            if (this.cookTime >= this.cookTimeTotal) {
                this.cookTime = 0;
                processCooking(recipe);
                didInventoryChange = true;
            }
        } else if (this.cookTime > 0) {
            this.cookTime = Math.max(0, this.cookTime - 2);
        }

        ItemStack mealStack = this.itemStacks.get(MEAL_DISPLAY_SLOT);
        if (!mealStack.isEmpty()) {
            if (!doesMealRequireContainer(mealStack)) {
                if (moveMealToOutput()) {
                    didInventoryChange = true;
                }
            } else if (!this.itemStacks.get(CONTAINER_SLOT).isEmpty()) {
                if (useStoredContainersOnMeal()) {
                    didInventoryChange = true;
                }
            }
        }

        if (didInventoryChange || this.cookTime != previousCookTime) {
            this.markDirty();
        }
    }

    public boolean isHeated() {
        return HeatSourceHelper.isDirectHeatSource(this.world, this.pos.down());
    }

    private List<ItemStack> getInputStacks() {
        List<ItemStack> inputStacks = new ArrayList<>(INPUT_SLOT_COUNT);
        for (int i = 0; i < INPUT_SLOT_COUNT; i++) {
            inputStacks.add(this.itemStacks.get(i));
        }
        return inputStacks;
    }

    public boolean isBoiling() {
        if (this.world == null) {
            return false;
        }
        CookingPotRecipe recipe = hasInput() ? CookingPotRecipeManager.findRecipe(getInputStacks()) : null;
        return recipe != null && canCook(recipe) && isHeated();
    }

    public boolean hasCookedMeal() {
        return !this.itemStacks.get(MEAL_DISPLAY_SLOT).isEmpty();
    }

    private boolean canCook(CookingPotRecipe recipe) {
        ItemStack resultStack = recipe.getResultStack();
        if (resultStack.isEmpty()) {
            return false;
        }

        ItemStack outputStack = this.itemStacks.get(MEAL_DISPLAY_SLOT);
        if (outputStack.isEmpty()) {
            return true;
        }

        if (!ItemStack.areItemsEqual(outputStack, resultStack)) {
            return false;
        }

        int mergedCount = outputStack.getCount() + resultStack.getCount();
        return mergedCount <= this.getInventoryStackLimit() && mergedCount <= outputStack.getMaxStackSize();
    }

    private void processCooking(CookingPotRecipe recipe) {
        ItemStack resultStack = recipe.getResultStack();
        ItemStack outputStack = this.itemStacks.get(MEAL_DISPLAY_SLOT);
        if (outputStack.isEmpty()) {
            this.itemStacks.set(MEAL_DISPLAY_SLOT, resultStack.copy());
        } else {
            outputStack.grow(resultStack.getCount());
        }

        ItemStack configuredContainer = recipe.getOutputContainer();
        this.mealContainerStack = configuredContainer.isEmpty() ? getMealCraftingRemainder(resultStack) : configuredContainer.copy();
        this.useDefaultMealContainer = !recipe.hasContainerDefinition();

        for (CookingPotRecipe.IngredientEntry ingredientEntry : recipe.getIngredients()) {
            consumeOneIngredient(ingredientEntry);
        }
    }

    private void consumeOneIngredient(CookingPotRecipe.IngredientEntry ingredientEntry) {
        for (int i = 0; i < INPUT_SLOT_COUNT; i++) {
            ItemStack inputStack = this.itemStacks.get(i);
            if (!inputStack.isEmpty() && ingredientEntry.matches(inputStack)) {
                ItemStack consumedStack = inputStack.copy();
                consumedStack.setCount(1);
                inputStack.shrink(1);
                if (inputStack.getCount() <= 0) {
                    this.itemStacks.set(i, ItemStack.EMPTY);
                }
                ItemStack ingredientRemainder = getIngredientRemainder(consumedStack);
                if (!ingredientRemainder.isEmpty()) {
                    ejectIngredientRemainder(ingredientRemainder);
                }
                return;
            }
        }
    }

    private boolean hasInput() {
        for (int i = 0; i < INPUT_SLOT_COUNT; i++) {
            if (!this.itemStacks.get(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private ItemStack getIngredientRemainder(ItemStack consumedStack) {
        if (consumedStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack containerItem = consumedStack.getItem().getContainerItem(consumedStack);
        if (containerItem.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return containerItem.copy();
    }

    private void ejectIngredientRemainder(ItemStack remainderStack) {
        if (this.world == null || remainderStack.isEmpty()) {
            return;
        }
        InventoryHelper.spawnItemStack(this.world,
                this.pos.getX() + 0.5D,
                this.pos.getY() + 0.7D,
                this.pos.getZ() + 0.5D,
                remainderStack.copy());
    }

    private boolean moveMealToOutput() {
        ItemStack mealStack = this.itemStacks.get(MEAL_DISPLAY_SLOT);
        ItemStack outputStack = this.itemStacks.get(OUTPUT_SLOT);
        if (mealStack.isEmpty()) {
            return false;
        }

        int transferableCount;
        if (outputStack.isEmpty()) {
            transferableCount = Math.min(mealStack.getCount(), mealStack.getMaxStackSize());
            this.itemStacks.set(OUTPUT_SLOT, mealStack.splitStack(transferableCount));
            if (mealStack.getCount() <= 0) {
                this.itemStacks.set(MEAL_DISPLAY_SLOT, ItemStack.EMPTY);
            }
            return transferableCount > 0;
        }

        if (!ItemStack.areItemsEqual(outputStack, mealStack)) {
            return false;
        }

        transferableCount = Math.min(mealStack.getCount(), outputStack.getMaxStackSize() - outputStack.getCount());
        if (transferableCount <= 0) {
            return false;
        }

        outputStack.grow(transferableCount);
        mealStack.shrink(transferableCount);
        if (mealStack.getCount() <= 0) {
            this.itemStacks.set(MEAL_DISPLAY_SLOT, ItemStack.EMPTY);
        }
        return true;
    }

    private boolean useStoredContainersOnMeal() {
        ItemStack mealStack = this.itemStacks.get(MEAL_DISPLAY_SLOT);
        ItemStack containerInputStack = this.itemStacks.get(CONTAINER_SLOT);
        ItemStack outputStack = this.itemStacks.get(OUTPUT_SLOT);
        if (mealStack.isEmpty() || containerInputStack.isEmpty() || !isContainerValid(containerInputStack)) {
            return false;
        }

        int transferCount;
        if (outputStack.isEmpty()) {
            transferCount = Math.min(mealStack.getCount(), containerInputStack.getCount());
            transferCount = Math.min(transferCount, mealStack.getMaxStackSize());
            if (transferCount <= 0) {
                return false;
            }
            containerInputStack.shrink(transferCount);
            this.itemStacks.set(OUTPUT_SLOT, mealStack.splitStack(transferCount));
        } else {
            if (!ItemStack.areItemsEqual(outputStack, mealStack)) {
                return false;
            }
            transferCount = Math.min(mealStack.getCount(), containerInputStack.getCount());
            transferCount = Math.min(transferCount, outputStack.getMaxStackSize() - outputStack.getCount());
            if (transferCount <= 0) {
                return false;
            }
            containerInputStack.shrink(transferCount);
            mealStack.shrink(transferCount);
            outputStack.grow(transferCount);
        }

        if (mealStack.getCount() <= 0) {
            this.itemStacks.set(MEAL_DISPLAY_SLOT, ItemStack.EMPTY);
        }
        if (containerInputStack.getCount() <= 0) {
            this.itemStacks.set(CONTAINER_SLOT, ItemStack.EMPTY);
        }
        return true;
    }

    public ItemStack useHeldItemOnMeal(ItemStack heldStack) {
        if (!isContainerValid(heldStack)) {
            return ItemStack.EMPTY;
        }

        ItemStack mealStack = this.itemStacks.get(MEAL_DISPLAY_SLOT);
        if (mealStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        heldStack.shrink(1);
        ItemStack serving = mealStack.splitStack(1);
        if (mealStack.getCount() <= 0) {
            this.itemStacks.set(MEAL_DISPLAY_SLOT, ItemStack.EMPTY);
        }
        this.markDirty();
        return serving;
    }

    public ItemStack getContainer() {
        ItemStack mealStack = this.itemStacks.get(MEAL_DISPLAY_SLOT);
        return inferServingContainerForMeal(mealStack, this.mealContainerStack, this.useDefaultMealContainer);
    }

    public static ItemStack inferServingContainerForMeal(ItemStack mealStack, ItemStack configuredContainer, boolean useDefaultContainer) {
        if (mealStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!configuredContainer.isEmpty()) {
            return configuredContainer.copy();
        }

        ItemStack mealRemainder = mealStack.getItem().getContainerItem(mealStack);
        if (!mealRemainder.isEmpty()) {
            return mealRemainder.copy();
        }

        if (!useDefaultContainer) {
            return ItemStack.EMPTY;
        }

        if (isDrinkMealStatic(mealStack)) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }
        // 上游语义：多数餐食默认使用碗分装。
        return new ItemStack(Items.BOWL);
    }

    public boolean isContainerValid(ItemStack containerItem) {
        if (containerItem.isEmpty()) {
            return false;
        }
        ItemStack expectedContainer = getContainer();
        if (!expectedContainer.isEmpty()) {
            return ItemStack.areItemsEqual(expectedContainer, containerItem);
        }
        return false;
    }

    public int getCookProgressionScaled(int progressWidth) {
        if (this.cookTimeTotal <= 0 || this.cookTime <= 0) {
            return 0;
        }
        return this.cookTime * progressWidth / this.cookTimeTotal;
    }

    private boolean doesMealHaveContainer(ItemStack mealStack) {
        return !getContainer().isEmpty();
    }

    private boolean doesMealRequireContainer(ItemStack mealStack) {
        return doesMealHaveContainer(mealStack);
    }

    private ItemStack getMealCraftingRemainder(ItemStack mealStack) {
        if (mealStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack container = mealStack.getItem().getContainerItem(mealStack);
        return container.isEmpty() ? ItemStack.EMPTY : container.copy();
    }

    public boolean isServingContainer(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() == Items.BOWL
                || stack.getItem() == Items.GLASS_BOTTLE
                || stack.getItem() == Items.BUCKET
                || isContainerValid(stack);
    }

    private boolean isDrinkMeal(ItemStack mealStack) {
        return isDrinkMealStatic(mealStack);
    }

    private static boolean isDrinkMealStatic(ItemStack mealStack) {
        if (mealStack.isEmpty() || mealStack.getItem().getRegistryName() == null) {
            return false;
        }
        ResourceLocation mealId = mealStack.getItem().getRegistryName();
        return Tags.MOD_ID.equals(mealId.getNamespace()) && DRINK_MEAL_ITEMS.contains(mealId.getPath());
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
        compound.setInteger("CookTime", this.cookTime);
        compound.setInteger("CookTimeTotal", this.cookTimeTotal);
        compound.setBoolean("UseDefaultMealContainer", this.useDefaultMealContainer);
        if (!this.mealContainerStack.isEmpty()) {
            compound.setTag("MealContainer", this.mealContainerStack.writeToNBT(new NBTTagCompound()));
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
            if (slot >= 0 && slot < SLOT_COUNT) {
                this.itemStacks.set(slot, new ItemStack(stackTag));
            }
        }
        this.cookTime = compound.getInteger("CookTime");
        this.cookTimeTotal = compound.getInteger("CookTimeTotal");
        this.useDefaultMealContainer = !compound.hasKey("UseDefaultMealContainer") || compound.getBoolean("UseDefaultMealContainer");
        if (compound.hasKey("MealContainer", 10)) {
            this.mealContainerStack = new ItemStack(compound.getCompoundTag("MealContainer"));
        } else {
            this.mealContainerStack = ItemStack.EMPTY;
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
