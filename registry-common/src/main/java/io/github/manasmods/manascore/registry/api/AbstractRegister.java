/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.registry.api;

import com.mojang.datafixers.types.Type;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import lombok.NonNull;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.EntityType.EntityFactory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <h1>AbstractRegister</h1>
 * <p>Inherit from this class to extend it's functionality (eg. for creating custom registry objects using a {@link ContentBuilder}-like system).</p>
 *
 * @param <R> The type of the inheriting class.
 * @see Register
 */
public abstract class AbstractRegister<R extends AbstractRegister<R>> {
    protected final String modId;
    protected DeferredRegister<Item> items = null;
    protected DeferredRegister<Block> blocks = null;
    protected DeferredRegister<BlockEntityType<?>> blockEntities = null;
    protected DeferredRegister<EntityType<?>> entityTypes = null;
    protected DeferredRegister<Attribute> attributes = null;
    protected DeferredRegister<MobEffect> mobEffects = null;
    protected DeferredRegister<Potion> potions = null;

    protected AbstractRegister(final String modId) {
        this.modId = modId;
    }

    protected R self() {
        return (R) this;
    }

    /**
     * <p>Registers all content</p>
     * <p>
     * Call the `init` method in your mod's initialization point.
     *     <ul>
     *           <li>On Fabric: `ModInitializer#onInitialize`</li>
     *           <li>On Forge: `Constructor` of your mod's main class</li>
     *           <li>On Architectury: In the `init` method of your common main class</li>
     *       </ul>
     * </p>
     *
     * @param beforeRegistration A {@link Runnable} that is executed before the registration of the content. Place all your static initialization code here.
     */
    public void init(final Runnable beforeRegistration) {
        // Initialize content
        beforeRegistration.run();
        if (entityTypes != null) entityTypes.register();
        if (blocks != null) blocks.register();
        if (items != null) items.register();
        if (blockEntities != null) blockEntities.register();
        if (attributes != null) attributes.register();
        if (mobEffects != null) mobEffects.register();
        if (potions != null) potions.register();
    }

    /**
     * Creates a new {@link ItemBuilder} for the given name.
     */
    public ItemBuilder<R, Item> item(final String name) {
        return item(name, Item::new);
    }

    /**
     * Creates a new {@link ItemBuilder} for the given name.
     */
    public <T extends Item> ItemBuilder<R, T> item(final String name, Function<Item.Properties, T> itemFactory) {
        if (this.items == null) this.items = DeferredRegister.create(this.modId, Registries.ITEM);
        return new ItemBuilder<>(self(), name, itemFactory);
    }

    protected BlockItemBuilder<R, BlockItem> blockItem(final String name) {
        if (this.items == null) this.items = DeferredRegister.create(this.modId, Registries.ITEM);
        return new BlockItemBuilder<>(self(), name, (blockRegistrySupplier, properties) -> new BlockItem(blockRegistrySupplier.get(), properties));
    }

    /**
     * Creates a new {@link BlockBuilder} for the given name.
     */
    public BlockBuilder<R, Block> block(final String name) {
        return block(name, Block::new);
    }

    /**
     * Creates a new {@link BlockBuilder} for the given name.
     */
    public <T extends Block> BlockBuilder<R, T> block(final String name, Function<Block.Properties, T> blockFactory) {
        if (this.blocks == null) this.blocks = DeferredRegister.create(this.modId, Registries.BLOCK);
        return new BlockBuilder<>(self(), name, blockFactory);
    }

    /**
     * Creates a new {@link EntityTypeBuilder} for the given name.
     * <p>
     * Remember to register an Entity Renderer on client side.
     *
     * @see dev.architectury.registry.client.level.entity.EntityRendererRegistry
     */
    public <T extends LivingEntity> EntityTypeBuilder<R, T> entity(final String name, final EntityFactory<T> entityFactory) {
        if (this.entityTypes == null) this.entityTypes = DeferredRegister.create(this.modId, Registries.ENTITY_TYPE);
        return new EntityTypeBuilder<>(self(), name, entityFactory);
    }

    /**
     * Creates a new {@link EntityTypeBuilder} for the given name.
     * <p>
     * Remember to register an Entity Renderer on client side.
     *
     * @see dev.architectury.registry.client.level.entity.EntityRendererRegistry
     */
    public AttributeBuilder<R> attribute(final String name) {
        if (this.attributes == null) this.attributes = DeferredRegister.create(this.modId, Registries.ATTRIBUTE);
        return new AttributeBuilder<>(self(), name);
    }

    /**
     * Creates a new {@link BlockEntityBuilder} for the given name.
     */
    public <T extends BlockEntity> BlockEntityBuilder<R, T> blockEntity(final String name, final BlockEntitySupplier<T> factory) {
        if (this.blockEntities == null) this.blockEntities = DeferredRegister.create(this.modId, Registries.BLOCK_ENTITY_TYPE);
        return new BlockEntityBuilder<>(self(), name, factory);
    }

    /**
     * Creates a new {@link MobEffectBuilder} for the given name.
     */
    public MobEffectBuilder<R> mobEffect(final String name, final MobEffectBuilder.MobEffectSupplier<MobEffect> factory) {
        if (this.mobEffects == null) this.mobEffects = DeferredRegister.create(this.modId, Registries.MOB_EFFECT);
        return new MobEffectBuilder<>(self(), name, factory);
    }

    /**
     * Creates a new {@link Potion} for the given name.
     */
    public <T extends Potion> PotionBuilder<R, T> potion(final String name, final PotionBuilder.PotionSupplier<T> factory) {
        if (this.potions == null) this.potions = DeferredRegister.create(this.modId, Registries.POTION);
        return new PotionBuilder<>(self(), name, factory);
    }

    /**
     * Builder class for {@link Item}s.
     */
    public static class ItemBuilder<R extends AbstractRegister<R>, T extends Item> extends ContentBuilder<T, R> {
        protected Item.Properties properties;
        protected final Function<Item.Properties, T> itemFactory;

        private ItemBuilder(final R register, final String name, Function<Item.Properties, T> itemFactory) {
            super(register, name);
            this.properties = new Item.Properties();
            this.itemFactory = itemFactory;
        }

        public ItemBuilder<R, T> withProperties(final Item.Properties properties) {
            this.properties = properties;
            return this;
        }

        public ItemBuilder<R, T> withProperties(Consumer<Item.Properties> properties) {
            properties.accept(this.properties);
            return this;
        }

        public ItemBuilder<R, T> withProperties(Function<Item.Properties, Item.Properties> properties) {
            this.properties = properties.apply(this.properties);
            return this;
        }

        public ItemBuilder<R, T> withStackSize(final int stackSize) {
            this.properties.stacksTo(stackSize);
            return this;
        }

        @Override
        public RegistrySupplier<T> end() {
            return this.register.items.register(this.id, () -> this.itemFactory.apply(this.properties));
        }

        @Override
        public Holder<T> endAsHolder() {
            return this.end().getRegistrar().getHolder(this.id);
        }
    }

    /**
     * Builder class for {@link BlockItem}s.
     * Internally used by {@link BlockBuilder}.
     */
    public static class BlockItemBuilder<R extends AbstractRegister<R>, T extends BlockItem> extends ContentBuilder<T, R> {
        protected Item.Properties properties;
        protected final BiFunction<RegistrySupplier<? extends Block>, Item.Properties, T> itemFactory;
        @Nullable
        protected RegistrySupplier<? extends Block> parentBlockRegistryEntry = null;

        private BlockItemBuilder(R register, String name, BiFunction<RegistrySupplier<? extends Block>, Item.Properties, T> itemFactory) {
            super(register, name);
            this.itemFactory = itemFactory;
            this.properties = new Item.Properties();
        }

        public BlockItemBuilder<R, T> withProperties(final Item.Properties properties) {
            this.properties = properties;
            return this;
        }

        public BlockItemBuilder<R, T> withProperties(Consumer<Item.Properties> properties) {
            properties.accept(this.properties);
            return this;
        }

        public BlockItemBuilder<R, T> withProperties(Function<Item.Properties, Item.Properties> properties) {
            this.properties = properties.apply(this.properties);
            return this;
        }

        public BlockItemBuilder<R, T> withStackSize(final int stackSize) {
            this.properties.stacksTo(stackSize);
            return this;
        }

        protected void setParentBlockRegistryEntry(@NonNull RegistrySupplier<? extends Block> parentBlockRegistryEntry) {
            this.parentBlockRegistryEntry = parentBlockRegistryEntry;
        }

        @Override
        public RegistrySupplier<T> end() {
            if (this.parentBlockRegistryEntry == null) throw new IllegalStateException("Parent block registry entry must not be null!");
            return this.register.items.register(this.id, () -> this.itemFactory.apply(this.parentBlockRegistryEntry, this.properties));
        }

        @Override
        public Holder<T> endAsHolder() {
            return this.end().getRegistrar().getHolder(this.id);
        }
    }

    /**
     * Builder class for {@link Block}s.
     */
    public static class BlockBuilder<R extends AbstractRegister<R>, T extends Block> extends ContentBuilder<T, R> {
        private Function<Block.Properties, T> blockFactory;
        private Block.Properties properties;
        private AbstractRegister.BlockItemBuilder<R, BlockItem> blockItemBuilder;

        private BlockBuilder(R register, String name, Function<Block.Properties, T> blockFactory) {
            super(register, name);
            this.blockFactory = blockFactory;
            this.properties = BlockBehaviour.Properties.ofFullCopy(Blocks.STONE);
            this.blockItemBuilder = register.blockItem(name);
        }

        public BlockBuilder<R, T> withBlockItem(BlockItemFactory<R, BlockItem> blockItemFactory) {
            this.blockItemBuilder = blockItemFactory.modify(this.blockItemBuilder);
            return this;
        }

        public BlockBuilder<R, T> withProperties(final Block.Properties properties) {
            this.properties = properties;
            return this;
        }

        public BlockBuilder<R, T> withProperties(Consumer<Block.Properties> properties) {
            properties.accept(this.properties);
            return this;
        }

        public BlockBuilder<R, T> withProperties(Function<Block.Properties, Block.Properties> properties) {
            this.properties = properties.apply(this.properties);
            return this;
        }

        @Override
        public RegistrySupplier<T> end() {
            RegistrySupplier<T> blockSupplier = this.register.blocks.register(this.id, () -> this.blockFactory.apply(this.properties));
            this.blockItemBuilder.setParentBlockRegistryEntry(blockSupplier);
            this.blockItemBuilder.end();
            return blockSupplier;
        }

        @Override
        public Holder<T> endAsHolder() {
            return this.end().getRegistrar().getHolder(this.id);
        }

        @FunctionalInterface
        public interface BlockItemFactory<R extends AbstractRegister<R>, T extends BlockItem> {
            BlockItemBuilder<R, T> modify(BlockItemBuilder<R, T> builder);
        }
    }

    public static class EntityTypeBuilder<R extends AbstractRegister<R>, T extends LivingEntity> extends ContentBuilder<EntityType<T>, R> {
        protected final EntityFactory<T> entityFactory;
        protected MobCategory category;
        protected int trackingRange;
        protected EntityDimensions dimensions;

        protected boolean summonable;
        protected boolean saveable;
        protected boolean fireImmune;
        protected Supplier<Block[]> immuneTo;
        protected boolean canSpawnFarFromPlayer;
        protected int updateInterval;
        private Supplier<AttributeSupplier.Builder> attributeBuilder;

        private EntityTypeBuilder(R register, String name, final EntityFactory<T> entityFactory) {
            super(register, name);
            this.entityFactory = entityFactory;
            this.category = MobCategory.MISC;
            this.trackingRange = 5;
            this.dimensions = EntityDimensions.scalable(0.6F, 1.8F);
            this.summonable = true;
            this.saveable = true;
            this.fireImmune = false;
            this.immuneTo = null;
            this.canSpawnFarFromPlayer = true;
            this.updateInterval = 3;
            this.attributeBuilder = Mob::createMobAttributes;
        }

        public EntityTypeBuilder<R, T> withCategory(final MobCategory category) {
            this.category = category;
            this.canSpawnFarFromPlayer = category == MobCategory.CREATURE | category == MobCategory.MISC;
            return this;
        }

        public EntityTypeBuilder<R, T> withTrackingRange(final int trackingRange) {
            this.trackingRange = trackingRange;
            return this;
        }

        public EntityTypeBuilder<R, T> withSize(final float width, final float height) {
            this.dimensions = EntityDimensions.scalable(width, height);
            return this;
        }

        public EntityTypeBuilder<R, T> notSummonable() {
            this.summonable = false;
            return this;
        }

        public EntityTypeBuilder<R, T> notSaveable() {
            this.saveable = false;
            return this;
        }

        public EntityTypeBuilder<R, T> fireImmune() {
            this.fireImmune = true;
            return this;
        }

        public EntityTypeBuilder<R, T> immuneTo(Supplier<Block[]> immuneTo) {
            this.immuneTo = immuneTo;
            return this;
        }

        public EntityTypeBuilder<R, T> canSpawnFarFromPlayer() {
            this.canSpawnFarFromPlayer = true;
            return this;
        }

        public EntityTypeBuilder<R, T> withUpdateInterval(int updateInterval) {
            this.updateInterval = updateInterval;
            return this;
        }

        public EntityTypeBuilder<R, T> withAttributeBuilder(Supplier<AttributeSupplier.Builder> attributeBuilder) {
            this.attributeBuilder = attributeBuilder;
            return this;
        }

        @Override
        public RegistrySupplier<EntityType<T>> end() {
            RegistrySupplier<EntityType<T>> supplier = this.register.entityTypes.register(this.id, () -> {
                EntityType.Builder<T> builder = EntityType.Builder.of(this.entityFactory, this.category)
                        .clientTrackingRange(this.trackingRange)
                        .sized(this.dimensions.width(), this.dimensions.height())
                        .updateInterval(this.updateInterval);

                if (!this.summonable) builder.noSummon();
                if (!this.saveable) builder.noSave();
                if (this.fireImmune) builder.fireImmune();
                if (this.immuneTo != null) builder.immuneTo(this.immuneTo.get());
                if (this.canSpawnFarFromPlayer) builder.canSpawnFarFromPlayer();

                return builder.build(this.id.toString());
            });

            //supplier.listen(type -> ManasAttributeRegistry.registerNew(() -> type, this.attributeBuilder));
            return supplier;
        }

        @Override
        public Holder<EntityType<T>> endAsHolder() {
            return this.end().getRegistrar().getHolder(this.id);
        }
    }

    /**
     * Builder class for {@link Attribute}s.
     */
    public static class AttributeBuilder<R extends AbstractRegister<R>> extends ContentBuilder<Attribute, R> {
        protected double defaultValue;
        protected double minimumValue;
        protected double maximumValue;

        protected boolean syncable;
        protected Map<Supplier<EntityType<? extends LivingEntity>>, Double> applicableEntityTypes;
        protected boolean applyToAll = false;

        private AttributeBuilder(R register, String name) {
            super(register, name);
            this.defaultValue = 1;
            this.minimumValue = 0;
            this.maximumValue = 100_000_000_000D;
            this.syncable = false;
            this.applicableEntityTypes = new HashMap<>();
        }

        /**
         * Sets the default value of the attribute.
         */
        public AttributeBuilder<R> withDefaultValue(double defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        /**
         * Sets the minimum value of the attribute.
         */
        public AttributeBuilder<R> withMinimumValue(double minimumValue) {
            this.minimumValue = minimumValue;
            return this;
        }

        /**
         * Sets the maximum value of the attribute.
         */
        public AttributeBuilder<R> withMaximumValue(double maximumValue) {
            this.maximumValue = maximumValue;
            return this;
        }

        /**
         * Makes the attribute syncable.
         */
        public AttributeBuilder<R> syncable() {
            this.syncable = true;
            return this;
        }

        /**
         * Applies the attribute to all given entities with the default value.
         */
        public AttributeBuilder<R> applyTo(double defaultValue, Supplier<EntityType<? extends LivingEntity>> entityType) {
            this.applicableEntityTypes.put(entityType, defaultValue);
            return this;
        }

        /**
         * Applies the attribute to all given entities with the default value.
         */
        public AttributeBuilder<R> applyTo(Supplier<EntityType<? extends LivingEntity>> entityType) {
            return applyTo(this.defaultValue, entityType);
        }

        /**
         * Applies the attribute to all given entities with the default value.
         */
        @SafeVarargs
        public final AttributeBuilder<R> applyTo(double defaultValue, Supplier<EntityType<? extends LivingEntity>>... entityType) {
            for (Supplier<EntityType<? extends LivingEntity>> typeSupplier : entityType) {
                this.applicableEntityTypes.put(typeSupplier, defaultValue);
            }
            return this;
        }

        /**
         * Applies the attribute to all given entities with the default value.
         */
        @SafeVarargs
        public final AttributeBuilder<R> applyTo(Supplier<EntityType<? extends LivingEntity>>... entityType) {
            return applyTo(this.defaultValue, entityType);
        }

        /**
         * Applies the attribute to all given entities with the default value.
         */
        public AttributeBuilder<R> applyTo(double defaultValue, List<Supplier<EntityType<? extends LivingEntity>>> entityTypes) {
            for (Supplier<EntityType<? extends LivingEntity>> typeSupplier : entityTypes) {
                this.applicableEntityTypes.put(typeSupplier, defaultValue);
            }
            return this;
        }

        /**
         * Applies the attribute to all given entities with the default value.
         */
        public AttributeBuilder<R> applyTo(List<Supplier<EntityType<? extends LivingEntity>>> entityTypes) {
            return applyTo(this.defaultValue, entityTypes);
        }

        /**
         * Applies the attribute to all known entities.
         */
        public AttributeBuilder<R> applyToAll() {
            this.applyToAll = true;
            return this;
        }

        @Override
        public RegistrySupplier<Attribute> end() {
            String name = String.format("%s.attribute.%s", this.id.getNamespace(), this.id.getPath().replaceAll("/", "."));
            Attribute attribute = new RangedAttribute(name, this.defaultValue, this.minimumValue, this.maximumValue).setSyncable(this.syncable);

            Holder<Attribute> holder = Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, this.id, attribute);
            RegistrySupplier<Attribute> supplier = this.register.attributes.register(this.id, () -> attribute);

            if (this.applyToAll) {
                ManasAttributeRegistry.registerToAll(builder -> builder.add(holder, this.defaultValue));
            } else this.applicableEntityTypes.forEach((typeSupplier, defaultValue) ->
                    ManasAttributeRegistry.register(typeSupplier, builder -> builder.add(holder, defaultValue)));
            return supplier;
        }

        public Holder<Attribute> endAsHolder() {
            return this.end().getRegistrar().getHolder(this.id);
        }
    }

    public static class BlockEntityBuilder<R extends AbstractRegister<R>, T extends BlockEntity> extends ContentBuilder<BlockEntityType<T>, R> {
        protected final BlockEntitySupplier<T> factory;
        protected Type<?> dataFixerType;
        protected Set<Supplier<? extends BaseEntityBlock>> validBlocks;

        private BlockEntityBuilder(R register, String name, BlockEntitySupplier<T> factory) {
            super(register, name);
            this.factory = factory;
            this.dataFixerType = null;
            this.validBlocks = new HashSet<>();
        }

        /**
         * Sets the data fixer type of the block entity.
         */
        public BlockEntityBuilder<R, T> withDataFixerType(Type<?> dataFixerType) {
            this.dataFixerType = dataFixerType;
            return this;
        }

        /**
         * Adds a valid block for the block entity.
         */
        @SafeVarargs
        public final BlockEntityBuilder<R, T> withValidBlocks(Supplier<? extends BaseEntityBlock> validBlock, Supplier<? extends BaseEntityBlock>... validBlocks) {
            this.validBlocks.add(validBlock);
            for (Supplier<? extends BaseEntityBlock> block : validBlocks) {
                this.validBlocks.add(block);
            }
            return this;
        }

        @Override
        public RegistrySupplier<BlockEntityType<T>> end() {
            return this.register.blockEntities.register(this.id, () -> BlockEntityType.Builder.of(this.factory, this.validBlocks.stream().map(Supplier::get).toArray(Block[]::new)).build(this.dataFixerType));
        }

        @Override
        public Holder<BlockEntityType<T>> endAsHolder() {
            return this.end().getRegistrar().getHolder(this.id);
        }
    }

    /**
     * Builder class for {@link MobEffect}s.
     */
    public static class MobEffectBuilder<R extends AbstractRegister<R>> extends ContentBuilder<MobEffect, R> {
        private final MobEffectSupplier<MobEffect> effectFactory;
        private MobEffectCategory category;
        private int colorId = 1;
        private @Nullable ParticleOptions particleOptions = null;
        private SoundEvent addedSoundEvent;
        private int blendDurationTicks;

        private MobEffectBuilder(R register, String name, MobEffectSupplier<MobEffect> effectFactory) {
            super(register, name);
            this.effectFactory = effectFactory;
        }

        public MobEffectBuilder<R> withCategory(final MobEffectCategory category) {
            this.category = category;
            return this;
        }

        public MobEffectBuilder<R> withColor(int colorId) {
            this.colorId = colorId;
            return this;
        }

        public MobEffectBuilder<R> withParticleOptions(ParticleOptions particleOptions) {
            this.particleOptions = particleOptions;
            return this;
        }

        public MobEffectBuilder<R> withAddedSoundEvent(SoundEvent soundEvent) {
            this.addedSoundEvent = soundEvent;
            return this;
        }

        public MobEffectBuilder<R> withBlendDurationTicks(int ticks) {
            this.blendDurationTicks = ticks;
            return this;
        }

        @Override
        public RegistrySupplier<MobEffect> end() {
            return this.register.mobEffects.register(this.id, () -> {
                MobEffect effect = this.effectFactory.create(this.category, this.colorId)
                        .setBlendDuration(this.blendDurationTicks);
                if (this.addedSoundEvent != null) effect = effect.withSoundOnAdded(this.addedSoundEvent);
                if (this.particleOptions != null) effect.particleFactory = (mobEffectInstance) -> this.particleOptions;
                return effect;
            });
        }

        @Override
        public Holder<MobEffect> endAsHolder() {
            return this.end().getRegistrar().getHolder(this.id);
        }

        @FunctionalInterface
        public interface MobEffectSupplier<T extends MobEffect> {
            T create(MobEffectCategory category, int color);
        }
    }

    /**
     * Builder class for {@link Potion}s.
     */
    public static class PotionBuilder<R extends AbstractRegister<R>, T extends Potion> extends ContentBuilder<T, R> {
        private final PotionSupplier<T> potionFactory;
        private final ArrayList<MobEffectInstance> instances = new ArrayList<>();
        private PotionBuilder(R register, String name, PotionSupplier<T> effectFactory) {
            super(register, name);
            this.potionFactory = effectFactory;
        }

        public PotionBuilder<R, T> withEffectInstance(final MobEffectInstance instance) {
            this.instances.add(instance);
            return this;
        }

        @Override
        public RegistrySupplier<T> end() {
            return this.register.potions.register(this.id,
                    () -> this.potionFactory.create(this.instances.toArray(new MobEffectInstance[0])));
        }

        @Override
        public Holder<T> endAsHolder() {
            return this.end().getRegistrar().getHolder(this.id);
        }

        @FunctionalInterface
        public interface PotionSupplier<T extends Potion> {
            T create(MobEffectInstance[] instance);
        }
    }

    /**
     * Base class for content builders.
     * Contains the common fields and methods.
     */
    public static abstract class ContentBuilder<T, R extends AbstractRegister<R>> {
        protected final R register;
        protected final ResourceLocation id;

        public ContentBuilder(final R register, final String name) {
            this.register = register;
            this.id = ResourceLocation.fromNamespaceAndPath(register.modId, name);
        }

        public R build() {
            end();
            return this.register;
        }

        public abstract RegistrySupplier<T> end();
        public abstract Holder<T> endAsHolder();
    }
}
