package io.github.manasmods.manascore.skill.impl.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class ManascoreEntityTags {
    public static TagKey<EntityType<?>> SKILL_COOLDOWN_ALLOWED = modTag("skill_cooldown_allowed");

    static TagKey<EntityType<?>> modTag(String name) {
        return create(ResourceLocation.fromNamespaceAndPath("manascore", name));
    }
    static TagKey<EntityType<?>> create(final ResourceLocation name) {
        return TagKey.create(Registries.ENTITY_TYPE, name);
    }
}