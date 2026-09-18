/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.api;

import io.github.manasmods.manascore.team.api.template.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * Registry entry describing one kind of team. Register with a
 * {@code DeferredRegister<TeamType<?>>} created from {@link TeamAPI#getTeamTypeRegistryKey()}.
 *
 * @param <T> team object class for GROUP shaped types. RELATION shaped types use {@link Team}.
 */
public abstract class TeamType<T extends Team> {

    public abstract TeamShape shape();

    public abstract Class<T> teamClass();

    /**
     * GROUP only. Creates the team object. Override to return a subclass with custom data.
     */
    @SuppressWarnings("unchecked")
    public T createTeam(UUID id, UUID owner) {
        return (T) new Team(id, this, owner);
    }

    /** How many teams of this type one entity may belong to. */
    public int maxTeamsPerMember() {
        return 1;
    }

    public int maxMembers() {
        return Integer.MAX_VALUE;
    }

    public LimitPolicy onLimitReached() {
        return LimitPolicy.REJECT;
    }

    /** Advisory for UIs. {@link TeamAPI#addMember} and {@link TeamAPI#addRelation} always bypass invites. */
    public boolean requiresInvite() {
        return true;
    }

    public int inviteTimeoutTicks() {
        return 20 * 60;
    }

    /** RELATION only. When true, adding a to b also adds b to a and checks look both ways. */
    public boolean symmetricRelation() {
        return true;
    }

    public boolean blocksFriendlyFire() {
        return false;
    }

    public boolean blocksTargeting() {
        return false;
    }

    /** Higher priority types decide the folded relation first. */
    public int priority() {
        return 0;
    }

    /**
     * Maps an entity to the entity whose membership counts. Default: identity.
     * Ally maps a tamed animal to its owner.
     */
    public LivingEntity resolveMember(LivingEntity entity) {
        return entity;
    }

    /**
     * Relation of {@code a} towards {@code b} for this type only. Both entities are already
     * passed through {@link #resolveMember(LivingEntity)}.
     */
    public Relation getRelation(LivingEntity a, Teams aTeams, LivingEntity b, Teams bTeams) {
        return switch (this.shape()) {
            case GROUP -> {
                Set<UUID> mine = aTeams.getTeamIds(this);
                if (mine.isEmpty()) yield Relation.NEUTRAL;
                for (UUID id : bTeams.getTeamIds(this)) {
                    if (mine.contains(id)) yield Relation.ALLY;
                }
                yield Relation.NEUTRAL;
            }
            case RELATION -> {
                if (aTeams.getRelated(this).contains(b.getUUID())) yield Relation.ALLY;
                if (this.symmetricRelation() && bTeams.getRelated(this).contains(a.getUUID())) yield Relation.ALLY;
                yield Relation.NEUTRAL;
            }
        };
    }

    public boolean canJoin(T team, LivingEntity joiner) {
        return true;
    }

    public boolean canInvite(T team, LivingEntity inviter, LivingEntity invitee) {
        return team.isOwner(inviter);
    }

    /** Called when the owner leaves and members remain. Return null to disband instead. */
    @Nullable
    public UUID pickNewOwner(T team) {
        for (UUID member : team.getMembers()) {
            if (!team.isOwner(member)) return member;
        }
        return null;
    }

    public void onTeamCreated(T team) {
    }

    public void onTeamDisbanded(T team) {
    }

    public void onMemberAdded(T team, UUID member) {
    }

    public void onMemberRemoved(T team, UUID member, LeaveReason reason) {
    }

    /** Registry id. Null before registration. */
    @Nullable
    public ResourceLocation getId() {
        return TeamAPI.getTeamTypeRegistry().getId(this);
    }

    public String toString() {
        return "TeamType{" + this.getId() + '}';
    }
}
