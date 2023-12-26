package com.github.manasmods.manascore.network;

import com.github.manasmods.manascore.network.toclient.SyncChunkStoragePacket;
import com.github.manasmods.manascore.network.toclient.SyncEntityStoragePacket;
import com.github.manasmods.manascore.network.toclient.SyncWorldStoragePacket;
import dev.architectury.networking.NetworkChannel;
import net.minecraft.resources.ResourceLocation;

public class NetworkManager {
    public static final NetworkChannel CHANNEL = NetworkChannel.create(new ResourceLocation("manascore", "main"));

    public static void init() {
        CHANNEL.register(SyncEntityStoragePacket.class, SyncEntityStoragePacket::encode, SyncEntityStoragePacket::new, SyncEntityStoragePacket::handle);
        CHANNEL.register(SyncChunkStoragePacket.class, SyncChunkStoragePacket::encode, SyncChunkStoragePacket::new, SyncChunkStoragePacket::handle);
        CHANNEL.register(SyncWorldStoragePacket.class, SyncWorldStoragePacket::encode, SyncWorldStoragePacket::new, SyncWorldStoragePacket::handle);
    }
}
