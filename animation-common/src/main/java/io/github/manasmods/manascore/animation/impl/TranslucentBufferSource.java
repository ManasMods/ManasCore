/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.animation.impl;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TranslucentBufferSource implements MultiBufferSource {
    private static final List<RenderType> GLINTS = List.of(RenderType.glint(), RenderType.glintTranslucent(), RenderType.entityGlint(), RenderType.entityGlintDirect(), RenderType.armorEntityGlint());
    private final MultiBufferSource source;
    private final float opacity;
    public TranslucentBufferSource(MultiBufferSource source, float opacity) {
        this.source = source;
        this.opacity = opacity;
    }

    public VertexConsumer getBuffer(RenderType type) {
        // Glint is additive and ignores vertex alpha, it would float over a faded item
        if (GLINTS.contains(type)) return EmptyVertexConsumer.INSTANCE;
        RenderType translucent = getTranslucentType(type);
        if (translucent == null) return this.source.getBuffer(type);
        return new AlphaVertexConsumer(this.source.getBuffer(translucent), this.opacity);
    }

    private static @Nullable RenderType getTranslucentType(RenderType type) {
        if (type.format() != DefaultVertexFormat.NEW_ENTITY) return null;
        if (!(type instanceof RenderType.CompositeRenderType composite)) return null;
        RenderType.CompositeState state = composite.state();
        if (!(state.textureState instanceof RenderStateShard.TextureStateShard textureState)) return null;
        ResourceLocation texture = textureState.texture.orElse(null);
        if (texture == null) return null;
        return state.cullState.enabled ? RenderType.itemEntityTranslucentCull(texture) : RenderType.entityTranslucent(texture);
    }
}
