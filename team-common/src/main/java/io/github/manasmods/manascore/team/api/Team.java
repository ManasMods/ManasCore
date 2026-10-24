/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
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
 * This is the team object of a {@code GROUP} shaped {@link TeamType}.
 * Extend from this Class and override {@link #save(CompoundTag)} and {@link #load(CompoundTag)} (calling super) to attach custom data.
 * Return your own implementation from {@link TeamType#createTeam(UUID, UUID)}.
 * <p>
 * Membership is mutated only through the {@link TeamAPI}.
 */
public class Team {
    public static final String OWNER_KEY = "owner";
    public static final String MEMBERS_KEY = "members";
    private static final String NAME_KEY = "name";
    private static final String OWNER_NAME_KEY = "ownerName";

    /**
     * Unique id of this team.
     */
    @Getter
    private final UUID id;
    /**
     * {@link TeamType} of this team.
     */
    @Getter
    private final TeamType<?> type;
    /**
     * Id of the owner, who is always a member.
     */
    @Getter
    private UUID owner;
    private final Set<UUID> members = new LinkedHashSet<>();
    /**
     * Custom name of this team, null if it has none.
     */
    @Getter
    @Nullable
    private String name = null;
    /**
     * Last known name of the owner, which is the display name of a team without a name.
     */
    @Getter
    @Nullable
    private String ownerName = null;

    public Team(UUID id, TeamType<?> type, UUID owner) {
        this.id = id;
        this.type = type;
        this.owner = owner;
        this.members.add(owner);
    }

    /**
     * Used to get the ids of every member, including the owner. The result is unmodifiable.
     */
    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(this.members);
    }

    /**
     * Used to get the number of members.
     */
    public int size() {
        return this.members.size();
    }

    /**
     * Determine if the id is the owner of this team.
     */
    public boolean isOwner(UUID id) {
        return this.owner.equals(id);
    }

    /**
     * Determine if the {@link Entity} is the owner of this team.
     */
    public boolean isOwner(Entity entity) {
        return this.isOwner(entity.getUUID());
    }

    /**
     * Determine if the id is a member of this team.
     */
    public boolean isMember(UUID id) {
        return this.members.contains(id);
    }

    /**
     * Determine if the {@link Entity} is a member of this team.
     */
    public boolean isMember(Entity entity) {
        return this.isMember(entity.getUUID());
    }

    /**
     * Determine if this team has a custom name.
     */
    public boolean hasName() {
        return this.name != null;
    }

    /**
     * Used to get the {@link Component} name of this team.
     * It is the custom name, else the last known name of the owner, else the first 8 characters of the id of the owner.
     */
    public Component getDisplayName() {
        return this.name != null ? Component.literal(this.name) : this.ownerName != null ? Component.literal(this.ownerName) : Component.literal(this.owner.toString().substring(0, 8));
    }

    /**
     * Used to get the members that are currently loaded.
     * Players are looked up in the player list, other entities by scanning every level.
     * This is server only.
     */
    public List<LivingEntity> getOnlineMembers(MinecraftServer server) {
        List<LivingEntity> result = new ArrayList<>();
        for (UUID member : this.members) {
            LivingEntity entity = findLoaded(server, member);
            if (entity != null) result.add(entity);
        }
        return result;
    }

    /**
     * Used to save this team and sync it to its online members.
     *
     * @see TeamAPI#markDirty(Team)
     */
    public void markDirty() {
        TeamAPI.markDirty(this);
    }

    /**
     * Used to find the loaded entity of an id, checking the player list before every level.
     *
     * @return the loaded {@link LivingEntity}, null if there is none.
     */
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

    /**
     * Used to save the data of this team to the given tag.
     * Override this Method to add your own data and call super.
     *
     * @see #load(CompoundTag)
     */
    public CompoundTag save(CompoundTag tag) {
        tag.putUUID(OWNER_KEY, this.owner);
        ListTag list = new ListTag();
        for (UUID member : this.members) list.add(NbtUtils.createUUID(member));
        tag.put(MEMBERS_KEY, list);
        if (this.name != null) tag.putString(NAME_KEY, this.name);
        if (this.ownerName != null) tag.putString(OWNER_NAME_KEY, this.ownerName);
        return tag;
    }

    /**
     * Used to load the data of this team from the given tag.
     * Override this Method to read your own data and call super.
     *
     * @see #save(CompoundTag)
     */
    public void load(CompoundTag tag) {
        this.members.clear();
        if (tag.hasUUID(OWNER_KEY)) this.owner = tag.getUUID(OWNER_KEY);
        ListTag list = tag.getList(MEMBERS_KEY, Tag.TAG_INT_ARRAY);
        for (Tag t : list) this.members.add(NbtUtils.loadUUID(t));
        this.members.add(this.owner);
        this.name = tag.contains(NAME_KEY, Tag.TAG_STRING) ? tag.getString(NAME_KEY) : null;
        this.ownerName = tag.contains(OWNER_NAME_KEY, Tag.TAG_STRING) ? tag.getString(OWNER_NAME_KEY) : null;
    }

    /**
     * Used to add a member without any checks, reached through {@link Internals#addMember(Team, UUID)}.
     */
    protected boolean addMemberInternal(UUID id) {
        return this.members.add(id);
    }

    /**
     * Used to remove a member without any checks, reached through {@link Internals#removeMember(Team, UUID)}.
     */
    protected boolean removeMemberInternal(UUID id) {
        return this.members.remove(id);
    }

    /**
     * Used to set the owner without any checks, reached through {@link Internals#setOwner(Team, UUID)}.
     */
    protected void setOwnerInternal(UUID id) {
        this.owner = id;
        this.members.add(id);
    }

    /**
     * Used to set the name without any checks, reached through {@link Internals#setName(Team, String)}.
     */
    protected void setNameInternal(@Nullable String name) {
        this.name = name;
    }

    /**
     * Used to set the owner name without any checks, reached through {@link Internals#setOwnerName(Team, String)}.
     */
    protected void setOwnerNameInternal(@Nullable String ownerName) {
        this.ownerName = ownerName;
    }

    /**
     * Bridge for {@code impl} code to reach the protected mutators without subclassing.
     * This is not part of the public API.
     */
    public static final class Internals {
        private Internals() {
        }

        /**
         * Used to add a member to the team.
         *
         * @return true if the member was added.
         */
        public static boolean addMember(Team team, UUID id) {
            return team.addMemberInternal(id);
        }

        /**
         * Used to remove a member from the team.
         *
         * @return true if the member was removed.
         */
        public static boolean removeMember(Team team, UUID id) {
            return team.removeMemberInternal(id);
        }

        /**
         * Used to set the owner of the team, who becomes a member.
         */
        public static void setOwner(Team team, UUID id) {
            team.setOwnerInternal(id);
        }

        /**
         * Used to set the name of the team.
         */
        public static void setName(Team team, @Nullable String name) {
            team.setNameInternal(name);
        }

        /**
         * Used to set the last known name of the owner.
         */
        public static void setOwnerName(Team team, @Nullable String name) {
            team.setOwnerNameInternal(name);
        }
    }

    public String toString() {
        return "Team{" + "id=" + id + ", type=" + type + ", owner=" + owner + ", members=" + members + ", name=" + name + ", ownerName=" + ownerName + '}';
    }
}
