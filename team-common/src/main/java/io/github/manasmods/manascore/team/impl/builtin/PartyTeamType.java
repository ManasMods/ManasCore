/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.impl.builtin;

import io.github.manasmods.manascore.team.api.*;
import io.github.manasmods.manascore.team.api.template.TeamShape;
import net.minecraft.world.entity.LivingEntity;

/** One party per entity, invite-only, members never hurt or target each other. */
public class PartyTeamType extends TeamType<Team> {
    public TeamShape getShape() {
        return TeamShape.GROUP;
    }

    public Class<Team> getTeamClass() {
        return Team.class;
    }

    public int getPriority() {
        return 200;
    }

    public int getMaxMembers() {
        int max = TeamConfig.get().party.maxMembers;
        return max <= 0 ? Integer.MAX_VALUE : max;
    }

    public boolean blocksFriendlyFire() {
        return TeamConfig.get().party.blocksFriendlyFire;
    }

    public boolean blocksTargeting() {
        return TeamConfig.get().party.blocksTargeting;
    }

    public int getInviteTimeoutTicks() {
        return TeamConfig.get().party.inviteTimeoutSeconds * 20;
    }

    public boolean canInvite(Team team, LivingEntity inviter, LivingEntity invitee) {
        return team.isOwner(inviter) || (TeamConfig.get().party.membersCanInvite && team.isMember(inviter));
    }
}
