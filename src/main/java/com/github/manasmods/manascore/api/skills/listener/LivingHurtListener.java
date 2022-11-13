package com.github.manasmods.manascore.api.skills.listener;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Event listener interface for {@link ManasSkill}s.
 * <p>
 * This listener subscribes to the <strong>server side</strong> of the {@link LivingHurtEvent}.
 * To use this Listener, just implement it in your {@link ManasSkill} class.
 */
@ApiStatus.AvailableSince("1.0.2.0")
public interface LivingHurtListener {
    void onLivingHurt(final ManasSkillInstance instance, final LivingHurtEvent event);
}
