/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.impl.builtin;

import io.github.manasmods.manascore.team.api.*;
import io.github.manasmods.manascore.team.api.template.TeamShape;

/** One party per entity, invite-only, members never hurt or target each other. */
public class PartyTeamType extends TeamType<Team> {
    public TeamShape shape() {
        return TeamShape.GROUP;
    }

    public Class<Team> teamClass() {
        return Team.class;
    }

    public int priority() {
        return 200;
    }

    public int maxMembers() {
        int max = TeamConfig.get().party.maxMembers;
        return max <= 0 ? Integer.MAX_VALUE : max;
    }

    public boolean blocksFriendlyFire() {
        return TeamConfig.get().party.blocksFriendlyFire;
    }

    public boolean blocksTargeting() {
        return TeamConfig.get().party.blocksTargeting;
    }

    public int inviteTimeoutTicks() {
        return TeamConfig.get().party.inviteTimeoutSeconds * 20;
    }
}
