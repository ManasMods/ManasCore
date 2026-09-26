/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.testing.registry;

import io.github.manasmods.manascore.attribute.ManasCoreAttributeRegister;
import io.github.manasmods.manascore.testing.ModuleConstants;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class TestAttributeRegistry {
    public static final Holder<Attribute> TEST_ATTRIBUTE_PLAYER = ManasCoreAttributeRegister.registerPlayerAttribute(ModuleConstants.MOD_ID,
            "test_attribute_player", "manascore.attribute.test_attribute_player",69, 0, 420, true, Attribute.Sentiment.NEUTRAL);
    public static final Holder<Attribute> TEST_ATTRIBUTE_ALL = ManasCoreAttributeRegister.registerGenericAttribute(ModuleConstants.MOD_ID,
            "test_attribute_all", "manascore.attribute.test_attribute_all", 420, 69, 4200, true, Attribute.Sentiment.NEGATIVE);

    public static void init() {
    }
}
