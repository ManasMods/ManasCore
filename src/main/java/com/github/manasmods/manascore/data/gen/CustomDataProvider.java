package com.github.manasmods.manascore.data.gen;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import lombok.extern.log4j.Log4j2;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Log4j2
public abstract class CustomDataProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();
    private final Path outputPath;

    public CustomDataProvider(String path, DataGenerator generator) {
        this.outputPath = generator.getOutputFolder().resolve("data").resolve(path);
    }

    protected abstract void run(BiConsumer<ResourceLocation, Supplier<JsonElement>> consumer);

    @Override
    public void run(HashCache pCache) {
        Map<ResourceLocation, Supplier<JsonElement>> map = Maps.newHashMap();
        BiConsumer<ResourceLocation, Supplier<JsonElement>> consumer = (location, jsonElementSupplier) -> {
            Supplier<JsonElement> supplier = map.put(location, jsonElementSupplier);
            if (supplier != null) {
                throw new IllegalStateException(String.format("Duplicate %s for %s", getName(), location));
            }
        };

        run(consumer);

        map.forEach((location, jsonElementSupplier) -> {
            Path path = this.outputPath.resolve(location.getNamespace()).resolve(location.getPath() + ".json");
            try {
                DataProvider.save(GSON, pCache, jsonElementSupplier.get(), path);
            } catch (IOException e) {
                log.error("Couldn't save {}", path, e);
            }
        });
    }
}
