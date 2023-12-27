package com.github.manasmods.testmod.registry;

import com.github.manasmods.manascore.api.registry.Register;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class RegisterTest {
    private static final Register register = new Register("testmod");
    private static final RegistrySupplier<Item> TEST_ITEM = register.item("test_item")
            .withStackSize(4)
            .end();
    public static final RegistrySupplier<Block> TEST_BLOCK = register.block("test_block")
            .withBlockItem(builder -> builder
                    .withStackSize(16))
            .end();

    public static void init() {
        register.init();
    }
}
