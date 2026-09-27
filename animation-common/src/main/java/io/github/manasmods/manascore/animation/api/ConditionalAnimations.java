/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.animation.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Client-side registry of condition-driven ambient poses.
 * <p>
 * Each render tick, when no packet-driven animation is active on an entity, the highest-priority
 * conditional whose predicate matches plays as an ambient pose (loop the animation for a held pose).
 * Purely client-evaluated on synced entity state (vehicle, pose, ...), so every viewer stays in sync
 * without any networking, and it survives relog / entering tracking range mid-condition.
 * <p>
 * Register on the client only (predicates run client-side).
 */
public final class ConditionalAnimations {
    private ConditionalAnimations() {}

    /**
     * @param nonPlayers whether this conditional is also evaluated for entities that are not a {@link Player}.
     *                   Anything posed through a {@code PlayerModel} reaches {@link #evaluate}, including mobs,
     *                   so predicates are player-only unless they opt in.
     */
    public record ConditionalAnimation(Predicate<LivingEntity> condition, String key, int priority, boolean firstPerson, boolean nonPlayers) {}

    private static final List<ConditionalAnimation> REGISTRY = new ArrayList<>();

    public static void register(Predicate<LivingEntity> condition, String key, int priority, boolean firstPerson, boolean nonPlayers) {
        REGISTRY.add(new ConditionalAnimation(condition, key, priority, firstPerson, nonPlayers));
        REGISTRY.sort((a, b) -> Integer.compare(b.priority(), a.priority()));
    }

    public static void register(Predicate<LivingEntity> condition, String key, int priority, boolean firstPerson) {
        register(condition, key, priority, firstPerson, false);
    }

    public static void register(Predicate<LivingEntity> condition, String key) {
        register(condition, key, 0, false);
    }

    /** Highest-priority conditional whose predicate matches the entity, or {@code null}. */
    public static ConditionalAnimation evaluate(LivingEntity entity) {
        boolean player = entity instanceof Player;
        for (ConditionalAnimation conditional : REGISTRY) {
            if (!player && !conditional.nonPlayers()) continue;
            if (conditional.condition().test(entity)) return conditional;
        }
        return null;
    }
}
