/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.impl.network;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import io.github.manasmods.manascore.team.ManasCoreTeam;
import io.github.manasmods.manascore.team.api.*;
import io.github.manasmods.manascore.team.api.template.*;
import io.github.manasmods.manascore.team.impl.TeamManager;
import io.github.manasmods.manascore.team.impl.network.c2s.TeamActionPayload;
import io.github.manasmods.manascore.team.impl.network.s2c.SyncInvitablePayload;
import io.github.manasmods.manascore.team.impl.network.s2c.TeamActionResultPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Server-side handler for {@link TeamActionPayload}, rate limited per sender and action.
 */
public final class TeamActionHandler {
    private static final Map<UUID, EnumMap<TeamAction, Long>> LAST_ACTION_TICK = new ConcurrentHashMap<>();

    private TeamActionHandler() {
    }

    public static void init() {
        PlayerEvent.PLAYER_QUIT.register(player -> LAST_ACTION_TICK.remove(player.getUUID()));
    }

    public static void handle(@Nullable Player player, TeamActionPayload payload) {
        if (!(player instanceof ServerPlayer sender)) return;
        MinecraftServer server = sender.getServer();
        if (server == null) return;
        if (isOnCooldown(sender, payload.action(), server)) return;
        ActionOutcome outcome = resolve(sender, server, payload);
        NetworkManager.sendToPlayer(sender, new TeamActionResultPayload(payload.action(), outcome.teamId(), outcome.result()));
    }

    private static boolean isOnCooldown(ServerPlayer sender, TeamAction action, MinecraftServer server) {
        long now = server.getTickCount();
        EnumMap<TeamAction, Long> perAction = LAST_ACTION_TICK.computeIfAbsent(sender.getUUID(), id -> new EnumMap<>(TeamAction.class));
        Long last = perAction.get(action);
        int cooldownTicks = action == TeamAction.REQUEST_INVITABLE ? TeamConfig.get().queryCooldownTicks : TeamConfig.get().actionCooldownTicks;
        if (last != null && now - last < cooldownTicks) {
            ManasCoreTeam.LOG.debug("Dropped team action {} from {} - on cooldown.", action, sender.getGameProfile().getName());
            return true;
        }
        perAction.put(action, now);
        return false;
    }

    private static ActionOutcome resolve(ServerPlayer sender, MinecraftServer server, TeamActionPayload payload) {
        return switch (payload.action()) {
            case CREATE -> create(sender, server, payload);
            case RENAME -> withTeam(server, sender, payload, team -> TeamAPI.trySetTeamName(team, sender, payload.name()));
            case INVITE -> invite(server, sender, payload);
            case KICK -> withTeam(server, sender, payload, team -> payload.target() == null ? TeamResult.NOT_FOUND : TeamAPI.tryKick(team, sender, payload.target()));
            case PROMOTE -> withTeam(server, sender, payload, team -> payload.target() == null ? TeamResult.NOT_FOUND : TeamAPI.tryPromote(team, sender, payload.target()));
            case LEAVE -> withTeam(server, sender, payload, team -> TeamAPI.tryLeave(team, sender));
            case DISBAND -> withTeam(server, sender, payload, team -> TeamAPI.tryDisband(team, sender));
            case ACCEPT_INVITE -> plain(payload.teamId() == null ? TeamResult.NOT_FOUND : TeamAPI.tryAcceptInvite(sender, payload.teamId()), payload.teamId());
            case DECLINE_INVITE -> plain(payload.teamId() == null ? TeamResult.NOT_FOUND : TeamAPI.tryDeclineInvite(sender, payload.teamId()), payload.teamId());
            case ADD_RELATION -> relation(server, sender, payload, true);
            case REMOVE_RELATION -> relation(server, sender, payload, false);
            case REQUEST_INVITABLE -> requestInvitable(server, sender, payload);
        };
    }

    private static ActionOutcome create(ServerPlayer sender, MinecraftServer server, TeamActionPayload payload) {
        if (payload.typeId() == null) return plain(TeamResult.NOT_FOUND, null);
        TeamType<?> type = TeamAPI.getTeamTypeRegistry().get(payload.typeId());
        if (type == null) return plain(TeamResult.NOT_FOUND, null);
        LivingEntity resolved = type.resolveMember(sender);
        Set<UUID> before = new HashSet<>(TeamAPI.getTeamsFrom(resolved).getTeamIds(type));
        TeamResult result = TeamAPI.tryCreateTeam(type, sender);
        if (!result.isAccepted()) return plain(result, null);
        UUID createdId = null;
        for (UUID id : TeamAPI.getTeamsFrom(resolved).getTeamIds(type)) {
            if (!before.contains(id)) createdId = id;
        }
        if (createdId != null && payload.name() != null) {
            TeamAPI.getTeam(server, createdId).ifPresent(team -> TeamAPI.trySetTeamName(team, sender, payload.name()));
        }
        return plain(result, createdId);
    }

    private static ActionOutcome invite(MinecraftServer server, ServerPlayer sender, TeamActionPayload payload) {
        return withTeam(server, sender, payload, team -> {
            if (payload.target() == null) return TeamResult.NOT_FOUND;
            ServerPlayer target = server.getPlayerList().getPlayer(payload.target());
            if (target == null) return TeamResult.NOT_FOUND;
            return TeamAPI.tryInvite(team, sender, target);
        });
    }

    private static ActionOutcome requestInvitable(MinecraftServer server, ServerPlayer sender, TeamActionPayload payload) {
        return withTeam(server, sender, payload, team -> {
            List<ServerPlayer> players = TeamAPI.getInvitable(team, sender);
            List<UUID> ids = players.stream().map(ServerPlayer::getUUID).toList();
            NetworkManager.sendToPlayer(sender, new SyncInvitablePayload(team.getId(), ids, TeamManager.namesFor(server, ids)));
            return TeamResult.ACCEPTED;
        });
    }

    private static ActionOutcome relation(MinecraftServer server, ServerPlayer sender, TeamActionPayload payload, boolean add) {
        if (payload.typeId() == null) return plain(TeamResult.NOT_FOUND, payload.teamId());
        TeamType<?> type = TeamAPI.getTeamTypeRegistry().get(payload.typeId());
        if (type == null) return plain(TeamResult.NOT_FOUND, payload.teamId());
        if (payload.target() == null) return plain(TeamResult.NOT_FOUND, payload.teamId());
        UUID subject = type.resolveMember(sender).getUUID();
        TeamResult result = add
                ? TeamAPI.tryAddRelation(server, type, subject, payload.target())
                : TeamAPI.tryRemoveRelation(server, type, subject, payload.target());
        return plain(result, payload.teamId());
    }

    private static ActionOutcome withTeam(MinecraftServer server, ServerPlayer sender, TeamActionPayload payload, Function<Team, TeamResult> action) {
        UUID teamId = payload.teamId();
        if (teamId == null) return plain(TeamResult.NOT_FOUND, null);
        Optional<Team> team = TeamAPI.getTeam(server, teamId);
        if (team.isEmpty()) return plain(TeamResult.NOT_FOUND, teamId);
        LivingEntity resolved = team.get().getType().resolveMember(sender);
        if (!team.get().isMember(resolved)) return plain(TeamResult.NOT_MEMBER, teamId);
        return plain(action.apply(team.get()), teamId);
    }

    private static ActionOutcome plain(TeamResult result, @Nullable UUID teamId) {
        return new ActionOutcome(result, teamId);
    }

    private record ActionOutcome(TeamResult result, @Nullable UUID teamId) {
    }
}
