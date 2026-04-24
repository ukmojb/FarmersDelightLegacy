package com.wdcftgg.farmersdelightlegacy.common.recipe;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CampfireCookingRecipe {

    private final String recipeId;
    private final List<IngredientEntry> ingredients;
    private final ItemStack resultStack;
    private final int cookingTime;

    public CampfireCookingRecipe(String recipeId, List<IngredientEntry> ingredients, ItemStack resultStack, int cookingTime) {
        this.recipeId = Objects.requireNonNull(recipeId);
        this.ingredients = ingredients;
        this.resultStack = resultStack;
        this.cookingTime = cookingTime;
    }

    public boolean matches(ItemStack input) {
        if (input.isEmpty()) {
            return false;
        }
        for (IngredientEntry entry : ingredients) {
            if (entry.matches(input)) {
                return true;
            }
        }
        return false;
    }

    public ItemStack getResultStack() {
        return resultStack.copy();
    }

    public String getRecipeId() {
        return recipeId;
    }

    public List<IngredientEntry> getIngredients() {
        return Collections.unmodifiableList(ingredients);
    }

    public int getCookingTime() {
        return Math.max(1, cookingTime);
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
            return new IngredientEntry(item, null, OreDictionary.WILDCARD_VALUE);
        }

        public static IngredientEntry forItem(Item item, int metadata) {
            return new IngredientEntry(item, null, metadata);
        }

        public static IngredientEntry forOreDict(String oreDictName) {
            return new IngredientEntry(null, oreDictName, OreDictionary.WILDCARD_VALUE);
        }

        public Item getItem() {
            return item;
        }

        public String getOreDictName() {
            return oreDictName;
        }

        public int getMetadata() {
            return metadata;
        }

        public boolean matches(ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            if (item != null) {
                return stack.getItem() == item
                        && (metadata == OreDictionary.WILDCARD_VALUE || metadata == stack.getMetadata());
            }
            if (oreDictName != null) {
                int oreId = OreDictionary.getOreID(oreDictName);
                if (oreId < 0) {
                    return false;
                }
                for (int id : OreDictionary.getOreIDs(stack)) {
                    if (id == oreId) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
