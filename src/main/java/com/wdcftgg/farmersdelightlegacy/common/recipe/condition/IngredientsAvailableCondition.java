package com.wdcftgg.farmersdelightlegacy.common.recipe.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.util.function.BooleanSupplier;

public final class IngredientsAvailableCondition implements IConditionFactory {

    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        return () -> matches(json);
    }

    public static boolean matches(JsonObject json) {
        if (json.has("items")) {
            for (JsonElement item : json.getAsJsonArray("items")) {
                if (!ForgeRegistries.ITEMS.containsKey(new ResourceLocation(item.getAsString()))) {
                    return false;
                }
            }
        }
        if (json.has("ores")) {
            for (JsonElement ore : json.getAsJsonArray("ores")) {
                if (OreDictionary.getOres(ore.getAsString(), false).isEmpty()) {
                    return false;
                }
            }
        }
        if (json.has("items_absent")) {
            for (JsonElement item : json.getAsJsonArray("items_absent")) {
                if (ForgeRegistries.ITEMS.containsKey(new ResourceLocation(item.getAsString()))) {
                    return false;
                }
            }
        }
        return true;
    }
}
