/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.race.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;
import io.github.manasmods.manascore.network.api.util.Changeable;
import io.github.manasmods.manascore.race.ManasCoreRace;
import io.github.manasmods.manascore.race.ModuleConstants;
import io.github.manasmods.manascore.race.api.*;
import io.github.manasmods.manascore.skill.api.EntityEvents;
import io.github.manasmods.manascore.storage.api.Storage;
import io.github.manasmods.manascore.storage.api.StorageEvents;
import io.github.manasmods.manascore.storage.api.StorageKey;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Log4j2
public class RaceStorage extends Storage implements Races {
    @Getter
    private static StorageKey<RaceStorage> key = null;
    public static final int INSTANCE_UPDATE = 20;
    public static final Multimap<UUID, TickingRace> tickingRaces = ArrayListMultimap.create();
    private static final String RACE_KEY = "race_key";
    /*
    private static final StorageEvents.RegisterStorage<Entity> listener = new StorageEvents.RegisterStorage<Entity>() {
        @Override
        public void register(StorageEvents.StorageRegistry<Entity> registry) {
            ManasCoreRace.LOG.info("ManasRace storage event triggered");
            key = registry.register(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "race_storage"),
                    RaceStorage.class, LivingEntity.class::isInstance, target -> new RaceStorage((LivingEntity) target));
            ManasCoreRace.LOG.info(key != null ? "storage Key registered " + key.toString() : "storage Key failed to register");
        }
    };*/

    public static void init() {
        ManasCoreRace.LOG.info("event registration");
        StorageEvents.RegisterStorage<Entity> listener = new StorageEvents.RegisterStorage<Entity>() {
            @Override
            public void register(StorageEvents.StorageRegistry<Entity> registry) {
                ManasCoreRace.LOG.info("storage event triggered");
                key = registry.register(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "race_storage"),
                        RaceStorage.class, LivingEntity.class::isInstance, target -> new RaceStorage((LivingEntity) target));
                ManasCoreRace.LOG.info(key != null ? "storage Key registered " + key.toString() : "storage Key failed to register");
            }
        };
        StorageEvents.REGISTER_ENTITY_STORAGE.register(listener);
        ManasCoreRace.LOG.info("storage event registered? {}", String.valueOf(StorageEvents.REGISTER_ENTITY_STORAGE.isRegistered(listener)));
        EntityEvents.LIVING_POST_TICK.register(entity -> {
            Level level = entity.level();
            if (level.isClientSide()) return;
            Races storage = RaceAPI.getRaceFrom(entity);
            handleRaceTick(entity, level, storage);
            if (entity instanceof Player player) handleRaceHeldTick(player, storage);
        });

        PlayerEvent.PLAYER_QUIT.register(player -> tickingRaces.removeAll(player.getUUID()));
        PlayerEvent.CHANGE_DIMENSION.register((player, resourceKey, resourceKey1) -> tickingRaces.removeAll(player.getUUID()));
    }

    private static void handleRaceTick(LivingEntity entity, Level level, Races storage) {
        MinecraftServer server = level.getServer();
        if (server == null) return;

        boolean shouldTickRace = server.getTickCount() % INSTANCE_UPDATE == 0;
        if (!shouldTickRace) return;
        tickRace(entity, storage);
    }

    private static void handleRaceHeldTick(Player player, Races storage) {
        if (!tickingRaces.containsKey(player.getUUID())) return;
        tickingRaces.get(player.getUUID()).removeIf(skill -> !skill.tick(storage, player));
        storage.markDirty();
    }

    private static void tickRace(LivingEntity entity, Races storage) {
        Optional<ManasRaceInstance> optional = storage.getRace();
        if (optional.isEmpty()) return;

        ManasRaceInstance instance = optional.get();
        if (instance.isOnCooldown()) {
            if (!RaceEvents.RACE_UPDATE_COOLDOWN.invoker().cooldown(instance, entity, instance.getCooldown()).isFalse())
                instance.setCooldown(instance.getCooldown() - 1);
            storage.checkAndMarkDirty(instance);
        }

        if (!instance.canActivateAbility(entity)) return;
        if (!instance.canTick(entity)) return;
        if (RaceEvents.RACE_PRE_TICK.invoker().tick(instance, entity).isFalse()) return;
        instance.onTick(entity);
        RaceEvents.RACE_POST_TICK.invoker().tick(instance, entity);
        storage.checkAndMarkDirty(instance);
    }

    private ManasRaceInstance raceInstance = null;

    protected RaceStorage(LivingEntity holder) {
        super(holder);
    }

    public Optional<ManasRaceInstance> getRace() {
        return Optional.ofNullable(this.raceInstance);
    }

    public boolean setRace(@NonNull ManasRaceInstance race, boolean evolution, boolean teleportToSpawn, @Nullable MutableComponent component) {
        ManasRaceInstance instance = this.raceInstance;
        Changeable<Boolean> teleport = Changeable.of(teleportToSpawn);
        Changeable<MutableComponent> raceMessage = Changeable.of(component);
        EventResult result = RaceEvents.SET_RACE.invoker().set(instance, getOwner(), race, evolution, teleport, raceMessage);
        if (result.isFalse()) return false;

        LivingEntity owner = this.getOwner();
        if (instance != null && instance != race) {
            instance.removeAttributeModifiers(owner);
            if (evolution) {
                race.deserialize(instance.serialize(new CompoundTag()));
                instance.onRaceEvolution(owner, race);
            }
        }

        if (raceMessage.isPresent()) getOwner().sendSystemMessage(raceMessage.get());
        race.markDirty();
        race.addAttributeModifiers(owner);
        owner.setHealth(owner.getMaxHealth());
        race.onRaceSet(owner);

        race.learnIntrinsicSkills(owner);
        this.raceInstance = race;

        if (teleport.get() && getOwner() instanceof ServerPlayer player) SpawnPointHelper.teleportToNewSpawn(player);
        markDirty();
        return true;
    }

    @Override
    public void save(CompoundTag data) {
        if (this.raceInstance == null) return;
        data.put(RACE_KEY, this.raceInstance.toNBT());
    }

    @Override
    public void load(CompoundTag data) {
        if (!data.contains(RACE_KEY)) return;
        this.raceInstance = ManasRaceInstance.fromNBT(data.getCompound(RACE_KEY));
    }

    protected LivingEntity getOwner() {
        return (LivingEntity) this.holder;
    }
}
