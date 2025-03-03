package io.github.manasmods.manascore.config.imp.network.s2c;


import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.config.ConfigRegistry;
import io.github.manasmods.manascore.config.ModuleConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Handles the syncing of configuration data from the server to the client.
 * This is triggered when a player joins a multiplayer server.
 */
public record SyncConfigToClientPayload(
        Map<String, String> configData
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncConfigToClientPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "sync_config_to_client"));
    public static final StreamCodec<FriendlyByteBuf, SyncConfigToClientPayload> STREAM_CODEC = CustomPacketPayload.codec(SyncConfigToClientPayload::encode, SyncConfigToClientPayload::new);

    public SyncConfigToClientPayload(FriendlyByteBuf buf) {
        this(buf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readUtf));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(this.configData, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeUtf);
    }

    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.CLIENT) return;
        context.queue(() -> ConfigRegistry.loadConfigSyncData(this.configData));
    }

    @NotNull
    public CustomPacketPayload.Type<SyncConfigToClientPayload> type() {
        return TYPE;
    }
}
