/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.attribute.api;

import io.github.manasmods.manascore.attribute.ManasCoreAttributeRegister;
import io.github.manasmods.manascore.attribute.ModuleConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class ManasCoreAttributes {
    /**
     * Determine how much the output damage is multiplied when the attacker does a critical attack.
     */
    public static final Holder<Attribute> CRITICAL_DAMAGE_MULTIPLIER = registerGenericAttribute(ModuleConstants.MOD_ID,
            "critical_damage_multiplier", "manascore.attribute.critical_damage_multiplier",
            1.5, 0, 1024, true, Attribute.Sentiment.POSITIVE);

    /**
     * Determine the percentage chance for the user to do a critical attack without jumping.
     */
    public static final Holder<Attribute> CRITICAL_ATTACK_CHANCE = registerGenericAttribute(ModuleConstants.MOD_ID,
            "critical_attack_chance", "manascore.attribute.critical_attack_chance",
            0, 0, 100, true, Attribute.Sentiment.POSITIVE);

    /**
     * Determine how fast the player can elytra glide without wearing an elytra.
     * Sets higher than 0 to allow the player to glide.
     * This also affects the speed of normal Elytra gliding speed.
     */
    public static final Holder<Attribute> GLIDE_SPEED_MULTIPLIER = registerPlayerAttribute(ModuleConstants.MOD_ID,
            "glide_speed_multiplier", "manascore.attribute.glide_speed_multiplier",
            0, 0, 1024, true, Attribute.Sentiment.POSITIVE);

    /**
     * Determine how fast the player can go inside Lava.
     */
    public static final Holder<Attribute> LAVA_SPEED_MULTIPLIER = registerGenericAttribute(ModuleConstants.MOD_ID,
            "lava_speed_multiplier", "manascore.attribute.lava_speed_multiplier",
            1, 0, 1024, true, Attribute.Sentiment.POSITIVE);

    /**
     * Determine how fast the player can swim in Water.
     * Similar to NeoForge/Forge's Swim speed instead of Vanilla's Water Movement Efficiency.
     */
    public static final Holder<Attribute> SWIM_SPEED_MULTIPLIER = registerGenericAttribute(ModuleConstants.MOD_ID,
            "swim_speed_multiplier", "manascore.attribute.swim_speed_multiplier",
            1, 0, 1024, true, Attribute.Sentiment.POSITIVE);

    public static Holder<Attribute> registerPlayerAttribute(String modID, String id, String name, double amount,
                                                            double min, double max, boolean syncable, Attribute.Sentiment sentiment) {
        Holder<Attribute> attribute = Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, getResourceKey(modID, id),
                new RangedAttribute(name, amount, min, max).setSyncable(syncable).setSentiment(sentiment));
        ManasCoreAttributeRegister.registerToPlayers(attribute);
        return attribute;
    }

    public static Holder<Attribute> registerGenericAttribute(String modID, String id, String name, double amount,
                                                            double min, double max, boolean syncable, Attribute.Sentiment sentiment) {
        Holder<Attribute> attribute = Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, getResourceKey(modID, id),
                new RangedAttribute(name, amount, min, max).setSyncable(syncable).setSentiment(sentiment));
        ManasCoreAttributeRegister.registerToGeneric(attribute);
        return attribute;
    }

    public static ResourceKey<Attribute> getResourceKey(String modID, String path) {
        return ResourceKey.create(Registries.ATTRIBUTE, ResourceLocation.fromNamespaceAndPath(modID, path));
    }
}
