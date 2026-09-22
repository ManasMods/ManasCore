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

public interface TeamEvents {
    Event<PostInitEvent> POST_INIT = EventFactory.createLoop();

    Event<TeamCreateEvent> TEAM_CREATE = EventFactory.createEventResult();
    Event<TeamEvent> TEAM_CREATED = EventFactory.createLoop();
    Event<TeamDisbandEvent> TEAM_DISBAND = EventFactory.createEventResult();
    Event<TeamEvent> TEAM_DISBANDED = EventFactory.createLoop();

    Event<MemberJoinEvent> MEMBER_JOIN = EventFactory.createEventResult();
    Event<MemberJoinedEvent> MEMBER_JOINED = EventFactory.createLoop();
    /** Not fired for {@link LeaveReason#DEATH} or {@link LeaveReason#DISBAND}. */
    Event<MemberLeaveEvent> MEMBER_LEAVE = EventFactory.createEventResult();
    Event<MemberLeftEvent> MEMBER_LEFT = EventFactory.createLoop();
    Event<OwnerChangedEvent> OWNER_CHANGED = EventFactory.createLoop();
    Event<TeamRenamedEvent> TEAM_RENAMED = EventFactory.createLoop();

    /** Fired on the client only. **/
    Event<TeamEvent> CLIENT_TEAM_UPDATED = EventFactory.createLoop();
    Event<TeamRemovedEvent> CLIENT_TEAM_REMOVED = EventFactory.createLoop();
    Event<InvitesUpdatedEvent> CLIENT_INVITES_UPDATED = EventFactory.createLoop();

    Event<InviteSendEvent> INVITE_SEND = EventFactory.createEventResult();
    Event<InviteEvent> INVITE_ACCEPTED = EventFactory.createLoop();
    Event<InviteEvent> INVITE_DECLINED = EventFactory.createLoop();
    Event<InviteEvent> INVITE_EXPIRED = EventFactory.createLoop();

    Event<RelationAddEvent> RELATION_ADD = EventFactory.createEventResult();
    Event<RelationEvent> RELATION_ADDED = EventFactory.createLoop();
    Event<RelationEvent> RELATION_REMOVED = EventFactory.createLoop();

    /** Fired after the priority fold. Set the changeable to override the final answer. */
    Event<ResolveRelationEvent> RESOLVE_RELATION = EventFactory.createLoop();

    @FunctionalInterface
    interface PostInitEvent {
        void run();
    }

    @FunctionalInterface
    interface TeamCreateEvent {
        EventResult create(TeamType<?> type, LivingEntity owner);
    }

    @FunctionalInterface
    interface TeamEvent {
        void run(Team team);
    }

    @FunctionalInterface
    interface TeamRemovedEvent {
        void run(UUID teamId);
    }

    @FunctionalInterface
    interface InvitesUpdatedEvent {
        void run(List<TeamInvite> invites);
    }

    @FunctionalInterface
    interface TeamDisbandEvent {
        EventResult run(Team team);
    }

    @FunctionalInterface
    interface MemberJoinEvent {
        EventResult run(Team team, LivingEntity entity);
    }

    @FunctionalInterface
    interface MemberJoinedEvent {
        void run(Team team, LivingEntity entity);
    }

    @FunctionalInterface
    interface MemberLeaveEvent {
        EventResult run(Team team, LivingEntity entity, LeaveReason reason);
    }

    @FunctionalInterface
    interface MemberLeftEvent {
        void run(Team team, UUID member, LeaveReason reason);
    }

    @FunctionalInterface
    interface OwnerChangedEvent {
        void run(Team team, UUID oldOwner, UUID newOwner);
    }

    @FunctionalInterface
    interface TeamRenamedEvent {
        void run(Team team, @Nullable String oldName, @Nullable String newName);
    }

    @FunctionalInterface
    interface InviteSendEvent {
        EventResult run(TeamInvite invite);
    }

    @FunctionalInterface
    interface InviteEvent {
        void run(TeamInvite invite);
    }

    @FunctionalInterface
    interface RelationAddEvent {
        EventResult run(TeamType<?> type, UUID a, UUID b);
    }

    @FunctionalInterface
    interface RelationEvent {
        void run(TeamType<?> type, UUID a, UUID b);
    }

    @FunctionalInterface
    interface ResolveRelationEvent {
        void resolve(LivingEntity a, LivingEntity b, Changeable<Relation> relation);
    }
}
