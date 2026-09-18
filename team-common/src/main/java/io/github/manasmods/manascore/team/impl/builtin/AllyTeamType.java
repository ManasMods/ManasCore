/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.impl.builtin;

import io.github.manasmods.manascore.team.api.*;
import io.github.manasmods.manascore.team.api.template.TeamShape;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;

/**
 * Symmetric pairwise alliance. A tamed or owned entity counts as its owner
 * (owner chain followed up to four steps).
 */
public class AllyTeamType extends TeamType<Team> {
    private static final int MAX_OWNER_DEPTH = 4;

    public TeamShape shape() {
        return TeamShape.RELATION;
    }

    public Class<Team> teamClass() {
        return Team.class;
    }

    public int priority() {
        return 100;
    }

    public boolean blocksFriendlyFire() {
        return TeamConfig.get().ally.blocksFriendlyFire;
    }

    public boolean blocksTargeting() {
        return TeamConfig.get().ally.blocksTargeting;
    }

    public int inviteTimeoutTicks() {
        return TeamConfig.get().ally.inviteTimeoutSeconds * 20;
    }

    public LivingEntity resolveMember(LivingEntity entity) {
        LivingEntity current = entity;
        for (int i = 0; i < MAX_OWNER_DEPTH; i++) {
            if (!(current instanceof OwnableEntity ownable)) break;
            LivingEntity owner = ownable.getOwner();
            if (owner == null || owner == current) break;
            current = owner;
        }
        return current;
    }
}
