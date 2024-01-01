package com.github.manasmods.manascore;

import com.github.manasmods.manascore.api.registry.Register;
import com.github.manasmods.manascore.api.world.entity.EntityEvents;
import com.github.manasmods.manascore.client.ManasCoreClient;
import com.github.manasmods.manascore.network.NetworkManager;
import com.github.manasmods.manascore.skill.SkillRegistry;
import com.github.manasmods.manascore.storage.StorageManager;
import com.github.manasmods.manascore.world.entity.attribute.ManasAttributeRegistry;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ManasCore {
    public static final String MOD_ID = "manascore";
    public static final Logger Logger = LogManager.getLogger("ManasCore");
    public static final Register REGISTER = new Register(MOD_ID);

    public static void init() {
        setupEventListeners();
        setupCustomRegistries();
        REGISTER.init(ManasCore::staticContentInit);
        LifecycleEvent.SETUP.register(StorageManager::init);
        NetworkManager.init();
        ManasAttributeRegistry.init();
        if (Platform.getEnvironment() == Env.CLIENT) {
            ManasCoreClient.init();
        }
    }

    private static void setupEventListeners() {
        EntityEvent.LIVING_HURT.register((entity, source, amount) -> EntityEvents.LIVING_ATTACK.invoker().attack(entity, source, amount));
    }

    private static void setupCustomRegistries() {
        SkillRegistry.init();
    }

    private static void staticContentInit() {

    }
}
