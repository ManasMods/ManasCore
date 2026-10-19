/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.cinematic.api.client;

import io.github.manasmods.manascore.cinematic.impl.client.CutsceneClientHandler;

public class CinematicClientAPI {
    public static boolean isPlaying() {
        return CutsceneClientHandler.isPlaying();
    }

    public static boolean isShowing() {
        return CutsceneClientHandler.isShowing();
    }
}
