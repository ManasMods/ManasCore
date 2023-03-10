package com.github.manasmods.manascore.client.animation;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class AnimationManager extends SimpleJsonResourceReloadListener {
    public AnimationManager(Gson pGson) {
        super(pGson, "animations");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {

    }

    @Override
    public @NotNull String getName() {
        return super.getName();
    }
}
