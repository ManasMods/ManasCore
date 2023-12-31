package com.github.manasmods.manascore.core.client;

import com.github.manasmods.manascore.attribute.ManasCoreAttributeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Shadow protected int missTime;
    @Shadow @Nullable public HitResult hitResult;

    @Shadow @Nullable public LocalPlayer player;
    @Shadow @Nullable public MultiPlayerGameMode gameMode;

    @Shadow @Nullable public ClientLevel level;
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void startAttack(CallbackInfoReturnable<Boolean> cir) {
        if (this.missTime > 0) return;
        if (this.hitResult == null) return;

        LocalPlayer player = this.player;
        if (player == null) return;
        if (player.isHandsBusy()) return;

        ItemStack itemStack = this.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (level != null && !itemStack.isItemEnabled(level.enabledFeatures())) {
            cir.setReturnValue(false);
            return;
        }

        if(!(this.hitResult instanceof EntityHitResult entityHit)) return;
        if(ManasCoreAttributeUtils.cantHit(player, entityHit.getEntity(), 3)) {
            if (this.gameMode != null && this.gameMode.hasMissTime()) this.missTime = 10;
            this.player.resetAttackStrengthTicker();
            this.player.swing(InteractionHand.MAIN_HAND);
            cir.setReturnValue(false);
        }
    }
}
