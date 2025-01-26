/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.attribute.mixin;

import io.github.manasmods.manascore.attribute.api.ManasCoreAttributeUtils;
import io.github.manasmods.manascore.attribute.api.ManasCoreAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
    public MixinLivingEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyArg(method = "updateFallFlying", at = @At(value = "INVOKE",
            target = "net/minecraft/world/entity/LivingEntity.setSharedFlag(IZ)V"))
    private boolean updateFallFlying(boolean value) {
        return ManasCoreAttributeUtils.canElytraGlide((LivingEntity) (Object) this, this.getSharedFlag(7));
    }

    @ModifyArg(method = "travel", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", ordinal = 2), index = 1)
    public Vec3 glideSpeed(Vec3 vec3) {
        LivingEntity entity = (LivingEntity) (Object) this;
        AttributeInstance instance = entity.getAttribute(ManasCoreAttributes.GLIDE_SPEED_MULTIPLIER);
        if (instance == null || instance.getValue() <= 0) return vec3;
        return vec3.multiply(instance.getValue(), 1, instance.getValue());
    }

    @ModifyArg(method = "travel", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V", ordinal = 1))
    public float lavaSpeed(float speed) {
        LivingEntity entity = (LivingEntity) (Object) this;
        AttributeInstance instance = entity.getAttribute(ManasCoreAttributes.LAVA_SPEED_MULTIPLIER);
        if (instance == null) return speed;
        return (float) (speed * instance.getValue());
    }

    @ModifyArg(method = "travel", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V", ordinal = 0))
    public float swimSpeed(float speed) {
        LivingEntity entity = (LivingEntity) (Object) this;
        AttributeInstance instance = entity.getAttribute(ManasCoreAttributes.SWIM_SPEED_MULTIPLIER);
        if (instance == null) return speed;
        return (float) (speed * instance.getValue());
    }
}
