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
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;

public interface Skills {
    void markDirty();
    /**
     * Marks this to start performing regular active ticks
     */
    void markActiveTick();
    /**
     * This Method is invoked to indicated that all {@link ManasSkillInstance} objects possessed by this have had their active ticks finished.
     * <p>
     * Do <strong>NOT</strong> use this method on your own!
     */
    @ApiStatus.Internal
    void clearActiveTick();
    /**
     * Returns if this instance was marked to tick cooldowns using {@link Skills#markActiveTick()}
     */
    boolean shouldActiveTick();
    /**
     * Returns the LivingEntity to which this object is attached.
     */
    LivingEntity getOwner();

    /**
     * Starts activating the specified skill and treating it as if it were being held down. Returns true if successful.
     */
    boolean startHoldSkill(ResourceLocation skillId, int keyNumber, int mode);

    /**
     * Starts activating the specified skill and treating it as if it were being held down. Returns true if successful.
     */
    boolean startHoldSkill(ManasSkillInstance skillInstance, int keyNumber, int mode);

    /**
     * If the provided instance is dirty, marks this as dirty.
     */
    default void checkAndMarkDirty(ManasSkillInstance instance) {
        if (instance.isDirty()) this.markDirty();
    }

    /**
     * Returns all {@link ManasSkillInstance} possessed by this object.
     */
    Collection<ManasSkillInstance> getLearnedSkills();

    /**
     * Updates a skill instance and optionally synchronizes the change across the network.
     * <p>
     * @param updatedInstance The instance to update
     * @param sync If true, synchronizes the change to all clients/server
     */
    void updateSkill(ManasSkillInstance updatedInstance, boolean sync);

    /**
     * @param skillId The corresponding skill is retrieded from the skill registry, then the storage attempts to learn {@link ManasSkill#createDefaultInstance()}
     * @return If the storage learned the skill.
     */
    default boolean learnSkill(@NotNull ResourceLocation skillId) {
        return learnSkill(SkillAPI.getSkillRegistry().get(skillId).createDefaultInstance());
    }

    /**
     * @param skillId The corresponding skill is retrieded from the skill registry, then the storage attempts to learn {@link ManasSkill#createDefaultInstance()}
     * @param component Shown to the owner of the storage if the skill was learned successfully.
     * @return If the storage learned the skill.
     */
    default boolean learnSkill(@NotNull ResourceLocation skillId, MutableComponent component) {
        return learnSkill(SkillAPI.getSkillRegistry().get(skillId).createDefaultInstance(), component);
    }

    /**
     * @param skill The storage attempts to learn {@link ManasSkill#createDefaultInstance()}
     * @return If the storage learned the skill.
     */
    default boolean learnSkill(@NonNull ManasSkill skill) {
        return learnSkill(skill.createDefaultInstance());
    }

    /**
     * @param skill The storage attempts to learn {@link ManasSkill#createDefaultInstance()}
     * @param component Shown to the owner of the storage if the skill was learned successfully.
     * @return If the storage learned the skill.
     */
    default boolean learnSkill(@NonNull ManasSkill skill, MutableComponent component) {
        return learnSkill(skill.createDefaultInstance(), component);
    }

    /**
     * @param instance The instance to be learned
     * @return If the storage learned the instance.
     */
    default boolean learnSkill(ManasSkillInstance instance) {
        return learnSkill(instance, Component.translatable("manascore.skill.learn_skill", instance.getChatDisplayName(true)));
    }

    /**
     * @param instance The instance to be learned
     * @param component Shown to the owner of the storage if the skill was learned successfully.
     * @return If the storage learned the instance.
     */
    boolean learnSkill(ManasSkillInstance instance, MutableComponent component);

    /**
     * Returns an optional containing a {@link ManasSkillInstance} corresponding to the provided skillId if present.
     */
    Optional<ManasSkillInstance> getSkill(@NotNull ResourceLocation skillId);

    /**
     * Returns an optional containing a {@link ManasSkillInstance} corresponding to the provided {@link ManasSkill} if present.
     */
    default Optional<ManasSkillInstance> getSkill(@NonNull ManasSkill skill) {
        return getSkill(skill.getRegistryName());
    }

    /**
     * Removes a {@link ManasSkillInstance} from this object if present.
     * @param skillId Same as {@link ManasSkillInstance#getSkillId()} of the instance to be removed
     * @param component If not null, played to the owner if the skill is removed.
     */
    void forgetSkill(@NotNull ResourceLocation skillId, @Nullable MutableComponent component);

    /**
     * Removes a {@link ManasSkillInstance} from this object if present.
     * @param skillId Same as {@link ManasSkillInstance#getSkillId()} of the instance to be removed
     */
    default void forgetSkill(@NotNull ResourceLocation skillId) {
        forgetSkill(skillId, null);
    }

    /**
     * Removes a {@link ManasSkillInstance} from this object if present.
     * <p>
     * See {@link Skills#forgetSkill(ResourceLocation, MutableComponent)}
     * @param skill Uses {@link ManasSkill#getRegistryName()} as the resource location of the skill to be removed.
     * @param component If not null, played to the owner if the skill is removed.
     */
    default void forgetSkill(@NonNull ManasSkill skill, @Nullable MutableComponent component) {
        forgetSkill(skill.getRegistryName(), component);
    }

    /**
     * Removes a {@link ManasSkillInstance} from this object if present.
     * <p>
     * See {@link Skills#forgetSkill(ResourceLocation)}
     * @param skill Uses {@link ManasSkill#getRegistryName()} as the resource location of the skill to be removed.
     */
    default void forgetSkill(@NonNull ManasSkill skill) {
        forgetSkill(skill.getRegistryName());
    }

    /**
     * Removes a {@link ManasSkillInstance} from this object if present.
     * <p>
     * See {@link Skills#forgetSkill(ResourceLocation, MutableComponent)}
     * @param instance Uses {@link ManasSkillInstance#getSkillId()} to determine the skill to be removed
     * @param component If not null, played to the owner if the skill is removed.
     */
    default void forgetSkill(@NonNull ManasSkillInstance instance, @Nullable MutableComponent component) {
        forgetSkill(instance.getSkillId(), component);
    }

    /**
     * Removes a {@link ManasSkillInstance} from this object if present.
     * <p>
     * See {@link Skills#forgetSkill(ResourceLocation)}
     * @param instance Uses {@link ManasSkillInstance#getSkillId()} to determine the skill to be removed
     */
    default void forgetSkill(@NonNull ManasSkillInstance instance) {
        forgetSkill(instance.getSkillId());
    }

    /**
     * Runs the provided {@link BiConsumer} over all {@link ManasSkillInstance} possessed by the storage.
     */
    void forEachSkill(BiConsumer<SkillStorage, ManasSkillInstance> skillInstanceConsumer);
}
