/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.keybind.api;

import io.github.manasmods.manascore.keybind.ModuleConstants;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class KeybindingCategory {
    private final String name;

    public String getCategoryString() {
        return String.format("%s.category.%s", ModuleConstants.MOD_ID, this.name);
    }
}