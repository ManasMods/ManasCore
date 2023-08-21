package com.github.manasmods.manascore.network;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.network.toclient.SyncSkillsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ManasCoreNetwork {
    private static final String PROTOCOL_VERSION = ModList.get().getModFileById(ManasCore.MOD_ID).versionString().replaceAll("\\.", "");
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ManasCore.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int i = 0;
        INSTANCE.registerMessage(i++, SyncSkillsPacket.class, SyncSkillsPacket::toBytes, SyncSkillsPacket::new, SyncSkillsPacket::handle);
    }

    public static <T> void toServer(T message) {
        INSTANCE.sendToServer(message);
    }
}