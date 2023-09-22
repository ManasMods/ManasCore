package com.github.manasmods.manascore.api.skills.event;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import org.jetbrains.annotations.ApiStatus;

/**
 * This Event is fired right before {@link ManasSkill#onScroll} is invoked.
 * Cancel this event to prevent the {@link ManasSkill#onScroll} invocation.
 */
@ApiStatus.AvailableSince("2.0.18.0")
@Cancelable
public class SkillScrollEvent extends SkillEvent {
    @Getter
    private final Player entity;
    @Getter
    @Setter
    private double scrollDelta;

    public SkillScrollEvent(ManasSkillInstance skillInstance, Player entity, double scrollDelta) {
        super(skillInstance);
        this.entity = entity;
        this.scrollDelta = scrollDelta;
    }
}