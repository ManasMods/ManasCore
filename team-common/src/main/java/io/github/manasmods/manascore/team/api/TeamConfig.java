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

@SyncToClient
public class TeamConfig extends ManasConfig {
    public String getFileName() {
        return "manascore/team";
    }

    public static TeamConfig get() {
        return ConfigRegistry.getConfig(TeamConfig.class);
    }

    @Comment("Settings of the built-in ally relation.")
    public AllyConfig ally = new AllyConfig();
    public static class AllyConfig extends ManasSubConfig {
        @Comment("Allies cannot damage each other.")
        public boolean blocksFriendlyFire = false;
        @Comment("Mobs never target their allies.")
        public boolean blocksTargeting = true;
    }

    @Comment("Settings of the built-in party team.")
    public PartyConfig party = new PartyConfig();
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
