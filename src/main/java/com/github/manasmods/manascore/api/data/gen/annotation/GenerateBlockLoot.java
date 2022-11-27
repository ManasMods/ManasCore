/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.data.gen.annotation;

import org.jetbrains.annotations.ApiStatus.AvailableSince;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@AvailableSince("2.0.3.0")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GenerateBlockLoot {
    @AvailableSince("2.0.3.0")
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface SelfDrop {}

    @AvailableSince("2.0.3.0")
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface OtherDrop {
        String value();
    }

    @AvailableSince("2.0.3.0")
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface OreDrop {
        String value();
    }

    @AvailableSince("2.0.3.0")
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface LeavesDrop {
        String value();

        float[] chances() default {0.05F, 0.0625F, 0.083333336F, 0.1F};
    }

    @AvailableSince("2.0.3.0")
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DoorDrop {}

    @AvailableSince("2.0.3.0")
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface SlabDrop {}
}
