package com.github.manasmods.manascore.api.client.keybinding;

import com.github.manasmods.manascore.ManasCore;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class KeybindingCategory {
    private final String name;

    public String getCategoryString() {
        return String.format("%s.category.%s", ManasCore.MOD_ID, this.name);
    }
}
