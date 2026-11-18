package com.wdcftgg.farmersdelightlegacy.common.compat;

import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sereneseasons.api.SSBlocks;
import sereneseasons.config.FertilityConfig;
import sereneseasons.init.ModFertility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class SereneSeasonsCompat {
    private SereneSeasonsCompat() {
    }

    public static void registerAll() {
        FertilityConfig.SeasonFertility config = FertilityConfig.seasonal_fertility;
        List<Set<String>> seasons = new ArrayList<>();
        seasons.add(new LinkedHashSet<>(Arrays.asList(config.spring_crops)));
        seasons.add(new LinkedHashSet<>(Arrays.asList(config.summer_crops)));
        seasons.add(new LinkedHashSet<>(Arrays.asList(config.autumn_crops)));
        seasons.add(new LinkedHashSet<>(Arrays.asList(config.winter_crops)));
        addCrop(seasons, new int[]{2, 3}, "cabbage_seeds", "cabbages");
        addCrop(seasons, new int[]{0, 2}, "onion", "onions");
        addCrop(seasons, new int[]{1, 2}, "rice", "rice_panicles");
        addCrop(seasons, new int[]{1}, "tomato_seeds", "budding_tomatoes", "tomatoes", "tomatoes_on_rope");
        config.spring_crops = seasons.get(0).toArray(new String[0]);
        config.summer_crops = seasons.get(1).toArray(new String[0]);
        config.autumn_crops = seasons.get(2).toArray(new String[0]);
        config.winter_crops = seasons.get(3).toArray(new String[0]);
        MinecraftForge.EVENT_BUS.register(SereneSeasonsCompat.class);
    }

    public static boolean canGrowOnion(World world, BlockPos pos) {
        if (!FertilityConfig.general_category.seasonal_crops
                || ModFertility.isCropFertile("farmersdelight:onions", world, pos)) {
            return true;
        }
        for (int height = 1; height <= FertilityConfig.general_category.greenhouse_glass_max_height; height++) {
            if (world.getBlockState(pos.up(height)).getBlock() == SSBlocks.greenhouse_glass) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBonemeal(BonemealEvent event) {
        if (event.getBlock().getBlock() == ModBlocks.ONIONS && !canGrowOnion(event.getWorld(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    private static void addCrop(List<Set<String>> seasons, int[] defaults, String... paths) {
        List<String> crops = new ArrayList<>();
        for (String path : paths) {
            crops.add("farmersdelight:" + path);
        }
        boolean[] fertile = new boolean[seasons.size()];
        boolean configured = false;
        for (int i = 0; i < seasons.size(); i++) {
            for (String crop : crops) {
                if (seasons.get(i).contains(crop)) {
                    fertile[i] = true;
                    configured = true;
                }
            }
        }
        if (!configured) {
            for (int season : defaults) {
                fertile[season] = true;
            }
        }
        for (int i = 0; i < seasons.size(); i++) {
            if (fertile[i]) {
                seasons.get(i).addAll(crops);
            }
        }
    }
}

