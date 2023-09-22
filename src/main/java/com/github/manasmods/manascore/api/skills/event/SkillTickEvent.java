package com.github.manasmods.manascore.api.skills.event;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import lombok.Getter;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import org.jetbrains.annotations.ApiStatus;

/**
 * This Event is fired right before {@link ManasSkill#onTick} is invoked.
 * Cancel this event to prevent the {@link ManasSkill#onTick} invocation.
 */
@ApiStatus.AvailableSince("2.0.18.0")
@Cancelable
public class SkillTickEvent extends SkillEvent {
    @Getter
    private final Player entity;

    public SkillTickEvent(ManasSkillInstance skillInstance, Player entity) {
        super(skillInstance);
        this.entity = entity;
    }
}