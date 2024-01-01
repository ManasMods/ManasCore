package com.github.manasmods.manascore.core.client;

import com.github.manasmods.manascore.attribute.ManasCoreAttributeUtils;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Shadow @Final Minecraft minecraft;
    @ModifyArg(method = "pick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/phys/AABB;expandTowards(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/AABB;"), index = 0)
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
            target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"),
            index = 2)
    private Vec3 getReacgDistance(Vec3 startVec, @Local(ordinal = 0) double d,
                                  @Local(ordinal = 0) Vec3 vec3, @Local(ordinal = 1) Vec3 vec32) {
        Player player = this.minecraft.player;
        if (player == null) return startVec;

        double reach = ManasCoreAttributeUtils.getEntityReachAddition(minecraft.player);
        if (minecraft.gameMode != null && minecraft.gameMode.hasFarPickRange() && minecraft.hitResult == null) {
            double newD = d + reach;
            return vec3.add(vec32.x * newD, vec32.y * newD, vec32.z * newD);
        }
        return startVec;
    }

    @ModifyArg(method = "pick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"), index = 5)
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
            target = "Lnet/minecraft/world/phys/Vec3;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D", ordinal = 1))
    private double getEntityReachDistance(Vec3 instance, Vec3 vec) {
        Player player = this.minecraft.player;
        if (player == null) return instance.distanceToSqr(vec);
        double reach = ManasCoreAttributeUtils.getEntityReachAddition(player);
        double reachSquared = reach * reach * (reach < 0 ? -1 : 1);
        return instance.distanceToSqr(vec) - reachSquared;
    }
}
