/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.cinematic.api.client;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import io.github.manasmods.manascore.cinematic.api.Cutscene;

public class CinematicClientEvents {
    public static Event<CutsceneStart> CUTSCENE_START = EventFactory.createEventResult();
    public static Event<CameraShake> CAMERA_SHAKE = EventFactory.of(listeners -> (speed, offset) -> {
        float total = 0;
        for (CameraShake listener : listeners) total += listener.getShake(speed, offset);
        return total;
    });

    @FunctionalInterface
    public interface CutsceneStart {
        EventResult start(Cutscene cutscene);
    }

    @FunctionalInterface
    public interface CameraShake {
        float getShake(int speed, int offset);
    }
}
