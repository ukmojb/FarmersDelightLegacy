package com.wdcftgg.farmersdelightlegacy.common.compat;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModItems;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;

@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID)
public final class AnimalFoodCompat {
    private AnimalFoodCompat() {
    }

    public static void registerAll() {
        for (String name : new String[]{"cabbage_seeds", "tomato_seeds", "rice"}) {
            Item item = ModItems.get(name);
            EntityChicken.TEMPTATION_ITEMS.add(item);
            EntityParrot.TAME_ITEMS.add(item);
        }
        EntityPig.TEMPTATION_ITEMS.add(ModItems.get("cabbage"));
        EntityPig.TEMPTATION_ITEMS.add(ModItems.get("tomato"));
    }

    @SubscribeEvent
    public static void onAnimalJoin(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote || !(event.getEntity() instanceof EntityCreature)) {
            return;
        }
        EntityCreature animal = (EntityCreature) event.getEntity();
        for (EntityAITasks.EntityAITaskEntry entry : animal.tasks.taskEntries) {
            if (entry.action instanceof EntityAITempt) {
                EntityAITempt temptation = (EntityAITempt) entry.action;
                if (animal instanceof EntityRabbit && temptation.temptItem.contains(Items.CARROT)) {
                    temptation.temptItem.add(ModItems.get("cabbage"));
                }
                if (animal instanceof EntityOcelot && temptation.temptItem.contains(Items.FISH)) {
                    temptation.temptItem.add(ModItems.get("cod_slice"));
                    temptation.temptItem.add(ModItems.get("salmon_slice"));
                }
            }
        }
        ResourceLocation id = EntityList.getKey(animal);
        if (id != null && Arrays.asList(Configuration.horseFeedTempted).contains(id.toString())
                && animal.tasks.taskEntries.stream().noneMatch(entry -> entry.action instanceof HorseFeedTemptation)) {
            if (!(animal.getNavigator() instanceof PathNavigateGround)) {
                FarmersDelightLegacy.LOGGER.warn("Horse feed temptation requires ground navigation: {}", id);
                return;
            }
            animal.tasks.addTask(3, new HorseFeedTemptation(animal));
        }
    }

    @SubscribeEvent
    public static void onFeedAnimal(PlayerInteractEvent.EntityInteract event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = event.getItemStack();
        if (event.isCanceled() || player.isSpectator() || stack.isEmpty()
                || !(event.getTarget() instanceof EntityAnimal) || !event.getTarget().isEntityAlive()) {
            return;
        }
        EntityAnimal animal = (EntityAnimal) event.getTarget();
        if (animal.getLeashed() && animal.getLeashHolder() == player) {
            return;
        }
        if (animal instanceof EntityRabbit && stack.getItem() == ModItems.get("cabbage")) {
            finishInteraction(event, feedForBreeding(animal, player, stack));
        } else if (animal instanceof EntityOcelot
                && (stack.getItem() == ModItems.get("cod_slice") || stack.getItem() == ModItems.get("salmon_slice"))) {
            EntityOcelot cat = (EntityOcelot) animal;
            finishInteraction(event, cat.isTamed() ? feedForBreeding(cat, player, stack) : tameCat(cat, player, stack));
        }
    }

    private static boolean feedForBreeding(EntityAnimal animal, EntityPlayer player, ItemStack stack) {
        if (animal.world.isRemote) {
            return true;
        }
        if (animal.getGrowingAge() == 0 && !animal.isInLove()) {
            consume(player, stack);
            animal.setInLove(player);
            return true;
        }
        if (animal.isChild()) {
            consume(player, stack);
            animal.ageUp((int) ((float) (-animal.getGrowingAge() / 20) * 0.1F), true);
            return true;
        }
        return false;
    }

    private static boolean tameCat(EntityOcelot cat, EntityPlayer player, ItemStack stack) {
        if (player.getDistanceSq(cat) >= 9.0D) {
            return false;
        }
        if (cat.world.isRemote) {
            return true;
        }
        boolean tempted = cat.tasks.taskEntries.stream().anyMatch(entry -> entry.action instanceof EntityAITempt
                && ((EntityAITempt) entry.action).isRunning());
        if (!tempted) {
            return false;
        }
        consume(player, stack);
        if (cat.getRNG().nextInt(3) == 0 && !ForgeEventFactory.onAnimalTame(cat, player)) {
            cat.setTamedBy(player);
            cat.setTameSkin(1 + cat.world.rand.nextInt(3));
            cat.getAISit().setSitting(true);
            cat.world.setEntityState(cat, (byte) 7);
        } else {
            cat.world.setEntityState(cat, (byte) 6);
        }
        return true;
    }

    private static void consume(EntityPlayer player, ItemStack stack) {
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
    }

    private static void finishInteraction(PlayerInteractEvent.EntityInteract event, boolean used) {
        event.setCancellationResult(used ? EnumActionResult.SUCCESS : EnumActionResult.PASS);
        event.setCanceled(true);
    }

    private static final class HorseFeedTemptation extends EntityAITempt {
        private HorseFeedTemptation(EntityCreature animal) {
            super(animal, 1.25D, ModItems.get("horse_feed"), false);
        }
    }
}
