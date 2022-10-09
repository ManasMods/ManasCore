package com.github.manasmods.manascore.api.skills.listener;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import net.minecraftforge.client.event.InputEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Event listener interface for {@link ManasSkill}s.
 * <p>
 * This listener subscribes to the <strong>client side</strong> of the {@link InputEvent.KeyInputEvent}.
 * To use this Listener, just implement it in your {@link ManasSkill} class.
 * <p>
 * Keep in mind that you're not allowed to use client side only classes in Registry Objects!
 */
@ApiStatus.AvailableSince("0.0.0.26")
public interface KeyInputListener {
    void onKeyInput(final ManasSkillInstance instance, final InputEvent.KeyInputEvent event);
}
