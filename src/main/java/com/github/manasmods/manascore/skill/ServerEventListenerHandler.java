package com.github.manasmods.manascore.skill;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import com.github.manasmods.manascore.api.skills.listener.LivingHurtListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ManasCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
class ServerEventListenerHandler {
    @SubscribeEvent
    public static void livingHurtListener(final LivingHurtEvent e) {
        final LivingEntity entity = e.getEntityLiving();
        // In this case, we only want this to happen on Server Side
        if (entity.getLevel().isClientSide()) return;
        // Get all listening skills and invoke their event consumer
        SkillStorage skillStorage = SkillAPI.getSkillsFrom(entity);
        for (ManasSkillInstance skillInstance : skillStorage.getLearnedSkills()) {
            if (skillInstance.getSkill() instanceof LivingHurtListener listener) {
                listener.onLivingHurt(skillInstance, e);
            }
        }
        //Sync changed Skills
        skillStorage.syncChanges();
    }
}
