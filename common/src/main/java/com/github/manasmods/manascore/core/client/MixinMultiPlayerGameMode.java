package com.github.manasmods.manascore.core.client;

import com.github.manasmods.manascore.attribute.ManasCoreAttributeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MixinMultiPlayerGameMode {
    @Shadow @Final private Minecraft minecraft;
    @Inject(method = "getPickRange", at = @At("RETURN"), cancellable = true)
    protected void getPickRange(CallbackInfoReturnable<Float> cir) {
        Player player = this.minecraft.player;
        if (player == null) return;
        cir.setReturnValue(cir.getReturnValue () + (float) ManasCoreAttributeUtils.getBlockReachAddition(player));
    }
}
