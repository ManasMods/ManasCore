/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.race.impl.data;

import io.github.manasmods.manascore.race.api.ManasRace;
import io.github.manasmods.manascore.race.impl.RaceRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;

import java.util.concurrent.CompletableFuture;

public abstract class RaceTagProvider extends IntrinsicHolderTagsProvider<ManasRace> {
    public RaceTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, RaceRegistry.KEY, lookupProvider, manasSkill -> RaceRegistry.RACES.getKey(manasSkill).orElseThrow());
    }

    public RaceTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<ManasRace>> parentProvider) {
        super(output, RaceRegistry.KEY, lookupProvider, parentProvider, manasSkill -> RaceRegistry.RACES.getKey(manasSkill).orElseThrow());
    }
}
