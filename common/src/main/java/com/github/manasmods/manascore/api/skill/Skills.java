package com.github.manasmods.manascore.api.skill;

import com.github.manasmods.manascore.skill.SkillStorage;
import lombok.NonNull;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;

public interface Skills {
    void markDirty();

    Collection<ManasSkillInstance> getLearnedSkills();

    void updateSkill(ManasSkillInstance updatedInstance, boolean sync);

    default boolean learnSkill(@NotNull ResourceLocation skillId) {
        return learnSkill(SkillAPI.getSkillRegistry().get(skillId).createDefaultInstance());
    }

    default boolean learnSkill(@NonNull ManasSkill skill) {
        return learnSkill(skill.createDefaultInstance());
    }

    boolean learnSkill(ManasSkillInstance instance);

    Optional<ManasSkillInstance> getSkill(@NotNull ResourceLocation skillId);

    default Optional<ManasSkillInstance> getSkill(@NonNull ManasSkill skill) {
        return getSkill(skill.getRegistryName());
    }

    void forgetSkill(@NotNull ResourceLocation skillId);

    default void forgetSkill(@NonNull ManasSkill skill) {
        forgetSkill(skill.getRegistryName());
    }

    default void forgetSkill(@NonNull ManasSkillInstance instance) {
        forgetSkill(instance.getSkillId());
    }

    void forEachSkill(BiConsumer<SkillStorage, ManasSkillInstance> skillInstanceConsumer);
}
