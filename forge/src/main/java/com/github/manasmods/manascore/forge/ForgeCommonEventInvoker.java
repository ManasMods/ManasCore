package com.github.manasmods.manascore.forge;

import com.github.manasmods.manascore.api.world.entity.EntityEvents;
import com.github.manasmods.manascore.utils.Changeable;
import net.minecraft.world.entity.LivingEntity;
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
}
