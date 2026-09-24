/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.impl;

import io.github.manasmods.manascore.team.api.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry of {@link OwnerResolver}s and the owner chain walk used by relation checks.
 * Works on both sides: the built-in fallback reads {@link OwnableEntity#getOwnerUUID()}, which is
 * synced entity data, and loaded lookups go through the level's entity map.
 */
public class OwnerResolvers {
    public static final int MAX_DEPTH = 8;
    private static final List<OwnerResolver> RESOLVERS = new CopyOnWriteArrayList<>();
    private static final OwnerResolver VANILLA = entity -> entity instanceof OwnableEntity ownable ? ownable.getOwnerUUID() : null;

    private OwnerResolvers() {
    }

    public static void register(OwnerResolver resolver) {
        RESOLVERS.add(resolver);
    }

    @Nullable
    private static UUID ownerOf(LivingEntity entity) {
        for (OwnerResolver resolver : RESOLVERS) {
            UUID owner = resolver.ownerOf(entity);
            if (owner != null) return owner;
        }
        return VANILLA.ownerOf(entity);
    }

    /**
     * Root owner id of the entity: the chain is followed while the owner is loaded, up to
     * {@link #MAX_DEPTH} steps, and stops on a cycle. The last id reached is returned, so an
     * unloaded or offline owner still resolves. An entity without an owner resolves to itself.
     */
    public static UUID resolveId(LivingEntity entity) {
        UUID first = ownerOf(entity);
        if (first == null || first.equals(entity.getUUID())) return entity.getUUID();
        UUID[] seen = new UUID[MAX_DEPTH + 1];
        seen[0] = entity.getUUID();
        seen[1] = first;
        UUID current = first;
        LivingEntity cursor = findLoaded(entity.level(), first);
        for (int depth = 2; cursor != null && depth <= MAX_DEPTH; depth++) {
            UUID owner = ownerOf(cursor);
            if (owner == null || contains(seen, depth, owner)) break;
            seen[depth] = owner;
            current = owner;
            cursor = findLoaded(entity.level(), owner);
        }
        return current;
    }

    /** Loaded entity behind {@link #resolveId(LivingEntity)}, or the entity itself when the owner is not loaded. */
    public static LivingEntity resolveEntity(LivingEntity entity) {
        UUID id = resolveId(entity);
        if (id.equals(entity.getUUID())) return entity;
        LivingEntity loaded = findLoaded(entity.level(), id);
        return loaded == null ? entity : loaded;
    }

    /** Loaded living entity for the id: this level first, then on the server every other level. */
    @Nullable
    public static LivingEntity findLoaded(Level level, UUID id) {
        Entity entity = level.getEntities().get(id);
        if (entity instanceof LivingEntity living) return living;
        MinecraftServer server = level.getServer();
        if (level.isClientSide() || server == null) return null;
        return Team.findLoaded(server, id);
    }

    private static boolean contains(UUID[] seen, int count, UUID id) {
        for (int i = 0; i < count; i++) if (id.equals(seen[i])) return true;
        return false;
    }
}
