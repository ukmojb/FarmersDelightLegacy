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
    private final List<IngredientEntry> ingredients;
    private final ItemStack resultStack;
    private final ItemStack outputContainer;
    private final int cookTime;
    private final float experience;
    private final boolean hasContainerDefinition;

    public CookingPotRecipe(String recipeId, List<IngredientEntry> ingredients, ItemStack resultStack, ItemStack outputContainer,
                            int cookTime, float experience, boolean hasContainerDefinition) {
        this.recipeId = Objects.requireNonNull(recipeId);
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

    public ItemStack getResultStack() {
        return this.resultStack.copy();
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

    public static final class IngredientEntry {

        private final Item item;
        private final String oreDictName;
        private final int metadata;

        private IngredientEntry(Item item, String oreDictName, int metadata) {
            this.item = item;
            this.oreDictName = oreDictName;
            this.metadata = metadata;
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

        public Item getItem() {
            return this.item;
        }

        public String getOreDictName() {
            return this.oreDictName;
        }

        public int getMetadata() {
            return this.metadata;
        }

        public boolean matches(ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
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
    }
}

