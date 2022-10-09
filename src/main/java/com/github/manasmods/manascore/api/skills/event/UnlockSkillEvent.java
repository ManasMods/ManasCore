package com.github.manasmods.manascore.api.skills.event;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import lombok.Getter;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Cancelable;
import org.jetbrains.annotations.ApiStatus;

/**
 * This Event is fired right before a {@link Entity} unlocks a new {@link ManasSkill}.
 * You can prevent an {@link Entity} from learning that {@link ManasSkill} by canceling the {@link UnlockSkillEvent}.
 * <p>
 */
@ApiStatus.AvailableSince("0.0.0.26")
@Cancelable
public class UnlockSkillEvent extends SkillEvent {
    @Getter
    private final Entity entity;

    public UnlockSkillEvent(ManasSkillInstance skillInstance, Entity entity) {
        super(skillInstance);
        this.entity = entity;
    }
}
