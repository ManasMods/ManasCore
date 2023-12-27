package com.github.manasmods.manascore.api.registry;


import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

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

    /**
     * Builder class for {@link Item}s.
     */
    public static class ItemBuilder<R extends AbstractRegister<R>> extends ContentBuilder<Item, R> {
        private Item.Properties properties;
        private Function<Item.Properties, Item> itemFactory;

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

        @Override
        public RegistrySupplier<Item> end() {
            return this.register.items.register(this.id, () -> this.itemFactory.apply(this.properties));
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
