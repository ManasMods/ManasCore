package com.github.manasmods.manascore.api.skills.event;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import lombok.Getter;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * Base class for all {@link Event}s that involve {@link ManasSkill}s
 *
 * @see UnlockSkillEvent
 */
@ApiStatus.AvailableSince("1.0.2.0")
public class SkillEvent extends Event {
    @Getter
    private final ManasSkillInstance skillInstance;

    public SkillEvent(ManasSkillInstance skillInstance) {
        this.skillInstance = skillInstance;
    }
}