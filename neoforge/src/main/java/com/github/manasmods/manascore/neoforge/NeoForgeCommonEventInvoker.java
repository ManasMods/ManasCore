package com.github.manasmods.manascore.neoforge;

import com.github.manasmods.manascore.api.world.entity.EntityEvents;
import com.github.manasmods.manascore.attribute.ManasCoreAttributes;
import com.github.manasmods.manascore.utils.Changeable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod.EventBusSubscriber
public class NeoForgeCommonEventInvoker {
    private NeoForgeCommonEventInvoker() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onCriticalHit(final CriticalHitEvent e) {
        double critChance = e.getEntity().getAttributeValue(ManasCoreAttributes.CRIT_CHANCE.get()) / 100;
        RandomSource rand = e.getEntity().getRandom();

        if (!e.isVanillaCritical() && rand.nextFloat() > critChance) return;
        float critMultiplier = (float) e.getEntity().getAttributeValue(ManasCoreAttributes.CRIT_MULTIPLIER.get());
        float critModifier = e.getDamageModifier() * critMultiplier;

        e.setDamageModifier(critModifier);
        e.setResult(Event.Result.ALLOW);
    }

    @SubscribeEvent
    public static void modifyMiningSpeed(final PlayerEvent.BreakSpeed e) {
        AttributeInstance instance = e.getEntity().getAttribute(ManasCoreAttributes.MINING_SPEED_MULTIPLIER.get());
        if (instance == null) return;
        e.setNewSpeed((float) (e.getOriginalSpeed() * instance.getValue()));
    }

    @SubscribeEvent
    static void onLivingChangeTarget(final LivingChangeTargetEvent e) {
        Changeable<LivingEntity> changeableTarget = Changeable.of(e.getNewTarget());
        if (EntityEvents.LIVING_CHANGE_TARGET.invoker().changeTarget(e.getEntity(), changeableTarget).isFalse()) {
            e.setCanceled(true);
        } else {
            e.setNewTarget(changeableTarget.get());
        }
    }

    @SubscribeEvent
    static void onLivingHurt(final LivingHurtEvent e) {
        Changeable<Float> amount = Changeable.of(e.getAmount());
        if (EntityEvents.LIVING_HURT.invoker().hurt(e.getEntity(), e.getSource(), amount).isFalse()) {
            e.setCanceled(true);
        } else {
            e.setAmount(amount.get());
        }
    }

    @SubscribeEvent
    static void onLivingDamage(final LivingDamageEvent e) {
        Changeable<Float> amount = Changeable.of(e.getAmount());
        if (EntityEvents.LIVING_DAMAGE.invoker().damage(e.getEntity(), e.getSource(), amount).isFalse()) {
            e.setCanceled(true);
        } else {
            e.setAmount(amount.get());
        }
    }

    @SubscribeEvent
    static void onProjectileHit(final ProjectileImpactEvent e) {
        Changeable<EntityEvents.ProjectileHitResult> result = Changeable.of(EntityEvents.ProjectileHitResult.DEFAULT);
        EntityEvents.PROJECTILE_HIT.invoker().hit(e.getRayTraceResult(), e.getProjectile(), result);
        if (result.get().equals(EntityEvents.ProjectileHitResult.PASS)) e.setCanceled(true);
    }
}
