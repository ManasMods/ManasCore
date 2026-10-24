/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.animation;

import dev.architectury.platform.Platform;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.animation.api.PlayerAnimationAPI;
import io.github.manasmods.manascore.animation.network.ManasAnimationNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

/**
 * This is the entry point of the animation module.
 * The loader specific entry classes call {@link #init()} once on startup.
 */
public class ManasCoreAnimation {
    /**
     * Used to initialize the animation module.
     * Registers the animation packet through {@link ManasAnimationNetwork#init()} and, on the client, the animation reload listener.
     */
    public static void init() {
        ManasAnimationNetwork.init();
        if (Platform.getEnvironment() == Env.CLIENT) registerClientAnimationLoader();
    }

    private static void registerClientAnimationLoader() {
        if (Minecraft.getInstance() == null) return;
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES,
                (ResourceManagerReloadListener) PlayerAnimationAPI::loadClientSideAnimations,
                ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "manas_animations"));
    }
}
