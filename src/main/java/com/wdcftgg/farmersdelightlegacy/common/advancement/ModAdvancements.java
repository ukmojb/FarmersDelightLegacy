package com.wdcftgg.farmersdelightlegacy.common.advancement;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.recipe.condition.IngredientsAvailableCondition;
import com.wdcftgg.farmersdelightlegacy.common.registry.BlockCategories;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.advancements.critereon.ItemPredicates;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID)
public final class ModAdvancements {

    public static final SimplePlayerTrigger USE_CUTTING_BOARD =
            new SimplePlayerTrigger(new ResourceLocation(FarmersDelightLegacy.MOD_ID, "use_cutting_board"));
    public static final SimplePlayerTrigger HARVEST_ROPELOGGED_TOMATO =
            new SimplePlayerTrigger(new ResourceLocation(FarmersDelightLegacy.MOD_ID, "harvest_ropelogged_tomato"));
    public static final SimplePlayerTrigger HIT_RAIDER_WITH_ROTTEN_TOMATO =
            new SimplePlayerTrigger(new ResourceLocation(FarmersDelightLegacy.MOD_ID, "hit_raider_with_rotten_tomato"));
    public static final SimplePlayerTrigger PLACE_CAMPFIRE =
            new SimplePlayerTrigger(new ResourceLocation(FarmersDelightLegacy.MOD_ID, "place_campfire"));

    private static boolean registered;

    private ModAdvancements() {
    }

    public static void registerAll() {
        if (registered) {
            return;
        }

        CriteriaTriggers.register(USE_CUTTING_BOARD);
        CriteriaTriggers.register(HARVEST_ROPELOGGED_TOMATO);
        CriteriaTriggers.register(HIT_RAIDER_WITH_ROTTEN_TOMATO);
        CriteriaTriggers.register(PLACE_CAMPFIRE);
        ItemPredicates.register(new ResourceLocation(FarmersDelightLegacy.MOD_ID, "optional_food"), json ->
                IngredientsAvailableCondition.matches(json.getAsJsonObject("requires"))
                        ? ItemPredicate.deserialize(json.get("item")) : ItemPredicate.ANY);
        ItemPredicates.register(new ResourceLocation(FarmersDelightLegacy.MOD_ID, "item_id"), json -> {
            ResourceLocation itemId = new ResourceLocation(json.get("item").getAsString());
            return new ItemPredicate() {
                @Override
                public boolean test(ItemStack stack) {
                    return !stack.isEmpty() && itemId.equals(stack.getItem().getRegistryName());
                }
            };
        });
        registered = true;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockPlaced(BlockEvent.PlaceEvent event) {
        if (!(event.getPlayer() instanceof EntityPlayerMP)) {
            return;
        }
        if (BlockCategories.contains("fdCampfires", event.getPlacedBlock())) {
            PLACE_CAMPFIRE.trigger((EntityPlayerMP) event.getPlayer());
        } else if (event.getPlacedBlock().getBlock() == ModBlocks.STOVE) {
            for (Block block : ForgeRegistries.BLOCKS) {
                if (BlockCategories.contains("fdCampfires", block.getDefaultState())) {
                    return;
                }
            }
            PLACE_CAMPFIRE.trigger((EntityPlayerMP) event.getPlayer());
        }
    }
}
