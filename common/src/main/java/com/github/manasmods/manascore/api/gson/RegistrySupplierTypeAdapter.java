package com.github.manasmods.manascore.api.gson;

import com.github.manasmods.manascore.ManasCore;
import com.google.gson.*;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class RegistrySupplierTypeAdapter<T> implements JsonSerializer<RegistrySupplier<T>>, JsonDeserializer<RegistrySupplier<T>> {
    private static final RegistrarManager registrarManager = RegistrarManager.get(ManasCore.MOD_ID);

    @Override
    public RegistrySupplier<T> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        var actualType = ((ParameterizedType) type).getActualTypeArguments()[0];
        ResourceLocation id = context.deserialize(jsonElement, ResourceLocation.class);

        if (actualType.equals(Item.class)) {
            return (RegistrySupplier<T>) registrarManager.get(Registries.ITEM).delegate(id);
        }

        throw new JsonParseException("Registry type " + actualType + "is not supported.");
    }

    @Override
    public JsonElement serialize(RegistrySupplier<T> registrySupplier, Type type, JsonSerializationContext context) {
        return context.serialize(registrySupplier.getRegistryId());
    }
}
