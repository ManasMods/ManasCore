package com.github.manasmods.testmod.registry;

import com.github.manasmods.manascore.api.registry.Register;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class RegisterTest {
    private static final Register register = new Register("testmod");
    private static final RegistrySupplier<Item> TEST_ITEM = register.item("test_item")
            .withStackSize(4)
            .end();
    private static final RegistrySupplier<Block> TEST_BLOCK = register.block("test_block")
            .withBlockItem(builder -> builder
                    .withStackSize(16))
            .end();
    private static final RegistrySupplier<EntityType<TestEntity>> TEST_ENTITY = register.entity("test_entity", TestEntity::new)
            .fireImmune()
            .withSize(1, 1)
            .end();
    private static final RegistrySupplier<RangedAttribute> TEST_ATTRIBUTE = register.attribute("test_attribute")
            .withDefaultValue(69)
            .withMaximumValue(420)
            .applyTo(() -> EntityType.PLAYER)
            .applyToAll()
            .end();

    public static void init() {
        register.init();
    }

    private static class TestEntity extends Villager {
        public TestEntity(EntityType<TestEntity> entityType, Level level) {
            super(TEST_ENTITY.get(), level);
        }
    }
}
