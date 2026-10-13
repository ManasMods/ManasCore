/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.attribute;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jetbrains.annotations.NotNull;

public class ManasCoreAttributeRegister {
    /**
     * Registers a player-specific attribute with the given parameters.
     *
     * @param modID      The mod ID associated with this attribute.
     * @param id         The unique identifier for the attribute.
     * @param name       The display name of the attribute.
     * @param amount     The default base value of the attribute.
     * @param min        The minimum allowed value for the attribute.
     * @param max        The maximum allowed value for the attribute.
     * @param syncable   Whether the attribute should be synchronized between client and server.
     * @param sentiment  The sentiment classification of the attribute (e.g., beneficial or harmful).
     * @return A {@link Holder} containing the registered player attribute.
     */
    @ExpectPlatform
    public static @NotNull Holder<Attribute> registerPlayerAttribute(String modID, String id, String name, double amount,
                                                                     double min, double max, boolean syncable, Attribute.Sentiment sentiment) {
        throw new AssertionError();
    }

    /**
     * Registers a generic attribute that applies to multiple entity types.
     *
     * @param modID      The mod ID associated with this attribute.
     * @param id         The unique identifier for the attribute.
     * @param name       The display name of the attribute.
     * @param amount     The default base value of the attribute.
     * @param min        The minimum allowed value for the attribute.
     * @param max        The maximum allowed value for the attribute.
     * @param syncable   Whether the attribute should be synchronized between client and server.
     * @param sentiment  The sentiment classification of the attribute (e.g., beneficial or harmful).
     * @return A {@link Holder} containing the registered generic attribute.
     */
    @ExpectPlatform
    public static @NotNull Holder<Attribute> registerGenericAttribute(String modID, String id, String name, double amount,
                                                                      double min, double max, boolean syncable, Attribute.Sentiment sentiment) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void init() {
        throw new AssertionError();
    }
}
