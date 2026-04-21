package com.wdcftgg.farmersdelightlegacy.common.event;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID)
public final class FoodEffectEventHandler {

    private FoodEffectEventHandler() {
    }

    @SubscribeEvent
    public static void onFinishUsingFood(LivingEntityUseItemEvent.Finish event) {
        ItemStack stack = event.getItem();
        EntityLivingBase entity = event.getEntityLiving();
        if (stack.isEmpty()) {
            return;
        }

        Item item = stack.getItem();
        if (item == Items.RABBIT_STEW) {
            entity.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.JUMP_BOOST, 200, 1));
        }

        PotionEffect effect = getVanillaSoupEffects().get(item);
        if (effect != null) {
            entity.addPotionEffect(new PotionEffect(effect));
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        PotionEffect effect = getVanillaSoupEffects().get(event.getItemStack().getItem());
        if (effect == null) {
            return;
        }

        String effectName = new TextComponentTranslation(effect.getEffectName()).getFormattedText();
        String duration = Potion.getPotionDurationString(effect, 1.0F);
        TextComponentTranslation effectTooltip = new TextComponentTranslation("farmersdelight.tooltip.food.effect",
                effectName, duration);

        effectTooltip.getStyle().setColor(TextFormatting.BLUE);
        event.getToolTip().add(effectTooltip.getFormattedText());
    }

    private static Map<Item, PotionEffect> getVanillaSoupEffects() {
        Potion comfort = ForgeRegistries.POTIONS.getValue(new ResourceLocation(FarmersDelightLegacy.MOD_ID, "comfort"));
        if (comfort == null) {
            return Collections.emptyMap();
        }

        Map<Item, PotionEffect> effects = new LinkedHashMap<>();
        effects.put(Items.MUSHROOM_STEW, new PotionEffect(comfort, 3600, 0));
        effects.put(Items.BEETROOT_SOUP, new PotionEffect(comfort, 3600, 0));
        effects.put(Items.RABBIT_STEW, new PotionEffect(comfort, 6000, 0));
        return effects;
    }
}
