package com.github.manasmods.manascore.core;

import com.github.manasmods.manascore.attribute.ManasCoreAttributeUtils;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class MixinItem {
    @Inject(method = "getPlayerPOVHitResult", at = @At(value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;", shift = At.Shift.AFTER))
    private static void getReachDistance(Level level, Player player, ClipContext.Fluid fluidMode, CallbackInfoReturnable<BlockHitResult> cir,
                                         @Local(ordinal = 6) float l, @Local(ordinal = 5) float k, @Local(ordinal = 7) float n, @Local(ordinal = 1) LocalRef<Vec3> vec32) {
        final double reach = ManasCoreAttributeUtils.getBlockReachAddition(player);
        vec32.set(vec32.get().add(l * reach, k * reach, n * reach));
    }
}
