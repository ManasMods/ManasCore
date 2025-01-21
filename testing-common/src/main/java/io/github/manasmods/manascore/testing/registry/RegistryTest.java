/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.testing.registry;

import com.mojang.serialization.MapCodec;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.manasmods.manascore.skill.api.ManasSkill;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import io.github.manasmods.manascore.skill.api.Skills;
import io.github.manasmods.manascore.skill.impl.SkillRegistry;
import io.github.manasmods.manascore.testing.ManasCoreTesting;
import io.github.manasmods.manascore.testing.ModuleConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RegistryTest {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(ModuleConstants.MOD_ID, Registries.CREATIVE_MODE_TAB);
    public static final RegistrySupplier<CreativeModeTab> TESTING_TAB = TABS.register("test_tab", () ->
            CreativeTabRegistry.create(Component.literal("Testing Creative Tab"),
                    () -> new ItemStack(RegistryTest.TEST_ITEM.get())));

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ModuleConstants.MOD_ID, Registries.ITEM);
    public static final RegistrySupplier<Item> TEST_ITEM = ITEMS.register("test_item",
                    () -> new Item(new Item.Properties().arch$tab(TESTING_TAB).stacksTo(69)
                            .attributes(ItemAttributeModifiers.builder()
                                    .add(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(ResourceLocation.withDefaultNamespace("test_block_reach"), 10,
                                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.OFFHAND).build())));

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ModuleConstants.MOD_ID, Registries.BLOCK);
    public static final RegistrySupplier<Block> TEST_BLOCK = BLOCKS.register("test_block",
            () -> new TestBlock(BlockBehaviour.Properties.of().lightLevel(value -> 15)));
    public static final RegistrySupplier<BlockItem> TEST_BLOCK_ITEM = ITEMS.register("test_block_item",
            () -> new BlockItem(RegistryTest.TEST_BLOCK.get(), new Item.Properties().arch$tab(TESTING_TAB).stacksTo(42)
                    .attributes(ItemAttributeModifiers.builder()
                    .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(ResourceLocation.withDefaultNamespace("test_entity_reach"), 10,
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build())));

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ModuleConstants.MOD_ID, Registries.BLOCK_ENTITY_TYPE);
    public static final RegistrySupplier<BlockEntityType<?>> TEST_BLOCK_ENTITY = BLOCK_ENTITIES.register("test_block_entity",
            () -> BlockEntityType.Builder.of(TestBlockEntity::new, RegistryTest.TEST_BLOCK.get()).build(null));

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ModuleConstants.MOD_ID, Registries.ENTITY_TYPE);
    public static final RegistrySupplier<EntityType<? extends Villager>> TEST_ENTITY_TYPE = ENTITY_TYPES.register("test_entity",
            () -> EntityType.Builder.of(TestEntity::new, MobCategory.MONSTER).fireImmune()
                    .sized(1F, 1F).clientTrackingRange(4).updateInterval(10).build("test_entity"));

    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ModuleConstants.MOD_ID, Registries.ATTRIBUTE);
    public static final RegistrySupplier<Attribute> TEST_ATTRIBUTE_PLAYER = ATTRIBUTES.register("test_attribute_player",
            () -> new RangedAttribute("test_attribute_player", 69, 0, 420).setSyncable(true));
    public static final RegistrySupplier<Attribute> TEST_ATTRIBUTE_ALL = ATTRIBUTES.register("test_attribute_all",
            () -> new RangedAttribute("test_attribute_all", 420, 69, 4200).setSyncable(true));

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ModuleConstants.MOD_ID, Registries.MOB_EFFECT);
    public static final RegistrySupplier<MobEffect> TEST_MOB_EFFECT = MOB_EFFECTS.register("test_mob_effect",
            () -> new TestMobEffect(MobEffectCategory.NEUTRAL, 4201604)
                    .withSoundOnAdded(SoundEvents.ALLAY_DEATH).setBlendDuration(60));
    public static final RegistrySupplier<MobEffect> TEST_MOB_EFFECT_PARTICLE = MOB_EFFECTS.register("test_mob_effect_particle",
            () -> new TestMobEffect(MobEffectCategory.HARMFUL, 6901604, ParticleTypes.ANGRY_VILLAGER)
                    .withSoundOnAdded(SoundEvents.BREWING_STAND_BREW)
                    .addAttributeModifier(Attributes.WATER_MOVEMENT_EFFICIENCY, ResourceLocation.withDefaultNamespace("test_swim_speed"),
                            3, AttributeModifier.Operation.ADD_VALUE));

    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ModuleConstants.MOD_ID, Registries.POTION);
    public static final RegistrySupplier<Potion> TEST_POTION = POTIONS.register("test_potion",
            () -> new Potion("lmao_potion", new MobEffectInstance(TEST_MOB_EFFECT, 100, 10),
                    new MobEffectInstance(TEST_MOB_EFFECT_PARTICLE, 200, 5, false, false, false)));

    public static final DeferredRegister<ManasSkill> SKILLS = DeferredRegister.create(ModuleConstants.MOD_ID, SkillRegistry.KEY);
    public static final RegistrySupplier<TestSkill> TEST_SKILL = SKILLS.register("test_skill", TestSkill::new);

    public static void init() {
        ManasCoreTesting.LOG.info("Registered test content!");
        TABS.register();
        BLOCKS.register();
        ITEMS.register();
        BLOCK_ENTITIES.register();
        ENTITY_TYPES.register();
        ATTRIBUTES.register();
        MOB_EFFECTS.register();
        POTIONS.register();
        SKILLS.register();

        EntityAttributeRegistry.register(TEST_ENTITY_TYPE, Villager::createAttributes);

        PlayerEvent.DROP_ITEM.register((player, entity) -> {
            //Test giving Skills
            if (entity.getItem().is(Items.DIAMOND)) {
                Skills storage = SkillAPI.getSkillsFrom(player);
                Registrar<ManasSkill> skills = SkillAPI.getSkillRegistry();
                RegistrySupplier<TestSkill> testSkill = TEST_SKILL;
                if (storage.learnSkill(RegistryTest.TEST_SKILL.get())) {
                    ManasCoreTesting.LOG.info("Added Tested Skill!");
                }
            } else if (entity.getItem().is(Items.EMERALD)) {
                Skills storage = SkillAPI.getSkillsFrom(player);
                storage.forgetSkill(RegistryTest.TEST_SKILL.get());
                ManasCoreTesting.LOG.info("Forgot Tested Skill!");
            }

            return EventResult.pass();
        });
    }

    private static class TestEntity extends Villager {
        public TestEntity(EntityType<TestEntity> entityType, Level level) {
            super(TEST_ENTITY_TYPE.get(), level);
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

        protected TestMobEffect(MobEffectCategory mobEffectCategory, int i, ParticleOptions particleOptions) {
            super(mobEffectCategory, i, particleOptions);
        }

        public void onMobRemoved(LivingEntity entity, int i, Entity.RemovalReason removalReason) {
            if (removalReason == Entity.RemovalReason.KILLED) {
                if (entity.level() instanceof ServerLevel level) {
                    double d = entity.getX();
                    double e = entity.getY() + (double)(entity.getBbHeight() / 2.0F);
                    double f = entity.getZ();
                    float g = 10.0F + entity.getRandom().nextFloat() * 2.0F;
                    if (this.equals(RegistryTest.TEST_MOB_EFFECT_PARTICLE.get()))
                        level.explode(entity, null, AbstractWindCharge.EXPLOSION_DAMAGE_CALCULATOR, d, e, f, g,
                                false, Level.ExplosionInteraction.TRIGGER, ParticleTypes.GUST_EMITTER_SMALL,
                                ParticleTypes.GUST_EMITTER_LARGE, SoundEvents.BREEZE_WIND_CHARGE_BURST);
                    else level.explode(entity, Explosion.getDefaultDamageSource(level, entity), null, d, e, f, g,
                            false, Level.ExplosionInteraction.MOB, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.GENERIC_EXPLODE);
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
