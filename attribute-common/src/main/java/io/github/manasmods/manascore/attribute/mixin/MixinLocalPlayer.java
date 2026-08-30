/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.attribute.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.architectury.networking.NetworkManager;
import io.github.manasmods.manascore.attribute.api.ManasCoreAttributeUtils;
import io.github.manasmods.manascore.attribute.impl.network.c2s.RequestGlideStartPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LocalPlayer.class})
public class MixinLocalPlayer {
    @Inject(method = "aiStep", at = @At(value = "INVOKE_ASSIGN",
            target = "net/minecraft/client/player/LocalPlayer.getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    public void canStartGliding(CallbackInfo cb) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (ManasCoreAttributeUtils.canElytraGlide(player, !player.isFallFlying() && !player.isInLiquid())) {
            player.startFallFlying();
            NetworkManager.sendToServer(new RequestGlideStartPacket());
        }
    }

    @WrapOperation( method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;tryToStartFallFlying()Z"))
    public boolean shouldActivateEquippedElytra(LocalPlayer player, Operation<Boolean> original) {
        if (ManasCoreAttributeUtils.canElytraGlide(player, !player.isFallFlying() && !player.isInLiquid())) return false;
        return original.call(player);
    }
}
