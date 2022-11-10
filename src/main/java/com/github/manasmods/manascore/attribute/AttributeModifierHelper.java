/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.attribute;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Optional;
import java.util.UUID;

@UtilityClass
@Log4j2
public class AttributeModifierHelper {
    /**
     * Safe way to apply {@link AttributeModifier} to an {@link LivingEntity}
     *
     * @param entity    Target Entity
     * @param attribute Target Attribute
     * @param modifier  Attribute modifier
     */
    public static void setModifier(final LivingEntity entity, final Attribute attribute, final AttributeModifier modifier) {
        AttributeInstance instance = entity.getAttribute(attribute);

        if (instance == null) {
            log.error("Tried to modify an unknown Attribute: {}", attribute.getRegistryName());
            return;
        }

        double oldMaxHealth = attribute == Attributes.MAX_HEALTH ? entity.getMaxHealth() : 0; // Store old max health or 0
        Optional.ofNullable(instance.getModifier(modifier.getId())) //Get old modifier if present
            .ifPresent(modifier1 -> instance.removeModifier(modifier1.getId())); //Remove old modifier

        instance.addPermanentModifier(modifier); //Add modifier

        if (attribute == Attributes.MAX_HEALTH) {
            double newMaxHealth = entity.getMaxHealth();
            if (newMaxHealth > oldMaxHealth) {
                double healAmount = (newMaxHealth - oldMaxHealth);
                entity.heal((float) healAmount); //Heal entity by the amount of gained health
            } else {
                if (entity.getHealth() > newMaxHealth) {
                    entity.setHealth((float) newMaxHealth); //Reduce the entity health to the new max value
                }
            }
        }
    }

    /**
     * @param entity                Target Entity
     * @param attribute             Target Attribute
     * @param attributeModifierId   A unique id to identify this {@link AttributeModifier}
     * @param attributeModifierName A unique name which is used to store the {@link AttributeModifier}
     * @param amount                Will be used to calculate the final value of the {@link Attribute}
     * @param attributeOperation    Mathematical operation type which is used to calculate the final value of the {@link Attribute}
     */
    public static void addModifier(final LivingEntity entity, final Attribute attribute, final UUID attributeModifierId, final String attributeModifierName, final double amount,
                                   final AttributeModifier.Operation attributeOperation) {
        setModifier(entity, attribute, new AttributeModifier(attributeModifierId, attributeModifierName, amount, attributeOperation));
    }

    /**
     * Safe way to remove {@link AttributeModifier} from Entities
     *
     * @param entity              Target Entity
     * @param attribute           Target Attribute
     * @param attributeModifierId Unique modifier id
     */
    public static void removeModifier(final LivingEntity entity, final Attribute attribute, final UUID attributeModifierId) {
        Optional.ofNullable(entity.getAttribute(attribute)).ifPresent(attributeInstance -> attributeInstance.removeModifier(attributeModifierId));
    }

    /**
     * Safe way to remove {@link AttributeModifier} from Entities
     *
     * @param entity    Target Entity
     * @param attribute Target Attribute
     * @param modifier  Modifier
     */
    public static void removeModifier(final LivingEntity entity, final Attribute attribute, final AttributeModifier modifier) {
        removeModifier(entity, attribute, modifier.getId());
    }
}
