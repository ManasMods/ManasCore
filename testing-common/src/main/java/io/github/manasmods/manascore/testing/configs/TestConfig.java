package io.github.manasmods.manascore.testing.configs;

import io.github.manasmods.manascore.command.api.Permission;
import io.github.manasmods.manascore.config.ConfigRegistry;
import io.github.manasmods.manascore.config.api.Comment;
import io.github.manasmods.manascore.config.api.ManasConfig;
import io.github.manasmods.manascore.config.api.ManasSubConfig;
import io.github.manasmods.manascore.config.api.SyncToClient;
import io.github.manasmods.manascore.testing.registry.RegistryTest;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

import static io.github.manasmods.manascore.testing.ManasCoreTesting.LOG;

@SyncToClient
public class TestConfig extends ManasConfig {
    public String getFileName() {
        return "manascore_test/test_folder/test_config";
    }

    public ResourceLocation testResourceLocation = RegistryTest.TEST_SKILL.getId();
    @Comment("Enum test!")
    public Permission.PermissionLevel permissionLevel = Permission.PermissionLevel.GAMEMASTER;

    @Comment("Random Lists of Values")
    public RandomLists random_lists = new RandomLists();
    public static class RandomLists extends ManasSubConfig {
        public NumberLists numberLists = new NumberLists();
        public static class NumberLists extends ManasSubConfig {
            public List<Double> doubleList = List.of(1.0, 2D, 3d);
            public List<Integer> intList = List.of(69, 420);
            public List<Long> longList = List.of(1L, 2L, 3L);
        }
        @Comment("Who doesn't hate bugs?")
        public List<String> stringList = List.of("I", "Hate", "Bugs", "soooooo much!");
    }

    @Comment("Test Sub Config")
    public TestSubConfig test_subConfig = new TestSubConfig();
    public static class TestSubConfig extends ManasSubConfig {
        public String initialMessage = "Config working!";
    }

    public static void printTestConfig(Player player) {
        Level level = player.level();
        logConfigValue(player, level, "Test Config Sync", ConfigRegistry.getConfig(TestConfig.class).testResourceLocation);
        logConfigValue(player, level, "Test Config Non-Sync", ConfigRegistry.getConfig(SkillConfig.class).permissionLevel);
    }

    private static void logConfigValue(Player player, Level level, String configType, Object value) {
        LOG.info("{} for entity {} on {}:\n{}", configType, player.getName(),
                level.isClientSide() ? "client" : "server", value);
    }
}
