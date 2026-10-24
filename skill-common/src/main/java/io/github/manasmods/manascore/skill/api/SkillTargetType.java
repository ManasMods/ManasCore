/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.skill.api;

/**
 * SELF: no aim, the user is the target.
 * ENEMY: a hostile entity, usually the attack target of the mob.
 * ALLY: an allied entity.
 * POSITION: a fixed position picked by {@link ManasSkill#getMobTargetPosition}.
 */
public enum SkillTargetType {
    SELF,
    ENEMY,
    ALLY,
    POSITION
}
