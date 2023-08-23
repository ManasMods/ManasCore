package com.github.manasmods.manascore.client.keybinding;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.client.keybinding.KeybindingCategory;
import com.github.manasmods.manascore.api.client.keybinding.ManasKeybinding;
import lombok.extern.log4j.Log4j2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.ArrayList;
import java.util.Arrays;

@Internal
@Mod.EventBusSubscriber(modid = ManasCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
@Log4j2
public class KeybindingRegistry {
    private static final ArrayList<ManasKeybinding> keybindings = new ArrayList<>();

    static {
        if (!FMLEnvironment.production) {
            keybindings.add(new ManasKeybinding("manascore.keybinding.test", KeybindingCategory.of("test"),
                    () -> log.info("Pressed"),
                    duration -> log.info("Released in {} Seconds", duration / 1000.0)
            ));
        }
    }

    @SubscribeEvent
    public static void register(final RegisterKeyMappingsEvent e) {
        keybindings.forEach(e::register);
    }

    public static void register(ManasKeybinding... keybinding) {
        keybindings.addAll(Arrays.asList(keybinding));
    }

    public static void checkKeybindings(final InputEvent.Key e) {
        keybindings.forEach(keybinding -> {
            if (keybinding.isDown()) {
                keybinding.getAction().onPress();
            } else if (keybinding.getRelease() != null) {
                keybinding.getRelease().run();
            }
        });
    }
}
