package com.wdcftgg.farmersdelightlegacy.common.compat;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;

import java.lang.reflect.Field;
import java.util.Collection;

public final class UniversalTweaksMilkCompat {

    private static final String MOD_ID = "universaltweaks";
    private static final String CONFIG_CLASS = "mod.acgaming.universaltweaks.config.UTConfigTweaks";
    private static final String POTION_LIST_CLASS = "mod.acgaming.universaltweaks.tweaks.misc.incurablepotions.UTIncurablePotions";

    private static boolean initialized;
    private static boolean available;
    private static boolean failureLogged;
    private static Field miscField;
    private static Field incurablePotionsField;
    private static Field toggleField;
    private static Field listModeField;
    private static Field potionListField;

    private UniversalTweaksMilkCompat() {
    }

    public static boolean canMilkCure(PotionEffect effect) {
        if (effect == null || !Loader.isModLoaded(MOD_ID)) {
            return true;
        }

        initialize();
        if (!available) {
            return true;
        }

        try {
            Object miscConfig = miscField.get(null);
            Object incurableConfig = incurablePotionsField.get(miscConfig);
            if (!toggleField.getBoolean(incurableConfig)) {
                return true;
            }

            ResourceLocation potionId = effect.getPotion().getRegistryName();
            if (potionId == null) {
                return true;
            }

            Object listMode = listModeField.get(incurableConfig);
            boolean whitelist = listMode != null && "WHITELIST".equals(listMode.toString());
            Object configuredPotions = potionListField.get(null);
            boolean listed = configuredPotions instanceof Collection
                    && ((Collection<?>) configuredPotions).contains(potionId.toString());
            return listed == whitelist;
        } catch (ReflectiveOperationException | LinkageError exception) {
            disable(exception);
            return true;
        }
    }

    private static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;
        try {
            Class<?> configClass = Class.forName(CONFIG_CLASS);
            miscField = configClass.getField("MISC");
            Object miscConfig = miscField.get(null);
            incurablePotionsField = miscConfig.getClass().getField("INCURABLE_POTIONS");
            Object incurableConfig = incurablePotionsField.get(miscConfig);
            toggleField = incurableConfig.getClass().getField("utIncurablePotionsToggle");
            listModeField = incurableConfig.getClass().getField("utIncurablePotionsListMode");

            Class<?> potionListClass = Class.forName(POTION_LIST_CLASS);
            potionListField = potionListClass.getField("potionList");
            available = true;
        } catch (ReflectiveOperationException | LinkageError exception) {
            disable(exception);
        }
    }

    private static void disable(Throwable throwable) {
        if (!failureLogged) {
            failureLogged = true;
            FarmersDelightLegacy.LOGGER.warn("Unable to enable Universal Tweaks milk curing compatibility.", throwable);
        }
        available = false;
    }
}
