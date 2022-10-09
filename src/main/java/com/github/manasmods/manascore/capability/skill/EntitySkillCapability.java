package com.github.manasmods.manascore.capability.skill;

import com.github.manasmods.manascore.ManasCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class EntitySkillCapability {
    static final Capability<InternalSkillStorage> CAP = CapabilityManager.get(new CapabilityToken<>() {});
    private static final ResourceLocation ID = new ResourceLocation(ManasCore.MOD_ID, "skills");

    private static void register(RegisterCapabilitiesEvent e) {
        e.register(InternalSkillStorage.class);
    }

    private static void attach(AttachCapabilitiesEvent<Entity> e) {
        e.addCapability(ID, new GenericCapabilityProvider<>(CAP, EntitySkillCapabilityStorage::new));
    }

    private static void login(final PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getPlayer() instanceof ServerPlayer player) {
            load(player).syncAll();
        }
    }

    private static void clonePlayer(final PlayerEvent.Clone e) {
        if (e.getPlayer() instanceof ServerPlayer player) {
            load(player).syncAll();
        }
    }

    private static void respawn(final PlayerEvent.PlayerRespawnEvent e) {
        if (e.getPlayer() instanceof ServerPlayer player) {
            load(player).syncAll();
        }
    }

    private static void changeDimension(final PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getPlayer() instanceof ServerPlayer player) {
            load(player).syncAll();
        }
    }

    private static void startTracking(PlayerEvent.StartTracking e) {
        load(e.getTarget()).syncPlayer(e.getPlayer());
    }

    public static void init(IEventBus modEventBus) {
        //Register Capability
        modEventBus.addListener(EntitySkillCapability::register);

        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        //Register attachment handler
        forgeEventBus.addGenericListener(Entity.class, EntitySkillCapability::attach);
        //Register sync handler
        forgeEventBus.addListener(EntitySkillCapability::login);
        forgeEventBus.addListener(EntitySkillCapability::clonePlayer);
        forgeEventBus.addListener(EntitySkillCapability::respawn);
        forgeEventBus.addListener(EntitySkillCapability::changeDimension);
        forgeEventBus.addListener(EntitySkillCapability::startTracking);
    }

    public static InternalSkillStorage load(final Entity entity) {
        InternalSkillStorage cap = entity.getCapability(CAP).orElseGet(EntitySkillCapabilityStorage::new);
        cap.setOwner(entity);
        return cap;
    }
}
