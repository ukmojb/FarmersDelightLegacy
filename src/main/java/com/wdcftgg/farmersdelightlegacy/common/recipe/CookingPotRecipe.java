package com.wdcftgg.farmersdelightlegacy.common.recipe;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CookingPotRecipe {

    private final String recipeId;
    private final String group;
    private final List<IngredientEntry> ingredients;
    private final ItemStack resultStack;
    private final ItemStack outputContainer;
    private final int cookTime;
    private final float experience;
    private final boolean hasContainerDefinition;

    public CookingPotRecipe(String recipeId, List<IngredientEntry> ingredients, ItemStack resultStack, ItemStack outputContainer,
                            int cookTime, float experience, boolean hasContainerDefinition) {
        this(recipeId, ingredients, resultStack, outputContainer, cookTime, experience, hasContainerDefinition, "");
    }

    public CookingPotRecipe(String recipeId, List<IngredientEntry> ingredients, ItemStack resultStack, ItemStack outputContainer,
                            int cookTime, float experience, boolean hasContainerDefinition, String group) {
        this.recipeId = Objects.requireNonNull(recipeId);
        this.group = Objects.requireNonNull(group);
        this.ingredients = new ArrayList<>(ingredients);
        this.resultStack = resultStack.copy();
        this.outputContainer = outputContainer.copy();
        this.cookTime = Math.max(1, cookTime);
        this.experience = Math.max(0.0F, experience);
        this.hasContainerDefinition = hasContainerDefinition;
    }

    public List<IngredientEntry> getIngredients() {
        return Collections.unmodifiableList(this.ingredients);
    }

    public String getRecipeId() {
        return this.recipeId;
    }

    public String getGroup() {
        return group;
    }

    public ItemStack getResultStack() {
        return this.resultStack.copy();
    }

    public ItemStack assemble(List<ItemStack> inputStacks) {
        return getResultStack();
    }

    public ItemStack getOutputContainer() {
        return this.outputContainer.copy();
    }

    public int getCookTime() {
        return this.cookTime;
    }

    public float getExperience() {
        return this.experience;
    }

    public boolean hasContainerDefinition() {
        return this.hasContainerDefinition;
    }

    public IngredientEntry[] matchIngredients(List<ItemStack> stacks) {
        int occupied = 0;
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                occupied++;
            }
        }
        if (occupied != ingredients.size()) {
            return null;
        }
        IngredientEntry[] matched = new IngredientEntry[stacks.size()];
        return matchIngredient(stacks, matched, 0) ? matched : null;
    }

    private boolean matchIngredient(List<ItemStack> stacks, IngredientEntry[] matched, int index) {
        if (index == ingredients.size()) {
            return true;
        }
        IngredientEntry ingredient = ingredients.get(index);
        for (int slot = 0; slot < stacks.size(); slot++) {
            if (matched[slot] == null && ingredient.matches(stacks.get(slot))) {
                matched[slot] = ingredient.resolve(stacks.get(slot));
                if (matchIngredient(stacks, matched, index + 1)) {
                    return true;
                }
                matched[slot] = null;
            }
        }
        return false;
    }

    public static final class IngredientEntry {

        private final Item item;
        private final String oreDictName;
        private final int metadata;
        private final IngredientStackMatcher stackMatcher;
        private final IngredientRemainderProvider remainderProvider;
        private final List<ItemStack> displayStacks;
        private final List<IngredientEntry> alternatives;

        private IngredientEntry(Item item, String oreDictName, int metadata) {
            this(item, oreDictName, metadata, null, null, null);
        }

        private IngredientEntry(Item item, String oreDictName, int metadata, IngredientStackMatcher stackMatcher,
                                IngredientRemainderProvider remainderProvider, List<ItemStack> displayStacks) {
            this(item, oreDictName, metadata, stackMatcher, remainderProvider, displayStacks, Collections.emptyList());
        }

        private IngredientEntry(Item item, String oreDictName, int metadata, IngredientStackMatcher stackMatcher,
                                IngredientRemainderProvider remainderProvider, List<ItemStack> displayStacks,
                                List<IngredientEntry> alternatives) {
            this.item = item;
            this.oreDictName = oreDictName;
            this.metadata = metadata;
            this.stackMatcher = stackMatcher;
            this.remainderProvider = remainderProvider;
            this.displayStacks = copyDisplayStacks(displayStacks);
            this.alternatives = Collections.unmodifiableList(new ArrayList<>(alternatives));
        }

        public static IngredientEntry forItem(Item item) {
            return new IngredientEntry(Objects.requireNonNull(item), null, OreDictionary.WILDCARD_VALUE);
        }

        public static IngredientEntry forItem(Item item, int metadata) {
            return new IngredientEntry(Objects.requireNonNull(item), null, metadata);
        }

        public static IngredientEntry forOreDict(String oreDictName) {
            return new IngredientEntry(null, Objects.requireNonNull(oreDictName), OreDictionary.WILDCARD_VALUE);
        }

        public static IngredientEntry forAlternatives(List<IngredientEntry> alternatives) {
            if (alternatives == null || alternatives.isEmpty() || alternatives.contains(null)) {
                throw new IllegalArgumentException("Ingredient alternatives must contain at least one valid entry");
            }
            return new IngredientEntry(null, null, OreDictionary.WILDCARD_VALUE, null, null, null, alternatives);
        }

        public List<IngredientEntry> getAlternatives() {
            return alternatives;
        }

        public static IngredientEntry forCustom(IngredientStackMatcher stackMatcher,
                                                IngredientRemainderProvider remainderProvider,
                                                List<ItemStack> displayStacks) {
            return new IngredientEntry(null, null, OreDictionary.WILDCARD_VALUE,
                    Objects.requireNonNull(stackMatcher), remainderProvider, displayStacks);
        }

        public Item getItem() {
            return this.item;
        }

        public String getOreDictName() {
            return this.oreDictName;
        }

        public int getMetadata() {
            return this.metadata;
        }

        public IngredientStackMatcher getStackMatcher() {
            return this.stackMatcher;
        }

        public List<ItemStack> getDisplayStacks() {
            if (!alternatives.isEmpty()) {
                List<ItemStack> stacks = new ArrayList<>();
                for (IngredientEntry alternative : alternatives) {
                    if (alternative.item != null) {
                        stacks.add(new ItemStack(alternative.item, 1, alternative.metadata));
                    } else if (alternative.oreDictName != null) {
                        stacks.addAll(OreDictionary.getOres(alternative.oreDictName));
                    } else {
                        stacks.addAll(alternative.getDisplayStacks());
                    }
                }
                return copyDisplayStacks(stacks);
            }
            return copyDisplayStacks(this.displayStacks);
        }

        private IngredientEntry resolve(ItemStack stack) {
            for (IngredientEntry alternative : alternatives) {
                if (alternative.matches(stack)) {
                    return alternative.resolve(stack);
                }
            }
            return this;
        }

        public boolean hasCustomRemainderProvider() {
            return this.remainderProvider != null;
        }

        public ItemStack getCustomRemainder(ItemStack stack) {
            if (this.remainderProvider == null || stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack remainderStack = this.remainderProvider.getRemainder(stack.copy());
            return remainderStack == null || remainderStack.isEmpty() ? ItemStack.EMPTY : remainderStack.copy();
        }

        public boolean matches(ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            if (!alternatives.isEmpty()) {
                return alternatives.stream().anyMatch(alternative -> alternative.matches(stack));
            }
            if (this.stackMatcher != null) {
                return this.stackMatcher.matches(stack);
            }
            if (this.item != null) {
                return stack.getItem() == this.item
                        && (this.metadata == OreDictionary.WILDCARD_VALUE || this.metadata == stack.getMetadata());
            }
            int[] oreIds = OreDictionary.getOreIDs(stack);
            int expectedId = OreDictionary.getOreID(this.oreDictName);
            for (int oreId : oreIds) {
                if (oreId == expectedId) {
                    return true;
                }
            }
            return false;
        }

        private static List<ItemStack> copyDisplayStacks(List<ItemStack> displayStacks) {
            if (displayStacks == null || displayStacks.isEmpty()) {
                return Collections.emptyList();
            }
            List<ItemStack> copiedStacks = new ArrayList<>();
            for (ItemStack displayStack : displayStacks) {
                if (displayStack != null && !displayStack.isEmpty()) {
                    copiedStacks.add(displayStack.copy());
                }
            }
            return Collections.unmodifiableList(copiedStacks);
        }
    }

    public interface IngredientStackMatcher {
        boolean matches(ItemStack stack);
    }

    public interface IngredientRemainderProvider {
        ItemStack getRemainder(ItemStack stack);
    }
}

