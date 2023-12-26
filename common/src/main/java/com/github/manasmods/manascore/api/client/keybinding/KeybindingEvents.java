package com.github.manasmods.manascore.api.client.keybinding;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;

public interface KeybindingEvents {
    Event<KeybindingRegister> REGISTER = EventFactory.createLoop();

    @FunctionalInterface
    interface KeybindingRegister {
        void register(KeybindingFactory registry);
    }

    @FunctionalInterface
    interface KeybindingFactory {
        ManasKeybinding create(ManasKeybinding keybinding);
    }
}
