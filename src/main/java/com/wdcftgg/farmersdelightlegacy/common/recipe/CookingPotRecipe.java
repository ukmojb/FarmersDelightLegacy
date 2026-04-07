package com.wdcftgg.farmersdelightlegacy.common.recipe;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CookingPotRecipe {

    private final List<IngredientEntry> ingredients;
    private final ItemStack resultStack;
    private final ItemStack outputContainer;
    private final int cookTime;
    private final boolean hasContainerDefinition;

    public CookingPotRecipe(List<IngredientEntry> ingredients, ItemStack resultStack, ItemStack outputContainer, int cookTime, boolean hasContainerDefinition) {
        this.ingredients = new ArrayList<>(ingredients);
        this.resultStack = resultStack.copy();
        this.outputContainer = outputContainer.copy();
        this.cookTime = Math.max(1, cookTime);
        this.hasContainerDefinition = hasContainerDefinition;
    }

    public List<IngredientEntry> getIngredients() {
        return Collections.unmodifiableList(this.ingredients);
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

    public boolean hasContainerDefinition() {
        return this.hasContainerDefinition;
    }

    public static final class IngredientEntry {

        private final Item item;
        private final String oreDictName;

        private IngredientEntry(Item item, String oreDictName) {
            this.item = item;
            this.oreDictName = oreDictName;
        }

        public static IngredientEntry forItem(Item item) {
            return new IngredientEntry(Objects.requireNonNull(item), null);
        }

        public static IngredientEntry forOreDict(String oreDictName) {
            return new IngredientEntry(null, Objects.requireNonNull(oreDictName));
        }

        public Item getItem() {
            return this.item;
        }

        public String getOreDictName() {
            return this.oreDictName;
        }

        public boolean matches(ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            if (this.item != null) {
                return stack.getItem() == this.item;
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

