package com.github.manasmods.manascore.fabric.client;

import com.github.manasmods.manascore.network.NetworkManager;
import com.github.manasmods.manascore.storage.CombinedStorage;
import com.github.manasmods.manascore.storage.StorageManager.StorageType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ManasCoreFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkManager.CHANNEL, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                Level level = client.level;
                if (level == null) return;
                boolean update = buf.readBoolean();
                StorageType type = buf.readEnum(StorageType.class);

                switch (type) {
                    case ENTITY -> {
                        Entity entity = client.level.getEntity(buf.readInt());
                        if (entity == null) return;
                        CompoundTag tag = buf.readNbt();
                        if (tag == null) return;
                        if (update) {
                            entity.manasCore$getCombinedStorage().handleUpdatePacket(tag);
                        } else {
                            entity.manasCore$setCombinedStorage(new CombinedStorage(entity, tag));
                        }
                    }
                }
            });
        });
    }
}
