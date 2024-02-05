package com.github.manasmods.manascore.network;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.network.toclient.SyncSkillsPacket;
import com.github.manasmods.manascore.network.toserver.*;
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
        //to client
        INSTANCE.registerMessage(i++, SyncSkillsPacket.class, SyncSkillsPacket::toBytes, SyncSkillsPacket::new, SyncSkillsPacket::handle);

        //to server
        INSTANCE.registerMessage(++i, RequestSkillActivationPacket.class, RequestSkillActivationPacket::toBytes, RequestSkillActivationPacket::new, RequestSkillActivationPacket::handle);
        INSTANCE.registerMessage(++i, RequestSkillReleasePacket.class, RequestSkillReleasePacket::toBytes, RequestSkillReleasePacket::new, RequestSkillReleasePacket::handle);
        INSTANCE.registerMessage(++i, RequestSkillScrollPacket.class, RequestSkillScrollPacket::toBytes, RequestSkillScrollPacket::new, RequestSkillScrollPacket::handle);
        INSTANCE.registerMessage(++i, RequestSkillTogglePacket.class, RequestSkillTogglePacket::toBytes, RequestSkillTogglePacket::new, RequestSkillTogglePacket::handle);

        INSTANCE.registerMessage(++i, RequestSweepChancePacket.class, RequestSweepChancePacket::toBytes, RequestSweepChancePacket::new, RequestSweepChancePacket::handle);
    }

    public static <T> void toServer(T message) {
        INSTANCE.sendToServer(message);
    }
}