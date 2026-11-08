package io.github.manasmods.manascore.config;

import io.github.manasmods.manascore.config.api.ManasConfig;
import io.github.manasmods.manascore.config.api.SyncToClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigSyncTest {
    private static final String BROKEN = "limit = = [ not toml";
    @TempDir
    static Path folder;

    @Test
    void aBrokenFileOnDiskNeverStopsTheSync() throws Exception {
        BrokenFileConfig config = new BrokenFileConfig();
        ConfigRegistry.registerConfig(config);
        Files.writeString(config.getConfigPath(), BROKEN);
        Map<String, String> data = assertDoesNotThrow(() -> ConfigRegistry.getConfigSyncData());
        assertTrue(data.get("BrokenFileConfig").contains("limit = 5"));
        assertEquals(BROKEN, Files.readString(config.getConfigPath()), "the sync never writes the file");
    }

    @Test
    void theSyncSendsTheValuesTheServerRuns() throws Exception {
        MemoryConfig config = new MemoryConfig();
        ConfigRegistry.registerConfig(config);
        Files.writeString(config.getConfigPath(), "limit = 7");
        config.limit = 9;
        String text = ConfigRegistry.getConfigSyncData(MemoryConfig.class).get("MemoryConfig");
        assertTrue(text.contains("limit = 9"), text);
        assertEquals(9, config.limit, "a hand edit waits for a reload");
    }

    @SyncToClient
    public static class BrokenFileConfig extends ManasConfig {
        public int limit = 5;

        public String getFileName() {
            return "test/broken";
        }

        public Path getConfigPath() {
            return folder.resolve("broken.toml");
        }
    }

    @SyncToClient
    public static class MemoryConfig extends ManasConfig {
        public int limit = 5;

        public String getFileName() {
            return "test/memory";
        }

        public Path getConfigPath() {
            return folder.resolve("memory.toml");
        }
    }
}
