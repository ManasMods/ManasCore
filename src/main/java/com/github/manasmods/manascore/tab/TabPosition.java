package com.github.manasmods.manascore.tab;

import com.github.manasmods.manascore.ManasCore;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

@AllArgsConstructor
public enum TabPosition {
    LEFT_TOP(new ResourceLocation(ManasCore.MOD_ID, "textures/gui/tabs/top-left.png"), false),
    TOP(new ResourceLocation(ManasCore.MOD_ID, "textures/gui/tabs/top.png"), false),
    RIGHT_TOP(new ResourceLocation(ManasCore.MOD_ID, "textures/gui/tabs/top-right.png"), false),
    LEFT_BOT(new ResourceLocation(ManasCore.MOD_ID, "textures/gui/tabs/bot-left.png"), true),
    BOT(new ResourceLocation(ManasCore.MOD_ID, "textures/gui/tabs/bot.png"), true),
    RIGHT_BOT(new ResourceLocation(ManasCore.MOD_ID, "textures/gui/tabs/bot-right.png"), true);

    private final ResourceLocation tabLocation;
    @Getter
    private final boolean bottom;

    public void bindTexture() {
        RenderSystem.setShaderTexture(0, this.tabLocation);
    }

    public boolean isTop() {
        return !this.bottom;
    }
}
