package com.github.manasmods.manascore.api.skills.event;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import lombok.Getter;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import org.jetbrains.annotations.ApiStatus;

/**
 * This Event is fired right before a {@link Player} activates a {@link ManasSkill} using a keybinding.
 * Prevent the {@link ManasSkill} activation by canceling this event.
 */
@ApiStatus.AvailableSince("2.0.18.0")
@Cancelable
public class SkillActivationEvent extends SkillEvent {
    @Getter
    private final Player entity;
    @Getter
    private final int keyNumber;

    public SkillActivationEvent(ManasSkillInstance skillInstance, Player entity, int keyNumber) {
        super(skillInstance);
        this.entity = entity;
        this.keyNumber = keyNumber;
    }
}