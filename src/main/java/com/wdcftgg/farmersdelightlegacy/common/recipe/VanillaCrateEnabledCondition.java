package com.wdcftgg.farmersdelightlegacy.common.recipe;

import com.google.gson.JsonObject;
import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

public class VanillaCrateEnabledCondition implements IConditionFactory {

    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        return new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() {
                return Configuration.enableVanillaCropCrates;
            }
        };
    }
}
