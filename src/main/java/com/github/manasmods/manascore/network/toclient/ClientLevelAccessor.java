package com.github.manasmods.manascore.network.toclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.function.Consumer;

public final class ClientLevelAccessor {
    static void execute(Consumer<ClientLevel> action) {
        action.accept(Minecraft.getInstance().level);
    }
}
