package com.github.manasmods.manascore.api.client.gui;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.awt.*;
import java.util.List;

public interface ExtendedGuiGraphics {
    void renderScaledText(final float scaling, final List<FormattedCharSequence> text, final float x, float y, final int color, final boolean shadow, final float spacePerLine);

    void renderScaledWrappedText(final FormattedText text, final float x, float y, final float width, final int color, final boolean shadow, final float spacePerLine);

    default void renderScaledWrappedText(final FormattedText text, final float x, float y, final float width, final Color color, final boolean shadow, final float spacePerLine) {
        renderScaledWrappedText(text, x, y, width, color.getRGB(), shadow, spacePerLine);
    }

    default void renderScaledWrappedText(final FormattedText text, final float x, float y, final float width, final int color, final boolean shadow) {
        renderScaledWrappedText(text, x, y, width, color, shadow, 0);
    }

    default void renderScaledWrappedText(final FormattedText text, final float x, float y, final float width, final Color color, final boolean shadow) {
        renderScaledWrappedText(text, x, y, width, color, shadow, 0);
    }

    void renderScaledTextInArea(final FormattedText text, final float x, float y, final float width, final float height, final int color, final boolean shadow, final float spacePerLine, final float scalingSteps);

    default void renderScaledTextInArea(final FormattedText text, final float x, float y, final float width, final float height, final Color color, final boolean shadow, final float spacePerLine, final float scalingSteps) {
        renderScaledTextInArea(text, x, y, width, height, color.getRGB(), shadow, spacePerLine, scalingSteps);
    }

    default void renderScaledTextInArea(final FormattedText text, final float x, float y, final float width, final float height, final int color, final boolean shadow, final float spacePerLine) {
        renderScaledTextInArea(text, x, y, width, height, color, shadow, spacePerLine, 0.01F);
    }

    default void renderScaledTextInArea(final FormattedText text, final float x, float y, final float width, final float height, final Color color, final boolean shadow, final float spacePerLine) {
        renderScaledTextInArea(text, x, y, width, height, color, shadow, spacePerLine, 0.01F);
    }

    default void renderScaledTextInArea(final FormattedText text, final float x, float y, final float width, final float height, final int color, final boolean shadow) {
        renderScaledTextInArea(text, x, y, width, height, color, shadow, 0F);
    }

    default void renderScaledTextInArea(final FormattedText text, final float x, float y, final float width, final float height, final Color color, final boolean shadow) {
        renderScaledTextInArea(text, x, y, width, height, color, shadow, 0F);
    }
}
