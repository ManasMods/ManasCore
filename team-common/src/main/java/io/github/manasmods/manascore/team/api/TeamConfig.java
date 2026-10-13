/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.api;

import io.github.manasmods.manascore.config.ConfigRegistry;
import io.github.manasmods.manascore.config.api.Comment;
import io.github.manasmods.manascore.config.api.ManasConfig;
import io.github.manasmods.manascore.config.api.ManasSubConfig;
import io.github.manasmods.manascore.config.api.SyncToClient;

/**
 * This is the config of the team module, stored as {@code manascore/team}.
 * It is synced to clients.
 */
@SyncToClient
public class TeamConfig extends ManasConfig {
    public String getFileName() {
        return "manascore/team";
    }

    /**
     * Used to get the loaded {@link TeamConfig}.
     */
    public static TeamConfig get() {
        return ConfigRegistry.getConfig(TeamConfig.class);
    }

    @Comment("Minimum ticks between two client team actions of the same kind per player.")
    public int actionCooldownTicks = 5;
    @Comment("Minimum ticks between two client team queries of the same kind per player.")
    public int queryCooldownTicks = 20;

    @Comment("Settings between an owned entity (pet, summon, minion) and its owner, and between two entities of the same owner.")
    public OwnershipConfig ownership = new OwnershipConfig();
    /**
     * Settings between an owned entity (pet, summon, minion) and its owner, and between two entities of the same owner.
     */
    public static class OwnershipConfig extends ManasSubConfig {
        @Comment("An owner and its owned entities cannot damage each other.")
        public boolean blocksFriendlyFire = false;
        @Comment("An owned entity never targets its owner or its owner's other owned entities.")
        public boolean blocksTargeting = true;
    }

    @Comment("Settings of the built-in ally relation.")
    public AllyConfig ally = new AllyConfig();
    /**
     * Settings of the built-in ally relation.
     */
    public static class AllyConfig extends ManasSubConfig {
        @Comment("Allies cannot damage each other.")
        public boolean blocksFriendlyFire = false;
        @Comment("Mobs never target their allies.")
        public boolean blocksTargeting = true;
    }

    @Comment("Settings of the built-in party team.")
    public PartyConfig party = new PartyConfig();
    /**
     * Settings of the built-in party team.
     */
    public static class PartyConfig extends ManasSubConfig {
        @Comment("Party members cannot damage each other.")
        public boolean blocksFriendlyFire = false;
        @Comment("Mobs never target members of their party.")
        public boolean blocksTargeting = true;
        @Comment("Seconds until a pending party invite expires.")
        public int inviteTimeoutSeconds = 60;
        @Comment("Maximum members per party. 0 or less means unlimited.")
        public int maxMembers = 0;
        @Comment("Any party member may invite. When false only the leader can.")
        public boolean membersCanInvite = false;
    }
}
