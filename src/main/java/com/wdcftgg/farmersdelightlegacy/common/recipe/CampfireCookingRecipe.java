package com.wdcftgg.farmersdelightlegacy.common.recipe;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;
import java.util.Collections;

public final class CampfireCookingRecipe {

    private final List<IngredientEntry> ingredients;
    private final ItemStack resultStack;
    private final int cookingTime;

    public CampfireCookingRecipe(List<IngredientEntry> ingredients, ItemStack resultStack, int cookingTime) {
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

    public List<IngredientEntry> getIngredients() {
        return Collections.unmodifiableList(ingredients);
    }

    public int getCookingTime() {
        return Math.max(1, cookingTime);
    }

    public static final class IngredientEntry {
        private final Item item;
        private final String oreDictName;

        private IngredientEntry(Item item, String oreDictName) {
            this.item = item;
            this.oreDictName = oreDictName;
        }

        public static IngredientEntry forItem(Item item) {
            return new IngredientEntry(item, null);
        }

        public static IngredientEntry forOreDict(String oreDictName) {
            return new IngredientEntry(null, oreDictName);
        }

        public Item getItem() {
            return item;
        }

        public String getOreDictName() {
            return oreDictName;
        }

        public boolean matches(ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            if (item != null) {
                return stack.getItem() == item;
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
