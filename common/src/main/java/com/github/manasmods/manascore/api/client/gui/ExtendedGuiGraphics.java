package com.github.manasmods.manascore.api.client.gui;

import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public interface ExtendedGuiGraphics {
    void renderScaledText(final float scaling, final List<FormattedCharSequence> text, final float x, float y, final int color, final boolean shadow, final float spacePerLine);
}
