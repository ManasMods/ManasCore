/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.api.template;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import io.github.manasmods.manascore.network.api.util.Changeable;
import io.github.manasmods.manascore.team.api.Team;
import io.github.manasmods.manascore.team.api.TeamInvite;
import io.github.manasmods.manascore.team.api.TeamType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Events of the team module.
 * <p>
 * Events whose listener returns an {@link EventResult} are fired before a mutation and can cancel it with {@link EventResult#interruptFalse()}.
 * Events starting with {@code CLIENT_} are fired on the client only, the events of team mutations are fired on the server.
 */
public interface TeamEvents {
    /**
     * Fired once the team module is initialized.
     */
    Event<PostInitEvent> POST_INIT = EventFactory.createLoop();

    /**
     * Fired before a team is created. A cancelled creation results in {@link TeamResult#CANCELLED}.
     */
    Event<TeamCreateEvent> TEAM_CREATE = EventFactory.createEventResult();
    /**
     * Fired after a team was created.
     */
    Event<TeamEvent> TEAM_CREATED = EventFactory.createLoop();
    /**
     * Fired before a team is disbanded on request.
     * Teams that dissolve because their last member left, or because no new owner could be picked, skip this event.
     */
    Event<TeamDisbandEvent> TEAM_DISBAND = EventFactory.createEventResult();
    /**
     * Fired after a team was disbanded and all of its members were removed.
     */
    Event<TeamEvent> TEAM_DISBANDED = EventFactory.createLoop();

    /**
     * Fired before an entity joins a team.
     */
    Event<MemberJoinEvent> MEMBER_JOIN = EventFactory.createEventResult();
    /**
     * Fired after an entity joined a team.
     */
    Event<MemberJoinedEvent> MEMBER_JOINED = EventFactory.createLoop();
    /**
     * Fired before an entity leaves a team.
     * Not fired for {@link LeaveReason#DEATH} or {@link LeaveReason#DISBAND}, nor for members that are not loaded.
     */
    Event<MemberLeaveEvent> MEMBER_LEAVE = EventFactory.createEventResult();
    /**
     * Fired after a member was removed from a team, for every {@link LeaveReason}.
     */
    Event<MemberLeftEvent> MEMBER_LEFT = EventFactory.createLoop();
    /**
     * Fired after the owner of a team changed, by promotion or because the owner left.
     */
    Event<OwnerChangedEvent> OWNER_CHANGED = EventFactory.createLoop();
    /**
     * Fired after a team was renamed.
     */
    Event<TeamRenamedEvent> TEAM_RENAMED = EventFactory.createLoop();

    /**
     * Fired on the client only, after a team was added or updated in the client cache.
     */
    Event<TeamEvent> CLIENT_TEAM_UPDATED = EventFactory.createLoop();
    /**
     * Fired on the client only, after a team was removed from the client cache.
     */
    Event<TeamRemovedEvent> CLIENT_TEAM_REMOVED = EventFactory.createLoop();
    /**
     * Fired on the client only, after the invites of the player were synced.
     */
    Event<InvitesUpdatedEvent> CLIENT_INVITES_UPDATED = EventFactory.createLoop();
    /**
     * Fired on the client only, when the server answered a team action of the player.
     */
    Event<ActionResultEvent> CLIENT_ACTION_RESULT = EventFactory.createLoop();
    /**
     * Fired on the client only, after the {@code RELATION} data of an entity changed.
     * It is deferred to the end of the client tick.
     */
    Event<RelationsUpdatedEvent> CLIENT_RELATIONS_UPDATED = EventFactory.createLoop();
    /**
     * Fired on the client only, after the list of invitable players of a team was received.
     */
    Event<InvitableUpdatedEvent> CLIENT_INVITABLE_UPDATED = EventFactory.createLoop();

    /**
     * Fired before an invite is created. A cancelled invite results in {@link TeamResult#CANCELLED}.
     */
    Event<InviteSendEvent> INVITE_SEND = EventFactory.createEventResult();
    /**
     * Fired after an invite was accepted.
     */
    Event<InviteEvent> INVITE_ACCEPTED = EventFactory.createLoop();
    /**
     * Fired after an invite was declined.
     */
    Event<InviteEvent> INVITE_DECLINED = EventFactory.createLoop();
    /**
     * Fired after an invite ran out.
     */
    Event<InviteEvent> INVITE_EXPIRED = EventFactory.createLoop();
    /**
     * Fired after an invite was revoked.
     */
    Event<InviteEvent> INVITE_REVOKED = EventFactory.createLoop();

    /**
     * Fired before a relation is added. A cancelled relation results in {@link TeamResult#CANCELLED}.
     */
    Event<RelationAddEvent> RELATION_ADD = EventFactory.createEventResult();
    /**
     * Fired after a relation was added.
     */
    Event<RelationEvent> RELATION_ADDED = EventFactory.createLoop();
    /**
     * Fired after a relation was removed.
     */
    Event<RelationEvent> RELATION_REMOVED = EventFactory.createLoop();

    /**
     * Fired after the team types were folded by priority.
     * Change the given {@link Changeable} to override the final answer.
     */
    Event<ResolveRelationEvent> RESOLVE_RELATION = EventFactory.createLoop();

    @FunctionalInterface
    interface PostInitEvent {
        /**
         * Called once the team module is initialized.
         */
        void run();
    }

    @FunctionalInterface
    interface TeamCreateEvent {
        /**
         * Called before a team is created.
         *
         * @param type  Affected {@link TeamType}
         * @param owner Affected {@link LivingEntity} that will own the team.
         * @return {@link EventResult#interruptFalse()} will cancel the creation of the team.
         */
        EventResult create(TeamType<?> type, LivingEntity owner);
    }

    @FunctionalInterface
    interface TeamEvent {
        /**
         * Called with the affected team.
         *
         * @param team Affected {@link Team}
         */
        void run(Team team);
    }

    @FunctionalInterface
    interface TeamRemovedEvent {
        /**
         * Called when a team was removed from the client cache.
         *
         * @param teamId Id of the removed {@link Team}.
         */
        void run(UUID teamId);
    }

    @FunctionalInterface
    interface InvitesUpdatedEvent {
        /**
         * Called when the invites of the player were synced.
         *
         * @param incoming {@link TeamInvite}s sent to the player.
         * @param outgoing {@link TeamInvite}s sent by the teams of the player.
         */
        void run(List<TeamInvite> incoming, List<TeamInvite> outgoing);
    }

    @FunctionalInterface
    interface ActionResultEvent {
        /**
         * Called when the server answered a team action of the player.
         *
         * @param action Requested {@link TeamAction}.
         * @param teamId Id of the affected {@link Team}, null if there is none.
         * @param result {@link TeamResult} of the request.
         */
        void run(TeamAction action, @Nullable UUID teamId, TeamResult result);
    }

    @FunctionalInterface
    interface RelationsUpdatedEvent {
        /**
         * Called when the relations of an entity under a type changed.
         *
         * @param owner Affected {@link LivingEntity} holding the relations.
         * @param type  Affected {@link TeamType}
         */
        void run(LivingEntity owner, TeamType<?> type);
    }

    @FunctionalInterface
    interface InvitableUpdatedEvent {
        /**
         * Called when the list of invitable players of a team was received.
         *
         * @param teamId  Id of the affected {@link Team}.
         * @param players Ids of the players that can be invited.
         */
        void run(UUID teamId, List<UUID> players);
    }

    @FunctionalInterface
    interface TeamDisbandEvent {
        /**
         * Called before a team is disbanded on request.
         *
         * @param team Affected {@link Team}
         * @return {@link EventResult#interruptFalse()} will cancel the disband.
         */
        EventResult run(Team team);
    }

    @FunctionalInterface
    interface MemberJoinEvent {
        /**
         * Called before an entity joins a team.
         *
         * @param team   Affected {@link Team}
         * @param entity Affected {@link LivingEntity} that joins the team.
         * @return {@link EventResult#interruptFalse()} will stop the entity from joining.
         */
        EventResult run(Team team, LivingEntity entity);
    }

    @FunctionalInterface
    interface MemberJoinedEvent {
        /**
         * Called after an entity joined a team.
         *
         * @param team   Affected {@link Team}
         * @param entity Affected {@link LivingEntity} that joined the team.
         */
        void run(Team team, LivingEntity entity);
    }

    @FunctionalInterface
    interface MemberLeaveEvent {
        /**
         * Called before an entity leaves a team.
         *
         * @param team   Affected {@link Team}
         * @param entity Affected {@link LivingEntity} that leaves the team.
         * @param reason {@link LeaveReason#LEAVE} or {@link LeaveReason#KICK}
         * @return {@link EventResult#interruptFalse()} will stop the entity from leaving.
         */
        EventResult run(Team team, LivingEntity entity, LeaveReason reason);
    }

    @FunctionalInterface
    interface MemberLeftEvent {
        /**
         * Called after a member was removed from a team.
         *
         * @param team   Affected {@link Team}
         * @param member Id of the removed member, who can be offline.
         * @param reason {@link LeaveReason} of the removal.
         */
        void run(Team team, UUID member, LeaveReason reason);
    }

    @FunctionalInterface
    interface OwnerChangedEvent {
        /**
         * Called after the owner of a team changed.
         *
         * @param team     Affected {@link Team}
         * @param oldOwner Id of the previous owner.
         * @param newOwner Id of the new owner.
         */
        void run(Team team, UUID oldOwner, UUID newOwner);
    }

    @FunctionalInterface
    interface TeamRenamedEvent {
        /**
         * Called after a team was renamed.
         *
         * @param team    Affected {@link Team}
         * @param oldName Previous name, null if the team had none.
         * @param newName New name, null if the name was removed.
         */
        void run(Team team, @Nullable String oldName, @Nullable String newName);
    }

    @FunctionalInterface
    interface InviteSendEvent {
        /**
         * Called before an invite is created.
         *
         * @param invite Affected {@link TeamInvite}
         * @return {@link EventResult#interruptFalse()} will cancel the invite.
         */
        EventResult run(TeamInvite invite);
    }

    @FunctionalInterface
    interface InviteEvent {
        /**
         * Called with the affected invite.
         *
         * @param invite Affected {@link TeamInvite}
         */
        void run(TeamInvite invite);
    }

    @FunctionalInterface
    interface RelationAddEvent {
        /**
         * Called before a relation is added.
         *
         * @param type Affected {@link TeamType}
         * @param a    Id of the entity that gets the relation.
         * @param b    Id of the entity it is related to.
         * @return {@link EventResult#interruptFalse()} will cancel the relation.
         */
        EventResult run(TeamType<?> type, UUID a, UUID b);
    }

    @FunctionalInterface
    interface RelationEvent {
        /**
         * Called with the affected relation.
         *
         * @param type Affected {@link TeamType}
         * @param a    Id of the entity that has the relation.
         * @param b    Id of the entity it is related to.
         */
        void run(TeamType<?> type, UUID a, UUID b);
    }

    @FunctionalInterface
    interface ResolveRelationEvent {
        /**
         * Called after the team types were folded by priority.
         *
         * @param a        Affected {@link LivingEntity} the relation is read from.
         * @param b        Affected {@link LivingEntity} the relation is read towards.
         * @param relation Folded {@link Relation}, change it to override the final answer.
         */
        void resolve(LivingEntity a, LivingEntity b, Changeable<Relation> relation);
    }
}
