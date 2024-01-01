package com.github.manasmods.manascore.forge;

import com.github.manasmods.manascore.api.world.entity.EntityEvents;
import com.github.manasmods.manascore.api.world.entity.EntityEvents.ProjectileHitResult;
import com.github.manasmods.manascore.utils.Changeable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ForgeCommonEventInvoker {
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
            default -> result = Changeable.of(ProjectileHitResult.DEFAULT);
            case STOP_AT_CURRENT -> result = Changeable.of(ProjectileHitResult.HIT);
            case STOP_AT_CURRENT_NO_DAMAGE -> result = Changeable.of(ProjectileHitResult.HIT_NO_DAMAGE);
            case SKIP_ENTITY -> result = Changeable.of(ProjectileHitResult.PASS);
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
