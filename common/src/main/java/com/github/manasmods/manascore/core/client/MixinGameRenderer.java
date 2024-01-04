package com.github.manasmods.manascore.core.client;

import com.github.manasmods.manascore.attribute.ManasCoreAttributeUtils;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Shadow @Final Minecraft minecraft;

    @Redirect(method = "pick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;getPickRange()F"))
    private float getPickRange(MultiPlayerGameMode gameMode) {
        Player player = this.minecraft.player;
        if (player == null) return gameMode.getPickRange();

        double reach = 3.0 + ManasCoreAttributeUtils.getEntityReachAddition(player);
        return (float) Math.max(reach, gameMode.getPickRange());
    }

    @Redirect(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasFarPickRange()Z"))
    private boolean hasFarPickRange(MultiPlayerGameMode gameMode) {
        return false;
    }

    @Inject(method = "pick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasFarPickRange()Z", shift = At.Shift.BY, by = 5))
    private void addBlockReachMethod(float partialTicks, CallbackInfo ci,
                                     @Local(ordinal = 0) double d, @Local(ordinal = 0) Vec3 vec3) {
        if (minecraft.gameMode == null) return;
        double blockReach = minecraft.gameMode.getPickRange();

        if (minecraft.hitResult == null) return;
        double e = minecraft.hitResult.getType() != net.minecraft.world.phys.HitResult.Type.MISS ? minecraft.hitResult.getLocation().distanceToSqr(vec3) : d * d;

        if (e > blockReach * blockReach) {
            Vec3 vec31 = this.minecraft.hitResult.getLocation();
            this.minecraft.hitResult = BlockHitResult.miss(vec31, Direction.getNearest(vec3.x, vec3.y, vec3.z), BlockPos.containing(vec31));
        }
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
