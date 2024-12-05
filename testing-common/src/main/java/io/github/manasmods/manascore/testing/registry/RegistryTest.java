/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.testing.registry;

import com.mojang.serialization.MapCodec;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.manasmods.manascore.skill.api.ManasSkill;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import io.github.manasmods.manascore.skill.api.Skills;
import io.github.manasmods.manascore.testing.ManasCoreTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.github.manasmods.manascore.testing.ManasCoreTesting.REGISTER;

public class RegistryTest {
    private static final RegistrySupplier<Item> TEST_ITEM = REGISTER.item("test_item")
            .withStackSize(4)
            .end();
    private static final RegistrySupplier<TestBlock> TEST_BLOCK = REGISTER.block("test_block", TestBlock::new)
            .withBlockItem(builder -> builder.withStackSize(16))
            .end();
    private static final RegistrySupplier<EntityType<TestEntity>> TEST_ENTITY = REGISTER.entity("test_entity", TestEntity::new)
            .fireImmune()
            .withSize(1, 1)
            .end();
    private static final RegistrySupplier<Attribute> TEST_ATTRIBUTE = REGISTER.attribute("test_attribute")
            .withDefaultValue(69)
            .withMaximumValue(420)
            .applyToAll()
            .end();
    private static final RegistrySupplier<Attribute> TEST_ENTITY_ATTRIBUTE = REGISTER.attribute("test_player_attribute")
            .withDefaultValue(5)
            .withMaximumValue(10)
            .applyTo(() -> EntityType.PLAYER)
            .end();
    private static final RegistrySupplier<BlockEntityType<TestBlockEntity>> TEST_BLOCK_ENTITY = REGISTER.blockEntity("test_block_entity", TestBlockEntity::new)
            .withValidBlocks(TEST_BLOCK)
            .end();
    private static final RegistrySupplier<MobEffect> TEST_MOB_EFFECT = REGISTER.mobEffect("test_effect", TestMobEffect::new)
            .withCategory(MobEffectCategory.NEUTRAL)
            .withColor(5882118)
            .withAddedSoundEvent(SoundEvents.ALLAY_DEATH)
            .withBlendDurationTicks(60)
            .end();

    private static final RegistrySupplier<MobEffect> TEST_MOB_EFFECT_PARTICLE = REGISTER.mobEffect("test_effect_particle", TestMobEffect::new)
            .withCategory(MobEffectCategory.BENEFICIAL)
            .withColor(5882118)
            .withAddedSoundEvent(SoundEvents.VEX_DEATH)
            .withParticleOptions(ParticleTypes.ANGRY_VILLAGER)
            .end();
    private static final RegistrySupplier<TestSkill> TEST_SKILL = REGISTER.skill("test_skill", TestSkill::new).end();

    public static void init() {
        ManasCoreTesting.LOG.info("Registered test content!");

        PlayerEvent.DROP_ITEM.register((player, entity) -> {
            //Test giving Skills
            if (entity.getItem().is(Items.DIAMOND)) {
                Skills storage = SkillAPI.getSkillsFrom(player);
                Registrar<ManasSkill> skills = SkillAPI.getSkillRegistry();
                RegistrySupplier<TestSkill> testSkill = TEST_SKILL;
                if (storage.learnSkill(RegistryTest.TEST_SKILL.get())) {
                    ManasCoreTesting.LOG.info("Added Tested Skill!");
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
            ManasCoreTesting.LOG.info("Created block entity!");
        }
    }

    private static class TestMobEffect extends MobEffect {
        protected TestMobEffect(MobEffectCategory mobEffectCategory, int i) {
            super(mobEffectCategory, i);
        }

        protected TestMobEffect(MobEffectCategory mobEffectCategory, int i, ParticleOptions options) {
            super(mobEffectCategory, i, options);
        }

        public void onMobRemoved(LivingEntity livingEntity, int i, Entity.RemovalReason removalReason) {
            if (removalReason == Entity.RemovalReason.KILLED) {
                Level var5 = livingEntity.level();
                if (var5 instanceof ServerLevel) {
                    ServerLevel serverLevel = (ServerLevel)var5;
                    double d = livingEntity.getX();
                    double e = livingEntity.getY() + (double)(livingEntity.getBbHeight() / 2.0F);
                    double f = livingEntity.getZ();
                    float g = 10.0F + livingEntity.getRandom().nextFloat() * 2.0F;
                    serverLevel.explode(livingEntity, null, AbstractWindCharge.EXPLOSION_DAMAGE_CALCULATOR, d, e, f, g, false, Level.ExplosionInteraction.TRIGGER, ParticleTypes.GUST_EMITTER_SMALL, ParticleTypes.GUST_EMITTER_LARGE, SoundEvents.BREEZE_WIND_CHARGE_BURST);
                }
            }
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
