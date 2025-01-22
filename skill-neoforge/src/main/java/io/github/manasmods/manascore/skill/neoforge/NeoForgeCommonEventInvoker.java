/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.neoforge;

import io.github.manasmods.manascore.skill.utils.Changeable;
import io.github.manasmods.manascore.skill.utils.EntityEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Objects;

@EventBusSubscriber
public class NeoForgeCommonEventInvoker {
    private NeoForgeCommonEventInvoker() {
    }

    @SubscribeEvent
    static void onLivingChangeTarget(final LivingChangeTargetEvent e) {
        if (!e.getTargetType().equals(LivingChangeTargetEvent.LivingTargetType.MOB_TARGET)) return;
        Changeable<LivingEntity> changeableTarget = Changeable.of(e.getNewAboutToBeSetTarget());
        if (EntityEvents.LIVING_CHANGE_TARGET.invoker().changeTarget(e.getEntity(), changeableTarget).isFalse()) {
            e.setCanceled(true);
        } else {
            e.setNewAboutToBeSetTarget(changeableTarget.get());
        }
    }

    @SubscribeEvent
    static void onLivingDamage(final LivingDamageEvent.Pre e) {
        Changeable<Float> changeableDamage = Changeable.of(e.getNewDamage());
        if (EntityEvents.LIVING_DAMAGE.invoker().damage(e.getEntity(), e.getSource(), changeableDamage).isFalse()) {
            e.setNewDamage(0);
        } else {
            e.setNewDamage(changeableDamage.get());
        }
    }

    @SubscribeEvent
    static void onProjectileHit(final ProjectileImpactEvent e) {
        Changeable<EntityEvents.ProjectileHitResult> result = Changeable.of(EntityEvents.ProjectileHitResult.DEFAULT);
        Changeable<ProjectileDeflection> deflection = Changeable.of(ProjectileDeflection.NONE);
        EntityEvents.PROJECTILE_HIT.invoker().hit(e.getRayTraceResult(), e.getProjectile(), deflection, result);
        if (!Objects.equals(result.get(), EntityEvents.ProjectileHitResult.DEFAULT)) e.setCanceled(true);
    }
}
