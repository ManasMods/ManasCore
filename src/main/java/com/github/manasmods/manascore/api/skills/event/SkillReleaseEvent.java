package com.github.manasmods.manascore.api.skills.event;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import org.jetbrains.annotations.ApiStatus;

/**
 * This Event is fired right before triggering onRelease method of a {@link ManasSkill}.
 * You can prevent an {@link Entity} from activating the release action by canceling the {@link SkillReleaseEvent}.
 * <p>
 */
@ApiStatus.AvailableSince("2.0.18.0")
@Cancelable
public class SkillReleaseEvent extends SkillEvent {
    @Getter
    private final Player entity;
    @Getter
    private final int keyNumber;
    @Getter
    @Setter
    private int ticks;

    public SkillReleaseEvent(ManasSkillInstance skillInstance, Player entity, int keyNumber, int ticks) {
        super(skillInstance);
        this.entity = entity;
        this.keyNumber = keyNumber;
        this.ticks = ticks;
    }
}