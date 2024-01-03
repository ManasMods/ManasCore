package com.github.manasmods.testmod.registry;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.skill.SkillAPI;
import com.github.manasmods.manascore.api.skill.Skills;
import com.mojang.serialization.MapCodec;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.github.manasmods.testmod.TestMod.REGISTER;

public class RegisterTest {
    private static final RegistrySupplier<Item> TEST_ITEM = REGISTER.item("test_item")
            .withStackSize(4)
            .end();
    private static final RegistrySupplier<TestBlock> TEST_BLOCK = REGISTER.block("test_block", TestBlock::new)
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
            .applyToAll()
            .end();
    private static final RegistrySupplier<RangedAttribute> TEST_ENTITY_ATTRIBUTE = REGISTER.attribute("test_player_attribute")
            .withDefaultValue(5)
            .withMaximumValue(10)
            .applyTo(() -> EntityType.PLAYER)
            .end();
    private static final RegistrySupplier<BlockEntityType<TestBlockEntity>> TEST_BLOCK_ENTITY = REGISTER.blockEntity("test_block_entity", TestBlockEntity::new)
            .withValidBlocks(TEST_BLOCK)
            .end();
    private static final RegistrySupplier<TestSkill> TEST_SKILL = REGISTER.skill("test_skill", TestSkill::new).end();

    public static void init() {
        ManasCore.Logger.info("Registered test content!");

        PlayerEvent.DROP_ITEM.register((player, entity) -> {
            //Test giving Skills
            if (entity.getItem().is(Items.DIAMOND)) {
                Skills storage = SkillAPI.getSkillsFrom(player);
                if (storage.learnSkill(RegisterTest.TEST_SKILL.get())) {
                    ManasCore.Logger.info("Added Tested Skill!");
                }
            }

            return EventResult.pass();
        });
    }

    private static class TestEntity extends Villager {
        public TestEntity(EntityType<TestEntity> entityType, Level level) {
            super(TEST_ENTITY.get(), level);
        }
    }

    private static class TestBlockEntity extends BlockEntity {
        TestBlockEntity(BlockPos pos, BlockState blockState) {
            super(TEST_BLOCK_ENTITY.get(), pos, blockState);
            ManasCore.Logger.info("Created block entity!");
        }
    }

    private static class TestBlock extends BaseEntityBlock {
        private static final MapCodec<TestBlock> TEST_BLOCK_MAP_CODEC = simpleCodec(TestBlock::new);

        TestBlock(Properties properties) {
            super(properties);
        }

        @Override
        protected @NotNull MapCodec<TestBlock> codec() {
            return TEST_BLOCK_MAP_CODEC;
        }

        @Nullable
        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return TEST_BLOCK_ENTITY.get().create(pos, state);
        }
    }
}
