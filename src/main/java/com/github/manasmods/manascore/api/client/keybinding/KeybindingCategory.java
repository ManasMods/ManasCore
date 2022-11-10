/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.client.keybinding;

import com.github.manasmods.manascore.ManasCore;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.ApiStatus.AvailableSince;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@AvailableSince("1.0.0.0")
@NonExtendable
@RequiredArgsConstructor(staticName = "of")
public final class KeybindingCategory {
    private final String id;

    public String getCategoryString() {
        return String.format("%s.category.%s", ManasCore.MOD_ID, this.id);
    }
}
