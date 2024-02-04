package com.github.manasmods.manascore.forge;

import com.github.manasmods.manascore.api.world.entity.EntityEvents;
import com.github.manasmods.manascore.api.world.entity.EntityEvents.ProjectileHitResult;
import com.github.manasmods.manascore.attribute.ManasCoreAttributes;
import com.github.manasmods.manascore.utils.Changeable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ForgeCommonEventInvoker {
    private ForgeCommonEventInvoker() {
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
        Changeable<ProjectileHitResult> result;

        switch (e.getImpactResult()) {
            case STOP_AT_CURRENT -> result = Changeable.of(ProjectileHitResult.HIT);
            case STOP_AT_CURRENT_NO_DAMAGE -> result = Changeable.of(ProjectileHitResult.HIT_NO_DAMAGE);
            case SKIP_ENTITY -> result = Changeable.of(ProjectileHitResult.PASS);
            default -> result = Changeable.of(ProjectileHitResult.DEFAULT);
        }

        EntityEvents.PROJECTILE_HIT.invoker().hit(e.getRayTraceResult(), e.getProjectile(), result);

        switch (result.get()) {
            case DEFAULT -> e.setImpactResult(ProjectileImpactEvent.ImpactResult.DEFAULT);
            case HIT -> e.setImpactResult(ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT);
            case HIT_NO_DAMAGE -> e.setImpactResult(ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE);
            case PASS -> e.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
        }
    }
}
