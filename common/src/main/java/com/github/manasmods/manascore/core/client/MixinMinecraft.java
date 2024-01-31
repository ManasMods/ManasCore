package com.github.manasmods.manascore.core.client;

import com.github.manasmods.manascore.attribute.ManasCoreAttributes;
import com.github.manasmods.manascore.network.NetworkManager;
import com.github.manasmods.manascore.network.toserver.RequestSweepChancePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Shadow @Nullable public LocalPlayer player;
    @Inject(method = "startAttack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;resetAttackStrengthTicker()V", shift = At.Shift.BEFORE))
    private void addSweepChance(CallbackInfoReturnable<Boolean> cir) {
        if (player == null) return;
        AttributeInstance instance = player.getAttribute(ManasCoreAttributes.SWEEP_CHANCE.get());
        if (instance == null || player.getRandom().nextInt(100) > instance.getValue()) return;
        NetworkManager.CHANNEL.sendToServer(new RequestSweepChancePacket());
    }
}
