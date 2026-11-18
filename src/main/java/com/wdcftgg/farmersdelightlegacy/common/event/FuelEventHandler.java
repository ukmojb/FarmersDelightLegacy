package com.wdcftgg.farmersdelightlegacy.common.event;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockCabinet;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID)
public final class FuelEventHandler {

    private static final int CANVAS_SIGN_BURN_TIME = 200;
    private static final int HANGING_CANVAS_SIGN_BURN_TIME = 800;

    private FuelEventHandler() {
    }

    @SubscribeEvent
    public static void onFurnaceFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        ItemStack itemStack = event.getItemStack();
        if (itemStack.isEmpty()) {
            return;
        }

        if (hasOreName(itemStack, "fdHangingCanvasSigns")) {
            event.setBurnTime(HANGING_CANVAS_SIGN_BURN_TIME);
            return;
        }

        if (hasOreName(itemStack, "fdCanvasSigns")) {
            event.setBurnTime(CANVAS_SIGN_BURN_TIME);
            return;
        }

        ResourceLocation itemId = itemStack.getItem().getRegistryName();
        if (hasOreName(itemStack, "fdCabinetsWooden")) {
            boolean fireproof = itemId != null && FarmersDelightLegacy.MOD_ID.equals(itemId.getNamespace())
                    && ("crimson_cabinet".equals(itemId.getPath()) || "warped_cabinet".equals(itemId.getPath()));
            event.setBurnTime(fireproof ? 0 : 300);
            return;
        }
        if (itemId == null || !FarmersDelightLegacy.MOD_ID.equals(itemId.getNamespace())) {
            return;
        }

        String path = itemId.getPath();
        if (Block.getBlockFromItem(itemStack.getItem()) instanceof BlockCabinet) {
            event.setBurnTime("crimson_cabinet".equals(path) || "warped_cabinet".equals(path) ? 0 : 300);
            return;
        }

        switch (path) {
            case "straw":
            case "half_tatami_mat":
                event.setBurnTime(100);
                break;
            case "tree_bark":
            case "rope":
            case "rope_fence":
            case "rope_fence_gate":
            case "safety_net":
            case "canvas_rug":
            case "full_tatami_mat":
            case "cutting_board":
                event.setBurnTime(200);
                break;
            case "basket":
            case "wooden_basket":
            case "bamboo_basket":
                event.setBurnTime(300);
                break;
            case "canvas":
            case "tatami":
                event.setBurnTime(400);
                break;
            case "straw_bale":
                event.setBurnTime(1000);
                break;
            default:
                break;
        }
    }

    private static boolean hasOreName(ItemStack itemStack, String expectedOreName) {
        for (int oreId : OreDictionary.getOreIDs(itemStack)) {
            if (expectedOreName.equals(OreDictionary.getOreName(oreId))) {
                return true;
            }
        }
        return false;
    }
}
