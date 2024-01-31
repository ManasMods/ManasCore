package com.github.manasmods.manascore.core.client;

import com.github.manasmods.manascore.api.client.gui.ExtendedGuiGraphics;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(GuiGraphics.class)
public abstract class MixinGuiGraphics implements ExtendedGuiGraphics {
    @Shadow public abstract PoseStack pose();

    @Shadow @Final private Minecraft minecraft;

    @Shadow public abstract int drawString(Font font, FormattedCharSequence text, int x, int y, int color, boolean dropShadow);

    @Override
    public void renderScaledText(float scaling, List<FormattedCharSequence> text, float x, float y, int color, boolean shadow, float spacePerLine) {
        this.pose().scale(scaling, scaling, scaling);

        for (FormattedCharSequence charSequence : text) {
            this.drawString(this.minecraft.font, charSequence, (int) (x / scaling), (int) (y / scaling), color, shadow);
            y += (this.minecraft.font.lineHeight + spacePerLine) * scaling;
        }

        this.pose().scale(1 / scaling, 1 / scaling, 1 / scaling);
    }
}
