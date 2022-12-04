/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.data.gen;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;

@RequiredArgsConstructor
public enum RenderType {
    DEFAULT(null), // pass no value into the render type (causes the builder to decide the value)
    SOLID(new ResourceLocation("minecraft", "solid")),
    CUTOUT(new ResourceLocation("minecraft", "cutout")),
    CUTOUT_MIPPED(new ResourceLocation("minecraft", "cutout_mipped")),
    CUTOUT_MIPPED_ALL(new ResourceLocation("minecraft", "cutout_mipped_all")),
    TRANSLUCENT(new ResourceLocation("minecraft", "translucent")),
    TRIPWIRE(new ResourceLocation("minecraft", "tripwire"));

    @Getter
    private final ResourceLocation id;
}
