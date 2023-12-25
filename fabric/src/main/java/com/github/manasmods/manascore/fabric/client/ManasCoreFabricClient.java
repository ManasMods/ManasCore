package com.github.manasmods.manascore.fabric.client;

import com.github.manasmods.manascore.network.NetworkManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ManasCoreFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkManager.CHANNEL, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                //TODO handle packet
            });
        });
    }
}
