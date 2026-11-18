package com.wdcftgg.farmersdelightlegacy.client.event;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.client.model.SkilletArmPose;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID, value = Side.CLIENT)
public final class SkilletRenderEvents {
    private SkilletRenderEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void beforePlayer(RenderPlayerEvent.Pre event) {
        SkilletArmPose.prepare(event.getRenderer().getMainModel(), event.getEntityLiving(), event.getPartialRenderTick());
        updateArmor(event, true);
    }

    @SubscribeEvent
    public static void afterPlayer(RenderPlayerEvent.Post event) {
        SkilletArmPose.restore(event.getRenderer().getMainModel());
        updateArmor(event, false);
    }

    private static void updateArmor(RenderPlayerEvent event, boolean prepare) {
        for (LayerRenderer<?> layer : event.getRenderer().layerRenderers) {
            if (layer instanceof LayerArmorBase) {
                LayerArmorBase<?> armor = (LayerArmorBase<?>) layer;
                updateArmorModel(event, armor.getModelFromSlot(EntityEquipmentSlot.CHEST), prepare);
                updateArmorModel(event, armor.getModelFromSlot(EntityEquipmentSlot.LEGS), prepare);
            }
        }
    }

    private static void updateArmorModel(RenderPlayerEvent event, ModelBase model, boolean prepare) {
        if (model instanceof ModelBiped) {
            if (prepare) {
                SkilletArmPose.prepare((ModelBiped) model, event.getEntityLiving(), event.getPartialRenderTick());
            } else {
                SkilletArmPose.restore((ModelBiped) model);
            }
        }
    }
}
