package com.github.manasmods.testmod.client;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.client.keybinding.KeybindingCategory;
import com.github.manasmods.manascore.api.client.keybinding.ManasKeybinding;
import com.github.manasmods.manascore.api.skill.SkillAPI;
import com.github.manasmods.manascore.client.keybinding.KeybindingManager;

public class KeybindingTest {
    public static void init() {
        KeybindingCategory category = KeybindingCategory.of("testmod.category");
        KeybindingManager.register(
                new ManasKeybinding("manascore.keybinding.test",
                        category, () -> ManasCore.Logger.info("Pressing"),
                        duration -> ManasCore.Logger.info("Released in {} Seconds", duration / 1000.0)
                ),
                new ManasKeybinding("manascore.keybinding.test_press", category, () -> ManasCore.Logger.info("Pressed")),
                new ManasKeybinding("manascore.keybinding.skill", category,
                        () -> SkillAPI.skillActivationPacket(0),
                        duration -> SkillAPI.skillReleasePacket(0, (int) (duration / 50))),
                new ManasKeybinding("manascore.keybinding.skill_toggle", category, SkillAPI::skillTogglePacket)
        );
    }
}
