package com.github.manasmods.manascore.api.skills.event;

import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import lombok.Getter;
import net.minecraftforge.eventbus.api.Event;

public class SkillEvent extends Event {
    @Getter
    private final ManasSkillInstance skillInstance;

    public SkillEvent(ManasSkillInstance skillInstance) {
        this.skillInstance = skillInstance;
    }
}
