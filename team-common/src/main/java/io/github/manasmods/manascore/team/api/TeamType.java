/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.api;

import io.github.manasmods.manascore.team.api.template.*;
import io.github.manasmods.manascore.team.impl.OwnerResolvers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * This is the Registry Object for team types, which describe one kind of team.
 * Extend from this Class to create your own team type.
 * <p>
 * Register it with a {@code DeferredRegister<TeamType<?>>} created from {@link TeamAPI#getTeamTypeRegistryKey()}.
 * <p>
 * The {@link #getShape()} decides how the type works. {@code GROUP} types share a {@link Team} object between their members, {@code RELATION} types store the ids of related entities on each entity.
 * Most of the other methods are hooks that can be overridden to change the rules of the type.
 *
 * @param <T> team object class for GROUP shaped types. RELATION shaped types use {@link Team}.
 */
public abstract class TeamType<T extends Team> {

    /**
     * Used to get the {@link TeamShape} of this type.
     */
    public abstract TeamShape getShape();

    /**
     * Used to get the class of the team object of this type.
     */
    public abstract Class<T> getTeamClass();

    /**
     * Used to create the team object of this type.
     * Override this Method to return your extended version of {@link Team} with custom data.
     * This is used by {@code GROUP} types only.
     *
     * @param id    Id of the new team.
     * @param owner Id of the owner of the new team.
     */
    @SuppressWarnings("unchecked")
    public T createTeam(UUID id, UUID owner) {
        return (T) new Team(id, this, owner);
    }

    /**
     * Used to get how many teams of this type one entity may belong to.
     */
    public int getMaxTeamsPerMember() {
        return 1;
    }

    /**
     * Used to get the maximum number of members of a team of this type.
     */
    public int getMaxMembers() {
        return Integer.MAX_VALUE;
    }

    /**
     * Used to get the maximum length of the name of a team of this type.
     */
    public int getMaxNameLength() {
        return 32;
    }

    /**
     * Determine if the entity can rename the team.
     *
     * @param team   Affected {@link Team}
     * @param entity Affected {@link LivingEntity} trying to rename the team.
     * @return false will stop the entity from renaming the team.
     */
    public boolean canRename(T team, LivingEntity entity) {
        return team.isOwner(entity);
    }

    /**
     * Determine if the sanitized name is valid for a team of this type.
     */
    public boolean isValidName(String name) {
        return !name.isBlank() && name.length() <= this.getMaxNameLength();
    }

    /**
     * Determine if a name can only be used by one team of this type, ignoring case. A taken name results in {@link TeamResult#NAME_TAKEN}.
     */
    public boolean requiresUniqueNames() {
        return false;
    }

    /**
     * Determine if a team of this type can have no name. If not, a missing name results in {@link TeamResult#INVALID}.
     */
    public boolean allowsNoName() {
        return true;
    }

    /**
     * Used to get the {@link LimitPolicy} for an entity that already belongs to {@link #getMaxTeamsPerMember()} teams of this type.
     */
    public LimitPolicy onLimitReached() {
        return LimitPolicy.REJECT;
    }

    /**
     * Determine if joining a team of this type needs an invite.
     * This is advisory for UIs, {@link TeamAPI#addMember} and {@link TeamAPI#addRelation} always bypass invites.
     */
    public boolean requiresInvite() {
        return true;
    }

    /**
     * Used to get how many ticks an invite into a team of this type stays valid.
     */
    public int getInviteTimeoutTicks() {
        return 20 * 60;
    }

    /**
     * Determine if relations of this type are symmetric.
     * If true, adding a to b also adds b to a and checks look both ways.
     * This is used by {@code RELATION} types only.
     */
    public boolean isSymmetricRelation() {
        return true;
    }

    /**
     * Used to get how many entities one entity may relate to under this type.
     * This is used by {@code RELATION} types only.
     */
    public int getMaxRelations() {
        return 64;
    }

    /**
     * Determine if entities that this type relates as allies can not damage each other.
     */
    public boolean blocksFriendlyFire() {
        return false;
    }

    /**
     * Determine if mobs never target entities that this type relates as allies.
     */
    public boolean blocksTargeting() {
        return false;
    }

    /**
     * Used to get the priority of this type. Higher priority types decide the folded relation first.
     */
    public int getPriority() {
        return 0;
    }

    /**
     * Determine if an owned entity counts as its root owner under this type, both in relation checks and in membership ops.
     * Ownership comes from the registered {@link OwnerResolver}s and the vanilla {@link net.minecraft.world.entity.OwnableEntity} fallback.
     * This is true by default.
     */
    public boolean inheritsOwnership() {
        return true;
    }

    /**
     * Used to get the id whose membership counts for the entity.
     * It is the root owner when {@link #inheritsOwnership()}, otherwise the entity itself.
     * This resolves even when the owner is offline or unloaded.
     */
    public UUID resolveMemberId(LivingEntity entity) {
        return this.inheritsOwnership() ? OwnerResolvers.resolveId(entity) : entity.getUUID();
    }

    /**
     * Used to get the loaded entity behind {@link #resolveMemberId(LivingEntity)}, for membership ops that need an entity.
     * It falls back to the entity itself when the owner is not loaded.
     * The relation fold does not call this, it works on ids through {@link #inheritsOwnership()}.
     */
    public LivingEntity resolveMember(LivingEntity entity) {
        return this.inheritsOwnership() ? OwnerResolvers.resolveEntity(entity) : entity;
    }

    /**
     * Used to get the {@link Relation} of {@code a} towards {@code b} for this type only.
     * This delegates to {@link #getRelation(Level, UUID, Teams, UUID, Teams)}.
     */
    public Relation getRelation(LivingEntity a, Teams aTeams, LivingEntity b, Teams bTeams) {
        return this.getRelation(a.level(), a.getUUID(), aTeams, b.getUUID(), bTeams);
    }

    /**
     * Used to get the {@link Relation} of {@code a} towards {@code b} for this type only.
     * Override this form, the resolver calls it.
     * <p>
     * Both ids are already resolved through ownership and {@code aTeams}/{@code bTeams} are their team data, read from the loaded entity or, on the server, from the saved table when the id is not loaded.
     * <p>
     * The default of {@code RELATION} also consults the inbound set of each side as a fallback, so an offline or untracked outbound side is still caught.
     * The default of {@code GROUP} falls back to the team objects the querying side knows (server table, client cache), so a client can resolve its own party mates although their storage is not synced.
     * <p>
     * Types are skipped by the resolver when both sides have no team data at all ({@link Teams#isEmpty()}).
     * A type that derives relations from something else must hook {@link TeamEvents#RESOLVE_RELATION}.
     *
     * @param level   Affected {@link Level}
     * @param a       Id of the entity the relation is read from.
     * @param aTeams  {@link Teams} of {@code a}
     * @param b       Id of the entity the relation is read towards.
     * @param bTeams  {@link Teams} of {@code b}
     * @return {@link Relation#NEUTRAL} if this type has no opinion.
     */
    public Relation getRelation(Level level, UUID a, Teams aTeams, UUID b, Teams bTeams) {
        return switch (this.getShape()) {
            case GROUP -> {
                boolean client = level.isClientSide();
                Set<UUID> mine = aTeams.getTeamIds(this);
                Set<UUID> theirs = bTeams.getTeamIds(this);
                for (UUID id : mine) {
                    if (theirs.contains(id) || (client && TeamAPI.getTeam(level, id).map(team -> team.isMember(b)).orElse(false))) yield Relation.ALLY;
                }
                for (UUID id : theirs) {
                    if (client && TeamAPI.getTeam(level, id).map(team -> team.isMember(a)).orElse(false)) yield Relation.ALLY;
                }
                yield Relation.NEUTRAL;
            }
            case RELATION -> {
                if (aTeams.getRelated(this).contains(b)) yield Relation.ALLY;
                if (bTeams.getRelatedBy(this).contains(a)) yield Relation.ALLY;
                if (!this.isSymmetricRelation()) yield Relation.NEUTRAL;
                if (bTeams.getRelated(this).contains(a)) yield Relation.ALLY;
                if (aTeams.getRelatedBy(this).contains(b)) yield Relation.ALLY;
                yield Relation.NEUTRAL;
            }
        };
    }

    /**
     * Determine if the entity can join the team.
     *
     * @param team   Affected {@link Team}
     * @param joiner Affected {@link LivingEntity} trying to join the team.
     * @return false will stop the entity from joining the team.
     */
    public boolean canJoin(T team, LivingEntity joiner) {
        return true;
    }

    /**
     * Determine if the inviter can send invites at all, regardless of the invitee.
     *
     * @param team    Affected {@link Team}
     * @param inviter Affected {@link LivingEntity} trying to invite.
     * @return false will stop the inviter from sending invites.
     */
    public boolean canInvite(T team, LivingEntity inviter) {
        return team.isOwner(inviter);
    }

    /**
     * Determine if the inviter can invite this specific invitee.
     *
     * @param team    Affected {@link Team}
     * @param inviter Affected {@link LivingEntity} trying to invite.
     * @param invitee Affected {@link LivingEntity} that would be invited.
     * @return false will stop the inviter from inviting the invitee.
     */
    public boolean canInvite(T team, LivingEntity inviter, LivingEntity invitee) {
        return this.canInvite(team, inviter, invitee.getUUID());
    }

    /**
     * Determine if the inviter can invite the invitee, who can be offline.
     * By default only {@link #canInvite(Team, LivingEntity)} decides.
     *
     * @param team    Affected {@link Team}
     * @param inviter Affected {@link LivingEntity} trying to invite.
     * @param invitee Id of the invitee.
     * @return false will stop the inviter from inviting the invitee.
     */
    public boolean canInvite(T team, LivingEntity inviter, UUID invitee) {
        return this.canInvite(team, inviter);
    }

    /**
     * Determine if the actor can revoke the invite of the invitee.
     * By default the inviter of the invite can always revoke it, anyone else needs to be allowed to invite.
     *
     * @param team    Affected {@link Team}
     * @param actor   Affected {@link LivingEntity} trying to revoke the invite.
     * @param invitee Id of the invitee.
     * @return false will stop the actor from revoking the invite.
     */
    public boolean canRevokeInvite(T team, LivingEntity actor, UUID invitee) {
        for (TeamInvite invite : TeamAPI.getOutgoingInvites(actor, team)) {
            if (invite.invitee().equals(invitee) && actor.getUUID().equals(invite.inviter())) return true;
        }
        return this.canInvite(team, actor);
    }

    /**
     * Determine if the actor can kick the target member.
     *
     * @param team   Affected {@link Team}
     * @param actor  Affected {@link LivingEntity} trying to kick.
     * @param target Id of the member that would be kicked.
     * @return false will stop the actor from kicking the member.
     */
    public boolean canKick(T team, LivingEntity actor, UUID target) {
        return team.isOwner(actor) && !team.isOwner(target);
    }

    /**
     * Determine if the actor can promote the target member to owner.
     *
     * @param team   Affected {@link Team}
     * @param actor  Affected {@link LivingEntity} trying to promote.
     * @param target Id of the member that would become the owner.
     * @return false will stop the actor from promoting the member.
     */
    public boolean canPromote(T team, LivingEntity actor, UUID target) {
        return team.isOwner(actor) && team.isMember(target) && !team.isOwner(target);
    }

    /**
     * Determine if the actor can leave the team.
     *
     * @param team  Affected {@link Team}
     * @param actor Affected {@link LivingEntity} trying to leave.
     * @return false will stop the actor from leaving the team.
     */
    public boolean canLeave(T team, LivingEntity actor) {
        return true;
    }

    /**
     * Determine if the actor can disband the team.
     *
     * @param team  Affected {@link Team}
     * @param actor Affected {@link LivingEntity} trying to disband the team.
     * @return false will stop the actor from disbanding the team.
     */
    public boolean canDisband(T team, LivingEntity actor) {
        return team.isOwner(actor);
    }

    /**
     * Called when the owner leaves and members remain.
     *
     * @param team Affected {@link Team}
     * @return the id of the new owner, null will disband the team instead.
     */
    @Nullable
    public UUID pickNewOwner(T team) {
        for (UUID member : team.getMembers()) {
            if (!team.isOwner(member)) return member;
        }
        return null;
    }

    /**
     * Called after a team of this type was created.
     *
     * @param team Affected {@link Team}
     */
    public void onTeamCreated(T team) {
    }

    /**
     * Called after a team of this type was disbanded.
     *
     * @param team Affected {@link Team}
     */
    public void onTeamDisbanded(T team) {
    }

    /**
     * Called after a member joined a team of this type.
     *
     * @param team   Affected {@link Team}
     * @param member Id of the new member.
     */
    public void onMemberAdded(T team, UUID member) {
    }

    /**
     * Called after a member was removed from a team of this type.
     *
     * @param team   Affected {@link Team}
     * @param member Id of the removed member.
     * @param reason {@link LeaveReason} of the removal.
     */
    public void onMemberRemoved(T team, UUID member, LeaveReason reason) {
    }

    /**
     * Used to get the registry id of this type, null before registration.
     */
    @Nullable
    public ResourceLocation getId() {
        return TeamAPI.getTeamTypeRegistry().getId(this);
    }

    public String toString() {
        return "TeamType{" + this.getId() + '}';
    }
}
