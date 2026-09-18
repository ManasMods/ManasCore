/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.impl;

import io.github.manasmods.manascore.storage.impl.StoragePersistentState;
import io.github.manasmods.manascore.team.ManasCoreTeam;
import io.github.manasmods.manascore.team.api.*;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;


/**
 * Server-global team table. Lives on the overworld so it is shared by every dimension.
 * Invites are session-only and not written to disk.
 */
public class TeamSavedData extends SavedData {
    private static final String FILE_NAME = "manascore_teams";
    private static final String TEAMS_KEY = "teams";
    private static final String ID_KEY = "id";
    private static final String TYPE_KEY = "type";
    private static final String DATA_KEY = "data";
    private static final Set<ResourceLocation> UNKNOWN_TYPE_WARNED = new HashSet<>();

    private static final Factory<TeamSavedData> FACTORY = new Factory<>(
            TeamSavedData::new,
            TeamSavedData::load,
            DataFixTypes.LEVEL
    );

    private final Map<UUID, Team> teams = new LinkedHashMap<>();
    @Getter
    private final List<TeamInvite> invites = new ArrayList<>();

    /**
     * The LOADING flag makes the storage module's DataFixTypes mixin skip datafixing this
     * tag, the same way ManasCore world storage is loaded.
     */
    public static TeamSavedData get(MinecraftServer server) {
        try {
            StoragePersistentState.LOADING.set(true);
            return server.overworld().getDataStorage().computeIfAbsent(FACTORY, FILE_NAME);
        } finally {
            StoragePersistentState.LOADING.set(false);
        }
    }

    public Optional<Team> getTeam(UUID id) {
        return Optional.ofNullable(this.teams.get(id));
    }

    public Collection<Team> getTeams() {
        return Collections.unmodifiableCollection(this.teams.values());
    }

    public void putTeam(Team team) {
        this.teams.put(team.getId(), team);
        this.setDirty();
    }

    public void removeTeam(UUID id) {
        if (this.teams.remove(id) != null) this.setDirty();
    }

    public List<TeamInvite> getInvitesFor(UUID invitee) {
        List<TeamInvite> result = new ArrayList<>();
        for (TeamInvite invite : this.invites) {
            if (invite.invitee().equals(invitee)) result.add(invite);
        }
        return result;
    }

    public Optional<TeamInvite> findInvite(UUID invitee, UUID teamId) {
        for (TeamInvite invite : this.invites) {
            if (invite.invitee().equals(invitee) && invite.teamId().equals(teamId)) return Optional.of(invite);
        }
        return Optional.empty();
    }

    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Team team : this.teams.values()) {
            ResourceLocation typeId = team.getType().getId();
            if (typeId == null) continue;
            CompoundTag entry = serialize(team);
            entry.putString(TYPE_KEY, typeId.toString());
            list.add(entry);
        }
        tag.put(TEAMS_KEY, list);
        return tag;
    }

    private static TeamSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        TeamSavedData data = new TeamSavedData();
        for (Tag t : tag.getList(TEAMS_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) t;
            ResourceLocation typeId = ResourceLocation.tryParse(entry.getString(TYPE_KEY));
            TeamType<?> type = typeId == null ? null : TeamRegistry.TEAM_TYPES.get(typeId);
            if (type == null) {
                if (UNKNOWN_TYPE_WARNED.add(typeId)) {
                    ManasCoreTeam.LOG.warn("Unknown team type {} in saved data, skipping its teams", typeId);
                }
                continue;
            }
            Team team = TeamSavedData.deserialize(type, entry);
            data.teams.put(team.getId(), team);
        }
        return data;
    }

    public static Team deserialize(TeamType<?> type, CompoundTag entry) {
        CompoundTag body = entry.getCompound(DATA_KEY);
        UUID id = entry.getUUID(ID_KEY);
        UUID owner = body.getUUID(Team.OWNER_KEY);
        Team team = type.createTeam(id, owner);
        team.load(body);
        return team;
    }

    public static CompoundTag serialize(Team team) {
        CompoundTag entry = new CompoundTag();
        entry.putUUID(ID_KEY, team.getId());
        entry.put(DATA_KEY, team.save(new CompoundTag()));
        return entry;
    }
}
