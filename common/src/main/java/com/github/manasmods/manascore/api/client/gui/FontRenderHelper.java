package com.github.manasmods.manascore.api.client.gui;

import lombok.experimental.UtilityClass;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.awt.*;
import java.util.List;

@UtilityClass
@Environment(EnvType.CLIENT)
public final class FontRenderHelper {

    public static void renderScaledTextInArea(final GuiGraphics graphics, final Font font, final FormattedText text,
                                              final float x, float y, final float width, final float height, final Color color, final boolean shadow) {
        renderScaledTextInArea(graphics, font, text, x, y, width, height, color, shadow, 0F);
    }

    public static void renderScaledTextInArea(final GuiGraphics graphics, final Font font, final FormattedText text,
                                              final float x, float y, final float width, final float height, final int color, final boolean shadow) {
        renderScaledTextInArea(graphics, font, text, x, y, width, height, color, shadow, 0F);
    }

    public static void renderScaledTextInArea(final GuiGraphics posgraphicsStack, final Font font, final FormattedText text,
                                              final float x, float y, final float width, final float height, final Color color, final boolean shadow,
                                              final float spacePerLine) {
        renderScaledTextInArea(posgraphicsStack, font, text, x, y, width, height, color, shadow, spacePerLine, 0.01F);
    }

    public static void renderScaledTextInArea(final GuiGraphics graphics, final Font font, final FormattedText text,
                                              final float x, float y, final float width, final float height, final int color, final boolean shadow,
                                              final float spacePerLine) {
        renderScaledTextInArea(graphics, font, text, x, y, width, height, color, shadow, spacePerLine, 0.01F);
    }

    public static void renderScaledTextInArea(final GuiGraphics graphics, final Font font, final FormattedText text,
                                              final float x, float y, final float width, final float height, final Color color, final boolean shadow,
                                              final float spacePerLine, final float scalingSteps) {
        renderScaledTextInArea(graphics, font, text, x, y, width, height, color.getRGB(), shadow, spacePerLine, scalingSteps);
    }

    public static void renderScaledTextInArea(final GuiGraphics graphics, final Font font, final FormattedText text,
                                              final float x, float y, final float width, final float height, final int color, final boolean shadow,
                                              final float spacePerLine, final float scalingSteps) {
        float scaling = 1F;

        while (true) {
            List<FormattedCharSequence> formattedCharSequences = font.split(text, Math.round(width / scaling));
            if (formattedCharSequences.size() * (font.lineHeight + spacePerLine) * scaling > height) {
                scaling -= scalingSteps;
            } else {
                break;
            }
        }

        renderScaledText(graphics, font, scaling, font.split(text, Math.round(width / scaling)), x, y, color, shadow, spacePerLine);
    }

    public static void renderScaledWrappedText(final GuiGraphics graphics, final Font font,
                                               final FormattedText text, final float x, float y, final float width, final Color color, final boolean shadow) {
        renderScaledWrappedText(graphics, font, text, x, y, width, color, shadow, 0);
    }

    public static void renderScaledWrappedText(final GuiGraphics graphics, final Font font,
                                               final FormattedText text, final float x, float y, final float width, final int color, final boolean shadow) {
        renderScaledWrappedText(graphics, font, text, x, y, width, color, shadow, 0);
    }

    public static void renderScaledWrappedText(final GuiGraphics graphics, final Font font, final FormattedText text,
                                               final float x, float y, final float width, final Color color, final boolean shadow, final float spacePerLine) {
        renderScaledWrappedText(graphics, font, text, x, y, width, color.getRGB(), shadow, spacePerLine);
    }

    public static void renderScaledWrappedText(final GuiGraphics graphics, final Font font, final FormattedText text,
                                               final float x, float y, final float width, final int color, final boolean shadow, final float spacePerLine) {
        float scaling = 1F;

        while (true) {
            List<FormattedCharSequence> sequences = font.split(text, Minecraft.getInstance().getWindow().getScreenWidth());
            float currentScaling = scaling;
            int maxWidth = sequences.stream()
                    .map(seq -> font.width(seq) * currentScaling)
                    .mapMultiToInt((aFloat, intConsumer) -> intConsumer.accept(Math.round(aFloat)))
                    .max()
                    .orElse(0);
            if (maxWidth > width) {
                scaling -= 0.01F;
            } else {
                break;
            }
        }

        renderScaledText(graphics, font, scaling, font.split(text, Math.round(width / scaling)),
                x, y, color, shadow, spacePerLine);
    }

    private static void renderScaledText(final GuiGraphics graphics, final Font font, final float scaling,
                                         final List<FormattedCharSequence> text, final float x, float y,
                                         final int color, final boolean shadow, final float spacePerLine) {
        graphics.pose().scale(scaling, scaling, scaling);

        for (FormattedCharSequence charSequence : text) {
            graphics.drawString(font, charSequence, (int) (x / scaling), (int) (y / scaling), color, shadow);
            y += (font.lineHeight + spacePerLine) * scaling;
        }

        graphics.pose().scale(1 / scaling, 1 / scaling, 1 / scaling);
    }
}
