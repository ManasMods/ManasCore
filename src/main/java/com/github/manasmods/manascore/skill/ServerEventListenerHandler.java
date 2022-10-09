package com.github.manasmods.manascore.skill;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.listener.LivingHurtListener;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ManasCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
class ServerEventListenerHandler {
    @SubscribeEvent
    public static void livingHurtListener(final LivingHurtEvent e) {
        // In this case, we only want this to happen on Server Side
        if (e.getEntityLiving().getLevel().isClientSide()) return;
        // Get all listening skills and invoke their event consumer
        for (ManasSkillInstance skillInstance : SkillAPI.getSkillsFrom(e.getEntity()).getLearnedSkills()) {
            if (skillInstance.getSkill() instanceof LivingHurtListener listener) {
                listener.onLivingHurt(skillInstance, e);
            }
        }
    }
}
