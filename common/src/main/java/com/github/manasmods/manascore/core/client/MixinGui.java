package com.github.manasmods.manascore.core.client;

import com.github.manasmods.manascore.attribute.ManasCoreAttributeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Gui.class)
public class MixinGui {
    @Shadow @Final
    private Minecraft minecraft;
    @ModifyConstant(method = "renderCrosshair", require = 2, allow = 1, constant = @Constant(floatValue = 1.0F))
    private float getActualReachDistance(float constant) {
        Player player = minecraft.player;
        if (player == null) return constant;
        if (minecraft.crosshairPickEntity == null) return constant;

        if (ManasCoreAttributeUtils.cantHit(player, minecraft.crosshairPickEntity, 0)) return 2.0F;
        return constant;
    }
}
