/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.api;

import io.github.manasmods.manascore.skill.impl.SkillStorage;
import lombok.NonNull;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;

public interface Skills {
    void markDirty();

    Collection<ManasSkillInstance> getLearnedSkills();

    /**
     * Updates a skill instance and optionally synchronizes the change across the network.
     * <p>
     * @param updatedInstance The instance to update
     * @param sync If true, synchronizes the change to all clients/server
     */
    void updateSkill(ManasSkillInstance updatedInstance, boolean sync);

    default boolean learnSkill(@NotNull ResourceLocation skillId) {
        return learnSkill(SkillAPI.getSkillRegistry().get(skillId).createDefaultInstance());
    }

    default boolean learnSkill(@NotNull ResourceLocation skillId, MutableComponent component) {
        return learnSkill(SkillAPI.getSkillRegistry().get(skillId).createDefaultInstance(), component);
    }

    default boolean learnSkill(@NonNull ManasSkill skill) {
        return learnSkill(skill.createDefaultInstance());
    }

    default boolean learnSkill(@NonNull ManasSkill skill, MutableComponent component) {
        return learnSkill(skill.createDefaultInstance(), component);
    }

    default boolean learnSkill(ManasSkillInstance instance) {
        return learnSkill(instance, Component.translatable("manascore.skill.learn_skill", instance.getChatDisplayName(true)));
    }

    boolean learnSkill(ManasSkillInstance instance, MutableComponent component);

    Optional<ManasSkillInstance> getSkill(@NotNull ResourceLocation skillId);

    default Optional<ManasSkillInstance> getSkill(@NonNull ManasSkill skill) {
        return getSkill(skill.getRegistryName());
    }

    void forgetSkill(@NotNull ResourceLocation skillId, @Nullable MutableComponent component);

    default void forgetSkill(@NotNull ResourceLocation skillId) {
        forgetSkill(skillId, null);
    }

    default void forgetSkill(@NonNull ManasSkill skill, @Nullable MutableComponent component) {
        forgetSkill(skill.getRegistryName(), component);
    }

    default void forgetSkill(@NonNull ManasSkill skill) {
        forgetSkill(skill.getRegistryName());
    }

    default void forgetSkill(@NonNull ManasSkillInstance instance, @Nullable MutableComponent component) {
        forgetSkill(instance.getSkillId(), component);
    }

    default void forgetSkill(@NonNull ManasSkillInstance instance) {
        forgetSkill(instance.getSkillId());
    }

    void forEachSkill(BiConsumer<SkillStorage, ManasSkillInstance> skillInstanceConsumer);
}
