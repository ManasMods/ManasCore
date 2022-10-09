package com.github.manasmods.manascore.skill;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.listener.LivingHurtListener;
import lombok.extern.log4j.Log4j2;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Log4j2
public class HealSkill extends ManasSkill implements LivingHurtListener {
    public HealSkill() {

    }

    @Override
    public void onLivingHurt(ManasSkillInstance instance, LivingHurtEvent event) {
        float amount = event.getAmount() / 2;
        event.getEntityLiving().heal(amount);
        log.debug("Healed {} by {} health", event.getEntityLiving().getName().getString(), amount);
    }
}
