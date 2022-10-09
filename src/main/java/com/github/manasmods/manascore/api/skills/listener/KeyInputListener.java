package com.github.manasmods.manascore.api.skills.listener;

import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import net.minecraftforge.client.event.InputEvent;

public interface KeyInputListener {
    void onKeyInput(final ManasSkillInstance instance, final InputEvent.KeyInputEvent event);
}
