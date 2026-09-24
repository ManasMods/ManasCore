/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.impl.builtin;

import io.github.manasmods.manascore.team.api.*;
import io.github.manasmods.manascore.team.api.template.TeamShape;

/**
 * One-way personal alliance. A tamed or owned entity counts as its root owner through the
 * registered {@link OwnerResolver}s.
 */
public class AllyTeamType extends TeamType<Team> {
    public TeamShape getShape() {
        return TeamShape.RELATION;
    }

    public Class<Team> getTeamClass() {
        return Team.class;
    }

    public int getPriority() {
        return 100;
    }

    public boolean blocksFriendlyFire() {
        return TeamConfig.get().ally.blocksFriendlyFire;
    }

    public boolean blocksTargeting() {
        return TeamConfig.get().ally.blocksTargeting;
    }

    public boolean isSymmetricRelation() {
        return false;
    }

    public boolean requiresInvite() {
        return false;
    }
}
