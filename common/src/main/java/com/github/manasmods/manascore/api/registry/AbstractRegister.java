package com.github.manasmods.manascore.api.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import lombok.NonNull;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

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

    AbstractRegister(final String modId) {
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
     */
    public void init() {
        if (blocks != null) blocks.register();
        if (items != null) items.register();
    }

    /**
     * Creates a new {@link ItemBuilder} for the given name.
     *
     * @param name The name of the {@link Item}.
     * @return A new {@link ItemBuilder} instance.
     */
    public ItemBuilder<R> item(final String name) {
        if (this.items == null) this.items = DeferredRegister.create(this.modId, Registries.ITEM);
        return new ItemBuilder<>(self(), name);
    }

    protected BlockItemBuilder<R> blockItem(final String name) {
        if (this.items == null) this.items = DeferredRegister.create(this.modId, Registries.ITEM);
        return new BlockItemBuilder<>(self(), name);
    }

    public BlockBuilder<R> block(final String name) {
        if (this.blocks == null) this.blocks = DeferredRegister.create(this.modId, Registries.BLOCK);
        return new BlockBuilder<>(self(), name);
    }


    /**
     * Builder class for {@link Item}s.
     */
    public static class ItemBuilder<R extends AbstractRegister<R>> extends ContentBuilder<Item, R> {
        protected Item.Properties properties;
        protected Function<Item.Properties, Item> itemFactory;

        private ItemBuilder(final R register, final String name) {
            super(register, name);
            this.properties = new Item.Properties();
            this.itemFactory = Item::new;
        }

        public ItemBuilder<R> withProperties(final Item.Properties properties) {
            this.properties = properties;
            return this;
        }

        public ItemBuilder<R> withProperties(Consumer<Item.Properties> properties) {
            properties.accept(this.properties);
            return this;
        }

        public ItemBuilder<R> withProperties(Function<Item.Properties, Item.Properties> properties) {
            this.properties = properties.apply(this.properties);
            return this;
        }

        public ItemBuilder<R> withItemFactory(final Function<Item.Properties, Item> itemFactory) {
            this.itemFactory = itemFactory;
            return this;
        }

        public ItemBuilder<R> withStackSize(final int stackSize) {
            this.properties.stacksTo(stackSize);
            return this;
        }

        @Override
        public RegistrySupplier<Item> end() {
            return this.register.items.register(this.id, () -> this.itemFactory.apply(this.properties));
        }
    }

    public static class BlockItemBuilder<R extends AbstractRegister<R>> extends ContentBuilder<Item, R> {
        protected Item.Properties properties;
        protected BiFunction<RegistrySupplier<Block>, Item.Properties, BlockItem> itemFactory;
        @Nullable
        protected RegistrySupplier<Block> parentBlockRegistryEntry = null;

        private BlockItemBuilder(R register, String name) {
            super(register, name);
            this.itemFactory = (blockRegistrySupplier, properties) -> new BlockItem(blockRegistrySupplier.get(), properties);
            this.properties = new Item.Properties();
        }

        public BlockItemBuilder<R> withProperties(final Item.Properties properties) {
            this.properties = properties;
            return this;
        }

        public BlockItemBuilder<R> withProperties(Consumer<Item.Properties> properties) {
            properties.accept(this.properties);
            return this;
        }

        public BlockItemBuilder<R> withProperties(Function<Item.Properties, Item.Properties> properties) {
            this.properties = properties.apply(this.properties);
            return this;
        }

        public BlockItemBuilder<R> withItemFactory(final BiFunction<RegistrySupplier<Block>, Item.Properties, BlockItem> itemFactory) {
            this.itemFactory = itemFactory;
            return this;
        }

        public BlockItemBuilder<R> withStackSize(final int stackSize) {
            this.properties.stacksTo(stackSize);
            return this;
        }

        protected void setParentBlockRegistryEntry(@NonNull RegistrySupplier<Block> parentBlockRegistryEntry) {
            this.parentBlockRegistryEntry = parentBlockRegistryEntry;
        }

        @Override
        public RegistrySupplier<Item> end() {
            if (this.parentBlockRegistryEntry == null) throw new IllegalStateException("Parent block registry entry must not be null!");
            return this.register.items.register(this.id, () -> this.itemFactory.apply(this.parentBlockRegistryEntry, this.properties));
        }
    }

    public static class BlockBuilder<R extends AbstractRegister<R>> extends ContentBuilder<Block, R> {
        private Function<Block.Properties, Block> blockFactory;
        private Block.Properties properties;
        private AbstractRegister.BlockItemBuilder<R> blockItemBuilder;

        private BlockBuilder(R register, String name) {
            super(register, name);
            this.blockFactory = Block::new;
            this.properties = BlockBehaviour.Properties.ofFullCopy(Blocks.STONE);
            this.blockItemBuilder = register.blockItem(name);
        }

        public BlockBuilder<R> withBlockItem(BlockItemFactory<R> blockItemFactory) {
            this.blockItemBuilder = blockItemFactory.modify(this.blockItemBuilder);
            return this;
        }

        public BlockBuilder<R> withProperties(final Block.Properties properties) {
            this.properties = properties;
            return this;
        }

        public BlockBuilder<R> withProperties(Consumer<Block.Properties> properties) {
            properties.accept(this.properties);
            return this;
        }

        public BlockBuilder<R> withProperties(Function<Block.Properties, Block.Properties> properties) {
            this.properties = properties.apply(this.properties);
            return this;
        }

        @Override
        public RegistrySupplier<Block> end() {
            RegistrySupplier<Block> blockSupplier = this.register.blocks.register(this.id, () -> this.blockFactory.apply(this.properties));
            this.blockItemBuilder.setParentBlockRegistryEntry(blockSupplier);
            this.blockItemBuilder.end();
            return blockSupplier;
        }

        @FunctionalInterface
        public interface BlockItemFactory<R extends AbstractRegister<R>> {
            BlockItemBuilder<R> modify(BlockItemBuilder<R> builder);
        }
    }

    /**
     * Base class for content builders.
     * Contains the common fields and methods.
     */
    protected static abstract class ContentBuilder<T, R extends AbstractRegister<R>> {
        protected final R register;
        protected final ResourceLocation id;

        private ContentBuilder(final R register, final String name) {
            this.register = register;
            this.id = new ResourceLocation(this.register.modId, name);
        }

        public R build() {
            end();
            return this.register;
        }

        public abstract RegistrySupplier<T> end();
    }
}
