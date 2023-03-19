package com.github.manasmods.manascore.client.animation;

import com.github.manasmods.manascore.api.client.animation.AnimationDefinition;
import com.github.manasmods.manascore.api.client.animation.AnimationPart;
import com.github.manasmods.manascore.api.client.animation.renderer.IAnimationRenderer;
import com.github.manasmods.manascore.api.client.animation.renderer.RendererRegistry;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AnimationManager extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();

    private Map<ResourceLocation, CompiledAnimation> byName = ImmutableMap.of();

    public AnimationManager(Gson pGson) {
        super(pGson, "animations");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        ImmutableMap.Builder<ResourceLocation, CompiledAnimation> builder = ImmutableMap.builder();

        for(Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            if (resourcelocation.getPath().startsWith("_")) continue; //Forge: filter anything beginning with "_" as it's used for metadata.

            try {
                AnimationDefinition animationDefinition = AnimationDefinition.fromJson(GsonHelper.convertToJsonObject(entry.getValue(), "top element"));
                CompiledAnimation animation = this.compile(animationDefinition);

                builder.put(resourcelocation, animation);
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                LOGGER.error("Parsing error loading recipe {}", resourcelocation, jsonparseexception);
            }
        }

        this.byName = builder.build();
    }

    public CompiledAnimation compile(AnimationDefinition definition) {
        List<CompiledAnimationPart> compiledAnimationParts = new ArrayList<>();

        for(AnimationPart part : definition.getParts()) {
            String name = part.getName();
            String abortAnimation = part.getAbortAnimation();

            List<CompiledAnimationPart.CompiledRendererConfig> rendererChain = new LinkedList<>();

            for(AnimationPart.RendererConfig rendererConfig : part.getRendererChain()) {
                //Lookup renderer
                ResourceLocation resourceLocation = new ResourceLocation(rendererConfig.getName());

                if(!RendererRegistry.REGISTRY.get().containsKey(resourceLocation)) {
                    throw new IllegalStateException("No renderer registered for " + resourceLocation.toString());
                }

                IAnimationRenderer renderer = RendererRegistry.REGISTRY.get().getValue(resourceLocation);

                CompiledAnimationPart.CompiledRendererConfig compiledRendererConfig = new CompiledAnimationPart.CompiledRendererConfig(rendererConfig.getName(), renderer, rendererConfig.getParams());

                rendererChain.add(compiledRendererConfig);
            }

            compiledAnimationParts.add(new CompiledAnimationPart(name, abortAnimation, rendererChain));
        }

        return new CompiledAnimation(definition.getName(), definition.isCancelable(), definition.getDurationTicks(), compiledAnimationParts);
    }

    @Override
    public @NotNull String getName() {
        return super.getName();
    }
}
