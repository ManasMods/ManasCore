/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.team.impl;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.manasmods.manascore.team.ModuleConstants;
import io.github.manasmods.manascore.team.api.TeamType;
import io.github.manasmods.manascore.team.impl.builtin.AllyTeamType;
import io.github.manasmods.manascore.team.impl.builtin.PartyTeamType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This is the Registry for {@link TeamType}s.
 * It also holds the built-in {@link AllyTeamType} and {@link PartyTeamType}.
 */
public class TeamRegistry {
    private static final RegistrarManager MANAGER = RegistrarManager.get(ModuleConstants.MOD_ID);
    private static final ResourceLocation REGISTRY_ID = ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "team_types");
    public static final Registrar<TeamType<?>> TEAM_TYPES = MANAGER.<TeamType<?>>builder(REGISTRY_ID).syncToClients().build();
    public static final ResourceKey<Registry<TeamType<?>>> KEY = ResourceKey.createRegistryKey(TEAM_TYPES.key().location());

    private static final DeferredRegister<TeamType<?>> BUILTIN = DeferredRegister.create(ModuleConstants.MOD_ID, KEY);
    public static final RegistrySupplier<AllyTeamType> ALLY = BUILTIN.register("ally", AllyTeamType::new);
    public static final RegistrySupplier<PartyTeamType> PARTY = BUILTIN.register("party", PartyTeamType::new);

    private static List<TeamType<?>> sortedCache = List.of();
    private static int sortedCacheSize = -1;

    private TeamRegistry() {
    }

    /**
     * Used to register the built-in team types.
     */
    public static void init() {
        BUILTIN.register();
    }

    /**
     * Used to get all registered types, highest priority first.
     * The result is cached and rebuilt when the registry size changes.
     */
    public static List<TeamType<?>> sortedByPriority() {
        int size = TEAM_TYPES.getIds().size();
        if (size != sortedCacheSize) {
            List<TeamType<?>> list = new ArrayList<>();
            for (TeamType<?> type : TEAM_TYPES) list.add(type);
            list.sort(Comparator.comparingInt((TeamType<?> type) -> type.getPriority()).reversed().thenComparing(TeamType::getId, Comparator.nullsLast(Comparator.naturalOrder())));
            sortedCache = List.copyOf(list);
            sortedCacheSize = size;
        }
        return sortedCache;
    }
}
