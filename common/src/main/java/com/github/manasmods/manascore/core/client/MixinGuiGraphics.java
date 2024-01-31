package com.github.manasmods.manascore.core.client;

import com.github.manasmods.manascore.api.client.gui.ExtendedGuiGraphics;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(GuiGraphics.class)
public abstract class MixinGuiGraphics implements ExtendedGuiGraphics {
    @Shadow
    public abstract PoseStack pose();

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract int drawString(Font font, FormattedCharSequence text, int x, int y, int color, boolean dropShadow);

    @Override
    public void renderScaledText(float scaling, List<FormattedCharSequence> text, float x, float y, int color, boolean shadow, float spacePerLine) {
        this.pose().scale(scaling, scaling, scaling);

        for (FormattedCharSequence charSequence : text) {
            this.drawString(this.minecraft.font, charSequence, (int) (x / scaling), (int) (y / scaling), color, shadow);
            y += (this.minecraft.font.lineHeight + spacePerLine) * scaling;
        }

        this.pose().scale(1 / scaling, 1 / scaling, 1 / scaling);
    }

    @Override
    public void renderScaledWrappedText(FormattedText text, float x, float y, float width, int color, boolean shadow, float spacePerLine) {
        float scaling = 1F;

        while (true) {
            List<FormattedCharSequence> sequences = this.minecraft.font.split(text, Minecraft.getInstance().getWindow().getScreenWidth());
            float currentScaling = scaling;
            int maxWidth = sequences.stream()
                    .map(seq -> this.minecraft.font.width(seq) * currentScaling)
                    .mapMultiToInt((aFloat, intConsumer) -> intConsumer.accept(Math.round(aFloat)))
                    .max()
                    .orElse(0);
            if (maxWidth > width) {
                scaling -= 0.01F;
            } else {
                break;
            }
        }
        renderScaledText(scaling, this.minecraft.font.split(text, Math.round(width / scaling)), x, y, color, shadow, spacePerLine);
    }

    @Override
    public void renderScaledTextInArea(FormattedText text, float x, float y, float width, float height, int color, boolean shadow, float spacePerLine, float scalingSteps) {
        float scaling = 1F;

        while (true) {
            List<FormattedCharSequence> formattedCharSequences = this.minecraft.font.split(text, Math.round(width / scaling));
            if (formattedCharSequences.size() * (this.minecraft.font.lineHeight + spacePerLine) * scaling > height) {
                scaling -= scalingSteps;
            } else {
                break;
            }
        }

        renderScaledText(scaling, this.minecraft.font.split(text, Math.round(width / scaling)), x, y, color, shadow, spacePerLine);
    }
}
