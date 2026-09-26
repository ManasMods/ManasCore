/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.race.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerList.class)
public abstract class MixinServerPlayer {
    @WrapOperation(method = "respawn", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V"))
    private void restoreFrom(ServerPlayer instance, ServerPlayer oldPlayer, boolean endConquered, Operation<Void> original) {
        if (!endConquered) original.call(instance, oldPlayer, endConquered);
        else {
            original.call(instance, oldPlayer, endConquered);
            instance.getAttributes().assignAllValues(oldPlayer.getAttributes());
            instance.setHealth(oldPlayer.getHealth());
        }
    }
}
