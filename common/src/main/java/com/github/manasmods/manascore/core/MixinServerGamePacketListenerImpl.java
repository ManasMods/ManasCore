package com.github.manasmods.manascore.core;

import com.github.manasmods.manascore.attribute.ManasCoreAttributeUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public class MixinServerGamePacketListenerImpl {
    @Shadow public ServerPlayer player;
    @Redirect(method = "handleUseItemOn", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D"))
    private double getBlockInteractDistancee(Vec3 instance, Vec3 vec) {
        double reach = ManasCoreAttributeUtils.getBlockReachAddition(player);
        double reachSquared = reach * reach * (reach < 0 ? -1 : 1);
        return instance.distanceToSqr(vec) - reachSquared;
    }

    @Redirect(method = "handleInteract", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/phys/AABB;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D"))
    private double getEntityInteractDistance(AABB instance, Vec3 vec) {
        double reach = ManasCoreAttributeUtils.getEntityReachAddition(player);
        double reachSquared = reach * reach * (reach < 0 ? -1 : 1);
        return instance.distanceToSqr(vec) - reachSquared;
    }
}
