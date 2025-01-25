/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.attribute;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class ManasCoreAttributeRegister {
    @ExpectPlatform
    public static void registerToPlayers(Holder<Attribute> holder) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerToGeneric(Holder<Attribute> holder) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void init() {
        throw new AssertionError();
    }
}
