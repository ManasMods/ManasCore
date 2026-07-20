/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.animation;

import dev.architectury.platform.Platform;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.animation.api.PlayerAnimationAPI;
import io.github.manasmods.manascore.animation.network.ManasAnimationNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class ManasCoreAnimation {
    public static void init() {
        ManasAnimationNetwork.init();
        if (Platform.getEnvironment() == Env.CLIENT) registerClientAnimationLoader();
    }

    private static void registerClientAnimationLoader() {
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES,
                (ResourceManagerReloadListener) PlayerAnimationAPI::loadClientSideAnimations,
                ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "manas_animations"));
    }
}
