/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.skill.impl.SkillRegistry;
import io.github.manasmods.manascore.skill.impl.SkillStorage;
import io.github.manasmods.manascore.skill.impl.network.ManasSkillNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ManasCoreSkill {
    public static final Logger LOG = LoggerFactory.getLogger("ManasCore - Skill");

    public static void init() {
        SkillStorage.init();
        SkillRegistry.init();
        ManasSkillNetwork.init();
        if (Platform.getEnvironment() == Env.CLIENT) {
            ManasCoreSkillClient.init();
        }
    }
}
