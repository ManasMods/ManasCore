package com.github.manasmods.manascore.tab;

import com.github.manasmods.manascore.ManasCore;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.AllArgsConstructor;
import net.minecraft.resources.ResourceLocation;

@AllArgsConstructor
public enum TabPosition {
    LEFT_TOP(new ResourceLocation(ManasCore.MOD_ID, "textures/gui/tabs/top-left.png")),
    TOP(new ResourceLocation(ManasCore.MOD_ID, "textures/gui/tabs/top.png")),
    RIGHT_TOP(new ResourceLocation(ManasCore.MOD_ID, "textures/gui/tabs/top-right.png")),
    LEFT_BOT(new ResourceLocation(ManasCore.MOD_ID, "textures/gui/tabs/bot-left.png")),
    BOT(new ResourceLocation(ManasCore.MOD_ID, "textures/gui/tabs/bot.png")),
    RIGHT_BOT(new ResourceLocation(ManasCore.MOD_ID, "textures/gui/tabs/bot-right.png"));

    private final ResourceLocation tabLocation;

    public void bindTexture() {
        RenderSystem.setShaderTexture(0, this.tabLocation);
    }
}
