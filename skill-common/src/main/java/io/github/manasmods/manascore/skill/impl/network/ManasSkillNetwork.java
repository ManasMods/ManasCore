/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.impl.network;

import io.github.manasmods.manascore.network.api.util.NetworkUtils;
import io.github.manasmods.manascore.skill.impl.network.c2s.RequestSkillActivationPacket;
import io.github.manasmods.manascore.skill.impl.network.c2s.RequestSkillReleasePacket;
import io.github.manasmods.manascore.skill.impl.network.c2s.RequestSkillScrollPacket;
import io.github.manasmods.manascore.skill.impl.network.c2s.RequestSkillTogglePacket;

public class ManasSkillNetwork {
    public static void init() {
        NetworkUtils.registerC2SPayload(RequestSkillActivationPacket.TYPE, RequestSkillActivationPacket.STREAM_CODEC, RequestSkillActivationPacket::handle);
        NetworkUtils.registerC2SPayload(RequestSkillReleasePacket.TYPE, RequestSkillReleasePacket.STREAM_CODEC, RequestSkillReleasePacket::handle);
        NetworkUtils.registerC2SPayload(RequestSkillScrollPacket.TYPE, RequestSkillScrollPacket.STREAM_CODEC, RequestSkillScrollPacket::handle);
        NetworkUtils.registerC2SPayload(RequestSkillTogglePacket.TYPE, RequestSkillTogglePacket.STREAM_CODEC, RequestSkillTogglePacket::handle);
    }
}
