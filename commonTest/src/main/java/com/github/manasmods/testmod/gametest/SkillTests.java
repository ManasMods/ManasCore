package com.github.manasmods.testmod.gametest;

import com.github.manasmods.testmod.registry.RegisterTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class SkillTests {
    public static final String BATCH = "Skill Tests";

    @GameTest(template = "testmod:empty_1x1", batch = BATCH)
    public static void validateRegistration(GameTestHelper helper) {
        helper.assertTrue(RegisterTest.TEST_SKILL.isPresent(), "Test Skill was not registered");
        helper.succeed();
    }
}
