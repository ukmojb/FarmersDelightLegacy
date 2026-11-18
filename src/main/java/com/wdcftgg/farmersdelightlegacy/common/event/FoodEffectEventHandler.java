package com.wdcftgg.farmersdelightlegacy.common.event;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModEffects;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onNourishedFoodUse(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = event.getItemStack();
        if (event.isCanceled() || stack.isEmpty() || !(stack.getItem() instanceof ItemFood)
                || player.isSpectator() || player.isHandActive() || player.canEat(false)
                || player.getCooldownTracker().hasCooldown(stack.getItem())
                || ModEffects.NOURISHMENT == null || !player.isPotionActive(ModEffects.NOURISHMENT)) {
            return;
        }
        player.setActiveHand(event.getHand());
        event.setCancellationResult(player.getItemInUseCount() > 0 ? EnumActionResult.SUCCESS : EnumActionResult.FAIL);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onFinishUsingFood(LivingEntityUseItemEvent.Finish event) {
        ItemStack stack = event.getItem();
        EntityLivingBase entity = event.getEntityLiving();
        if (stack.isEmpty()) {
            return;
        }

        Item item = stack.getItem();
        if (item instanceof ItemFood && entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            if (player.capabilities.isCreativeMode) {
                event.setResultStack(stack.copy());
            } else if (stack.getCount() > 1 && (item == Items.MUSHROOM_STEW
                    || item == Items.BEETROOT_SOUP || item == Items.RABBIT_STEW)) {
                ItemStack remainder = stack.copy();
                remainder.shrink(1);
                event.setResultStack(remainder);
                if (!entity.world.isRemote) {
                    ItemStack bowl = new ItemStack(Items.BOWL);
                    if (!player.inventory.addItemStackToInventory(bowl)) {
                        player.dropItem(bowl, false);
                    }
                }
            }
        }
        if (entity.world.isRemote) {
            return;
        }
        PotionEffect effect = getVanillaSoupEffects().get(item);
        if (effect != null) {
            entity.addPotionEffect(new PotionEffect(effect));
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!Configuration.foodEffectTooltip) {
            return;
        }

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
        Potion nourishment = ForgeRegistries.POTIONS.getValue(new ResourceLocation(FarmersDelightLegacy.MOD_ID, "nourishment"));
        if (nourishment == null) {
            return Collections.emptyMap();
        }

        Map<Item, PotionEffect> effects = new LinkedHashMap<>();
        if (Configuration.vanillaSoupExtraEffects) {
            effects.put(Items.MUSHROOM_STEW, new PotionEffect(nourishment, 3600, 0, false, false));
            effects.put(Items.BEETROOT_SOUP, new PotionEffect(nourishment, 3600, 0, false, false));
        }
        if (Configuration.vanillaSoupExtraEffects || Configuration.enableRabbitStewBuff) {
            effects.put(Items.RABBIT_STEW, new PotionEffect(nourishment, 6000, 0, false, false));
        }
        return effects;
    }
}
