package com.github.manasmods.manascore.api.client.keybinding;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.Getter;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ManasKeybinding extends KeyMapping {
    private static final HashMap<ManasKeybinding, Long> PRESSED_KEYBINDINGS = new HashMap<>();
    @Getter
    private final KeyBindingAction action;
    @Getter
    @Nullable
    private final Runnable release;

    /**
     * Creates a Keybinding which handles the given action automatically.
     *
     * @param langKey    Translation String
     * @param defaultKey Default Key
     * @param category   Category
     * @param action     Action when pressed
     */
    public ManasKeybinding(String langKey, int defaultKey, KeybindingCategory category, KeyBindingAction action) {
        this(langKey, InputConstants.Type.KEYSYM.getOrCreate(defaultKey), category, action, null);
    }

    /**
     * Creates a Keybinding which handles the given action automatically.
     *
     * @param langKey    Translation String
     * @param defaultKey Default Key
     * @param category   Category
     * @param action     Action when pressed
     */
    public ManasKeybinding(String langKey, InputConstants.Key defaultKey, KeybindingCategory category, KeyBindingAction action) {
        this(langKey, defaultKey, category, action, null);
    }

    /**
     * Creates a Keybinding without a default key.
     *
     * @param langKey  Translation String
     * @param category Category
     * @param action   Action when pressed.
     */
    public ManasKeybinding(String langKey, KeybindingCategory category, KeyBindingAction action) {
        this(langKey, InputConstants.UNKNOWN.getValue(), category, action, null);
    }

    /**
     * Creates a Keybinding without a default key.
     *
     * @param langKey  Translation String
     * @param category Category
     * @param action   Action when pressed.
     * @param release  Action when released
     */
    public ManasKeybinding(String langKey, KeybindingCategory category, KeyBindingAction action, @Nullable KeyBindingRelease release) {
        this(langKey, InputConstants.UNKNOWN.getValue(), category, action, release);
    }

    /**
     * Creates a Keybinding which handles the given action automatically.
     *
     * @param langKey    Translation String
     * @param defaultKey Default Key
     * @param category   Category
     * @param action     Action when pressed
     * @param release    Action when released
     */
    public ManasKeybinding(String langKey, int defaultKey, KeybindingCategory category, KeyBindingAction action, @Nullable KeyBindingRelease release) {
        this(langKey, InputConstants.Type.KEYSYM.getOrCreate(defaultKey), category, action, release);
    }

    /**
     * Creates a Keybinding which handles the given action automatically.
     *
     * @param langKey    Translation String
     * @param defaultKey Default Key
     * @param category   Category
     * @param action     Action when pressed
     * @param release    Action when released
     */
    public ManasKeybinding(String langKey, InputConstants.Key defaultKey, KeybindingCategory category, KeyBindingAction action, @Nullable KeyBindingRelease release) {
        super(langKey, defaultKey.getType(), defaultKey.getValue(), category.getCategoryString());
        if (release == null) {
            this.action = action;
            this.release = null;
        } else {
            this.action = () -> {
                if (!PRESSED_KEYBINDINGS.containsKey(this)) {
                    PRESSED_KEYBINDINGS.put(this, System.currentTimeMillis());
                    action.onPress();
                }
            };
            this.release = () -> {
                if (PRESSED_KEYBINDINGS.containsKey(this)) {
                    long start = PRESSED_KEYBINDINGS.remove(this);
                    long end = System.currentTimeMillis();
                    release.onRelease(end - start);
                }
            };
        }
    }


    @FunctionalInterface
    public interface KeyBindingAction {
        void onPress();
    }

    @FunctionalInterface
    public interface KeyBindingRelease {
        /**
         * @param duration in milliseconds
         */
        void onRelease(long duration);
    }
}
