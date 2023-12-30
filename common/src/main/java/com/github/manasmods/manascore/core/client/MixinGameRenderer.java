package com.github.manasmods.manascore.core.client;

import com.github.manasmods.manascore.attribute.ManasCoreAttributeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Shadow @Final Minecraft minecraft;
    @ModifyConstant(method = "pick", require = 1, allow = 1, constant = @Constant(doubleValue = 6.0))
    private double getReachDistance(double reachDistance) {
        Player player = this.minecraft.player;
        if (player == null) return reachDistance;
        return ManasCoreAttributeUtils.getReachDistance(player);
    }

    @ModifyConstant(method = "pick", constant = @Constant(doubleValue = 9.0))
    private double getAttackRange(double attackRange) {
        Player player = this.minecraft.player;
        if (player == null) return attackRange;
        double range = ManasCoreAttributeUtils.getAttackRange(player);
        return range * range;
    }
}
