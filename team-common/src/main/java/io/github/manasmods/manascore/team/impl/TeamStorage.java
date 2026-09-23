/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.impl;

import io.github.manasmods.manascore.storage.api.Storage;
import io.github.manasmods.manascore.storage.api.StorageEvents;
import io.github.manasmods.manascore.storage.api.StorageKey;
import io.github.manasmods.manascore.team.ManasCoreTeam;
import io.github.manasmods.manascore.team.ModuleConstants;
import io.github.manasmods.manascore.team.api.*;
import io.github.manasmods.manascore.team.api.template.*;
import io.github.manasmods.manascore.team.impl.client.ClientTeamCache;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;


public class TeamStorage extends Storage implements Teams {
    private static final String GROUPS_KEY = "groups";
    private static final String RELATIONS_KEY = "relations";
    private static final String INBOUND_KEY = "inbound";

    @Getter
    private static StorageKey<TeamStorage> key = null;

    public static void init() {
        StorageEvents.RegisterStorage<Entity> listener = registry -> key = registry.register(
                ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "team_storage"),
                TeamStorage.class,
                LivingEntity.class::isInstance,
                target -> new TeamStorage((LivingEntity) target));

        StorageEvents.REGISTER_ENTITY_STORAGE.register(listener);

        if (!StorageEvents.REGISTER_ENTITY_STORAGE.isRegistered(listener)) ManasCoreTeam.LOG.warn("Failed to register storage event");
    }

    private final Map<ResourceLocation, Set<UUID>> groups = new HashMap<>();
    private final Map<ResourceLocation, Set<UUID>> relations = new HashMap<>();
    private final Map<ResourceLocation, Set<UUID>> inbound = new HashMap<>();

    protected TeamStorage(LivingEntity holder) {
        super(holder);
    }

    public LivingEntity getOwner() {
        return (LivingEntity) this.holder;
    }

    public Set<UUID> getTeamIds(TeamType<?> type) {
        ResourceLocation id = type.getId();
        if (id == null) return Collections.emptySet();
        Set<UUID> set = this.groups.get(id);
        return set == null ? Collections.emptySet() : Collections.unmodifiableSet(set);
    }

    public Map<ResourceLocation, Set<UUID>> getAllTeamIds() {
        return copy(this.groups);
    }

    public Set<UUID> getRelated(TeamType<?> type) {
        ResourceLocation id = type.getId();
        if (id == null) return Collections.emptySet();
        Set<UUID> set = this.relations.get(id);
        return set == null ? Collections.emptySet() : Collections.unmodifiableSet(set);
    }

    public Map<ResourceLocation, Set<UUID>> getAllRelated() {
        return copy(this.relations);
    }

    public Set<UUID> getRelatedBy(TeamType<?> type) {
        ResourceLocation id = type.getId();
        if (id == null) return Collections.emptySet();
        Set<UUID> set = this.inbound.get(id);
        return set == null ? Collections.emptySet() : Collections.unmodifiableSet(set);
    }

    public Map<ResourceLocation, Set<UUID>> getAllRelatedBy() {
        return copy(this.inbound);
    }

    public boolean isEmpty() {
        return this.groups.isEmpty() && this.relations.isEmpty() && this.inbound.isEmpty();
    }

    private static Map<ResourceLocation, Set<UUID>> copy(Map<ResourceLocation, Set<UUID>> source) {
        Map<ResourceLocation, Set<UUID>> result = new HashMap<>();
        source.forEach((k, v) -> result.put(k, new LinkedHashSet<>(v)));
        return result;
    }

    public boolean addTeamId(ResourceLocation typeId, UUID teamId) {
        boolean changed = this.groups.computeIfAbsent(typeId, k -> new LinkedHashSet<>()).add(teamId);
        if (changed) this.markDirty();
        return changed;
    }

    public boolean removeTeamId(ResourceLocation typeId, UUID teamId) {
        Set<UUID> set = this.groups.get(typeId);
        if (set == null) return false;
        boolean changed = set.remove(teamId);
        if (set.isEmpty()) this.groups.remove(typeId);
        if (changed) this.markDirty();
        return changed;
    }

    public void setTeamIds(ResourceLocation typeId, Collection<UUID> ids) {
        if (ids.isEmpty()) this.groups.remove(typeId);
        else this.groups.put(typeId, new LinkedHashSet<>(ids));
        this.markDirty();
    }

    public boolean addRelated(ResourceLocation typeId, UUID entityId) {
        boolean changed = this.relations.computeIfAbsent(typeId, k -> new LinkedHashSet<>()).add(entityId);
        if (changed) this.markDirty();
        return changed;
    }

    public boolean removeRelated(ResourceLocation typeId, UUID entityId) {
        Set<UUID> set = this.relations.get(typeId);
        if (set == null) return false;
        boolean changed = set.remove(entityId);
        if (set.isEmpty()) this.relations.remove(typeId);
        if (changed) this.markDirty();
        return changed;
    }

    public void setRelated(ResourceLocation typeId, Collection<UUID> ids) {
        if (ids.isEmpty()) this.relations.remove(typeId);
        else this.relations.put(typeId, new LinkedHashSet<>(ids));
        this.markDirty();
    }

    public boolean addInbound(ResourceLocation typeId, UUID entityId) {
        boolean changed = this.inbound.computeIfAbsent(typeId, k -> new LinkedHashSet<>()).add(entityId);
        if (changed) this.markDirty();
        return changed;
    }

    public boolean removeInbound(ResourceLocation typeId, UUID entityId) {
        Set<UUID> set = this.inbound.get(typeId);
        if (set == null) return false;
        boolean changed = set.remove(entityId);
        if (set.isEmpty()) this.inbound.remove(typeId);
        if (changed) this.markDirty();
        return changed;
    }

    public void setInbound(ResourceLocation typeId, Collection<UUID> ids) {
        if (ids.isEmpty()) this.inbound.remove(typeId);
        else this.inbound.put(typeId, new LinkedHashSet<>(ids));
        this.markDirty();
    }

    public void clearAll() {
        this.groups.clear();
        this.relations.clear();
        this.inbound.clear();
        this.markDirty();
    }

    public boolean isOwnerOnly() {
        return true;
    }

    public void save(CompoundTag data) {
        data.put(GROUPS_KEY, writeMap(this.groups));
        data.put(RELATIONS_KEY, writeMap(this.relations));
        data.put(INBOUND_KEY, writeMap(this.inbound));
    }

    public void load(CompoundTag data) {
        Map<ResourceLocation, Set<UUID>> before = copy(this.relations);
        Map<ResourceLocation, Set<UUID>> beforeIn = copy(this.inbound);
        readMap(data.getCompound(GROUPS_KEY), this.groups);
        readMap(data.getCompound(RELATIONS_KEY), this.relations);
        readMap(data.getCompound(INBOUND_KEY), this.inbound);
        if (this.getOwner().level().isClientSide()) this.fireRelationChanges(before, beforeIn);
    }

    private void fireRelationChanges(Map<ResourceLocation, Set<UUID>> before, Map<ResourceLocation, Set<UUID>> beforeIn) {
        if (!this.getOwner().level().isClientSide()) return;
        Set<ResourceLocation> ids = new HashSet<>();
        ids.addAll(before.keySet());
        ids.addAll(beforeIn.keySet());
        ids.addAll(this.relations.keySet());
        ids.addAll(this.inbound.keySet());
        for (ResourceLocation id : ids) {
            if (Objects.equals(before.get(id), this.relations.get(id)) && Objects.equals(beforeIn.get(id), this.inbound.get(id))) continue;
            TeamType<?> type = TeamAPI.getTeamTypeRegistry().get(id);
            if (type == null) continue;
            LivingEntity owner = this.getOwner();
            ClientTeamCache.defer(() -> TeamEvents.CLIENT_RELATIONS_UPDATED.invoker().run(owner, type));
        }
    }

    private static CompoundTag writeMap(Map<ResourceLocation, Set<UUID>> map) {
        CompoundTag tag = new CompoundTag();
        map.forEach((typeId, ids) -> {
            ListTag list = new ListTag();
            for (UUID id : ids) list.add(NbtUtils.createUUID(id));
            tag.put(typeId.toString(), list);
        });
        return tag;
    }

    private static void readMap(CompoundTag tag, Map<ResourceLocation, Set<UUID>> target) {
        target.clear();
        for (String key : tag.getAllKeys()) {
            ResourceLocation typeId = ResourceLocation.tryParse(key);
            if (typeId == null) continue;
            Set<UUID> ids = new LinkedHashSet<>();
            for (Tag t : tag.getList(key, Tag.TAG_INT_ARRAY)) ids.add(NbtUtils.loadUUID(t));
            if (!ids.isEmpty()) target.put(typeId, ids);
        }
    }
}
