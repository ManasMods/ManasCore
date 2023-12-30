package com.github.manasmods.manascore.core;

import com.github.manasmods.manascore.attribute.ManasCoreAttributeUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public class MixinServerGamePacketListenerImpl {
    @Shadow public ServerPlayer player;
    @Redirect(method = "handleUseItemOn", at = @At(value = "FIELD",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;MAX_INTERACTION_DISTANCE:D", opcode = Opcodes.GETSTATIC))
    private double getBlockInteractDistance() {
        double reach = ManasCoreAttributeUtils.getReachDistance(this.player) +  3;
        return reach * reach;
    }

    @Redirect(method = "handleInteract", at = @At(value = "FIELD",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;MAX_INTERACTION_DISTANCE:D", opcode = Opcodes.GETSTATIC))
    private double getEntityInteractDistance() {
        double reach = ManasCoreAttributeUtils.getAttackRange(this.player) +  3;
        return reach * reach;
    }
}
