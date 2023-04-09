package com.github.manasmods.manascore.client.animation;

import com.github.manasmods.manascore.api.client.animation.AnimationDefinition;
import com.github.manasmods.manascore.api.client.animation.AnimationPart;
import com.github.manasmods.manascore.api.client.animation.renderer.IAnimationRenderer;
import com.github.manasmods.manascore.api.client.animation.renderer.RendererRegistry;
import com.github.manasmods.manascore.api.util.Lookup;
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
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

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

                //Create fast storage
                Lookup lookup = null;

                if(FMLEnvironment.dist.isClient()) {
                    lookup = Lookup.create(rendererConfig);
                }

                CompiledAnimationPart.CompiledRendererConfig compiledRendererConfig = new CompiledAnimationPart.CompiledRendererConfig(rendererConfig.getName(), renderer, lookup, rendererConfig.getParams());

                List<AnimationPart.RendererConfig.ConfigValue> configValues = compiledRendererConfig.getParams().values().stream().sorted(Comparator.comparingInt(AnimationPart.RendererConfig.ConfigValue::getPosition)).collect(Collectors.toList());

                if(FMLEnvironment.dist.isClient()) {
                    Method renderMethod = null;

                    for(Method method : Objects.requireNonNull(renderer).getClass().getMethods()) {
                        if(method.getName().equalsIgnoreCase("render")) {
                            //First 3 params are always PoseStack, MultiBufferSource and RunningAnimation
                            for(int i = 3; i < method.getParameterCount(); i++) {
                                int j = i - 3;

                                Parameter parameter = method.getParameters()[i];

                                if(configValues.get(j).getValue() != null && !configValues.get(j).getValue().getClass().equals(parameter.getType())) {
                                    throw new RuntimeException(String.format("Type %s doesn't equal type %s from config", parameter.getType(), configValues.get(j).getValue().getClass()));
                                }
                            }

                            renderMethod = method;
                        }
                    }

                    compiledRendererConfig.setCachedRenderMethod(renderMethod);
                }

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
