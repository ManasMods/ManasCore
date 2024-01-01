package com.github.manasmods.testmod.client;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.client.keybinding.KeybindingCategory;
import com.github.manasmods.manascore.api.client.keybinding.KeybindingEvents;
import com.github.manasmods.manascore.api.client.keybinding.ManasKeybinding;
import com.github.manasmods.manascore.api.skill.SkillAPI;

public class KeybindingTest {
    public static void init() {
        KeybindingEvents.REGISTER.register(registry -> {
            KeybindingCategory category = KeybindingCategory.of("testmod.category");

            registry.create(new ManasKeybinding("manascore.keybinding.test",
                    category, () -> ManasCore.Logger.info("Pressing"),
                    duration -> ManasCore.Logger.info("Released in {} Seconds", duration / 1000.0)
            ));

            registry.create(new ManasKeybinding("manascore.keybinding.test_press", category, () -> ManasCore.Logger.info("Pressed")));

            registry.create(new ManasKeybinding("manascore.keybinding.skill", category,
                    () -> SkillAPI.skillActivationPacket(0),
                    duration -> SkillAPI.skillReleasePacket(0, (int) (duration / 50))
            ));
            registry.create(new ManasKeybinding("manascore.keybinding.skill_toggle", category, SkillAPI::skillTogglePacket));
        });
    }
}
