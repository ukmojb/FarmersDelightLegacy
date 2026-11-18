package com.wdcftgg.farmersdelightlegacy.common.recipe;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.CookingPotRecipeManager;
import net.minecraft.nbt.NBTTagCompound;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public final class CookingExperience {
    private final Map<String, Integer> recipesUsed = new LinkedHashMap<>();
    private float legacyExperience;

    public void record(CookingPotRecipe recipe) {
        recipesUsed.merge(recipe.getRecipeId(), 1, Integer::sum);
    }

    public int claim(Random random) {
        Map<String, CookingPotRecipe> recipes = new LinkedHashMap<>();
        for (CookingPotRecipe recipe : CookingPotRecipeManager.getRecipes()) {
            recipes.put(recipe.getRecipeId(), recipe);
        }
        int total = roundExperience(legacyExperience, random);
        for (Map.Entry<String, Integer> entry : recipesUsed.entrySet()) {
            CookingPotRecipe recipe = recipes.get(entry.getKey());
            if (recipe == null) {
                FarmersDelightLegacy.LOGGER.warn("Cannot award cooking experience for removed recipe {}", entry.getKey());
                continue;
            }
            total += roundExperience(entry.getValue() * recipe.getExperience(), random);
        }
        clear();
        return total;
    }

    private static int roundExperience(float amount, Random random) {
        int whole = (int) Math.floor(amount);
        return whole + (random.nextFloat() < amount - whole ? 1 : 0);
    }

    public void clear() {
        recipesUsed.clear();
        legacyExperience = 0;
    }

    public void writeToNBT(NBTTagCompound tag) {
        NBTTagCompound recipes = new NBTTagCompound();
        recipesUsed.forEach(recipes::setInteger);
        tag.setTag("RecipesUsed", recipes);
        tag.setFloat("StoredExperience", legacyExperience);
    }

    public void readFromNBT(NBTTagCompound tag) {
        clear();
        NBTTagCompound recipes = tag.getCompoundTag("RecipesUsed");
        for (String id : recipes.getKeySet()) {
            recipesUsed.put(id, recipes.getInteger(id));
        }
        legacyExperience = tag.getFloat("StoredExperience");
    }
}
