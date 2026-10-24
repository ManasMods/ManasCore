/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.cinematic.impl.network.s2c;

import io.github.manasmods.manascore.cinematic.api.Cutscene;
import io.github.manasmods.manascore.cinematic.api.CutsceneHelper;
import io.github.manasmods.manascore.cinematic.impl.client.CutsceneClientHandler;
import io.github.manasmods.manascore.cinematic.impl.client.DialogueOverlay;

class ClientAccess {
    static void handle(CutscenePayload packet) {
        Cutscene cutscene = CutsceneHelper.getById(packet.id());
        if (cutscene != null) CutsceneClientHandler.start(cutscene, packet.anchor(), packet.yaw());
    }

    static void handle(DialoguePayload packet) {
        DialogueOverlay.add(packet.speaker(), packet.line(), packet.instant());
    }
}
