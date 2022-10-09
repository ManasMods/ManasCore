package com.github.manasmods.manascore.api.skills;

import lombok.RequiredArgsConstructor;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("0.0.0.26")
@RequiredArgsConstructor
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
