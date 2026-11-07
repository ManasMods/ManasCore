/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.race.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.manasmods.manascore.race.api.RaceAPI;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
    @ModifyReturnValue(method = "getDimensions", at = @At("RETURN"))
    private EntityDimensions getRaceDimensions(EntityDimensions original) {
        LivingEntity entity = (LivingEntity) (Object) this;
        Float multiplier = RaceAPI.getRaceFrom(entity).getRace().map(race -> race.getEyeHeightMultiplier(entity)).orElse(null);
        if (multiplier == null) return original;
        return original.withEyeHeight(original.height() * multiplier);
    }
}
