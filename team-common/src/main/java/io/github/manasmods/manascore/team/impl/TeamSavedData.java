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
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

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
    private static final String RELATIONS_KEY = "relations";
    private static final String RELATIONS_MIGRATED_KEY = "relationsMigrated";
    private static final String NAMES_KEY = "names";
    private static final Set<ResourceLocation> UNKNOWN_TYPE_WARNED = new HashSet<>();

    private static final Factory<TeamSavedData> FACTORY = new Factory<>(
            TeamSavedData::new,
            TeamSavedData::load,
            DataFixTypes.LEVEL
    );

    private final Map<UUID, Team> teams = new LinkedHashMap<>();
    @Getter
    private final List<TeamInvite> invites = new ArrayList<>();
    private final Map<ResourceLocation, Map<UUID, Set<UUID>>> relations = new LinkedHashMap<>();
    private final Map<ResourceLocation, Map<UUID, Set<UUID>>> relatedBy = new LinkedHashMap<>();
    private final Set<UUID> relationsMigrated = new LinkedHashSet<>();
    private final Map<UUID, String> names = new LinkedHashMap<>();
    private final Map<UUID, Set<UUID>> memberTeams = new HashMap<>();

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
        this.indexMember(team.getId(), team.getOwner());
        for (UUID member : team.getMembers()) this.indexMember(team.getId(), member);
        this.setDirty();
    }

    public void removeTeam(UUID id) {
        Team team = this.teams.remove(id);
        if (team == null) return;
        this.unindexMember(id, team.getOwner());
        for (UUID member : team.getMembers()) this.unindexMember(id, member);
        this.setDirty();
    }

    /**
     * Every team id {@code member} belongs to, as owner or member. Not persisted; rebuilt on load.
     */
    public Set<UUID> getTeamIdsOf(UUID member) {
        Set<UUID> ids = this.memberTeams.get(member);
        return ids == null ? Collections.emptySet() : Collections.unmodifiableSet(ids);
    }

    public void indexMember(UUID teamId, UUID member) {
        this.memberTeams.computeIfAbsent(member, k -> new LinkedHashSet<>()).add(teamId);
    }

    public void unindexMember(UUID teamId, UUID member) {
        Set<UUID> ids = this.memberTeams.get(member);
        if (ids == null) return;
        ids.remove(teamId);
        if (ids.isEmpty()) this.memberTeams.remove(member);
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

    /**
     * Determine what {@code entity} is related to under {@code typeId}.
     */
    public Set<UUID> getRelated(ResourceLocation typeId, UUID entity) {
        Map<UUID, Set<UUID>> byEntity = this.relations.get(typeId);
        if (byEntity == null) return Collections.emptySet();
        Set<UUID> related = byEntity.get(entity);
        return related == null ? Collections.emptySet() : Collections.unmodifiableSet(related);
    }

    public Map<ResourceLocation, Set<UUID>> getAllRelated(UUID entity) {
        Map<ResourceLocation, Set<UUID>> result = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, Map<UUID, Set<UUID>>> entry : this.relations.entrySet()) {
            Set<UUID> related = entry.getValue().get(entity);
            if (related != null && !related.isEmpty()) result.put(entry.getKey(), new LinkedHashSet<>(related));
        }
        return result;
    }

    /**
     * Determine what is related to {@code entity} under {@code typeId}, i.e. every {@code a} for
     * which {@code addRelated(typeId, a, entity)} was called.
     */
    public Set<UUID> getRelatedBy(ResourceLocation typeId, UUID entity) {
        Map<UUID, Set<UUID>> byEntity = this.relatedBy.get(typeId);
        if (byEntity == null) return Collections.emptySet();
        Set<UUID> related = byEntity.get(entity);
        return related == null ? Collections.emptySet() : Collections.unmodifiableSet(related);
    }

    public Map<ResourceLocation, Set<UUID>> getAllRelatedBy(UUID entity) {
        Map<ResourceLocation, Set<UUID>> result = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, Map<UUID, Set<UUID>>> entry : this.relatedBy.entrySet()) {
            Set<UUID> related = entry.getValue().get(entity);
            if (related != null && !related.isEmpty()) result.put(entry.getKey(), new LinkedHashSet<>(related));
        }
        return result;
    }

    public boolean addRelated(ResourceLocation typeId, UUID a, UUID b) {
        boolean changed = this.relations.computeIfAbsent(typeId, k -> new LinkedHashMap<>())
                .computeIfAbsent(a, k -> new LinkedHashSet<>()).add(b);
        if (changed) {
            this.relatedBy.computeIfAbsent(typeId, k -> new LinkedHashMap<>())
                    .computeIfAbsent(b, k -> new LinkedHashSet<>()).add(a);
            this.setDirty();
        }
        return changed;
    }

    public boolean removeRelated(ResourceLocation typeId, UUID a, UUID b) {
        Map<UUID, Set<UUID>> byEntity = this.relations.get(typeId);
        if (byEntity == null) return false;
        Set<UUID> related = byEntity.get(a);
        if (related == null) return false;
        boolean changed = related.remove(b);
        if (related.isEmpty()) byEntity.remove(a);
        if (byEntity.isEmpty()) this.relations.remove(typeId);
        if (changed) {
            this.unindexRelatedBy(typeId, b, a);
            this.setDirty();
        }
        return changed;
    }

    private void unindexRelatedBy(ResourceLocation typeId, UUID entity, UUID source) {
        Map<UUID, Set<UUID>> byEntity = this.relatedBy.get(typeId);
        if (byEntity == null) return;
        Set<UUID> sources = byEntity.get(entity);
        if (sources == null) return;
        sources.remove(source);
        if (sources.isEmpty()) byEntity.remove(entity);
        if (byEntity.isEmpty()) this.relatedBy.remove(typeId);
    }

    /**
     * Removes every relation involving {@code entity}, as subject and as object, across every type.
     */
    public void removeRelatedEverywhere(UUID entity) {
        boolean changed = false;
        Iterator<Map.Entry<ResourceLocation, Map<UUID, Set<UUID>>>> typeIt = this.relations.entrySet().iterator();
        while (typeIt.hasNext()) {
            Map.Entry<ResourceLocation, Map<UUID, Set<UUID>>> typeEntry = typeIt.next();
            ResourceLocation typeId = typeEntry.getKey();
            Map<UUID, Set<UUID>> byEntity = typeEntry.getValue();

            Set<UUID> ownRelated = byEntity.remove(entity);
            if (ownRelated != null) {
                changed = true;
                for (UUID target : ownRelated) this.unindexRelatedBy(typeId, target, entity);
            }

            Iterator<Map.Entry<UUID, Set<UUID>>> entityIt = byEntity.entrySet().iterator();
            while (entityIt.hasNext()) {
                Map.Entry<UUID, Set<UUID>> entry = entityIt.next();
                if (entry.getValue().remove(entity)) changed = true;
                if (entry.getValue().isEmpty()) entityIt.remove();
            }
            if (byEntity.isEmpty()) typeIt.remove();

            Map<UUID, Set<UUID>> byEntityBy = this.relatedBy.get(typeId);
            if (byEntityBy != null) {
                byEntityBy.remove(entity);
                if (byEntityBy.isEmpty()) this.relatedBy.remove(typeId);
            }
        }
        if (changed) this.setDirty();
    }

    public boolean isRelationsMigrated(UUID entity) {
        return this.relationsMigrated.contains(entity);
    }

    public void markRelationsMigrated(UUID entity) {
        if (this.relationsMigrated.add(entity)) this.setDirty();
    }

    public void putName(UUID id, String name) {
        String previous = this.names.put(id, name);
        if (!name.equals(previous)) this.setDirty();
    }

    public Optional<String> getName(UUID id) {
        return Optional.ofNullable(this.names.get(id));
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
        tag.put(RELATIONS_KEY, this.saveRelations());

        ListTag migrated = new ListTag();
        for (UUID id : this.relationsMigrated) migrated.add(NbtUtils.createUUID(id));
        tag.put(RELATIONS_MIGRATED_KEY, migrated);
        tag.put(NAMES_KEY, this.saveNames());
        return tag;
    }

    private CompoundTag saveNames() {
        CompoundTag tag = new CompoundTag();
        this.names.forEach((id, name) -> tag.putString(id.toString(), name));
        return tag;
    }

    private CompoundTag saveRelations() {
        CompoundTag tag = new CompoundTag();
        this.relations.forEach((typeId, byEntity) -> {
            CompoundTag entityTag = new CompoundTag();
            byEntity.forEach((entity, related) -> {
                ListTag list = new ListTag();
                for (UUID id : related) list.add(NbtUtils.createUUID(id));
                entityTag.put(entity.toString(), list);
            });
            tag.put(typeId.toString(), entityTag);
        });
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

        loadRelations(tag.getCompound(RELATIONS_KEY), data.relations);
        for (Tag t : tag.getList(RELATIONS_MIGRATED_KEY, Tag.TAG_INT_ARRAY)) data.relationsMigrated.add(NbtUtils.loadUUID(t));
        loadNames(tag.getCompound(NAMES_KEY), data.names);
        data.rebuildRelatedBy();
        data.rebuildMemberIndex();
        return data;
    }

    private void rebuildMemberIndex() {
        this.memberTeams.clear();
        for (Team team : this.teams.values()) {
            this.indexMember(team.getId(), team.getOwner());
            for (UUID member : team.getMembers()) this.indexMember(team.getId(), member);
        }
    }

    private void rebuildRelatedBy() {
        this.relatedBy.clear();
        for (Map.Entry<ResourceLocation, Map<UUID, Set<UUID>>> typeEntry : this.relations.entrySet()) {
            ResourceLocation typeId = typeEntry.getKey();
            for (Map.Entry<UUID, Set<UUID>> entry : typeEntry.getValue().entrySet()) {
                UUID a = entry.getKey();
                for (UUID b : entry.getValue()) {
                    this.relatedBy.computeIfAbsent(typeId, k -> new LinkedHashMap<>())
                            .computeIfAbsent(b, k -> new LinkedHashSet<>()).add(a);
                }
            }
        }
    }

    private static void loadNames(CompoundTag tag, Map<UUID, String> target) {
        for (String key : tag.getAllKeys()) {
            UUID id = parseUuid(key);
            if (id != null) target.put(id, tag.getString(key));
        }
    }

    private static void loadRelations(CompoundTag tag, Map<ResourceLocation, Map<UUID, Set<UUID>>> target) {
        for (String typeKey : tag.getAllKeys()) {
            ResourceLocation typeId = ResourceLocation.tryParse(typeKey);
            if (typeId == null) continue;

            CompoundTag entityTag = tag.getCompound(typeKey);
            Map<UUID, Set<UUID>> byEntity = new LinkedHashMap<>();
            for (String entityKey : entityTag.getAllKeys()) {
                UUID entity = parseUuid(entityKey);
                if (entity == null) continue;

                Set<UUID> related = new LinkedHashSet<>();
                for (Tag t : entityTag.getList(entityKey, Tag.TAG_INT_ARRAY)) related.add(NbtUtils.loadUUID(t));
                if (!related.isEmpty()) byEntity.put(entity, related);
            }
            if (!byEntity.isEmpty()) target.put(typeId, byEntity);
        }
    }

    @Nullable
    private static UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
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

    /**
     * Used to get every entity id referenced by teams, relations or invites.
     */
    public Set<UUID> referencedIds() {
        Set<UUID> result = new HashSet<>();
        for (Team team : this.teams.values()) {
            result.add(team.getOwner());
            result.addAll(team.getMembers());
        }
        for (Map.Entry<ResourceLocation, Map<UUID, Set<UUID>>> typeEntry : this.relations.entrySet()) {
            Map<UUID, Set<UUID>> byEntity = typeEntry.getValue();
            result.addAll(byEntity.keySet());
            for (Set<UUID> related : byEntity.values()) result.addAll(related);
        }
        for (TeamInvite invite : this.invites) {
            result.add(invite.inviter());
            result.add(invite.invitee());
        }
        return result;
    }

    /**
     * Drops last-known names of entities not in {@code keep}.
     */
    public void pruneNames(Set<UUID> keep) {
        boolean changed = false;
        Iterator<UUID> it = this.names.keySet().iterator();
        while (it.hasNext()) {
            if (!keep.contains(it.next())) {
                it.remove();
                changed = true;
            }
        }
        if (changed) this.setDirty();
    }
}
