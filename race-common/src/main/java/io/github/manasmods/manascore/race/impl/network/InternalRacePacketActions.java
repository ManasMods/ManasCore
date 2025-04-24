/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.race.impl.network;

import dev.architectury.networking.NetworkManager;
import io.github.manasmods.manascore.race.api.ManasRace;
import io.github.manasmods.manascore.race.impl.network.c2s.RequestRaceAbilityActivationPacket;
import io.github.manasmods.manascore.race.impl.network.c2s.RequestRaceAbilityReleasePacket;
import io.github.manasmods.manascore.race.impl.network.c2s.RequestRaceEvolutionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class InternalRacePacketActions {
    private InternalRacePacketActions() {
    }

    /**
     * This Method sends packet for the {@link ManasRace} Ability Activation.
     * Only executes on client using the dist executor.
     */
    public static void sendRaceAbilityActivationPacket() {
        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        NetworkManager.sendToServer(new RequestRaceAbilityActivationPacket());
    }

    /**
     * This Method sends packet for the {@link ManasRace} Ability Release.
     * Only executes on client using the dist executor.
     */
    public static void sendRaceAbilityReleasePacket(int heldTick) {
        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        NetworkManager.sendToServer(new RequestRaceAbilityReleasePacket(heldTick));
    }

    /**
     * This Method sends packet for the {@link ManasRace} Evolution.
     * Only executes on client using the dist executor.
     */
    public static void sendRaceEvolutionPacket(ResourceLocation evolution) {
        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        NetworkManager.sendToServer(new RequestRaceEvolutionPacket(evolution));
    }
}
