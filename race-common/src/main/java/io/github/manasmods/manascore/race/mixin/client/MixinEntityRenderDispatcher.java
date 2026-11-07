/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.race.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.manasmods.manascore.race.api.RaceAPI;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderDispatcher.class)
public abstract class MixinEntityRenderDispatcher {
    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;getShadowRadius(Lnet/minecraft/world/entity/Entity;)F"))
    private float getRaceShadowRadius(EntityRenderer<Entity> renderer, Entity entity, Operation<Float> original) {
        Float multiplier = entity instanceof LivingEntity living ? RaceAPI.getRaceFrom(living).getRace().map(race -> race.getShadowMultiplier(living)).orElse(null) : null;
        if (multiplier == null) return original.call(renderer, entity);
        return entity.getBbWidth() * multiplier;
    }
}
