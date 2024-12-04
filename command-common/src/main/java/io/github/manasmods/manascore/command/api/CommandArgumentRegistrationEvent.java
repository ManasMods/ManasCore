/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.command.api;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import io.github.manasmods.manascore.command.internal.CommandArgumentRegistry;

public interface CommandArgumentRegistrationEvent {
    Event<CommandArgumentRegistrationEvent> EVENT = EventFactory.createLoop();

    void register(CommandArgumentRegistry registry);
}
