package com.wdcftgg.farmersdelightlegacy.common.event;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.item.ItemSkillet;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModSounds;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Map;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID)
public final class SkilletCombat {
    private static final Map<EntityLivingBase, Attack> attacks = new WeakHashMap<>();

    private SkilletCombat() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerAttack(AttackEntityEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        attacks.remove(player);
        if (usesSkillet(player) && event.getTarget() instanceof EntityLivingBase) {
            attacks.put(player, new Attack(player, (EntityLivingBase) event.getTarget(),
                    player.getCooledAttackStrength(0.5F)));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        Entity attacker = event.getSource().getImmediateSource();
        if (attacker instanceof EntityLivingBase && !(attacker instanceof EntityPlayer)
                && "mob".equals(event.getSource().getDamageType()) && usesSkillet((EntityLivingBase) attacker)) {
            attacks.put((EntityLivingBase) attacker, new Attack((EntityLivingBase) attacker, event.getEntityLiving(), 1));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        Entity source = event.getSource().getImmediateSource();
        if (!(source instanceof EntityLivingBase) || event.getAmount() <= 0) {
            return;
        }
        EntityLivingBase attacker = (EntityLivingBase) source;
        Attack attack = attacks.get(attacker);
        if (!matches(attack, attacker, event.getEntityLiving()) || attack.damaged
                || !("player".equals(event.getSource().getDamageType()) || "mob".equals(event.getSource().getDamageType()))) {
            return;
        }
        attack.damaged = true;
        boolean strong = attack.power > 0.8F;
        attacker.world.playSound(null, attacker.posX, attacker.posY, attacker.posZ,
                strong ? ModSounds.SKILLET_ATTACK_STRONG : ModSounds.SKILLET_ATTACK_WEAK,
                SoundCategory.PLAYERS, strong ? 1.0F : 0.8F,
                strong ? 0.9F + attacker.getRNG().nextFloat() * 0.2F : 0.9F);
        if (!attack.basePending && attack.vanillaBonus == 0) {
            applyExtraKnockback(attacker, attack);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onKnockback(LivingKnockBackEvent event) {
        if (!(event.getAttacker() instanceof EntityLivingBase)) {
            return;
        }
        EntityLivingBase attacker = (EntityLivingBase) event.getAttacker();
        Attack attack = attacks.get(attacker);
        if (!matches(attack, attacker, event.getEntityLiving()) || !attack.damaged || attack.applied) {
            return;
        }
        if (attack.basePending) {
            attack.basePending = false;
            if (attack.vanillaBonus > 0 || event.isCanceled()) {
                return;
            }
            applyBaseKnockback(event);
            event.setCanceled(true);
            applyExtraKnockback(attacker, attack);
        } else if (!event.isCanceled()) {
            attack.applied = true;
            event.setStrength(event.getStrength() + 0.5F);
            event.getEntityLiving().velocityChanged = true;
        }
    }

    private static void applyBaseKnockback(LivingKnockBackEvent event) {
        EntityLivingBase target = event.getEntityLiving();
        if (target.getRNG().nextDouble() < target.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue()) {
            return;
        }
        target.isAirBorne = true;
        double length = MathHelper.sqrt(event.getRatioX() * event.getRatioX() + event.getRatioZ() * event.getRatioZ());
        target.motionX = target.motionX / 2 - event.getRatioX() / length * event.getStrength();
        target.motionZ = target.motionZ / 2 - event.getRatioZ() / length * event.getStrength();
        if (target.onGround) {
            target.motionY = Math.min(0.4000000059604645D, target.motionY / 2 + event.getStrength());
        }
    }

    private static void applyExtraKnockback(EntityLivingBase attacker, Attack attack) {
        attack.applied = true;
        EntityLivingBase target = attack.target;
        double x = target.motionX;
        double y = target.motionY;
        double z = target.motionZ;
        target.knockBack(attacker, 0.5F, MathHelper.sin(attacker.rotationYaw * 0.017453292F),
                -MathHelper.cos(attacker.rotationYaw * 0.017453292F));
        if (x != target.motionX || y != target.motionY || z != target.motionZ) {
            target.velocityChanged = true;
        }
        attacker.motionX *= 0.6D;
        attacker.motionZ *= 0.6D;
        attacker.setSprinting(false);
    }

    private static boolean usesSkillet(EntityLivingBase attacker) {
        return !attacker.world.isRemote && attacker.getHeldItemMainhand().getItem() instanceof ItemSkillet;
    }

    private static boolean matches(Attack attack, EntityLivingBase attacker, EntityLivingBase target) {
        return attack != null && attack.target == target && attack.tick == attacker.world.getTotalWorldTime()
                && usesSkillet(attacker);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            attacks.clear();
        }
    }

    private static final class Attack {
        private final EntityLivingBase target;
        private final long tick;
        private final float power;
        private final int vanillaBonus;
        private boolean basePending;
        private boolean damaged;
        private boolean applied;

        private Attack(EntityLivingBase attacker, EntityLivingBase target, float power) {
            this.target = target;
            this.tick = attacker.world.getTotalWorldTime();
            this.power = power;
            this.vanillaBonus = EnchantmentHelper.getKnockbackModifier(attacker)
                    + (attacker instanceof EntityPlayer && attacker.isSprinting() && power > 0.9F ? 1 : 0);
            this.basePending = target.hurtResistantTime <= target.maxHurtResistantTime / 2.0F;
        }
    }
}
