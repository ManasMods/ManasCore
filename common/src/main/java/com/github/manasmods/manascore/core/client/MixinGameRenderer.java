package com.github.manasmods.manascore.core.client;

import com.github.manasmods.manascore.attribute.ManasCoreAttributeUtils;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Shadow @Final Minecraft minecraft;
    @Inject(method = "pick", at = @At(value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;", shift = At.Shift.AFTER))
    private void getReachDistance(float partialTicks, CallbackInfo ci, @Local(ordinal = 0) double d, @Local(ordinal = 0) Vec3 vec3,
                                  @Local(ordinal = 1) Vec3 vec32, @Local(ordinal = 2) LocalRef<Vec3> vec33) {
        if (minecraft.player == null) return;
        double reach = ManasCoreAttributeUtils.getEntityReachAddition(minecraft.player);
        if (minecraft.gameMode != null && minecraft.gameMode.hasFarPickRange()) {
            double newD = d + reach;
            vec33.set(vec3.add(vec32.x * newD, vec32.y * newD, vec32.z * newD));
        }
    }

    @ModifyArg(method = "pick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/phys/AABB;expandTowards(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/AABB;",
            opcode = Opcodes.GETSTATIC), index = 0)
    private Vec3 getEntityReachDistance(Vec3 vector, @Local(ordinal = 0) double d) {
        Player player = this.minecraft.player;
        if (player == null) return vector;

        double reach = ManasCoreAttributeUtils.getEntityReachAddition(minecraft.player);
        if (minecraft.gameMode != null && minecraft.gameMode.hasFarPickRange() && minecraft.hitResult == null) {
            double newD = d + reach;
            return vector.scale(newD);
        }
        return vector;
    }

    @ModifyArg(method = "pick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;",
            opcode = Opcodes.GETSTATIC), index = 5)
    private double getEntityDistance(double distance, @Local(ordinal = 0) double d) {
        Player player = this.minecraft.player;
        if (player == null) return distance;

        double reach = ManasCoreAttributeUtils.getEntityReachAddition(minecraft.player);
        if (minecraft.gameMode != null && minecraft.gameMode.hasFarPickRange() && minecraft.hitResult == null) {
            double newD = d + reach;
            return newD * newD;
        }
        return distance;
    }

    @Redirect(method = "pick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D", ordinal = 1, opcode = Opcodes.GETSTATIC))
    private double getEntityReachDistance(Vec3 instance, Vec3 vec) {
        Player player = this.minecraft.player;
        if (player == null) return instance.distanceToSqr(vec);

        double reach = ManasCoreAttributeUtils.getEntityReachAddition(player);
        return instance.distanceToSqr(vec) - reach * reach;
    }
}
