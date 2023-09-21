package com.github.manasmods.manascore.api.skills.event;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import lombok.Getter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import org.jetbrains.annotations.ApiStatus;

/**
 * This Event is fired right before a {@link Player} toggle a {@link ManasSkill} with keybind.
 * You can prevent an {@link Entity} from toggling that {@link ManasSkill} by canceling the {@link SkillTickEvent}.
 * <p>
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