package com.wdcftgg.farmersdelightlegacy.common.tile;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockCookingPot;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockFeast;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CookingPotRecipe;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CookingExperience;
import com.wdcftgg.farmersdelightlegacy.common.inventory.CookingPotItemHandler;
import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.CookingPotRecipeManager;
import com.wdcftgg.farmersdelightlegacy.common.util.CookingPotParticleDispatcher;
import com.wdcftgg.farmersdelightlegacy.common.util.HeatSourceHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public class TileEntityCookingPot extends TileEntity implements IInventory, ISidedInventory, ITickable {

    private static final int INPUT_SLOT_COUNT = 6;
    private static final int SLOT_COUNT = 9;
    private static final int MEAL_DISPLAY_SLOT = 6;
    private static final int CONTAINER_SLOT = 7;
    private static final int OUTPUT_SLOT = 8;
    public static final String TAG_BLOCK_ENTITY = "BlockEntityTag";
    public static final String TAG_MEAL = "Meal";
    public static final String TAG_MEAL_CONTAINER = "MealContainer";
    public static final String TAG_USE_DEFAULT_CONTAINER = "UseDefaultMealContainer";
    private static final int[] TOP_SLOTS = new int[]{0, 1, 2, 3, 4, 5};
    private static final int[] SIDE_SLOTS = new int[]{CONTAINER_SLOT, OUTPUT_SLOT};

    private final List<ItemStack> itemStacks;
    private int cookTime;
    private int cookTimeTotal = 200;
    private ItemStack mealContainerStack = ItemStack.EMPTY;
    private boolean useDefaultMealContainer = true;
    private final CookingExperience cookingExperience = new CookingExperience();
    private String customName = "";
    private final IItemHandler inputHandler = new CookingPotItemHandler(this, EnumFacing.UP);
    private final IItemHandler outputHandler = new CookingPotItemHandler(this, EnumFacing.DOWN);

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
        this.cookingExperience.clear();
        this.cookTime = 0;
        this.cookTimeTotal = 200;
    }

    @Override
    public String getName() {
        return hasCustomName() ? customName : FarmersDelightLegacy.MOD_ID + ".container.cooking_pot";
    }

    @Override
    public boolean hasCustomName() {
        return !customName.isEmpty();
    }

    public void setCustomName(String name) {
        this.customName = name;
        markDirty();
    }

    @Override
    public ITextComponent getDisplayName() {
        return hasCustomName() ? new TextComponentString(customName) : new TextComponentTranslation(this.getName());
    }

    @Override
    public void update() {
        if (this.world == null) {
            return;
        }

        if (this.world.isRemote) {
            animationTick();
            return;
        }

        updateSupportState();

        boolean didInventoryChange = false;
        int previousCookTime = this.cookTime;

        CookingPotRecipe recipe = this.hasInput() ? CookingPotRecipeManager.findRecipe(getInputStacks()) : null;
        ItemStack result = recipe != null ? recipe.assemble(getInputStacks()) : ItemStack.EMPTY;
        if (isHeated() && recipe != null && canCook(result)) {
            this.cookTimeTotal = Math.max(1, recipe.getCookTime());
            this.cookTime++;
            if (this.cookTime >= this.cookTimeTotal) {
                this.cookTime = 0;
                processCooking(recipe, result);
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

    private void animationTick() {
        if (!isHeated()) {
            return;
        }

        if (this.world.rand.nextFloat() < 0.2F) {
            double x = this.pos.getX() + 0.5D + (this.world.rand.nextDouble() * 0.6D - 0.3D);
            double y = this.pos.getY() + 0.7D;
            double z = this.pos.getZ() + 0.5D + (this.world.rand.nextDouble() * 0.6D - 0.3D);
            CookingPotParticleDispatcher.spawnCookingPotBubble(this.world, x, y, z, 0.0D, 0.0D, 0.0D);
        }

        if (this.world.rand.nextFloat() < 0.05F) {
            double x = this.pos.getX() + 0.5D + (this.world.rand.nextDouble() * 0.4D - 0.2D);
            double y = this.pos.getY() + 0.7D;
            double z = this.pos.getZ() + 0.5D + (this.world.rand.nextDouble() * 0.4D - 0.2D);
            double motionY = this.world.rand.nextBoolean() ? 0.015D : 0.005D;
            CookingPotParticleDispatcher.spawnSteam(this.world, x, y, z, 0.0D, motionY, 0.0D);
        }
    }

    public boolean isHeated() {
        return HeatSourceHelper.isCookwareHeated(this.world, this.pos);
    }

    private void updateSupportState() {
        IBlockState state = this.world.getBlockState(this.pos);
        if (!(state.getBlock() instanceof BlockCookingPot)) {
            return;
        }

        boolean support = HeatSourceHelper.hasVisualSupportForCookware(this.world, this.pos);
        if (state.getValue(BlockCookingPot.SUPPORT) != support) {
            NBTTagCompound savedData = this.writeToNBT(new NBTTagCompound());
            this.world.setBlockState(this.pos, state.withProperty(BlockCookingPot.SUPPORT, support), 2);
            TileEntity te = this.world.getTileEntity(this.pos);
            if (te instanceof TileEntityCookingPot) {
                te.readFromNBT(savedData);
            }
        }
    }

    private List<ItemStack> getInputStacks() {
        List<ItemStack> inputStacks = new ArrayList<>(INPUT_SLOT_COUNT);
        for (int i = 0; i < INPUT_SLOT_COUNT; i++) {
            inputStacks.add(this.itemStacks.get(i).copy());
        }
        return inputStacks;
    }

    public boolean isBoiling() {
        if (this.world == null) {
            return false;
        }
        CookingPotRecipe recipe = hasInput() ? CookingPotRecipeManager.findRecipe(getInputStacks()) : null;
        return recipe != null && canCook(recipe.assemble(getInputStacks())) && isHeated();
    }

    public boolean hasCookedMeal() {
        return !this.itemStacks.get(MEAL_DISPLAY_SLOT).isEmpty();
    }

    private boolean canCook(ItemStack resultStack) {
        if (resultStack.isEmpty() || resultStack.getCount() > getInventoryStackLimit()) {
            return false;
        }

        ItemStack outputStack = this.itemStacks.get(MEAL_DISPLAY_SLOT);
        if (outputStack.isEmpty()) {
            return true;
        }

        if (!ItemStack.areItemsEqual(outputStack, resultStack) || !ItemStack.areItemStackTagsEqual(outputStack, resultStack)) {
            return false;
        }

        int mergedCount = outputStack.getCount() + resultStack.getCount();
        return mergedCount <= this.getInventoryStackLimit();
    }

    private void processCooking(CookingPotRecipe recipe, ItemStack resultStack) {
        CookingPotRecipe.IngredientEntry[] matched = recipe.matchIngredients(this.itemStacks.subList(0, INPUT_SLOT_COUNT));
        if (matched == null) {
            com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy.LOGGER.error("Cooking inputs no longer match recipe {}", recipe.getRecipeId());
            return;
        }
        ItemStack outputStack = this.itemStacks.get(MEAL_DISPLAY_SLOT);
        if (outputStack.isEmpty()) {
            this.itemStacks.set(MEAL_DISPLAY_SLOT, resultStack.copy());
        } else {
            outputStack.grow(resultStack.getCount());
        }

        ItemStack configuredContainer = recipe.getOutputContainer();
        this.mealContainerStack = configuredContainer.isEmpty() ? getMealCraftingRemainder(resultStack) : configuredContainer.copy();
        this.useDefaultMealContainer = !recipe.hasContainerDefinition();
        this.cookingExperience.record(recipe);

        for (int slot = 0; slot < INPUT_SLOT_COUNT; slot++) {
            if (matched[slot] != null) {
                consumeIngredientAtSlot(matched[slot], slot);
            }
        }
    }

    private void consumeIngredientAtSlot(CookingPotRecipe.IngredientEntry ingredientEntry, int slot) {
        ItemStack inputStack = this.itemStacks.get(slot);
        if (inputStack.isEmpty()) {
            return;
        }

        ItemStack consumedStack = inputStack.copy();
        consumedStack.setCount(1);
        inputStack.shrink(1);
        if (inputStack.getCount() <= 0) {
            this.itemStacks.set(slot, ItemStack.EMPTY);
        }
        ItemStack ingredientRemainder = ingredientEntry.hasCustomRemainderProvider()
                ? ingredientEntry.getCustomRemainder(consumedStack)
                : getIngredientRemainder(consumedStack);
        if (!ingredientRemainder.isEmpty()) {
            ejectIngredientRemainder(ingredientRemainder);
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

    public static ItemStack getIngredientRemainder(ItemStack consumedStack) {
        if (consumedStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack containerItem = consumedStack.getItem().getContainerItem(consumedStack);
        if (!containerItem.isEmpty()) {
            return containerItem.copy();
        }
        if (consumedStack.getItem() == Items.MUSHROOM_STEW || consumedStack.getItem() == Items.RABBIT_STEW
                || consumedStack.getItem() == Items.BEETROOT_SOUP) {
            return new ItemStack(Items.BOWL);
        }
        if (consumedStack.getItem() == Items.POTIONITEM || consumedStack.getItem() == Items.SPLASH_POTION
                || consumedStack.getItem() == Items.LINGERING_POTION || consumedStack.getItem() == Items.EXPERIENCE_BOTTLE) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }
        return ItemStack.EMPTY;
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

        if (!ItemStack.areItemsEqual(outputStack, mealStack) || !ItemStack.areItemStackTagsEqual(outputStack, mealStack)) {
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
            if (!ItemStack.areItemsEqual(outputStack, mealStack) || !ItemStack.areItemStackTagsEqual(outputStack, mealStack)) {
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

    public void awardExperience(EntityPlayer player, int servingsTaken) {
        if (player == null || servingsTaken <= 0) {
            return;
        }
        dropExperience(player.posX, player.posY + 0.5D, player.posZ);
    }

    public void dropExperience(double x, double y, double z) {
        if (this.world == null || this.world.isRemote) {
            return;
        }
        int whole = this.cookingExperience.claim(this.world.rand);
        while (whole > 0) {
            int orbValue = EntityXPOrb.getXPSplit(whole);
            whole -= orbValue;
            this.world.spawnEntity(new EntityXPOrb(this.world,
                    x, y, z,
                    orbValue));
        }
        markDirty();
    }

    public List<ItemStack> getDroppableInventoryWithoutMeal() {
        List<ItemStack> drops = new ArrayList<>();
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (i == MEAL_DISPLAY_SLOT) {
                continue;
            }
            ItemStack stack = this.itemStacks.get(i);
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
        }
        return drops;
    }

    public ItemStack getStoredMealStack() {
        return this.itemStacks.get(MEAL_DISPLAY_SLOT).copy();
    }

    public void writeToItem(ItemStack stack) {
        writeMealToItem(stack, getStoredMealStack(), getContainer(), isUsingDefaultMealContainer());
        if (hasCustomName()) {
            stack.setStackDisplayName(customName);
        }
    }

    public void readFromItem(ItemStack stack) {
        setCustomName(stack.hasDisplayName() ? stack.getDisplayName() : "");
        applyStoredMealFromStack(getMealFromItem(stack), getContainerFromItem(stack), useDefaultContainerFromItem(stack));
    }

    public void applyStoredMealFromStack(ItemStack mealStack, ItemStack containerStack, boolean useDefaultContainer) {
        this.itemStacks.set(MEAL_DISPLAY_SLOT, mealStack.isEmpty() ? ItemStack.EMPTY : mealStack.copy());
        this.mealContainerStack = containerStack.isEmpty() ? ItemStack.EMPTY : containerStack.copy();
        this.useDefaultMealContainer = useDefaultContainer;
        this.cookingExperience.clear();
        this.markDirty();
    }

    public static ItemStack getMealFromItem(ItemStack cookingPotStack) {
        if (cookingPotStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        NBTTagCompound blockEntityTag = cookingPotStack.getSubCompound(TAG_BLOCK_ENTITY);
        if (blockEntityTag == null || !blockEntityTag.hasKey(TAG_MEAL, 10)) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(blockEntityTag.getCompoundTag(TAG_MEAL));
    }

    public static ItemStack getContainerFromItem(ItemStack cookingPotStack) {
        if (cookingPotStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        NBTTagCompound blockEntityTag = cookingPotStack.getSubCompound(TAG_BLOCK_ENTITY);
        if (blockEntityTag == null || !blockEntityTag.hasKey(TAG_MEAL_CONTAINER, 10)) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(blockEntityTag.getCompoundTag(TAG_MEAL_CONTAINER));
    }

    public static boolean useDefaultContainerFromItem(ItemStack cookingPotStack) {
        NBTTagCompound blockEntityTag = cookingPotStack.getSubCompound(TAG_BLOCK_ENTITY);
        if (blockEntityTag == null) {
            return true;
        }
        return !blockEntityTag.hasKey(TAG_USE_DEFAULT_CONTAINER) || blockEntityTag.getBoolean(TAG_USE_DEFAULT_CONTAINER);
    }

    public static ItemStack consumeServingFromItem(ItemStack cookingPotStack) {
        ItemStack resultPot = cookingPotStack.copy();
        resultPot.setCount(1);
        NBTTagCompound blockEntityTag = resultPot.getSubCompound(TAG_BLOCK_ENTITY);
        if (blockEntityTag == null || !blockEntityTag.hasKey(TAG_MEAL, 10)) {
            return resultPot;
        }

        ItemStack meal = new ItemStack(blockEntityTag.getCompoundTag(TAG_MEAL));
        if (meal.isEmpty()) {
            return resultPot;
        }

        meal.shrink(1);
        if (meal.getCount() <= 0) {
            NBTTagCompound rootTag = resultPot.getTagCompound();
            if (rootTag != null) {
                rootTag.removeTag(TAG_BLOCK_ENTITY);
                if (rootTag.getSize() == 0) {
                    resultPot.setTagCompound(null);
                }
            }
            return resultPot;
        }

        blockEntityTag.setTag(TAG_MEAL, meal.writeToNBT(new NBTTagCompound()));
        return resultPot;
    }

    public static ItemStack resolveServingResult(ItemStack mealStack) {
        if (mealStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack feastServing = getFeastServingResult(mealStack);
        if (!feastServing.isEmpty()) {
            return feastServing;
        }

        ItemStack servingResult = mealStack.copy();
        servingResult.setCount(1);
        return servingResult;
    }

    public static void writeMealToItem(ItemStack cookingPotStack, ItemStack mealStack, ItemStack containerStack, boolean useDefaultContainer) {
        if (cookingPotStack.isEmpty()) {
            return;
        }
        if (mealStack.isEmpty()) {
            NBTTagCompound rootTag = cookingPotStack.getTagCompound();
            if (rootTag != null) {
                rootTag.removeTag(TAG_BLOCK_ENTITY);
                if (rootTag.getSize() == 0) {
                    cookingPotStack.setTagCompound(null);
                }
            }
            return;
        }

        NBTTagCompound blockEntityTag = cookingPotStack.getOrCreateSubCompound(TAG_BLOCK_ENTITY);
        blockEntityTag.setTag(TAG_MEAL, mealStack.copy().writeToNBT(new NBTTagCompound()));
        if (!containerStack.isEmpty()) {
            blockEntityTag.setTag(TAG_MEAL_CONTAINER, containerStack.copy().writeToNBT(new NBTTagCompound()));
        } else {
            blockEntityTag.removeTag(TAG_MEAL_CONTAINER);
        }
        blockEntityTag.setBoolean(TAG_USE_DEFAULT_CONTAINER, useDefaultContainer);
    }

    private static ItemStack getFeastServingResult(ItemStack mealStack) {
        if (!(mealStack.getItem() instanceof ItemBlock)) {
            return ItemStack.EMPTY;
        }

        Block mealBlock = ((ItemBlock) mealStack.getItem()).getBlock();
        if (!(mealBlock instanceof BlockFeast)) {
            return ItemStack.EMPTY;
        }

        return ((BlockFeast) mealBlock).getCookingPotServingStack();
    }

    public ItemStack getContainer() {
        ItemStack mealStack = this.itemStacks.get(MEAL_DISPLAY_SLOT);
        return inferServingContainerForMeal(mealStack, this.mealContainerStack, this.useDefaultMealContainer);
    }

    public boolean isUsingDefaultMealContainer() {
        return this.useDefaultMealContainer;
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
                || com.wdcftgg.farmersdelightlegacy.common.registry.ItemCategories.contains("fdServingContainers", stack)
                || isContainerValid(stack);
    }

    public boolean isCompletelyEmpty() {
        return this.isEmpty() && this.mealContainerStack.isEmpty();
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        if (side == null || side == EnumFacing.UP) {
            return TOP_SLOTS;
        }
        return SIDE_SLOTS;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        if (direction == null || direction == EnumFacing.UP) {
            return index >= 0 && index < INPUT_SLOT_COUNT && this.isItemValidForSlot(index, itemStackIn);
        }
        return index == CONTAINER_SLOT && this.isServingContainer(itemStackIn);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return direction == null || direction == EnumFacing.UP
                ? index >= 0 && index < INPUT_SLOT_COUNT : index == OUTPUT_SLOT;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(
                    facing == null || facing == EnumFacing.UP ? inputHandler : outputHandler);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos,
                                 IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
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
        if (hasCustomName()) {
            compound.setString("CustomName", customName);
        } else {
            compound.removeTag("CustomName");
        }
        compound.setInteger("CookTime", this.cookTime);
        compound.setInteger("CookTimeTotal", this.cookTimeTotal);
        this.cookingExperience.writeToNBT(compound);
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
        this.customName = compound.getString("CustomName");
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
        this.cookingExperience.readFromNBT(compound);
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
