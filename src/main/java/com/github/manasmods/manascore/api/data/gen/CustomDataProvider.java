/*
 * Copyright (c) 2022. ManasMods
 */
package com.github.manasmods.manascore.api.data.gen;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import lombok.extern.log4j.Log4j2;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus.AvailableSince;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@AvailableSince("1.0.0.0")
@Log4j2
public abstract class CustomDataProvider implements DataProvider {
    @ScheduledForRemoval(inVersion = "2.1.0.0")
    @Deprecated(forRemoval = true)
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();
    private final String outputPath;
    private final DataGenerator generator;

    public CustomDataProvider(String path, DataGenerator generator) {
        this.outputPath = path;
        this.generator = generator;
    }

    @OverrideOnly
    protected abstract void run(BiConsumer<ResourceLocation, Supplier<JsonElement>> consumer);

    @Override
    public void run(CachedOutput pOutput) {
        Map<ResourceLocation, Supplier<JsonElement>> map = Maps.newHashMap();
        BiConsumer<ResourceLocation, Supplier<JsonElement>> consumer = (location, jsonElementSupplier) -> {
            Supplier<JsonElement> supplier = map.put(location, jsonElementSupplier);
            if (supplier != null) {
                throw new IllegalStateException(String.format("Duplicate %s for %s", getName(), location));
            }
        };

        run(consumer);

        map.forEach((location, jsonElementSupplier) -> {
            Path path = this.generator.getOutputFolder()
                .resolve("data")
                .resolve(location.getNamespace())
                .resolve(this.outputPath)
                .resolve(location.getPath() + ".json");
            try {
                DataProvider.saveStable(pOutput, jsonElementSupplier.get(), path);
            } catch (IOException e) {
                log.error("Couldn't save {}", path, e);
            }
        });
    }
}
