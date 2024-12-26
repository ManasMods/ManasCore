/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.testing.client;

import io.github.manasmods.manascore.keybind.api.KeybindingCategory;
import io.github.manasmods.manascore.keybind.api.KeybindingManager;
import io.github.manasmods.manascore.keybind.api.ManasKeybinding;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import io.github.manasmods.manascore.testing.ManasCoreTesting;

public class KeybindingTest {
    public static void init() {
        KeybindingCategory category = KeybindingCategory.of("testmod.category");
        KeybindingManager.register(
                new ManasKeybinding("manascore.keybinding.test",
                        category, () -> ManasCoreTesting.LOG.info("Pressing"),
                        duration -> ManasCoreTesting.LOG.info("Released in {} Seconds", duration / 1000.0)
                ),
                new ManasKeybinding("manascore.keybinding.test_press", category, () -> ManasCoreTesting.LOG.info("Pressed")),
                new ManasKeybinding("manascore.keybinding.skill", category,
                        () -> SkillAPI.skillActivationPacket(0),
                        duration -> SkillAPI.skillReleasePacket(0, (int) (duration / 50))),
                new ManasKeybinding("manascore.keybinding.skill_2", category,
                        () -> SkillAPI.skillActivationPacket(1),
                        duration -> SkillAPI.skillReleasePacket(1, (int) (duration / 50))),
                new ManasKeybinding("manascore.keybinding.skill_toggle", category, SkillAPI::skillTogglePacket)
        );
    }
}
