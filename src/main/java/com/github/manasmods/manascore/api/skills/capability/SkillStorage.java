package com.github.manasmods.manascore.api.skills.capability;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.network.toclient.SyncSkillsPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Interface for Interactions with the Player Skill Storage.
 * This allows you to modify the Skill data of the selected Player (including skills you don't own).
 */
@ApiStatus.NonExtendable
@ApiStatus.AvailableSince("1.0.2.0")
public interface SkillStorage extends INBTSerializable<CompoundTag> {
    /**
     * Returns a list of all learned {@link ManasSkill}s as {@link ManasSkillInstance}.
     */
    Collection<ManasSkillInstance> getLearnedSkills();

    /**
     * Replaces (or adds) {@link ManasSkillInstance}s based on their registry id.
     * This is mainly used for Synchronization between Server and Clients.
     * <p>
     * Use {@link SkillStorage#updateSkills(List)} or {@link SkillStorage#updateSkills(ManasSkillInstance...)} to update multiple instances at once.
     *
     * @param updatedInstance the new {@link ManasSkillInstance} Object which replaces the old instances.
     */
    void updateSkill(ManasSkillInstance updatedInstance);

    /**
     * Replaces (or adds) {@link ManasSkillInstance}s based on their registry id.
     * This is mainly used for Synchronization between Server and Clients.
     *
     * @see SkillStorage#updateSkill(ManasSkillInstance)
     */
    void updateSkills(List<ManasSkillInstance> updatedInstances);

    /**
     * Replaces (or adds) {@link ManasSkillInstance}s based on their registry id.
     * This is mainly used for Synchronization between Server and Clients.
     *
     * @see SkillStorage#updateSkill(ManasSkillInstance)
     */
    void updateSkills(ManasSkillInstance... updatedInstances);

    /**
     * Creates a new {@link ManasSkillInstance} by calling {@link ManasSkill#createDefaultInstance()} and stores it in the {@link Player} Skill Storage.
     * Use this Method to add new Skills to a Player.
     *
     * @return true if the skill has been learned or false if it hasn't
     */
    boolean learnSkill(ManasSkill skill);

    /**
     * Stores the given {@link ManasSkillInstance} as new Skill in the Skill {@link SkillStorage}.
     *
     * @return true if the skill has been learned or false if it hasn't
     */
    boolean learnSkill(ManasSkillInstance instance);

    /**
     * Removes a skill from the Player.
     */
    void forgetSkill(ManasSkillInstance skill);

    /**
     * Removes a skill from the Player.
     */
    void forgetSkill(ManasSkill skill);

    /**
     * Returns an {@link Optional} of the stored {@link ManasSkillInstance} or {@link Optional#empty()}
     */
    Optional<ManasSkillInstance> getSkill(ManasSkill skill);

    /**
     * Sends a {@link SyncSkillsPacket} to all tracking Clients to update their local states.
     * <p>
     * The {@link SyncSkillsPacket} only contains {@link ManasSkillInstance}s which are marked as dirty.
     */
    void syncChanges();

    /**
     * Sends a {@link SyncSkillsPacket} to all tracking Clients to update their local states.
     * <p>
     * The {@link SyncSkillsPacket} contains <strong>ALL</strong> {@link ManasSkillInstance}s.
     */
    void syncAll();

    /**
     * Sends a {@link SyncSkillsPacket} to the given Player.
     * <p>
     * The {@link SyncSkillsPacket} contains <strong>ALL</strong> {@link ManasSkillInstance}s.
     */
    void syncPlayer(Player player);
}