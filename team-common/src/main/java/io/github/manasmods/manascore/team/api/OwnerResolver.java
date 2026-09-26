/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.api;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Tells the team module which entity another entity answers to, so summons, pets and minions
 * inherit their owner's teams and allies during relation checks.
 * <p>Registered through {@link TeamAPI#registerOwnerResolver(OwnerResolver)}. Resolvers are asked in
 * registration order, the built-in {@link net.minecraft.world.entity.OwnableEntity} fallback last,
 * and the first non-null answer wins. The chain is followed up to
 * {@link io.github.manasmods.manascore.team.impl.OwnerResolvers#MAX_DEPTH} steps and stops on a cycle.
 * <p>Runs on every damage, targeting and {@code isAlliedTo} check, so an implementation must be
 * O(1), must not look entities up by scanning and must not call back into relation checks.
 */
@FunctionalInterface
public interface OwnerResolver {
    /** The id this entity answers to, or null when it has no owner under this resolver. */
    @Nullable
    UUID ownerOf(LivingEntity entity);
}
