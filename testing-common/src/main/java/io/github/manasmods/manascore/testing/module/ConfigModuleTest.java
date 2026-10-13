package io.github.manasmods.manascore.testing.module;

import io.github.manasmods.manascore.command.api.Command;
import io.github.manasmods.manascore.command.api.CommandRegistry;
import io.github.manasmods.manascore.command.api.Execute;
import io.github.manasmods.manascore.command.api.parameter.SenderArg;
import io.github.manasmods.manascore.command.api.parameter.primitive.LiteralArg;
import io.github.manasmods.manascore.command.api.parameter.primitive.TextArg;
import io.github.manasmods.manascore.config.ConfigRegistry;
import io.github.manasmods.manascore.config.api.ConfigReloadResult;
import io.github.manasmods.manascore.testing.configs.SkillConfig;
import io.github.manasmods.manascore.testing.configs.TestConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class ConfigModuleTest {

    public static void init() {
        System.out.println("ConfigModuleTest initialized");
        ConfigRegistry.registerConfig(new SkillConfig());
        ConfigRegistry.registerConfig(new TestConfig());
        TestConfig testConfig = ConfigRegistry.getConfig(TestConfig.class);
        System.out.println(testConfig.test_subConfig.initialMessage);
        CommandRegistry.registerCommand(ConfigCommand.class);
    }

    @Command(value = "manasconfig")
    public static class ConfigCommand {
        @Execute
        public boolean reload(@SenderArg CommandSourceStack sender, @LiteralArg("reload") String l) {
            return reply(sender, ConfigRegistry.reloadConfigs());
        }

        @Execute
        public boolean reloadMod(@SenderArg CommandSourceStack sender, @LiteralArg("reload") String l, @TextArg(name = "modId") String modId) {
            return reply(sender, ConfigRegistry.reloadConfigs(modId));
        }

        private static boolean reply(CommandSourceStack sender, ConfigReloadResult result) {
            if (result.isSuccess()) sender.sendSystemMessage(Component.literal("Reloaded " + result.getReloadedFiles()));
            else sender.sendSystemMessage(Component.literal("Reload failed in " + result.getFailedFile() + ": " + result.getCause()));
            sender.sendSystemMessage(Component.literal("initialMessage = " + ConfigRegistry.getConfig(TestConfig.class).test_subConfig.initialMessage
                    + ", ironGolemDamageMultiplier = " + ConfigRegistry.getConfig(SkillConfig.class).ironGolemDamageMultiplier));
            return result.isSuccess();
        }
    }
}
