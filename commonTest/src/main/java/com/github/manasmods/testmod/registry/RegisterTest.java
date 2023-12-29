package com.github.manasmods.testmod.registry;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import static com.github.manasmods.testmod.TestMod.REGISTER;

public class RegisterTest {
    private static final RegistrySupplier<Item> TEST_ITEM = REGISTER.item("test_item")
            .withStackSize(4)
            .end();
    private static final RegistrySupplier<Block> TEST_BLOCK = REGISTER.block("test_block")
            .withBlockItem(builder -> builder
                    .withStackSize(16))
            .end();
    private static final RegistrySupplier<EntityType<TestEntity>> TEST_ENTITY = REGISTER.entity("test_entity", TestEntity::new)
            .fireImmune()
            .withSize(1, 1)
            .end();
    private static final RegistrySupplier<RangedAttribute> TEST_ATTRIBUTE = REGISTER.attribute("test_attribute")
            .withDefaultValue(69)
            .withMaximumValue(420)
            .applyTo(() -> EntityType.PLAYER)
            .applyToAll()
            .end();

    public static void init() {
    }

    private static class TestEntity extends Villager {
        public TestEntity(EntityType<TestEntity> entityType, Level level) {
            super(TEST_ENTITY.get(), level);
        }
    }
}
