/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.client.keybinding;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.Getter;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.ApiStatus.AvailableSince;

@AvailableSince("1.0.0.0")
public class ManasKeybinding extends KeyMapping {
    @Getter
    private final KeyBindingAction action;

    /**
     * Creates a Keybinding which handles the given action automatically.
     * No need to create an {@link Mod.EventBusSubscriber}.
     *
     * @param langKey    Translation String
     * @param defaultKey Default Key
     * @param category   Category
     * @param action     Action when pressed
     */
    public ManasKeybinding(String langKey, int defaultKey, KeybindingCategory category, KeyBindingAction action) {
        this(langKey, InputConstants.Type.KEYSYM.getOrCreate(defaultKey), category, action);
    }

    /**
     * Creates a Keybinding which handles the given action automatically.
     * No need to create an {@link Mod.EventBusSubscriber}.
     *
     * @param langKey    Translation String
     * @param defaultKey Default Key
     * @param category   Category
     * @param action     Action when pressed
     */
    public ManasKeybinding(String langKey, InputConstants.Key defaultKey, KeybindingCategory category, KeyBindingAction action) {
        super(langKey, KeyConflictContext.UNIVERSAL, defaultKey, category.getCategoryString());
        this.action = action;
    }

    /**
     * Creates a Keybinding without a default key.
     *
     * @param langKey  Translation String
     * @param category Category
     * @param action   Action when pressed.
     */
    public ManasKeybinding(String langKey, KeybindingCategory category, KeyBindingAction action) {
        this(langKey, InputConstants.UNKNOWN.getValue(), category, action);
    }


    @FunctionalInterface
    public interface KeyBindingAction {
        void onPress();
    }
}
