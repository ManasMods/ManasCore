package com.github.manasmods.manascore.core.client;

import com.github.manasmods.manascore.attribute.ManasCoreAttributeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Gui.class)
public class MixinGui {
    @Shadow @Final
    private Minecraft minecraft;
    @Redirect(method = "renderCrosshair", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;isAlive()Z"))
    private boolean getEntityReachtDistance(Entity instance) {
        Player player = this.minecraft.player;
        if (player == null) return instance.isAlive();
        if (minecraft.crosshairPickEntity == null) return instance.isAlive();
        return instance.isAlive() && ManasCoreAttributeUtils.cantHit(player, minecraft.crosshairPickEntity, 3);
    }
}
