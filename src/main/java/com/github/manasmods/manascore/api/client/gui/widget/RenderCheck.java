/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.client.gui.widget;

import org.jetbrains.annotations.ApiStatus.AvailableSince;

@AvailableSince("1.0.0.0")
@FunctionalInterface
public interface RenderCheck {
    boolean check();
}
