package com.github.manasmods.manascore.client.keybinding;

import com.github.manasmods.manascore.ManasCore;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class KeybindingCategory {
    private final String id;

    public String getCategoryString() {
        return String.format("%s.category.%s", ManasCore.MOD_ID, this.id);
    }
}
