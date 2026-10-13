/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.testing.module;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.manasmods.manascore.command.api.Command;
import io.github.manasmods.manascore.command.api.CommandRegistry;
import io.github.manasmods.manascore.command.api.Execute;
import io.github.manasmods.manascore.command.api.parameter.EntityArg;
import io.github.manasmods.manascore.command.api.parameter.SenderArg;
import io.github.manasmods.manascore.command.api.parameter.primitive.IntegerArg;
import io.github.manasmods.manascore.command.api.parameter.primitive.LiteralArg;
import io.github.manasmods.manascore.skill.api.ManasSkillInstance;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import io.github.manasmods.manascore.skill.api.SkillTarget;
import io.github.manasmods.manascore.skill.api.Skills;
import io.github.manasmods.manascore.testing.registry.RegistryTest;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class SkillModuleTest {
    public static void init() {
        CommandRegistry.registerCommand(SkillCommand.class);
    }

    @Command(value = "manasskill")
    public static class SkillCommand {
        private static @Nullable LivingEntity getLiving(CommandSourceStack sender, EntitySelector selector) throws CommandSyntaxException {
            Entity entity = selector.findSingleEntity(sender);
            return entity instanceof LivingEntity living ? living : null;
        }

        private static @Nullable ManasSkillInstance getOrLearnInstance(LivingEntity living) {
            Skills skills = SkillAPI.getSkillsFrom(living);
            if (skills.getSkill(RegistryTest.TEST_SKILL.get()).isEmpty()) skills.learnSkill(RegistryTest.TEST_SKILL.get());
            return skills.getSkill(RegistryTest.TEST_SKILL.get()).orElse(null);
        }

        private static void reply(CommandSourceStack sender, String text) {
            sender.sendSystemMessage(Component.literal(text));
        }

        @Execute
        public boolean start(@SenderArg CommandSourceStack sender, @LiteralArg("start") String literal, @EntityArg EntitySelector selector,
                             @IntegerArg(value = "mode", min = 0, max = 1) int mode) throws CommandSyntaxException {
            ServerPlayer player = sender.getPlayer();
            LivingEntity living = getLiving(sender, selector);
            if (player == null || living == null) return false;
            ManasSkillInstance instance = getOrLearnInstance(living);
            if (instance == null) return false;
            boolean started = SkillAPI.getSkillsFrom(living).startHoldSkill(instance, -1, mode, SkillTarget.of(player));
            reply(sender, started ? "Started, aiming at you" : "Start failed");
            return started;
        }

        @Execute
        public boolean startPosition(@SenderArg CommandSourceStack sender, @LiteralArg("startpos") String literal, @EntityArg EntitySelector selector,
                                     @IntegerArg(value = "mode", min = 0, max = 1) int mode) throws CommandSyntaxException {
            ServerPlayer player = sender.getPlayer();
            LivingEntity living = getLiving(sender, selector);
            if (player == null || living == null) return false;
            ManasSkillInstance instance = getOrLearnInstance(living);
            if (instance == null) return false;
            boolean started = SkillAPI.getSkillsFrom(living).startHoldSkill(instance, -1, mode, SkillTarget.of(player.position()));
            reply(sender, started ? "Started, aiming at your position" : "Start failed");
            return started;
        }

        @Execute
        public boolean release(@SenderArg CommandSourceStack sender, @LiteralArg("release") String literal, @EntityArg EntitySelector selector,
                               @IntegerArg(value = "mode", min = 0, max = 1) int mode) throws CommandSyntaxException {
            LivingEntity living = getLiving(sender, selector);
            if (living == null) return false;
            ManasSkillInstance instance = getOrLearnInstance(living);
            if (instance == null) return false;
            SkillAPI.getSkillsFrom(living).handleSkillRelease(instance, -1, mode, false);
            reply(sender, "Released");
            return true;
        }

        @Execute
        public boolean stop(@SenderArg CommandSourceStack sender, @LiteralArg("stop") String literal, @EntityArg EntitySelector selector,
                            @IntegerArg(value = "mode", min = 0, max = 1) int mode) throws CommandSyntaxException {
            LivingEntity living = getLiving(sender, selector);
            if (living == null) return false;
            ManasSkillInstance instance = getOrLearnInstance(living);
            if (instance == null) return false;
            SkillAPI.getSkillsFrom(living).stopHoldSkill(instance, mode);
            reply(sender, "Stopped");
            return true;
        }
    }
}
