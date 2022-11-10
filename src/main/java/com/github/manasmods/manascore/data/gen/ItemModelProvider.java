/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.data.gen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import java.util.Objects;

@SuppressWarnings("unused")
public abstract class ItemModelProvider extends net.minecraftforge.client.model.generators.ItemModelProvider {
    public ItemModelProvider(final GatherDataEvent gatherDataEvent, String modId) {
        super(gatherDataEvent.getGenerator(), modId, gatherDataEvent.getExistingFileHelper());
    }

    protected abstract void generate();

    @Override
    protected void registerModels() {
        generate();
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
        getBuilder(Objects.requireNonNull(item.getRegistryName()).getPath())
            .parent(new ModelFile.UncheckedModelFile(mcLoc("item/generated")))
            .texture("layer0", new ResourceLocation(Objects.requireNonNull(textureItem.getRegistryName()).getNamespace(), "item/" + textureItem.getRegistryName().getPath()));
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
        getBuilder(Objects.requireNonNull(item.getRegistryName()).getPath())
            .parent(new ModelFile.UncheckedModelFile(mcLoc("item/handheld")))
            .texture("layer0", new ResourceLocation(Objects.requireNonNull(textureItem.getRegistryName()).getNamespace(), "item/" + textureItem.getRegistryName().getPath()));
    }
}
