package com.github.manasmods.manascore.client.animation;

import com.github.manasmods.manascore.api.client.animation.AnimationPart;
import com.github.manasmods.manascore.api.client.animation.renderer.IAnimationRenderer;
import com.github.manasmods.manascore.api.util.Lookup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@Getter
public class CompiledAnimationPart {

    private final String name;

    /**
     * Will be resolved at run-time
     */
    private final String abortAnimation;

    private final List<CompiledRendererConfig> rendererChain;

    public CompiledAnimationPart(String name, String abortAnimation, List<CompiledRendererConfig> rendererChain) {
        this.name = name;
        this.abortAnimation = abortAnimation;

        this.rendererChain = rendererChain;
    }


    @Data
    @RequiredArgsConstructor
    public static class CompiledRendererConfig {
        private final String name;

        private final IAnimationRenderer renderer;
        private Method cachedRenderMethod;

        private final Lookup lookup;

        private final Map<String, AnimationPart.RendererConfig.ConfigValue> params;
    }

}
