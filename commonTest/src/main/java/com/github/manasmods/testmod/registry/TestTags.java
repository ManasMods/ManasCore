package com.github.manasmods.testmod.registry;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.skill.ManasSkill;
import com.github.manasmods.manascore.api.skill.SkillAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class TestTags {
    public static final TagKey<ManasSkill> TEST_SKILL_TAG = TagKey.create(SkillAPI.getSkillRegistryKey(), new ResourceLocation(ManasCore.MOD_ID, "test_skill"));
}
