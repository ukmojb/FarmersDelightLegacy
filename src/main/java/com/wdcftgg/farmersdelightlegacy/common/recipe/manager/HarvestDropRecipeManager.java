package com.wdcftgg.farmersdelightlegacy.common.recipe.manager;

import com.google.common.collect.ImmutableList;
import com.wdcftgg.farmersdelightlegacy.api.recipe.knife.HuntingDropOutput;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockRicePanicles;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockWildRice;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModItems;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;

import java.util.*;

public final class HarvestDropRecipeManager {

    private static final float grassStrawDropChance = 0.2F;
    private static final Map<String, HarvestDropRecipe> recipes = new LinkedHashMap<>();
    private static final Set<String> removedRecipeKeys = new HashSet<>();
    private static boolean defaultsRegistered;

    private HarvestDropRecipeManager() {
    }

    public static synchronized boolean registerRecipe(String key, HarvestTargetMatcher targetMatcher, ItemStack outputStack) {
        return registerRecipe(key, targetMatcher, outputStack, false);
    }

    public static synchronized boolean registerRecipeJei(String key, HarvestTargetMatcher targetMatcher, ItemStack outputStack) {
        return registerRecipeJei(key, targetMatcher, outputStack, false);
    }

    public static synchronized boolean registerRecipe(String key, HarvestTargetMatcher targetMatcher, ItemStack outputStack,
                                                       boolean preventDuplicateStacking) {
        return registerRecipe(key, targetMatcher, outputStack, preventDuplicateStacking, 1.0F, 0.0F,
                getDefaultBlockState(targetMatcher), null);
    }

    public static synchronized boolean registerRecipeJei(String key, HarvestTargetMatcher targetMatcher, ItemStack outputStack,
                                                          boolean preventDuplicateStacking) {
        return registerRecipeJei(key, targetMatcher, outputStack, preventDuplicateStacking, 1.0F, 0.0F,
                getDefaultBlockState(targetMatcher), null);
    }

    public static synchronized boolean registerRecipe(String key, HarvestTargetMatcher targetMatcher, ItemStack outputStack,
                                                      boolean preventDuplicateStacking, float chance, float fortuneBonus,
                                                      IBlockState displayBlockState, IBlockState displaySupportBlockState) {
        List<HuntingDropOutput> outputs = new ArrayList<>(1);
        outputs.add(new HuntingDropOutput(outputStack, chance, fortuneBonus));
        return registerRecipe(key, targetMatcher, outputs, preventDuplicateStacking, displayBlockState, displaySupportBlockState);
    }

    public static synchronized boolean registerRecipe(String key, HarvestTargetMatcher targetMatcher, ItemStack outputStack,
                                                       boolean preventDuplicateStacking, float chance, float fortuneBonus,
                                                       List<HarvestDropDisplayBlockState> displayBlockStates,
                                                       IBlockState displaySupportBlockState) {
        List<HuntingDropOutput> outputs = new ArrayList<>(1);
        outputs.add(new HuntingDropOutput(outputStack, chance, fortuneBonus));
        return registerRecipe(key, targetMatcher, outputs, preventDuplicateStacking, displayBlockStates,
                displaySupportBlockState);
    }

    public static synchronized boolean registerRecipeJei(String key, HarvestTargetMatcher targetMatcher, ItemStack outputStack,
                                                         boolean preventDuplicateStacking, float chance, float fortuneBonus,
                                                         IBlockState displayBlockState, IBlockState displaySupportBlockState) {
        List<HuntingDropOutput> outputs = new ArrayList<>(1);
        outputs.add(new HuntingDropOutput(outputStack, chance, fortuneBonus));
        return registerRecipeJei(key, targetMatcher, outputs, preventDuplicateStacking, displayBlockState, displaySupportBlockState);
    }

    public static synchronized boolean registerRecipeJei(String key, HarvestTargetMatcher targetMatcher, ItemStack outputStack,
                                                          boolean preventDuplicateStacking, float chance, float fortuneBonus,
                                                          List<HarvestDropDisplayBlockState> displayBlockStates,
                                                          IBlockState displaySupportBlockState) {
        List<HuntingDropOutput> outputs = new ArrayList<>(1);
        outputs.add(new HuntingDropOutput(outputStack, chance, fortuneBonus));
        return registerRecipeJei(key, targetMatcher, outputs, preventDuplicateStacking, displayBlockStates,
                displaySupportBlockState);
    }

    public static synchronized boolean registerRecipe(String key, HarvestTargetMatcher targetMatcher, List<HuntingDropOutput> outputs,
                                                      boolean preventDuplicateStacking, IBlockState displayBlockState,
                                                      IBlockState displaySupportBlockState) {
        return registerRecipe(key, targetMatcher, outputs, preventDuplicateStacking,
                createDisplayBlockStates(displayBlockState), displaySupportBlockState);
    }

    public static synchronized boolean registerRecipe(String key, HarvestTargetMatcher targetMatcher, List<HuntingDropOutput> outputs,
                                                      boolean preventDuplicateStacking,
                                                      List<HarvestDropDisplayBlockState> displayBlockStates,
                                                      IBlockState displaySupportBlockState) {
        return registerRecipeInternal(key, targetMatcher, outputs, preventDuplicateStacking, displayBlockStates,
                displaySupportBlockState, false);
    }

    public static synchronized boolean registerRecipeJei(String key, HarvestTargetMatcher targetMatcher, List<HuntingDropOutput> outputs,
                                                          boolean preventDuplicateStacking, IBlockState displayBlockState,
                                                          IBlockState displaySupportBlockState) {
        return registerRecipeJei(key, targetMatcher, outputs, preventDuplicateStacking,
                createDisplayBlockStates(displayBlockState), displaySupportBlockState);
    }

    public static synchronized boolean registerRecipeJei(String key, HarvestTargetMatcher targetMatcher, List<HuntingDropOutput> outputs,
                                                         boolean preventDuplicateStacking,
                                                         List<HarvestDropDisplayBlockState> displayBlockStates,
                                                         IBlockState displaySupportBlockState) {
        return registerRecipeInternal(key, targetMatcher, outputs, preventDuplicateStacking, displayBlockStates,
                displaySupportBlockState, true);
    }

    public static HarvestDropDisplayBlockState createHarvestDropDisplayBlockState(IBlockState blockState, int offsetX,
                                                                                   int offsetY, int offsetZ) {
        return new HarvestDropDisplayBlockState(blockState, offsetX, offsetY, offsetZ);
    }

    public static synchronized boolean unregisterRecipe(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }

        registerDefaults();
        boolean removedRecipe = recipes.remove(key) != null;
        boolean addedRemovalKey = removedRecipeKeys.add(key);
        return removedRecipe || addedRemovalKey;
    }

    public static void addDrops(BlockEvent.HarvestDropsEvent event, ItemStack toolStack) {
        addDrops(event, false);
    }

    public static void addStrawDrops(BlockEvent.HarvestDropsEvent event) {
        addDrops(event, true);
    }

    public static void addStrawDrops(IBlockState state, int fortuneLevel, Random random, List<ItemStack> drops) {
        addDrops(state, fortuneLevel, random, drops, true);
    }

    private static void addDrops(BlockEvent.HarvestDropsEvent event, boolean strawOnly) {
        addDrops(event.getState(), event.getFortuneLevel(), event.getWorld().rand, event.getDrops(), strawOnly);
    }

    private static void addDrops(IBlockState state, int fortuneLevel, Random random, List<ItemStack> drops, boolean strawOnly) {
        registerDefaults();
        for (HarvestDropRecipe recipe : getRecipeSnapshot()) {
            if (recipe.jeiOnly || !recipe.matches(state)) {
                continue;
            }
            for (HuntingDropOutput output : recipe.getOutputs()) {
                if (strawOnly && output.getOutputStack().getItem() != ModItems.get("straw")) {
                    continue;
                }
                if (random.nextFloat() > output.getChanceWithLooting(fortuneLevel)) {
                    continue;
                }
                addExtraDrop(drops, output.getOutputStack(), recipe.preventDuplicateStacking);
            }
        }
    }

    public static List<HarvestDropRecipeView> getRecipes() {
        registerDefaults();
        List<HarvestDropRecipeView> views = new ArrayList<>();
        synchronized (HarvestDropRecipeManager.class) {
            for (HarvestDropRecipe recipe : recipes.values()) {
                views.add(recipe.toView());
            }
        }
        return views;
    }

    public static synchronized void registerDefaults() {
        if (defaultsRegistered) {
            return;
        }
        defaultsRegistered = true;

        registerRecipe("farmersdelight:short_grass_straw", HarvestDropRecipeManager::isShortGrass,
                Collections.singletonList(new HuntingDropOutput(itemStack("straw"), grassStrawDropChance, 0.0F)),
                false, Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS),
                Blocks.GRASS.getDefaultState());
        registerRecipe("farmersdelight:tall_grass_straw", HarvestDropRecipeManager::isTallGrass,
                Collections.singletonList(new HuntingDropOutput(itemStack("straw"), grassStrawDropChance, 0.0F)),
                false, createTallGrassDisplayBlockStates(), Blocks.GRASS.getDefaultState());
        registerRecipe("farmersdelight:wheat_straw", HarvestDropRecipeManager::isMatureWheat,
                Collections.singletonList(new HuntingDropOutput(itemStack("straw"))),
                false, Blocks.WHEAT.getDefaultState().withProperty(BlockCrops.AGE, 7),
                Blocks.FARMLAND.getDefaultState());
        registerRecipe("farmersdelight:sandy_shrub_straw", state -> state.getBlock() == ModBlocks.SANDY_SHRUB,
                Collections.singletonList(new HuntingDropOutput(itemStack("straw"), 0.3F, 0.0F)),
                false, ModBlocks.SANDY_SHRUB.getDefaultState(), Blocks.SAND.getDefaultState());
        registerRecipe("farmersdelight:rice_panicle_straw", HarvestDropRecipeManager::isMatureRicePanicle,
                Collections.singletonList(new HuntingDropOutput(itemStack("straw"))),
                false, createWildRiceDisplayBlockStates(), Blocks.DIRT.getDefaultState());
    }

    private static boolean registerRecipeInternal(String key, HarvestTargetMatcher targetMatcher, List<HuntingDropOutput> outputs,
                                                  boolean preventDuplicateStacking,
                                                  List<HarvestDropDisplayBlockState> displayBlockStates,
                                                  IBlockState displaySupportBlockState, boolean jeiOnly) {
        if (key == null || key.trim().isEmpty() || targetMatcher == null) {
            return false;
        }
        if (removedRecipeKeys.contains(key)) {
            return false;
        }
        List<HuntingDropOutput> copiedOutputs = copyOutputs(outputs);
        if (copiedOutputs.isEmpty()) {
            return false;
        }
        List<HarvestDropDisplayBlockState> copiedDisplayBlockStates = copyDisplayBlockStates(displayBlockStates);
        if (copiedDisplayBlockStates.isEmpty()) {
            copiedDisplayBlockStates = createDisplayBlockStates(Blocks.GRASS.getDefaultState());
        }
        recipes.put(key, new HarvestDropRecipe(key, targetMatcher, copiedOutputs, preventDuplicateStacking,
                copiedDisplayBlockStates, displaySupportBlockState, jeiOnly));
        return true;
    }

    private static List<HarvestDropRecipe> getRecipeSnapshot() {
        synchronized (HarvestDropRecipeManager.class) {
            return new ArrayList<>(recipes.values());
        }
    }

    private static List<HuntingDropOutput> copyOutputs(List<HuntingDropOutput> outputs) {
        if (outputs == null || outputs.isEmpty()) {
            return Collections.emptyList();
        }
        List<HuntingDropOutput> copiedOutputs = new ArrayList<>();
        for (HuntingDropOutput output : outputs) {
            if (output == null || output.isEmpty()) {
                continue;
            }
            copiedOutputs.add(new HuntingDropOutput(output.getOutputStack(), output.getChance(), output.getLootingBonus()));
        }
        return copiedOutputs.isEmpty() ? Collections.emptyList() : ImmutableList.copyOf(copiedOutputs);
    }

    private static List<HarvestDropDisplayBlockState> createDisplayBlockStates(IBlockState displayBlockState) {
        if (displayBlockState == null) {
            return Collections.emptyList();
        }
        return ImmutableList.of(createHarvestDropDisplayBlockState(displayBlockState, 0, 0, 0));
    }

    private static List<HarvestDropDisplayBlockState> createTallGrassDisplayBlockStates() {
        IBlockState lowerTallGrassState = Blocks.DOUBLE_PLANT.getDefaultState()
                .withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.GRASS)
                .withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.LOWER);
        IBlockState upperTallGrassState = lowerTallGrassState.withProperty(BlockDoublePlant.HALF,
                BlockDoublePlant.EnumBlockHalf.UPPER);
        List<HarvestDropDisplayBlockState> displayBlockStates = new ArrayList<>(2);
        displayBlockStates.add(createHarvestDropDisplayBlockState(lowerTallGrassState, 0, 0, 0));
        displayBlockStates.add(createHarvestDropDisplayBlockState(upperTallGrassState, 0, 1, 0));
        return ImmutableList.copyOf(displayBlockStates);
    }

    private static List<HarvestDropDisplayBlockState> createWildRiceDisplayBlockStates() {
        IBlockState lowerWildRiceState = ModBlocks.WILD_RICE.getDefaultState()
                .withProperty(BlockWildRice.HALF, BlockDoublePlant.EnumBlockHalf.LOWER);
        IBlockState upperWildRiceState = ModBlocks.WILD_RICE.getDefaultState()
                .withProperty(BlockWildRice.HALF, BlockDoublePlant.EnumBlockHalf.UPPER);
        List<HarvestDropDisplayBlockState> displayBlockStates = new ArrayList<>(2);
        displayBlockStates.add(createHarvestDropDisplayBlockState(lowerWildRiceState, 0, 0, 0));
        displayBlockStates.add(createHarvestDropDisplayBlockState(upperWildRiceState, 0, 1, 0));
        return ImmutableList.copyOf(displayBlockStates);
    }

    private static List<HarvestDropDisplayBlockState> copyDisplayBlockStates(
            List<HarvestDropDisplayBlockState> displayBlockStates) {
        if (displayBlockStates == null || displayBlockStates.isEmpty()) {
            return Collections.emptyList();
        }
        List<HarvestDropDisplayBlockState> copiedDisplayBlockStates = new ArrayList<>();
        for (HarvestDropDisplayBlockState displayBlockState : displayBlockStates) {
            if (displayBlockState == null || displayBlockState.getBlockState() == null) {
                continue;
            }
            copiedDisplayBlockStates.add(new HarvestDropDisplayBlockState(displayBlockState.getBlockState(),
                    displayBlockState.getOffsetX(), displayBlockState.getOffsetY(), displayBlockState.getOffsetZ()));
        }
        return copiedDisplayBlockStates.isEmpty() ? Collections.emptyList() : ImmutableList.copyOf(copiedDisplayBlockStates);
    }

    private static IBlockState getDefaultBlockState(HarvestTargetMatcher targetMatcher) {
        return Blocks.GRASS.getDefaultState();
    }

    private static ItemStack itemStack(String itemPath) {
        Item item = ModItems.get(itemPath);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    private static void addExtraDrop(List<ItemStack> drops, ItemStack stack, boolean preventDuplicateStacking) {
        if (stack.isEmpty()) {
            return;
        }
        if (preventDuplicateStacking) {
            for (ItemStack dropStack : drops) {
                if (!dropStack.isEmpty() && areStacksMergeable(dropStack, stack)) {
                    dropStack.grow(stack.getCount());
                    return;
                }
            }
        }
        drops.add(stack.copy());
    }

    private static boolean areStacksMergeable(ItemStack firstStack, ItemStack secondStack) {
        return firstStack.getItem() == secondStack.getItem()
                && firstStack.getMetadata() == secondStack.getMetadata()
                && ItemStack.areItemStackTagsEqual(firstStack, secondStack);
    }

    private static boolean isShortGrass(IBlockState state) {
        return state.getBlock() == Blocks.TALLGRASS && state.getValue(BlockTallGrass.TYPE) == BlockTallGrass.EnumType.GRASS;
    }

    private static boolean isTallGrass(IBlockState state) {
        return state.getBlock() == Blocks.DOUBLE_PLANT && state.getValue(BlockDoublePlant.VARIANT) == BlockDoublePlant.EnumPlantType.GRASS;
    }

    private static boolean isMatureWheat(IBlockState state) {
        return state.getBlock() == Blocks.WHEAT && state.getValue(BlockCrops.AGE) >= 7;
    }

    private static boolean isMatureRicePanicle(IBlockState state) {
        return state.getBlock() == ModBlocks.RICE_PANICLES && state.getValue(BlockRicePanicles.AGE) >= 3;
    }

    private static List<ItemStack> getKnifeToolOptions() {
        return KnifeItemManager.getJeiDisplayStacks();
    }

    @FunctionalInterface
    public interface HarvestTargetMatcher {
        boolean matches(IBlockState state);
    }

    public static final class HarvestDropDisplayBlockState {
        private final IBlockState blockState;
        private final int offsetX;
        private final int offsetY;
        private final int offsetZ;

        public HarvestDropDisplayBlockState(IBlockState blockState, int offsetX, int offsetY, int offsetZ) {
            this.blockState = blockState;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
        }

        public IBlockState getBlockState() {
            return this.blockState;
        }

        public int getOffsetX() {
            return this.offsetX;
        }

        public int getOffsetY() {
            return this.offsetY;
        }

        public int getOffsetZ() {
            return this.offsetZ;
        }
    }

    private static final class HarvestDropRecipe {
        private final String key;
        private final HarvestTargetMatcher targetMatcher;
        private final List<HuntingDropOutput> outputs;
        private final boolean preventDuplicateStacking;
        private final List<HarvestDropDisplayBlockState> displayBlockStates;
        private final IBlockState displaySupportBlockState;
        private final boolean jeiOnly;

        private HarvestDropRecipe(String key, HarvestTargetMatcher targetMatcher, List<HuntingDropOutput> outputs,
                                  boolean preventDuplicateStacking,
                                  List<HarvestDropDisplayBlockState> displayBlockStates,
                                  IBlockState displaySupportBlockState, boolean jeiOnly) {
            this.key = key;
            this.targetMatcher = targetMatcher;
            this.outputs = outputs;
            this.preventDuplicateStacking = preventDuplicateStacking;
            this.displayBlockStates = displayBlockStates;
            this.displaySupportBlockState = displaySupportBlockState;
            this.jeiOnly = jeiOnly;
        }

        private boolean matches(IBlockState state) {
            return this.targetMatcher.matches(state);
        }

        private List<HuntingDropOutput> getOutputs() {
            return copyOutputs(this.outputs);
        }

        private HarvestDropRecipeView toView() {
            return new HarvestDropRecipeView(this.key, this.displayBlockStates, this.displaySupportBlockState,
                    getKnifeToolOptions(), this.outputs);
        }
    }

    public static final class HarvestDropRecipeView {
        private final String key;
        private final List<HarvestDropDisplayBlockState> displayBlockStates;
        private final IBlockState displaySupportBlockState;
        private final List<ItemStack> toolOptions;
        private final List<HuntingDropOutput> outputs;

        private HarvestDropRecipeView(String key, List<HarvestDropDisplayBlockState> displayBlockStates,
                                      IBlockState displaySupportBlockState,
                                      List<ItemStack> toolOptions, List<HuntingDropOutput> outputs) {
            this.key = key;
            this.displayBlockStates = copyDisplayBlockStates(displayBlockStates);
            this.displaySupportBlockState = displaySupportBlockState;
            this.toolOptions = toolOptions;
            this.outputs = copyOutputs(outputs);
        }

        public String getKey() {
            return this.key;
        }

        public IBlockState getDisplayBlockState() {
            return this.displayBlockStates.isEmpty() ? null : this.displayBlockStates.get(0).getBlockState();
        }

        public List<HarvestDropDisplayBlockState> getDisplayBlockStates() {
            return copyDisplayBlockStates(this.displayBlockStates);
        }

        public IBlockState getDisplaySupportBlockState() {
            return this.displaySupportBlockState;
        }

        public List<ItemStack> getToolOptions() {
            return this.toolOptions;
        }

        public List<HuntingDropOutput> getOutputs() {
            return copyOutputs(this.outputs);
        }

        public List<ItemStack> getOutputStacks() {
            List<ItemStack> stacks = new ArrayList<>();
            for (HuntingDropOutput output : this.outputs) {
                stacks.add(output.getOutputStack());
            }
            return stacks.isEmpty() ? Collections.emptyList() : ImmutableList.copyOf(stacks);
        }

        public float getOutputChance(int outputIndex) {
            if (outputIndex < 0 || outputIndex >= this.outputs.size()) {
                return 1.0F;
            }
            return this.outputs.get(outputIndex).getChance();
        }
    }
}
