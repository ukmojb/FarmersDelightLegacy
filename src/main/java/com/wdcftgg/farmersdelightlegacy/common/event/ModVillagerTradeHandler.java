package com.wdcftgg.farmersdelightlegacy.common.event;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModItems;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import java.util.Random;

public final class ModVillagerTradeHandler {
    private static boolean registered;

    private ModVillagerTradeHandler() {
    }

    public static void registerAll() {
        if (registered) {
            return;
        }
        registered = true;

        VillagerRegistry.VillagerProfession farmerProfession = VillagerRegistry.FARMER;
        if (farmerProfession == null) {
            farmerProfession = VillagerRegistry.getById(0);
        }
        if (farmerProfession == null) {
            FarmersDelightLegacy.LOGGER.warn("Unable to find vanilla Farmer profession; skipped Farmer's Delight crop trade injection.");
            return;
        }

        VillagerRegistry.VillagerCareer farmerCareer = farmerProfession.getCareer(0);
        farmerCareer.addTrade(1,
                new ConfiguredEmeraldForItems("onion", 26, 16),
                new ConfiguredEmeraldForItems("tomato", 26, 16));
        farmerCareer.addTrade(2,
                new ConfiguredEmeraldForItems("cabbage", 16, 16),
                new ConfiguredEmeraldForItems("rice", 20, 16));
    }

    private static final class ConfiguredEmeraldForItems implements EntityVillager.ITradeList {
        private final String itemName;
        private final int itemCount;
        private final int maxTrades;

        private ConfiguredEmeraldForItems(String itemName, int itemCount, int maxTrades) {
            this.itemName = itemName;
            this.itemCount = itemCount;
            this.maxTrades = maxTrades;
        }

        @Override
        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
            if (!Configuration.farmersBuyFDCrops) {
                return;
            }
            Item item = ModItems.get(this.itemName);
            if (item == null) {
                return;
            }
            recipeList.add(new MerchantRecipe(
                    new ItemStack(item, this.itemCount),
                    ItemStack.EMPTY,
                    new ItemStack(Items.EMERALD),
                    0,
                    this.maxTrades));
        }
    }
}
