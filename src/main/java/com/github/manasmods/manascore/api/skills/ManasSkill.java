package com.github.manasmods.manascore.api.skills;

import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.ApiStatus;

/**
 * This is the Registry Object for Skills.
 * Extend from this Class to create your own Skills.
 * <p>
 * To add functionality to the {@link ManasSkill}, you need to implement a listener interface.
 * Those interfaces allow you to invoke a Method when an {@link Event} happens.
 * The Method will only be invoked for an {@link Entity} that learned the {@link ManasSkill}.
 * <p>
 * Skills can be learned by calling the {@link SkillStorage#learnSkill} method.
 * You can simply use {@link SkillAPI#getSkillsFrom(Entity)} to get the {@link SkillStorage} of an {@link Entity}.
 * <p>
 * You're also allowed to override the {@link ManasSkill#createDefaultInstance()} method to create your own implementation
 * of a {@link ManasSkillInstance}. This is required if you want to attach additional data to the {@link ManasSkill}
 * (for example to allow to disable a skill or make the skill gain exp on usage).
 */
@ApiStatus.AvailableSince("0.0.0.26")
public class ManasSkill extends ForgeRegistryEntry<ManasSkill> {
    /**
     * Used to create a {@link ManasSkillInstance} of this Skill.
     * <p>
     * Override this Method to use your extended version of {@link ManasSkillInstance}
     */
    public ManasSkillInstance createDefaultInstance() {
        return new ManasSkillInstance(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManasSkill that = (ManasSkill) o;
        return this.getRegistryName().equals(that.getRegistryName());
    }
}
