/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.testing;

import dev.architectury.platform.Platform;
import io.github.manasmods.manascore.testing.client.ManasCoreTestingClient;
import io.github.manasmods.manascore.testing.module.ConfigModuleTest;
import io.github.manasmods.manascore.testing.module.CommandModuleTest;
import io.github.manasmods.manascore.testing.module.StorageModuleTest;
import io.github.manasmods.manascore.testing.registry.RegistryTest;
import io.github.manasmods.manascore.testing.registry.TestRegister;
import net.fabricmc.api.EnvType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ManasCoreTesting {
    public static final Logger LOG = LoggerFactory.getLogger("ManasCore - Testing");
    public  static final TestRegister REGISTER = new TestRegister(ModuleConstants.MOD_ID);

    public static void init() {
        StorageModuleTest.init();
        REGISTER.init(RegistryTest::init);
        if (Platform.getEnv() == EnvType.CLIENT) {
            ManasCoreTestingClient.init();
        }
        StorageModuleTest.init();
        ConfigModuleTest.init();
        CommandModuleTest.init();
    }
}
