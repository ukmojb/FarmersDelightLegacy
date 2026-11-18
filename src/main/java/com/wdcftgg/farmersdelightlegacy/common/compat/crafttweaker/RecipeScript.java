package com.wdcftgg.farmersdelightlegacy.common.compat.crafttweaker;

import com.google.gson.JsonPrimitive;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CookingPotRecipe.IngredientEntry;
import com.wdcftgg.farmersdelightlegacy.common.recipe.ToolAction;
import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.CuttingBoardRecipeManager.IngredientMatcher;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final class RecipeScript {
    private RecipeScript() {
    }

    static String quote(String value) {
        return new JsonPrimitive(value).toString();
    }

    static SoundEvent sound(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        try {
            SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(name));
            if (sound == null) {
                CraftTweakerAPI.logError("Unknown cutting recipe sound: " + name);
            }
            return sound;
        } catch (IllegalArgumentException exception) {
            CraftTweakerAPI.logError("Invalid cutting recipe sound: " + name, exception);
            return null;
        }
    }

    static IIngredient ingredient(IngredientEntry entry) {
        if (!entry.getAlternatives().isEmpty()) {
            IIngredient result = ingredient(entry.getAlternatives().get(0));
            for (int index = 1; index < entry.getAlternatives().size(); index++) {
                result = result.or(ingredient(entry.getAlternatives().get(index)));
            }
            return result;
        }
        if (entry.getStackMatcher() instanceof ScriptIngredientMatcher) {
            return ((ScriptIngredientMatcher) entry.getStackMatcher()).getIngredient();
        }
        if (entry.getStackMatcher() instanceof ToolAction) {
            return ZenToolIngredient.of(((ToolAction) entry.getStackMatcher()).getAction());
        }
        if (entry.getItem() != null) {
            return CraftTweakerMC.getIItemStack(new ItemStack(entry.getItem(), 1, entry.getMetadata()));
        }
        if (entry.getOreDictName() != null) {
            return CraftTweakerMC.getOreDict(entry.getOreDictName());
        }
        CraftTweakerAPI.logError("Recipe ingredient has no CraftTweaker representation");
        throw new IllegalArgumentException("Recipe ingredient has no CraftTweaker representation");
    }

    static IIngredient[] ingredients(IngredientMatcher matcher, boolean tool) {
        List<IIngredient> result = new ArrayList<>();
        for (ItemStack stack : matcher.getItemStacks()) {
            result.add(CraftTweakerMC.getIItemStack(stack));
        }
        for (String ore : matcher.getOreDictNames()) {
            result.add(tool ? ZenToolIngredient.ore(ore) : CraftTweakerMC.getOreDict(ore));
        }
        for (IngredientEntry entry : matcher.getCustomIngredients()) {
            result.add(ingredient(entry));
        }
        return result.toArray(new IIngredient[0]);
    }

    static String ingredients(IIngredient[] ingredients) {
        return Arrays.stream(ingredients).map(RecipeScript::ingredientCommand).collect(Collectors.joining(", ", "[", "]"));
    }

    private static String ingredientCommand(IIngredient ingredient) {
        if (ingredient instanceof IItemStack || ingredient instanceof crafttweaker.api.oredict.IOreDictEntry
                || ingredient instanceof ZenToolIngredient || ingredient == crafttweaker.api.item.IngredientAny.INSTANCE) {
            return ingredient.toCommandString();
        }
        if (ingredient instanceof crafttweaker.api.item.IngredientOr) {
            IIngredient[] alternatives = (IIngredient[]) ingredient.getInternal();
            return Arrays.stream(alternatives).map(RecipeScript::ingredientCommand)
                    .collect(Collectors.joining(" | ", "(", ")"));
        }
        if (ingredient instanceof crafttweaker.api.item.IngredientStack) {
            return "(" + ingredientCommand((IIngredient) ingredient.getInternal()) + ") * " + ingredient.getAmount();
        }
        CraftTweakerAPI.logError("Ingredient callbacks cannot be exported as source code; use the recipe snapshot to preserve them");
        throw new IllegalArgumentException("Ingredient callbacks cannot be exported as source code");
    }

    static String stack(IItemStack stack) {
        return stack == null ? "null" : stack.toCommandString();
    }
}
