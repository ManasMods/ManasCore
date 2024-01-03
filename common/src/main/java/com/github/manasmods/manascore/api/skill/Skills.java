package com.github.manasmods.manascore.api.skill;

import com.github.manasmods.manascore.skill.SkillStorage;
import lombok.NonNull;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;

public interface Skills {
    void markDirty();

    Collection<ManasSkillInstance> getLearnedSkills();

    void updateSkill(ManasSkillInstance updatedInstance, boolean sync);

    default boolean learnSkill(@NonNull ManasSkill skill) {
        return learnSkill(new ManasSkillInstance(skill));
    }

    boolean learnSkill(ManasSkillInstance instance);

    Optional<ManasSkillInstance> getSkill(@NonNull ResourceLocation skillId);

    default Optional<ManasSkillInstance> getSkill(@NonNull ManasSkill skill) {
        return getSkill(skill.getRegistryName());
    }

    default void forgetSkill(ManasSkill skill) {
        getSkill(skill).ifPresent(this::forgetSkill);
    }

    void forgetSkill(ManasSkillInstance instance);

    void forEachSkill(BiConsumer<SkillStorage, ManasSkillInstance> skillInstanceConsumer);
}
