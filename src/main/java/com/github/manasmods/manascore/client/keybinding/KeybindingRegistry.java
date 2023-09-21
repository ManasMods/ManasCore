package com.github.manasmods.manascore.client.keybinding;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.client.keybinding.KeybindingCategory;
import com.github.manasmods.manascore.api.client.keybinding.ManasKeybinding;
import com.github.manasmods.manascore.network.ManasCoreNetwork;
import com.github.manasmods.manascore.network.toserver.RequestSkillActivationPacket;
import com.github.manasmods.manascore.network.toserver.RequestSkillReleasePacket;
import com.github.manasmods.manascore.network.toserver.RequestSkillTogglePacket;
import com.mojang.blaze3d.platform.InputConstants;
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
            KeybindingCategory category = KeybindingCategory.of("test");
            keybindings.add(new ManasKeybinding("manascore.keybinding.test", InputConstants.KEY_X,
                    category, () -> log.info("Pressing"),
                    duration -> log.info("Released in {} Seconds", duration / 1000.0)
            ));
            keybindings.add(new ManasKeybinding("manascore.keybinding.test_action_once", InputConstants.KEY_C,
                    category, () -> log.info("Pressed"),
                    duration -> log.info("Released in {} Seconds", duration / 1000.0), true
            ));
            keybindings.add(new ManasKeybinding("manascore.keybinding.test_press", category, () -> log.info("Pressed")));

            //duration - milliseconds
            keybindings.add(new ManasKeybinding("manascore.keybinding.skill", category,
                    () -> ManasCoreNetwork.INSTANCE.sendToServer(new RequestSkillActivationPacket(0)),
                    duration -> ManasCoreNetwork.INSTANCE.sendToServer(new RequestSkillReleasePacket(0, (int) (duration / 50)))
            ));
            keybindings.add(new ManasKeybinding("manascore.keybinding.skill_toggle", category,
                    () -> ManasCoreNetwork.INSTANCE.sendToServer(new RequestSkillTogglePacket())));
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
