package com.github.manasmods.testmod.registry;

import com.github.manasmods.manascore.api.registry.Register;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.Item;

public class RegisterTest {
    private static final Register register = new Register("testmod");
    private static final RegistrySupplier<Item> TEST_ITEM = register.item("test_item").end();

    public static void init() {
        register.init();
    }
}
