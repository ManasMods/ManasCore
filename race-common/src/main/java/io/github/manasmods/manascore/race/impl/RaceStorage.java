/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.race.impl;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;
import io.github.manasmods.manascore.race.ModuleConstants;
import io.github.manasmods.manascore.race.api.*;
import io.github.manasmods.manascore.network.api.util.Changeable;
import io.github.manasmods.manascore.skill.api.EntityEvents;
import io.github.manasmods.manascore.storage.api.Storage;
import io.github.manasmods.manascore.storage.api.StorageEvents;
import io.github.manasmods.manascore.storage.api.StorageKey;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;

@Log4j2
public class RaceStorage extends Storage implements Races {
    @Getter
    private static StorageKey<RaceStorage> key = null;
    public static final int INSTANCE_UPDATE = 20;
    private static final String RACE_KEY = "race_key";

    public static void init() {
        StorageEvents.REGISTER_ENTITY_STORAGE.register(registry ->
                key = registry.register(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "race_storage"),
                        RaceStorage.class, LivingEntity.class::isInstance, target -> new RaceStorage((LivingEntity) target)));

        EntityEvents.LIVING_POST_TICK.register(entity -> {
            Level level = entity.level();
            if (level.isClientSide()) return;
            Races storage = RaceAPI.getRaceFrom(entity);
            handleSkillTick(entity, level, storage);
            storage.markDirty();
        });

        PlayerEvent.PLAYER_RESPAWN.register((player, conqueredEnd, removalReason) -> {
            Level level = player.level();
            if (level.isClientSide() || conqueredEnd) return;
            Races storage = RaceAPI.getRaceFrom(player);
            Optional<ManasRaceInstance> optional = storage.getRace();
            if (optional.isEmpty()) return;
            optional.get().addAttributeModifiers(player);
        });
    }

    private static void handleSkillTick(LivingEntity entity, Level level, Races storage) {
        MinecraftServer server = level.getServer();
        if (server == null) return;
        boolean shouldTickRace = server.getTickCount() % INSTANCE_UPDATE == 0;
        if (!shouldTickRace) return;
        tickRace(entity, storage);
    }

    private static void tickRace(LivingEntity entity, Races storage) {
        Optional<ManasRaceInstance> optional = storage.getRace();
        if (optional.isEmpty()) return;

        ManasRaceInstance raceInstance = optional.get();
        if (!raceInstance.canActivateAbility(entity)) return;
        if (!raceInstance.canTick(entity)) return;
        if (RaceEvents.RACE_PRE_TICK.invoker().tick(raceInstance, entity).isFalse()) return;
        raceInstance.onTick(entity);
        RaceEvents.RACE_POST_TICK.invoker().tick(raceInstance, entity);
    }

    private ManasRaceInstance raceInstance = null;

    protected RaceStorage(LivingEntity holder) {
        super(holder);
    }

    public Optional<ManasRaceInstance> getRace() {
        return Optional.ofNullable(this.raceInstance);
    }

    public boolean setRace(@NonNull ManasRaceInstance race, boolean evolution, boolean teleportToSpawn) {
        ManasRaceInstance instance = this.raceInstance;
        Changeable<Boolean> teleport = Changeable.of(teleportToSpawn);
        EventResult result = RaceEvents.SET_RACE.invoker().set(instance, getOwner(), race, evolution, teleport);
        if (result.isFalse()) return false;

        LivingEntity owner = this.getOwner();
        if (instance != null && instance != race) {
            instance.removeAttributeModifiers(owner);
            if (evolution) instance.onRaceEvolution(owner, race);
        }

        race.markDirty();
        race.addAttributeModifiers(owner);
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
        this.raceInstance.addAttributeModifiers(this.getOwner());
    }

    protected LivingEntity getOwner() {
        return (LivingEntity) this.holder;
    }
}
