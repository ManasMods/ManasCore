/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.ApiStatus.AvailableSince;

import java.awt.Color;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@AvailableSince("1.0.1.0")
@UtilityClass
public final class FontRenderHelper {

    public static void renderScaledTextInArea(final PoseStack poseStack, final Font font, final FormattedText text, final float x, float y, final float width, final float height, final Color color) {
        renderScaledTextInArea(poseStack, font, text, x, y, width, height, color, 0F);
    }

    public static void renderScaledTextInArea(final PoseStack poseStack, final Font font, final FormattedText text, final float x, float y, final float width, final float height, final int color) {
        renderScaledTextInArea(poseStack, font, text, x, y, width, height, color, 0F);
    }

    public static void renderScaledTextInArea(final PoseStack poseStack, final Font font, final FormattedText text, final float x, float y, final float width, final float height, final Color color,
                                              final float spacePerLine) {
        renderScaledTextInArea(poseStack, font, text, x, y, width, height, color, spacePerLine, 0.01F);
    }

    public static void renderScaledTextInArea(final PoseStack poseStack, final Font font, final FormattedText text, final float x, float y, final float width, final float height, final int color,
                                              final float spacePerLine) {
        renderScaledTextInArea(poseStack, font, text, x, y, width, height, color, spacePerLine, 0.01F);
    }

    public static void renderScaledTextInArea(final PoseStack poseStack, final Font font, final FormattedText text, final float x, float y, final float width, final float height, final Color color,
                                              final float spacePerLine, final float scalingSteps) {
        renderScaledTextInArea(poseStack, font, text, x, y, width, height, color.getRGB(), spacePerLine, scalingSteps);
    }

    public static void renderScaledTextInArea(final PoseStack poseStack, final Font font, final FormattedText text, final float x, float y, final float width, final float height, final int color,
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

        renderScaledText(poseStack,
            font,
            scaling,
            font.split(text, Math.round(width / scaling)),
            x,
            y,
            color,
            spacePerLine
        );
    }

    public static void renderScaledWrappedText(final PoseStack poseStack, final Font font, final FormattedText text, final float x, float y, final float width, final Color color) {
        renderScaledWrappedText(poseStack, font, text, x, y, width, color, 0);
    }

    public static void renderScaledWrappedText(final PoseStack poseStack, final Font font, final FormattedText text, final float x, float y, final float width, final int color) {
        renderScaledWrappedText(poseStack, font, text, x, y, width, color, 0);
    }

    public static void renderScaledWrappedText(final PoseStack poseStack, final Font font, final FormattedText text, final float x, float y, final float width, final Color color,
                                               final float spacePerLine) {
        renderScaledWrappedText(poseStack, font, text, x, y, width, color.getRGB(), spacePerLine);
    }

    public static void renderScaledWrappedText(final PoseStack poseStack, final Font font, final FormattedText text, final float x, float y, final float width, final int color,
                                               final float spacePerLine) {
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

        renderScaledText(poseStack,
            font,
            scaling,
            font.split(text, Math.round(width / scaling)),
            x,
            y,
            color,
            spacePerLine
        );
    }

    private static void renderScaledText(final PoseStack poseStack, final Font font, final float scaling, final List<FormattedCharSequence> text, final float x, float y, final int color,
                                         final float spacePerLine) {
        poseStack.scale(scaling, scaling, scaling);

        for (FormattedCharSequence charSequence : text) {
            font.draw(poseStack, charSequence, x / scaling, y / scaling, color);
            y += (font.lineHeight + spacePerLine) * scaling;
        }

        poseStack.scale(1 / scaling, 1 / scaling, 1 / scaling);
    }
}
