/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.cinematic.impl.client;

import io.github.manasmods.manascore.cinematic.api.Cutscene;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class CutsceneOverlay {
    public static void render(GuiGraphics graphics, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        Cutscene cutscene = CutsceneClientHandler.getCutscene();
        if (cutscene == null) {
            float fade = CutsceneClientHandler.getFadeOut(partialTick);
            renderBars(graphics, width, height, fade);
            DialogueOverlay.render(graphics, partialTick, true);
            renderBlack(graphics, width, height, fade);
            return;
        }

        float time = CutsceneClientHandler.getTime(partialTick);
        int fadeStart = cutscene.getDuration() - 12;
        renderBars(graphics, width, height, Math.min(time / 10F, 1F));
        renderTitle(graphics, minecraft.font, cutscene, time, width, height);
        if (time > 20 && time < fadeStart) {
            Component hint = Component.translatable("manascore.cinematic.skip", minecraft.options.keyJump.getTranslatedKeyMessage());
            graphics.drawString(minecraft.font, hint, width - minecraft.font.width(hint) - 8, height - 14, 0x999999);
        }
        DialogueOverlay.render(graphics, partialTick, true);
        renderBlack(graphics, width, height, Math.max(1F - time / 10F, (time - fadeStart) / 12F));
    }

    private static void renderBars(GuiGraphics graphics, int width, int height, float progress) {
        int bar = (int) (height * 0.12F * progress);
        if (bar <= 0) return;
        graphics.fill(0, 0, width, bar, 0xFF000000);
        graphics.fill(0, height - bar, width, height, 0xFF000000);
    }

    private static void renderTitle(GuiGraphics graphics, Font font, Cutscene cutscene, float time, int width, int height) {
        if (cutscene.getTitle().isEmpty() || time < cutscene.getTitleStart() || time > cutscene.getTitleEnd()) return;
        float alpha = Math.min(Math.min(time - cutscene.getTitleStart(), cutscene.getTitleEnd() - time) / 10F, 1F);
        if (alpha < 0.05F) return;

        int color = (int) (alpha * 255) << 24;
        graphics.pose().pushPose();
        graphics.pose().translate(width / 2F, height * 0.62F, 0);
        graphics.pose().scale(3F, 3F, 1F);
        graphics.drawCenteredString(font, Component.translatable(cutscene.getTitle()), 0, 0, color | 0xFFFFFF);
        graphics.pose().popPose();

        graphics.pose().pushPose();
        graphics.pose().translate(width / 2F, height * 0.62F + 32, 0);
        graphics.pose().scale(1.5F, 1.5F, 1F);
        graphics.drawCenteredString(font, Component.translatable(cutscene.getSubtitle()), 0, 0, color | 0xAAAAAA);
        graphics.pose().popPose();
    }

    private static void renderBlack(GuiGraphics graphics, int width, int height, float alpha) {
        if (alpha <= 0) return;
        graphics.fill(0, 0, width, height, (int) (Math.min(alpha, 1F) * 255) << 24);
    }
}
