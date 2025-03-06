/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.testing.client;

import io.github.manasmods.manascore.keybind.api.KeybindingCategory;
import io.github.manasmods.manascore.keybind.api.KeybindingManager;
import io.github.manasmods.manascore.keybind.api.ManasKeybinding;
import io.github.manasmods.manascore.race.api.RaceAPI;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import io.github.manasmods.manascore.testing.ManasCoreTesting;
import io.github.manasmods.manascore.testing.ModuleConstants;
import net.minecraft.resources.ResourceLocation;

public class KeybindingTest {
    public static void init() {
        KeybindingCategory category = KeybindingCategory.of("test.category");
        KeybindingManager.register(
                new ManasKeybinding("manascore.keybinding.test",
                        category, () -> {
                    ManasCoreTesting.LOG.info("Pressing");
                    RaceAPI.raceAbilityActivationPacket();
                }, duration -> ManasCoreTesting.LOG.info("Released in {} Seconds", duration / 1000.0)
                ),
                new ManasKeybinding("manascore.keybinding.test_press", category, () -> {
                    ManasCoreTesting.LOG.info("Pressed");
                    RaceAPI.raceEvolutionPacket(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "test_race_evolved"));
                }),
                new ManasKeybinding("manascore.keybinding.skill", category,
                        () -> SkillAPI.skillActivationPacket(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID,
                                "test_skill"), 0, 0),
                        duration -> SkillAPI.skillReleasePacket(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID,
                                "test_skill"), 0, 0, (int) (duration / 50))),
                new ManasKeybinding("manascore.keybinding.skill_2", category,
                        () -> SkillAPI.skillActivationPacket(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID,
                                "test_skill"), 1, 1),
                        duration -> SkillAPI.skillReleasePacket(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID,
                                "test_skill"), 1, 1, (int) (duration / 50))),
                new ManasKeybinding("manascore.keybinding.skill_toggle", category,
                        () -> SkillAPI.skillTogglePacket(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "test_skill")))
        );
    }
}
