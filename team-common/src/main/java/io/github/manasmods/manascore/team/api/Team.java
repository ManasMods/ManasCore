/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.api;

import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A GROUP-shaped team. Subclass and override {@link #save(CompoundTag)} / {@link #load(CompoundTag)}
 * (calling super) to attach custom data. Membership is mutated only through {@code TeamAPI}.
 */
public class Team {
    public static final String OWNER_KEY = "owner";
    private static final String MEMBERS_KEY = "members";
    private static final String NAME_KEY = "name";
    private static final String OWNER_NAME_KEY = "ownerName";

    @Getter
    private final UUID id;
    @Getter
    private final TeamType<?> type;
    @Getter
    private UUID owner;
    private final Set<UUID> members = new LinkedHashSet<>();
    @Getter
    @Nullable
    private String name = null;
    @Getter
    @Nullable
    private String ownerName = null;

    public Team(UUID id, TeamType<?> type, UUID owner) {
        this.id = id;
        this.type = type;
        this.owner = owner;
        this.members.add(owner);
    }

    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(this.members);
    }

    public int size() {
        return this.members.size();
    }

    public boolean isOwner(UUID id) {
        return this.owner.equals(id);
    }

    public boolean isOwner(Entity entity) {
        return this.isOwner(entity.getUUID());
    }

    public boolean isMember(UUID id) {
        return this.members.contains(id);
    }

    public boolean isMember(Entity entity) {
        return this.isMember(entity.getUUID());
    }

    public boolean hasName() {
        return this.name != null;
    }

    public Component getDisplayName() {
        return this.name != null ? Component.literal(this.name) : this.ownerName != null ? Component.literal(this.ownerName) : Component.literal(this.owner.toString().substring(0, 8));
    }

    /**
     * Server only. Resolves members that are currently loaded: players via the player list,
     * other entities by scanning every level.
     */
    public List<LivingEntity> getOnlineMembers(MinecraftServer server) {
        List<LivingEntity> result = new ArrayList<>();
        for (UUID member : this.members) {
            LivingEntity entity = findLoaded(server, member);
            if (entity != null) result.add(entity);
        }
        return result;
    }

    @Nullable
    public static LivingEntity findLoaded(MinecraftServer server, UUID id) {
        ServerPlayer player = server.getPlayerList().getPlayer(id);
        if (player != null) return player;
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(id);
            if (entity instanceof LivingEntity living) return living;
        }
        return null;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putUUID(OWNER_KEY, this.owner);
        ListTag list = new ListTag();
        for (UUID member : this.members) list.add(NbtUtils.createUUID(member));
        tag.put(MEMBERS_KEY, list);
        if (this.name != null) tag.putString(NAME_KEY, this.name);
        if (this.ownerName != null) tag.putString(OWNER_NAME_KEY, this.ownerName);
        return tag;
    }

    public void load(CompoundTag tag) {
        this.members.clear();
        if (tag.hasUUID(OWNER_KEY)) this.owner = tag.getUUID(OWNER_KEY);
        ListTag list = tag.getList(MEMBERS_KEY, Tag.TAG_INT_ARRAY);
        for (Tag t : list) this.members.add(NbtUtils.loadUUID(t));
        this.members.add(this.owner);
        this.name = tag.contains(NAME_KEY, Tag.TAG_STRING) ? tag.getString(NAME_KEY) : null;
        this.ownerName = tag.contains(OWNER_NAME_KEY, Tag.TAG_STRING) ? tag.getString(OWNER_NAME_KEY) : null;
    }

    protected boolean addMemberInternal(UUID id) {
        return this.members.add(id);
    }

    protected boolean removeMemberInternal(UUID id) {
        return this.members.remove(id);
    }

    protected void setOwnerInternal(UUID id) {
        this.owner = id;
        this.members.add(id);
    }

    protected void setNameInternal(@Nullable String name) {
        this.name = name;
    }

    protected void setOwnerNameInternal(@Nullable String ownerName) {
        this.ownerName = ownerName;
    }

    /**
     * Bridge so {@code impl} code can reach the protected mutators without subclassing.
     * Not part of the public API.
     */
    public static final class Internals {
        private Internals() {
        }

        public static boolean addMember(Team team, UUID id) {
            return team.addMemberInternal(id);
        }

        public static boolean removeMember(Team team, UUID id) {
            return team.removeMemberInternal(id);
        }

        public static void setOwner(Team team, UUID id) {
            team.setOwnerInternal(id);
        }

        public static void setName(Team team, @Nullable String name) {
            team.setNameInternal(name);
        }

        public static void setOwnerName(Team team, @Nullable String name) {
            team.setOwnerNameInternal(name);
        }
    }

    public String toString() {
        return "Team{" + "id=" + id + ", type=" + type + ", owner=" + owner + ", members=" + members + ", name=" + name + ", ownerName=" + ownerName + '}';
    }
}
