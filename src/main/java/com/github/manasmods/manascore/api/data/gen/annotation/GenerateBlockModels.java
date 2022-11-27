/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.data.gen.annotation;

import org.jetbrains.annotations.ApiStatus.AvailableSince;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@AvailableSince("2.0.2.0")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GenerateBlockModels {
    @AvailableSince("2.0.2.0")
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface CubeAllModel {
        /**
         * ResourceLocation to the block providing a Texture
         */
        String value() default "";
    }

    @AvailableSince("2.0.2.0")
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface PillarModel {}

    @AvailableSince("2.0.2.0")
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface StairModel {
        /**
         * ResourceLocation to the block providing a Texture
         */
        String value();
    }

    @AvailableSince("2.0.2.0")
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface SlabModel {
        /**
         * ResourceLocation to the block providing a Texture
         */
        String value();
    }
}
