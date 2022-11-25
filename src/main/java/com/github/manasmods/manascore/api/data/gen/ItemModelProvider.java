/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.data.gen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.ApiStatus.AvailableSince;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

import java.util.Objects;

@AvailableSince("1.0.0.0")
@SuppressWarnings("unused")
public abstract class ItemModelProvider extends net.minecraftforge.client.model.generators.ItemModelProvider {
    public ItemModelProvider(final GatherDataEvent gatherDataEvent, String modId) {
        super(gatherDataEvent.getGenerator(), modId, gatherDataEvent.getExistingFileHelper());
    }

    @OverrideOnly
    protected abstract void generate();

    @Override
    protected void registerModels() {
        generate();
    }

    @AvailableSince("2.0.0.0")
    @NonExtendable
    protected final ResourceLocation rl(Item item) {
        return Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item));
    }

    @AvailableSince("2.0.0.0")
    @NonExtendable
    protected final String name(Item item) {
        return rl(item).getPath();
    }

    /**
     * Generates the item model json file.
     * Can be used for any default rendered {@link Item} object.
     *
     * @param item the target {@link Item}
     */
    protected void singleTexture(Item item) {
        singleTexture(item, item);
    }

    /**
     * Generates the item model json file.
     * Can be used for any default rendered {@link Item} object.
     *
     * @param item        the target {@link Item}
     * @param textureItem the texture providing {@link Item}
     */
    protected void singleTexture(Item item, Item textureItem) {
        getBuilder(name(item))
            .parent(new ModelFile.UncheckedModelFile(mcLoc("item/generated")))
            .texture("layer0", new ResourceLocation(rl(textureItem).getNamespace(), "item/" + name(item)));
    }

    /**
     * Generates the item model json file.
     * Can be used for any handheld rendered {@link Item} like Tools and Torches.
     *
     * @param item the target {@link Item}
     */
    protected void handheldSingleTexture(Item item) {
        handheldSingleTexture(item, item);
    }

    /**
     * Generates the item model json file.
     * Can be used for any handheld rendered {@link Item} like Tools and Torches.
     *
     * @param item        the target {@link Item}
     * @param textureItem the texture providing {@link Item}
     */
    protected void handheldSingleTexture(Item item, Item textureItem) {
        getBuilder(name(item))
            .parent(new ModelFile.UncheckedModelFile(mcLoc("item/handheld")))
            .texture("layer0", new ResourceLocation(rl(textureItem).getNamespace(), "item/" + name(item)));
    }
}
