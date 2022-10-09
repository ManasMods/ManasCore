package com.github.manasmods.manascore.api.skills.event;

import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import lombok.Getter;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class UnlockSkillEvent extends SkillEvent {
    @Getter
    private final Entity entity;

    public UnlockSkillEvent(ManasSkillInstance skillInstance, Entity entity) {
        super(skillInstance);
        this.entity = entity;
    }
}
