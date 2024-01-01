package com.github.manasmods.manascore.forge;

import com.github.manasmods.manascore.api.world.entity.EntityEvents;
import com.github.manasmods.manascore.utils.Changeable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
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
}
