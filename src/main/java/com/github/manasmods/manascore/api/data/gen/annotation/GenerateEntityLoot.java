/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.data.gen.annotation;

import org.jetbrains.annotations.ApiStatus.AvailableSince;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@AvailableSince("2.0.5.0")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GenerateEntityLoot {
    @AvailableSince("2.0.5.0")
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface WithLootTables {}

    @AvailableSince("2.0.5.0")
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface EmptyLootTable {}
}
