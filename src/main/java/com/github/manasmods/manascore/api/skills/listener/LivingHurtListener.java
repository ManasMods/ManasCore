package com.github.manasmods.manascore.api.skills.listener;

import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import net.minecraftforge.event.entity.living.LivingHurtEvent;


public interface LivingHurtListener {
    void onLivingHurt(final ManasSkillInstance instance, final LivingHurtEvent event);
}
