/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.cinematic.impl.client;

import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DialogueOverlay {
    private static final float CHARACTERS_PER_TICK = 1.5F;
    private static final List<Line> QUEUE = new ArrayList<>();
    private static @Nullable Line current = null;
    private static int elapsed = 0;

    public static void init() {
        ClientTickEvent.CLIENT_PRE.register(minecraft -> {
            if (current == null && QUEUE.isEmpty()) return;
            if (minecraft.isPaused() || minecraft.player == null || minecraft.screen instanceof ReceivingLevelScreen) return;
            if (current == null) {
                current = QUEUE.removeFirst();
                elapsed = 0;
            }

            int typed = current.getTyped(elapsed);
            elapsed++;
            if (current.getTyped(elapsed) > typed && elapsed % 2 == 0)
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HAT.value(), 1.8F, 0.25F));
            if (elapsed >= current.getDuration()) current = null;
        });

        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> {
            QUEUE.clear();
            current = null;
        });
    }

    public static void add(Component speaker, Component line, boolean instant) {
        Line entry = new Line(speaker, line, instant);
        if (!instant) {
            QUEUE.add(entry);
            return;
        }
        current = entry;
        elapsed = 0;
    }

    public static void render(GuiGraphics graphics, float partialTick, boolean cutscene) {
        if (current == null) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (!cutscene && minecraft.options.hideGui) return;

        float time = elapsed + partialTick;
        float alpha = Math.min(Math.min(current.isInstant() ? 1F : time / 4F, (current.getDuration() - time) / 10F), 1F);
        // Font draws any alpha this low as fully opaque
        if (alpha < 0.05F) return;

        Font font = minecraft.font;
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        current.wrap(font, (int) (width * 0.55F));
        List<String> lines = current.getLines();
        int offset = current.isNamed() ? 1 : 0;
        int bottom = cutscene ? height - 6 : height - 80;
        int top = bottom - (lines.size() + offset) * 10;
        int color = (int) (alpha * 255) << 24;

        if (!cutscene) {
            int boxWidth = current.getBoxWidth();
            graphics.fill((width - boxWidth) / 2 - 6, top - 4, (width + boxWidth) / 2 + 6, bottom + 2, (int) (alpha * 144) << 24);
        }
        if (current.isNamed()) graphics.drawCenteredString(font, current.getSpeaker(), width / 2, top, color | 0xFFAA00);

        int shown = current.getTyped(time);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int visible = Math.min(shown, line.length());
            if (visible <= 0) break;
            shown -= visible;
            Component text = visible == line.length() ? current.getStyledLines().get(i) : current.getStyled(line.substring(0, visible));
            graphics.drawString(font, text, (width - current.getWidths().get(i)) / 2, top + (i + offset) * 10, color | 0xFFFFFF);
        }
    }

    private static class Line {
        private final Component speaker;
        private final Component text;
        private final boolean instant;
        private final boolean named;
        private final int length;
        private final int duration;
        private final List<String> lines = new ArrayList<>();
        private final List<Component> styledLines = new ArrayList<>();
        private final List<Integer> widths = new ArrayList<>();
        private int boxWidth = 0;
        private int wrapWidth = -1;

        public Line(Component speaker, Component text, boolean instant) {
            this.speaker = speaker;
            this.text = text;
            this.instant = instant;
            this.named = !speaker.getString().isEmpty();
            this.length = text.getString().length();
            this.duration = instant ? 40 + this.length / 3 : (int) Math.ceil(this.length / CHARACTERS_PER_TICK) + 30 + this.length / 3;
        }

        public Component getSpeaker() {
            return this.speaker;
        }

        public boolean isInstant() {
            return this.instant;
        }

        public boolean isNamed() {
            return this.named;
        }

        public int getDuration() {
            return this.duration;
        }

        public int getTyped(float time) {
            if (this.instant) return this.length;
            return Math.min((int) (time * CHARACTERS_PER_TICK), this.length);
        }

        public Component getStyled(String part) {
            return Component.literal(part).withStyle(this.text.getStyle());
        }

        public List<String> getLines() {
            return this.lines;
        }

        public List<Component> getStyledLines() {
            return this.styledLines;
        }

        public List<Integer> getWidths() {
            return this.widths;
        }

        public int getBoxWidth() {
            return this.boxWidth;
        }

        public void wrap(Font font, int width) {
            if (width == this.wrapWidth) return;
            this.wrapWidth = width;
            this.lines.clear();
            this.styledLines.clear();
            this.widths.clear();
            this.boxWidth = this.named ? font.width(this.speaker) : 0;
            for (FormattedText line : font.getSplitter().splitLines(this.text.getString(), width, this.text.getStyle())) {
                String part = line.getString();
                Component styled = this.getStyled(part);
                int lineWidth = font.width(styled);
                this.lines.add(part);
                this.styledLines.add(styled);
                this.widths.add(lineWidth);
                this.boxWidth = Math.max(this.boxWidth, lineWidth);
            }
        }
    }
}
