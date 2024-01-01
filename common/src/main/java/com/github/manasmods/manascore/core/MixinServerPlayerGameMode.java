package com.github.manasmods.manascore.core;

import com.github.manasmods.manascore.attribute.ManasCoreAttributeUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerGameMode.class)
public class MixinServerPlayerGameMode {
    @Shadow @Final protected ServerPlayer player;
    @Redirect(method = "handleBlockBreakAction", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D"))
    private double getBlockInteractDistancee(Vec3 instance, Vec3 vec) {
        double reach = ManasCoreAttributeUtils.getBlockReachAddition(player);
        double reachSquared = reach * reach * (reach < 0 ? -1 : 1);
        return instance.distanceToSqr(vec) - reachSquared;
    }
}
