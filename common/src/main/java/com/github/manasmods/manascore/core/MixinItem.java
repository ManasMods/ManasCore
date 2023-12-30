package com.github.manasmods.manascore.core;

import com.github.manasmods.manascore.attribute.ManasCoreAttributeUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Item.class)
public class MixinItem {
    @ModifyConstant(method = "getPlayerPOVHitResult(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/ClipContext$Fluid;)Lnet/minecraft/world/phys/BlockHitResult;",
            require = 4, allow = 4, constant = @Constant(doubleValue = 5.0))
    private static double getReachDistance(double reachDistance, Level level, Player player) {
        return ManasCoreAttributeUtils.getReachDistance(player);
    }
}
