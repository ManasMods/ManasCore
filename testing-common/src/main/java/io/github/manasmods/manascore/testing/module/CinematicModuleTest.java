/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.testing.module;

import dev.architectury.networking.NetworkManager;
import io.github.manasmods.manascore.cinematic.api.Cutscene;
import io.github.manasmods.manascore.cinematic.api.CutsceneHelper;
import io.github.manasmods.manascore.cinematic.api.DialogueHelper;
import io.github.manasmods.manascore.cinematic.impl.network.s2c.CutscenePayload;
import io.github.manasmods.manascore.command.api.Command;
import io.github.manasmods.manascore.command.api.CommandRegistry;
import io.github.manasmods.manascore.command.api.Execute;
import io.github.manasmods.manascore.command.api.parameter.primitive.LiteralArg;
import io.github.manasmods.manascore.command.api.parameter.primitive.TextArg;
import io.github.manasmods.manascore.command.api.parameter.SenderArg;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class CinematicModuleTest {
    public static final Cutscene TEST_CUTSCENE = CutsceneHelper.register(new Cutscene(ResourceLocation.fromNamespaceAndPath("manascore", "test_cutscene"), 100)
            .title("manascore.testing.cinematic.title", "manascore.testing.cinematic.subtitle", 50, 90)
            .keyframe(0, 0, 3, 8, 0, 1.5, 0)
            .keyframe(30, 6, 2.5, 4, 0, 1.5, 0)
            .keyframe(60, 0, 2, -6, 0, 1.5, 0)
            .keyframe(100, -5, 1.8, 3, 0, 1.6, 0));

    public static void init() {
        CommandRegistry.registerCommand(CinematicCommand.class);
    }

    @Command("cinematic")
    public static class CinematicCommand {
        @Execute
        public boolean play(@SenderArg ServerPlayer player, @LiteralArg("play") String l) {
            CutsceneHelper.play(TEST_CUTSCENE, List.of(player), player.position(), player.getYRot());
            return true;
        }

        @Execute
        public boolean unknown(@SenderArg ServerPlayer player, @LiteralArg("unknown") String l) {
            NetworkManager.sendToPlayer(player, new CutscenePayload(ResourceLocation.fromNamespaceAndPath("manascore", "missing"), player.position(), player.getYRot()));
            return true;
        }

        @Execute
        public boolean say(@SenderArg ServerPlayer player, @LiteralArg("say") String l, @TextArg(TextArg.Type.GREEDY_STRING) String text) {
            DialogueHelper.send(player, Component.literal("Tester"), Component.literal(text));
            return true;
        }

        @Execute
        public boolean instant(@SenderArg ServerPlayer player, @LiteralArg("instant") String l, @TextArg(TextArg.Type.GREEDY_STRING) String text) {
            DialogueHelper.sendInstant(player, Component.literal(text));
            return true;
        }

        @Execute
        public boolean burst(@SenderArg ServerPlayer player, @LiteralArg("burst") String l) {
            DialogueHelper.send(player, Component.literal("Tester"), Component.literal("First typed line."));
            DialogueHelper.send(player, Component.literal("Tester"), Component.literal("Second typed line."));
            DialogueHelper.send(player, Component.literal("Tester"), Component.literal("Third typed line."));
            DialogueHelper.sendInstant(player, Component.literal("Instant line"));
            return true;
        }
    }
}
