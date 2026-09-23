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

    public abstract TeamShape getShape();

    public abstract Class<T> getTeamClass();

    /**
     * GROUP only. Creates the team object. Override to return a subclass with custom data.
     */
    @SuppressWarnings("unchecked")
    public T createTeam(UUID id, UUID owner) {
        return (T) new Team(id, this, owner);
    }

    /** How many teams of this type one entity may belong to. */
    public int getMaxTeamsPerMember() {
        return 1;
    }

    public int getMaxMembers() {
        return Integer.MAX_VALUE;
    }

    public int getMaxNameLength() {
        return 32;
    }

    public boolean canRename(T team, LivingEntity entity) {
        return team.isOwner(entity);
    }

    public LimitPolicy onLimitReached() {
        return LimitPolicy.REJECT;
    }

    /** Advisory for UIs. {@link TeamAPI#addMember} and {@link TeamAPI#addRelation} always bypass invites. */
    public boolean requiresInvite() {
        return true;
    }

    public int getInviteTimeoutTicks() {
        return 20 * 60;
    }

    /** RELATION only. When true, adding a to b also adds b to a and checks look both ways. */
    public boolean isSymmetricRelation() {
        return true;
    }

    /** RELATION only. How many entities one entity may relate to under this type. */
    public int getMaxRelations() {
        return 64;
    }

    public boolean blocksFriendlyFire() {
        return false;
    }

    public boolean blocksTargeting() {
        return false;
    }

    /** Higher priority types decide the folded relation first. */
    public int getPriority() {
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
     * passed through {@link #resolveMember(LivingEntity)}. RELATION's default also consults each
     * side's inbound set as a fallback, so an offline or untracked outbound side is still caught.
     * GROUP falls back to the team objects the querying side knows (server table, client cache),
     * so a client can resolve its own party mates although their storage is not synced.
     * <p>Types are skipped by the resolver when both sides have no team data at all
     * ({@link Teams#isEmpty()}); a type that derives relations from something else must hook
     * {@link TeamEvents#RESOLVE_RELATION}.
     */
    public Relation getRelation(LivingEntity a, Teams aTeams, LivingEntity b, Teams bTeams) {
        return switch (this.getShape()) {
            case GROUP -> {
                boolean client = a.level().isClientSide();
                Set<UUID> mine = aTeams.getTeamIds(this);
                Set<UUID> theirs = bTeams.getTeamIds(this);
                for (UUID id : mine) {
                    if (theirs.contains(id) || (client && TeamAPI.getTeam(a, id).map(team -> team.isMember(b)).orElse(false))) yield Relation.ALLY;
                }
                for (UUID id : theirs) {
                    if (client && TeamAPI.getTeam(b, id).map(team -> team.isMember(a)).orElse(false)) yield Relation.ALLY;
                }
                yield Relation.NEUTRAL;
            }
            case RELATION -> {
                if (aTeams.getRelated(this).contains(b.getUUID())) yield Relation.ALLY;
                if (bTeams.getRelatedBy(this).contains(a.getUUID())) yield Relation.ALLY;
                if (!this.isSymmetricRelation()) yield Relation.NEUTRAL;
                if (bTeams.getRelated(this).contains(a.getUUID())) yield Relation.ALLY;
                if (aTeams.getRelatedBy(this).contains(b.getUUID())) yield Relation.ALLY;
                yield Relation.NEUTRAL;
            }
        };
    }

    public boolean canJoin(T team, LivingEntity joiner) {
        return true;
    }

    /** Determine if the inviter may send invites at all, regardless of invitee. */
    public boolean canInvite(T team, LivingEntity inviter) {
        return team.isOwner(inviter);
    }

    /** Determine if the inviter may invite this specific invitee. */
    public boolean canInvite(T team, LivingEntity inviter, LivingEntity invitee) {
        return this.canInvite(team, inviter);
    }

    /** Determine if the actor may kick the target member. */
    public boolean canKick(T team, LivingEntity actor, UUID target) {
        return team.isOwner(actor) && !team.isOwner(target);
    }

    /** Determine if the actor may promote the target member to owner. */
    public boolean canPromote(T team, LivingEntity actor, UUID target) {
        return team.isOwner(actor) && team.isMember(target) && !team.isOwner(target);
    }

    /** Determine if the actor may leave the team. */
    public boolean canLeave(T team, LivingEntity actor) {
        return true;
    }

    /** Determine if the actor may disband the team. */
    public boolean canDisband(T team, LivingEntity actor) {
        return team.isOwner(actor);
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
