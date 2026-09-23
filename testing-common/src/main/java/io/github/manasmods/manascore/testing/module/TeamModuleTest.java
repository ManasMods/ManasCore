/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.testing.module;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.command.api.Command;
import io.github.manasmods.manascore.command.api.CommandRegistry;
import io.github.manasmods.manascore.command.api.Execute;
import io.github.manasmods.manascore.command.api.parameter.EntityArg;
import io.github.manasmods.manascore.command.api.parameter.SenderArg;
import io.github.manasmods.manascore.command.api.parameter.ResourceLocationArg;
import io.github.manasmods.manascore.command.api.parameter.UuidArg;
import io.github.manasmods.manascore.command.api.parameter.primitive.LiteralArg;
import io.github.manasmods.manascore.command.api.parameter.primitive.TextArg;
import io.github.manasmods.manascore.team.api.ResolvedRelation;
import io.github.manasmods.manascore.team.api.Team;
import io.github.manasmods.manascore.team.api.TeamAPI;
import io.github.manasmods.manascore.team.api.TeamInvite;
import io.github.manasmods.manascore.team.api.TeamType;
import io.github.manasmods.manascore.team.api.template.LeaveReason;
import io.github.manasmods.manascore.team.api.template.Relation;
import io.github.manasmods.manascore.team.api.template.TeamEvents;
import io.github.manasmods.manascore.team.api.template.TeamResult;
import io.github.manasmods.manascore.team.api.template.TeamShape;
import io.github.manasmods.manascore.team.api.template.Teams;
import io.github.manasmods.manascore.testing.ModuleConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.github.manasmods.manascore.testing.ManasCoreTesting.LOG;

public class TeamModuleTest {
    private static final DeferredRegister<TeamType<?>> TEAM_TYPES = DeferredRegister.create(ModuleConstants.MOD_ID, TeamAPI.getTeamTypeRegistryKey());
    public static final RegistrySupplier<FactionTeamType> FACTION = TEAM_TYPES.register("test_faction", FactionTeamType::new);

    public static void init() {
        TEAM_TYPES.register();
        CommandRegistry.registerCommand(TeamCommand.class);

        TeamEvents.TEAM_CREATED.register(team -> LOG.info("[team] created {}", team));
        TeamEvents.TEAM_DISBANDED.register(team -> LOG.info("[team] disbanded {}", team.getId()));
        TeamEvents.MEMBER_JOINED.register((team, entity) -> LOG.info("[team] {} joined {}", entity.getName().getString(), team.getId()));
        TeamEvents.MEMBER_LEFT.register((team, member, reason) -> LOG.info("[team] {} left {} ({})", member, team.getId(), reason));
        TeamEvents.OWNER_CHANGED.register((team, oldOwner, newOwner) -> LOG.info("[team] owner of {}: {} -> {}", team.getId(), oldOwner, newOwner));
        TeamEvents.INVITE_ACCEPTED.register(invite -> LOG.info("[team] invite accepted {}", invite));
        TeamEvents.INVITE_DECLINED.register(invite -> LOG.info("[team] invite declined {}", invite));
        TeamEvents.INVITE_EXPIRED.register(invite -> LOG.info("[team] invite expired {}", invite));
        TeamEvents.RELATION_ADDED.register((type, a, b) -> LOG.info("[team] relation {} added {} <-> {}", type.getId(), a, b));
        TeamEvents.RELATION_REMOVED.register((type, a, b) -> LOG.info("[team] relation {} removed {} <-> {}", type.getId(), a, b));

        if (Platform.getEnvironment() == Env.CLIENT) TeamModuleTestClient.init();
    }

    /** Two members of different factions are enemies; same faction allies; up to two factions each. */
    public static class FactionTeamType extends TeamType<Team> {
        public TeamShape getShape() {
            return TeamShape.GROUP;
        }
        public Class<Team> getTeamClass() {
            return Team.class;
        }
        public int getMaxTeamsPerMember() {
            return 2;
        }
        public int getPriority() {
            return 50;
        }
        public boolean blocksFriendlyFire() {
            return true;
        }
        public Relation getRelation(LivingEntity a, Teams aTeams, LivingEntity b, Teams bTeams) {
            Set<UUID> mine = aTeams.getTeamIds(this);
            Set<UUID> theirs = bTeams.getTeamIds(this);
            if (mine.isEmpty() || theirs.isEmpty()) return Relation.NEUTRAL;
            for (UUID id : mine) if (theirs.contains(id)) return Relation.ALLY;
            return Relation.ENEMY;
        }
    }

    @Command(value = "manasteam")
    public static class TeamCommand {
        private static TeamType<?> type(ResourceLocation id) {
            return TeamAPI.getTeamTypeRegistry().get(id);
        }

        private static void reply(CommandSourceStack sender, String text) {
            sender.sendSystemMessage(Component.literal(text));
        }

        @Execute
        public boolean create(@SenderArg CommandSourceStack sender, @LiteralArg("create") String l, @ResourceLocationArg ResourceLocation typeId) {
            ServerPlayer player = sender.getPlayer();
            TeamType<?> type = type(typeId);
            if (player == null || type == null) return false;
            Optional<? extends Team> team = TeamAPI.createTeam(type, player);
            reply(sender, team.map(t -> "Created " + t.getId()).orElse("Create failed"));
            return true;
        }

        @Execute
        public boolean invite(@SenderArg CommandSourceStack sender, @LiteralArg("invite") String l, @ResourceLocationArg ResourceLocation typeId,
                              @EntityArg(name = "target", value = EntityArg.Type.PLAYER) EntitySelector selector) throws CommandSyntaxException {
            ServerPlayer player = sender.getPlayer();
            TeamType<?> type = type(typeId);
            if (player == null || type == null) return false;
            ServerPlayer target = selector.findSinglePlayer(sender);
            if (type.getShape() != TeamShape.GROUP) {
                reply(sender, "Not a group type");
                return false;
            }

            Optional<? extends Team> team = TeamAPI.getTeam(player, type);
            if (team.isEmpty()) {
                reply(sender, "You are not in a team of that type");
                return false;
            }
            Optional<TeamInvite> invite = TeamAPI.invite(team.get(), player, target);
            reply(sender, invite.map(i -> "Invited " + target.getName().getString() + " (expires tick " + i.expiresAtTick() + ")").orElse("Invite failed"));
            return true;
        }

        @Execute
        public boolean accept(@SenderArg CommandSourceStack sender, @LiteralArg("accept") String l) {
            ServerPlayer player = sender.getPlayer();
            if (player == null) return false;
            for (TeamInvite invite : TeamAPI.getPendingInvites(player)) {
                boolean ok = TeamAPI.acceptInvite(player, invite.teamId());
                reply(sender, (ok ? "Accepted " : "Failed to accept ") + invite.typeId());
                return ok;
            }
            reply(sender, "No pending invites");
            return false;
        }

        @Execute
        public boolean decline(@SenderArg CommandSourceStack sender, @LiteralArg("decline") String l) {
            ServerPlayer player = sender.getPlayer();
            if (player == null) return false;
            for (TeamInvite invite : TeamAPI.getPendingInvites(player)) {
                TeamAPI.declineInvite(player, invite.teamId());
                reply(sender, "Declined " + invite.typeId());
                return true;
            }
            reply(sender, "No pending invites");
            return false;
        }

        @Execute
        public boolean leave(@SenderArg CommandSourceStack sender, @LiteralArg("leave") String l, @ResourceLocationArg ResourceLocation typeId) {
            ServerPlayer player = sender.getPlayer();
            TeamType<?> type = type(typeId);
            if (player == null || type == null) return false;
            Optional<? extends Team> team = TeamAPI.getTeam(player, type);
            boolean ok = team.isPresent() && TeamAPI.removeMember(team.get(), player, LeaveReason.LEAVE);
            reply(sender, ok ? "Left" : "Leave failed");
            return ok;
        }

        @Execute
        public boolean kick(@SenderArg CommandSourceStack sender, @LiteralArg("kick") String l, @ResourceLocationArg ResourceLocation typeId,
                            @EntityArg(name = "target", value = EntityArg.Type.PLAYER) EntitySelector selector) throws CommandSyntaxException {
            ServerPlayer player = sender.getPlayer();
            TeamType<?> type = type(typeId);
            if (player == null || type == null) return false;
            ServerPlayer target = selector.findSinglePlayer(sender);
            Optional<? extends Team> team = TeamAPI.getTeam(player, type);
            boolean ok = team.isPresent() && team.get().isOwner(player) && TeamAPI.removeMember(team.get(), target, LeaveReason.KICK);
            reply(sender, ok ? "Kicked" : "Kick failed");
            return ok;
        }

        @Execute
        public boolean disband(@SenderArg CommandSourceStack sender, @LiteralArg("disband") String l, @ResourceLocationArg ResourceLocation typeId) {
            ServerPlayer player = sender.getPlayer();
            TeamType<?> type = type(typeId);
            if (player == null || type == null) return false;
            Optional<? extends Team> team = TeamAPI.getTeam(player, type);
            boolean ok = team.isPresent() && team.get().isOwner(player) && TeamAPI.disbandTeam(player.server, team.get());
            reply(sender, ok ? "Disbanded" : "Disband failed");
            return ok;
        }

        @Execute
        public boolean list(@SenderArg CommandSourceStack sender, @LiteralArg("list") String l) {
            ServerPlayer player = sender.getPlayer();
            if (player == null) return false;
            Teams teams = TeamAPI.getTeamsFrom(player);
            reply(sender, "Groups: " + teams.getAllTeamIds());
            reply(sender, "Relations: " + teams.getAllRelated());
            reply(sender, "Invites: " + TeamAPI.getPendingInvites(player));
            for (Team team : TeamAPI.getAllTeams(player.server)) {
                if (team.isMember(player)) reply(sender, team + " (" + team.getDisplayName().getString() + ")");
            }
            return true;
        }

        @Execute
        public boolean name(@SenderArg CommandSourceStack sender, @LiteralArg("name") String l, @ResourceLocationArg ResourceLocation typeId,
                            @TextArg(value = TextArg.Type.GREEDY_STRING, name = "name") String name) {
            ServerPlayer player = sender.getPlayer();
            TeamType<?> type = type(typeId);
            if (player == null || type == null) return false;
            Optional<? extends Team> team = TeamAPI.getTeam(player, type);
            String newName = "clear".equalsIgnoreCase(name) ? null : name;
            boolean ok = team.isPresent() && TeamAPI.setTeamName(team.get(), player, newName);
            reply(sender, ok ? "Renamed" : "Rename failed");
            return ok;
        }

        @Execute
        public boolean invitable(@SenderArg CommandSourceStack sender, @LiteralArg("invitable") String l, @ResourceLocationArg ResourceLocation typeId) {
            ServerPlayer player = sender.getPlayer();
            TeamType<?> type = type(typeId);
            if (player == null || type == null) return false;
            Optional<? extends Team> team = TeamAPI.getTeam(player, type);
            if (team.isEmpty()) {
                reply(sender, "You are not in a team of that type");
                return false;
            }
            List<ServerPlayer> invitable = TeamAPI.getInvitable(team.get(), player);
            reply(sender, "Invitable: " + invitable.stream().map(p -> p.getName().getString()).toList());
            return true;
        }

        @Execute
        public boolean outgoing(@SenderArg CommandSourceStack sender, @LiteralArg("outgoing") String l, @ResourceLocationArg ResourceLocation typeId) {
            ServerPlayer player = sender.getPlayer();
            TeamType<?> type = type(typeId);
            if (player == null || type == null) return false;
            Optional<? extends Team> team = TeamAPI.getTeam(player, type);
            if (team.isEmpty()) {
                reply(sender, "You are not in a team of that type");
                return false;
            }
            List<TeamInvite> outgoing = TeamAPI.getOutgoingInvites(player, team.get());
            reply(sender, "Outgoing: " + outgoing.stream().map(i -> TeamAPI.getMemberName(player.level(), i.invitee()).getString()).toList());
            return true;
        }

        @Execute
        public boolean inbound(@SenderArg CommandSourceStack sender, @LiteralArg("inbound") String l, @ResourceLocationArg ResourceLocation typeId) {
            ServerPlayer player = sender.getPlayer();
            TeamType<?> type = type(typeId);
            if (player == null || type == null) return false;
            reply(sender, "Inbound: " + TeamAPI.getRelatedBy(player, type));
            return true;
        }

        @Execute
        public boolean tryInvite(@SenderArg CommandSourceStack sender, @LiteralArg("tryinvite") String l, @ResourceLocationArg ResourceLocation typeId,
                                 @EntityArg(name = "target", value = EntityArg.Type.PLAYER) EntitySelector selector) throws CommandSyntaxException {
            ServerPlayer player = sender.getPlayer();
            TeamType<?> type = type(typeId);
            if (player == null || type == null) return false;
            ServerPlayer target = selector.findSinglePlayer(sender);
            Optional<? extends Team> team = TeamAPI.getTeam(player, type);
            if (team.isEmpty()) {
                reply(sender, "You are not in a team of that type");
                return false;
            }
            TeamResult result = TeamAPI.tryInvite(team.get(), player, target);
            reply(sender, result.name());
            return result.isAccepted();
        }

        @Execute
        public boolean tryKick(@SenderArg CommandSourceStack sender, @LiteralArg("trykick") String l, @ResourceLocationArg ResourceLocation typeId,
                               @EntityArg(name = "target", value = EntityArg.Type.PLAYER) EntitySelector selector) throws CommandSyntaxException {
            ServerPlayer player = sender.getPlayer();
            TeamType<?> type = type(typeId);
            if (player == null || type == null) return false;
            ServerPlayer target = selector.findSinglePlayer(sender);
            Optional<? extends Team> team = TeamAPI.getTeam(player, type);
            if (team.isEmpty()) {
                reply(sender, "You are not in a team of that type");
                return false;
            }
            TeamResult result = TeamAPI.tryKick(team.get(), player, target.getUUID());
            reply(sender, result.name());
            return result.isAccepted();
        }

        @Execute
        public boolean tryPromote(@SenderArg CommandSourceStack sender, @LiteralArg("trypromote") String l, @ResourceLocationArg ResourceLocation typeId,
                                  @EntityArg(name = "target", value = EntityArg.Type.PLAYER) EntitySelector selector) throws CommandSyntaxException {
            ServerPlayer player = sender.getPlayer();
            TeamType<?> type = type(typeId);
            if (player == null || type == null) return false;
            ServerPlayer target = selector.findSinglePlayer(sender);
            Optional<? extends Team> team = TeamAPI.getTeam(player, type);
            if (team.isEmpty()) {
                reply(sender, "You are not in a team of that type");
                return false;
            }
            TeamResult result = TeamAPI.tryPromote(team.get(), player, target.getUUID());
            reply(sender, result.name());
            return result.isAccepted();
        }

        @Execute
        public boolean tryLeave(@SenderArg CommandSourceStack sender, @LiteralArg("tryleave") String l, @ResourceLocationArg ResourceLocation typeId) {
            ServerPlayer player = sender.getPlayer();
            TeamType<?> type = type(typeId);
            if (player == null || type == null) return false;
            Optional<? extends Team> team = TeamAPI.getTeam(player, type);
            if (team.isEmpty()) {
                reply(sender, "You are not in a team of that type");
                return false;
            }
            TeamResult result = TeamAPI.tryLeave(team.get(), player);
            reply(sender, result.name());
            return result.isAccepted();
        }

        @Execute
        public boolean tryDisband(@SenderArg CommandSourceStack sender, @LiteralArg("trydisband") String l, @ResourceLocationArg ResourceLocation typeId) {
            ServerPlayer player = sender.getPlayer();
            TeamType<?> type = type(typeId);
            if (player == null || type == null) return false;
            Optional<? extends Team> team = TeamAPI.getTeam(player, type);
            if (team.isEmpty()) {
                reply(sender, "You are not in a team of that type");
                return false;
            }
            TeamResult result = TeamAPI.tryDisband(team.get(), player);
            reply(sender, result.name());
            return result.isAccepted();
        }

        private static boolean can(String action, Team team, ServerPlayer player, @Nullable UUID target) {
            return switch (action.toLowerCase(Locale.ROOT)) {
                case "invite" -> TeamAPI.canInvite(team, player);
                case "kick" -> target != null && TeamAPI.canKick(team, player, target);
                case "promote" -> target != null && TeamAPI.canPromote(team, player, target);
                case "leave" -> TeamAPI.canLeave(team, player);
                case "disband" -> TeamAPI.canDisband(team, player);
                default -> false;
            };
        }

        @Execute
        public boolean can(@SenderArg CommandSourceStack sender, @LiteralArg("can") String l, @ResourceLocationArg ResourceLocation typeId,
                           @TextArg(value = TextArg.Type.WORD, name = "action") String action) {
            ServerPlayer player = sender.getPlayer();
            TeamType<?> type = type(typeId);
            if (player == null || type == null) return false;
            Optional<? extends Team> team = TeamAPI.getTeam(player, type);
            if (team.isEmpty()) {
                reply(sender, "You are not in a team of that type");
                return false;
            }
            boolean result = can(action, team.get(), player, null);
            reply(sender, String.valueOf(result));
            return result;
        }

        @Execute
        public boolean can(@SenderArg CommandSourceStack sender, @LiteralArg("can") String l, @ResourceLocationArg ResourceLocation typeId,
                           @TextArg(value = TextArg.Type.WORD, name = "action") String action,
                           @EntityArg(name = "target", value = EntityArg.Type.PLAYER) EntitySelector selector) throws CommandSyntaxException {
            ServerPlayer player = sender.getPlayer();
            TeamType<?> type = type(typeId);
            if (player == null || type == null) return false;
            ServerPlayer target = selector.findSinglePlayer(sender);
            Optional<? extends Team> team = TeamAPI.getTeam(player, type);
            if (team.isEmpty()) {
                reply(sender, "You are not in a team of that type");
                return false;
            }
            boolean result = can(action, team.get(), player, target.getUUID());
            reply(sender, String.valueOf(result));
            return result;
        }

        @Execute
        public boolean allyid(@SenderArg CommandSourceStack sender, @LiteralArg("allyid") String l, @UuidArg UUID uuid) {
            ServerPlayer player = sender.getPlayer();
            if (player == null) return false;
            boolean ok = TeamAPI.addRelation(player.server, TeamAPI.ALLY.get(), player.getUUID(), uuid);
            reply(sender, ok ? "Allied" : "Ally failed");
            return ok;
        }

        @Execute
        public boolean unallyid(@SenderArg CommandSourceStack sender, @LiteralArg("unallyid") String l, @UuidArg UUID uuid) {
            ServerPlayer player = sender.getPlayer();
            if (player == null) return false;
            boolean ok = TeamAPI.removeRelation(player.server, TeamAPI.ALLY.get(), player.getUUID(), uuid);
            reply(sender, ok ? "Un-allied" : "Un-ally failed");
            return ok;
        }

        @Execute
        public boolean relation(@SenderArg CommandSourceStack sender, @LiteralArg("relation") String l,
                                @EntityArg(name = "a") EntitySelector aSel, @EntityArg(name = "b") EntitySelector bSel) throws CommandSyntaxException {
            Entity a = aSel.findSingleEntity(sender);
            Entity b = bSel.findSingleEntity(sender);
            if (!(a instanceof LivingEntity la) || !(b instanceof LivingEntity lb)) return false;
            ResolvedRelation resolved = TeamAPI.resolveRelation(la, lb);
            reply(sender, a.getName().getString() + " -> " + b.getName().getString() + ": " + resolved.relation()
                    + " (by " + (resolved.decidedBy() == null ? "none" : resolved.decidedBy().getId()) + ")");
            return true;
        }

        @Execute
        public boolean ally(@SenderArg CommandSourceStack sender, @LiteralArg("ally") String l,
                            @EntityArg(name = "a") EntitySelector aSel, @EntityArg(name = "b") EntitySelector bSel) throws CommandSyntaxException {
            Entity a = aSel.findSingleEntity(sender);
            Entity b = bSel.findSingleEntity(sender);
            if (!(a instanceof LivingEntity la) || !(b instanceof LivingEntity lb)) return false;
            boolean ok = TeamAPI.addRelation(TeamAPI.ALLY.get(), la, lb);
            reply(sender, ok ? "Allied" : "Ally failed");
            return ok;
        }

        @Execute
        public boolean unally(@SenderArg CommandSourceStack sender, @LiteralArg("unally") String l,
                              @EntityArg(name = "a") EntitySelector aSel, @EntityArg(name = "b") EntitySelector bSel) throws CommandSyntaxException {
            Entity a = aSel.findSingleEntity(sender);
            Entity b = bSel.findSingleEntity(sender);
            if (!(a instanceof LivingEntity la) || !(b instanceof LivingEntity lb)) return false;
            boolean ok = TeamAPI.removeRelation(TeamAPI.ALLY.get(), la, lb);
            reply(sender, ok ? "Un-allied" : "Un-ally failed");
            return ok;
        }

        @Execute
        public boolean mutual(@SenderArg CommandSourceStack sender, @LiteralArg("mutual") String l,
                              @EntityArg(name = "a") EntitySelector aSel, @EntityArg(name = "b") EntitySelector bSel) throws CommandSyntaxException {
            Entity a = aSel.findSingleEntity(sender);
            Entity b = bSel.findSingleEntity(sender);
            if (!(a instanceof LivingEntity la) || !(b instanceof LivingEntity lb)) return false;
            reply(sender, a.getName().getString() + " <-> " + b.getName().getString() + ": mutual ally " + TeamAPI.isMutuallyAllied(la, lb)
                    + ", mutual ally relation " + TeamAPI.hasMutualRelation(TeamAPI.ALLY.get(), la, lb));
            return true;
        }
    }
}
