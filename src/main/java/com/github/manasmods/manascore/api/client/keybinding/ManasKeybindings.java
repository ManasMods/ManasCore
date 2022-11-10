/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.client.keybinding;

import com.github.manasmods.manascore.client.keybinding.KeybindingRegistry;
import org.jetbrains.annotations.ApiStatus.AvailableSince;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@AvailableSince("1.0.0.0")
@NonExtendable
public final class ManasKeybindings {
    /**
     * Registers {@link ManasKeybinding} to ManasCore and Minecraft.
     * This has to be used in the mod constructor
     */
    public static void register(ManasKeybinding... keybinding) {
        KeybindingRegistry.register(keybinding);
    }
}
