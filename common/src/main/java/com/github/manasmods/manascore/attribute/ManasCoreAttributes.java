package com.github.manasmods.manascore.attribute;

import com.github.manasmods.manascore.ManasCore;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class ManasCoreAttributes {
    public static final RegistrySupplier<RangedAttribute> BLOCK_REACH = ManasCore.REGISTER.attribute("block_reach_addition")
            .withDefaultValue(0)
            .withMinimumValue(0)
            .withMaximumValue(1024)
            .applyTo(() -> EntityType.PLAYER)
            .syncable()
            .end();
    public static final RegistrySupplier<RangedAttribute> CRIT_CHANCE = ManasCore.REGISTER.attribute("crit_chance")
            .withDefaultValue(0)
            .withMinimumValue(0)
            .withMaximumValue(100)
            .applyToAll()
            .syncable()
            .end();
    public static final RegistrySupplier<RangedAttribute> CRIT_MULTIPLIER = ManasCore.REGISTER.attribute("crit_multiplier")
            .withDefaultValue(1.5)
            .withMinimumValue(0)
            .withMaximumValue(1024)
            .applyToAll()
            .syncable()
            .end();
    public static final RegistrySupplier<RangedAttribute> ENTITY_REACH = ManasCore.REGISTER.attribute("entity_reach_addition")
            .withDefaultValue(0)
            .withMinimumValue(0)
            .withMaximumValue(1024)
            .applyTo(() -> EntityType.PLAYER)
            .syncable()
            .end();
    public static final RegistrySupplier<RangedAttribute> JUMP_STRENGTH = ManasCore.REGISTER.attribute("jump_strength")
            .withDefaultValue(0.42)
            .withMinimumValue(0)
            .withMaximumValue(1024)
            .applyToAll()
            .syncable()
            .end();
    public static final RegistrySupplier<RangedAttribute> MINING_SPEED_MULTIPLIER = ManasCore.REGISTER.attribute("mining_speed_multiplier")
            .withDefaultValue(1.0)
            .withMinimumValue(0)
            .withMaximumValue(1024)
            .applyTo(() -> EntityType.PLAYER)
            .syncable()
            .end();
    public static final RegistrySupplier<RangedAttribute> STEP_HEIGHT_ADDITION = ManasCore.REGISTER.attribute("step_height_addition")
            .withDefaultValue(0)
            .withMinimumValue(-1024)
            .withMaximumValue(1024)
            .applyToAll()
            .syncable()
            .end();
    public static final RegistrySupplier<RangedAttribute> SWIM_SPEED = ManasCore.REGISTER.attribute("swim_speed")
            .withDefaultValue(1)
            .withMinimumValue(0)
            .withMaximumValue(1024)
            .applyToAll()
            .syncable()
            .end();

    public static void init() {
    }
}
