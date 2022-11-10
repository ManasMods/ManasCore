/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.tab.annotation;

import com.github.manasmods.manascore.api.tab.AbstractInventoryTab;
import org.jetbrains.annotations.ApiStatus.AvailableSince;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@AvailableSince("1.0.0.0")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ScreenForTab {
    Class<? extends AbstractInventoryTab> value();
}
