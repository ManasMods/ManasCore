package com.github.manasmods.manascore.api.skills.event;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import lombok.Getter;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Cancelable;
import org.jetbrains.annotations.ApiStatus;

/**
 * This Event is fired right before a {@link Entity} forgets a {@link ManasSkill}.
 * You can prevent an {@link Entity} from forgetting that {@link ManasSkill} by canceling the {@link RemoveSkillEvent}.
 * <p>
 */
@ApiStatus.AvailableSince("1.0.2.0")
@Cancelable
public class RemoveSkillEvent extends SkillEvent {
    @Getter
    private final Entity entity;

    public RemoveSkillEvent(ManasSkillInstance skillInstance, Entity entity) {
        super(skillInstance);
        this.entity = entity;
    }
}