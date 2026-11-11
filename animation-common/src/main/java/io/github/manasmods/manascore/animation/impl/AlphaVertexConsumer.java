/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.animation.impl;

import com.mojang.blaze3d.vertex.VertexConsumer;

public class AlphaVertexConsumer implements VertexConsumer {
    private final VertexConsumer consumer;
    private final float opacity;
    public AlphaVertexConsumer(VertexConsumer consumer, float opacity) {
        this.consumer = consumer;
        this.opacity = opacity;
    }

    public VertexConsumer addVertex(float x, float y, float z) {
        this.consumer.addVertex(x, y, z);
        return this;
    }

    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        this.consumer.setColor(red, green, blue, (int) (alpha * this.opacity));
        return this;
    }

    public VertexConsumer setUv(float u, float v) {
        this.consumer.setUv(u, v);
        return this;
    }

    public VertexConsumer setUv1(int u, int v) {
        this.consumer.setUv1(u, v);
        return this;
    }

    public VertexConsumer setUv2(int u, int v) {
        this.consumer.setUv2(u, v);
        return this;
    }

    public VertexConsumer setNormal(float x, float y, float z) {
        this.consumer.setNormal(x, y, z);
        return this;
    }
}
