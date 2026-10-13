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
 * This is the client side registry of condition driven ambient poses.
 * Register conditions on the client only, because the predicates run client side.
 * <p>
 * Each render tick, when no packet driven animation is active on an entity, the highest priority
 * {@link ConditionalAnimation} whose predicate matches plays as an ambient pose. Loop the animation to hold the pose.
 * <p>
 * The predicates read synced entity state (vehicle, pose, ...), so every viewer stays in sync without any networking,
 * also after a relog or when an entity enters the tracking range mid condition.
 */
public final class ConditionalAnimations {
    private ConditionalAnimations() {}

    /**
     * One registered condition and the animation it plays.
     *
     * @param condition   Predicate that decides if the pose plays for a {@link LivingEntity}.
     * @param key         Animation key ({@code <modid>:<name>}) played while the condition matches.
     * @param priority    Higher values are evaluated first.
     * @param firstPerson true shows the pose from the first person view of the owner.
     * @param nonPlayers  true will also evaluate this condition for entities that are not a {@link Player}.
     *                    Anything posed through a {@code PlayerModel} reaches {@link #evaluate}, including mobs,
     *                    so predicates are player only unless they opt in.
     */
    public record ConditionalAnimation(Predicate<LivingEntity> condition, String key, int priority, boolean firstPerson, boolean nonPlayers) {}

    private static final List<ConditionalAnimation> REGISTRY = new ArrayList<>();

    /**
     * Used to register a conditional animation.
     * Conditions with the same priority are evaluated in the order they were registered.
     *
     * @see ConditionalAnimation
     */
    public static void register(Predicate<LivingEntity> condition, String key, int priority, boolean firstPerson, boolean nonPlayers) {
        REGISTRY.add(new ConditionalAnimation(condition, key, priority, firstPerson, nonPlayers));
        REGISTRY.sort((a, b) -> Integer.compare(b.priority(), a.priority()));
    }

    /**
     * Used to register a conditional animation that is only evaluated for players.
     *
     * @see #register(Predicate, String, int, boolean, boolean)
     */
    public static void register(Predicate<LivingEntity> condition, String key, int priority, boolean firstPerson) {
        register(condition, key, priority, firstPerson, false);
    }

    /**
     * Used to register a conditional animation with priority 0 that is only evaluated for players and is not shown in first person.
     *
     * @see #register(Predicate, String, int, boolean, boolean)
     */
    public static void register(Predicate<LivingEntity> condition, String key) {
        register(condition, key, 0, false);
    }

    /**
     * Used to get the highest priority {@link ConditionalAnimation} whose predicate matches the {@link LivingEntity}.
     * Entities that are not a {@link Player} only match conditions registered with {@code nonPlayers}.
     *
     * @return null if no condition matches.
     */
    public static ConditionalAnimation evaluate(LivingEntity entity) {
        boolean player = entity instanceof Player;
        for (ConditionalAnimation conditional : REGISTRY) {
            if (!player && !conditional.nonPlayers()) continue;
            if (conditional.condition().test(entity)) return conditional;
        }
        return null;
    }
}
