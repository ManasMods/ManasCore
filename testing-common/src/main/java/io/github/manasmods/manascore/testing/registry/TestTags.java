/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.testing.registry;

import io.github.manasmods.manascore.skill.api.ManasSkill;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import io.github.manasmods.manascore.testing.ModuleConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class TestTags {
    public static final TagKey<ManasSkill> TEST_SKILL_TAG = TagKey.create(SkillAPI.getSkillRegistryKey(), ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "test_skill"));
}
