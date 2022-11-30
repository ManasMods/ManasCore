/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.data.gen;


import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.ApiStatus.AvailableSince;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

@AvailableSince("1.0.0.0")
public abstract class LanguageProvider extends net.minecraftforge.common.data.LanguageProvider {
    public LanguageProvider(GatherDataEvent gatherDataEvent, String modid) {
        this(gatherDataEvent, modid, "en_us");
    }

    public LanguageProvider(GatherDataEvent gatherDataEvent, String modid, String localeCode) {
        super(gatherDataEvent.getGenerator(), modid, localeCode);
    }

    @OverrideOnly
    protected abstract void generate();

    @Override
    protected void addTranslations() {
        generate();
    }

    protected <T extends Block> void addBlock(RegistryObject<T> obj, String value) {
        add(obj.get(), value);
    }

    protected <T extends Item> void addItem(RegistryObject<T> obj, String value) {
        add(obj.get(), value);
    }
    protected <T extends EntityType<?>> void addEntityType(RegistryObject<T> obj, String value) {
        add(obj.get(), value);
    }
}
