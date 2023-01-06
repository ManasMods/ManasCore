/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.world.gen.biome;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.ApiStatus.AvailableSince;

@AvailableSince("1.0.0.0")
@SuppressWarnings({"unused", "UnusedReturnValue"})
@RequiredArgsConstructor
public class BiomeGenerationSettingsHelper {
    private final BiomeGenerationSettings.Builder biomeGenSettings;

    public BiomeGenerationSettingsHelper(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers) {
        this.biomeGenSettings = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
    }

    public static BiomeGenerationSettingsHelper from(BiomeGenerationSettings.Builder biomeGenSettings) {
        return new BiomeGenerationSettingsHelper(biomeGenSettings);
    }

    public BiomeGenerationSettings.Builder toBuilder() {
        return biomeGenSettings;
    }

    public BiomeGenerationSettingsHelper apply(DefaultBiomeFeature defaultBiomeFeature) {
        defaultBiomeFeature.apply(this.biomeGenSettings);
        return this;
    }

    public BiomeGenerationSettingsHelper apply(DefaultBiomeFeature defaultBiomeFeature, DefaultBiomeFeature... defaultBiomeFeatures) {
        apply(defaultBiomeFeature);

        for (DefaultBiomeFeature feature : defaultBiomeFeatures) {
            apply(feature);
        }

        return this;
    }

    public BiomeGenerationSettingsHelper addCarver(GenerationStep.Carving pCarving, Holder<ConfiguredWorldCarver<?>> pCarver) {
        biomeGenSettings.addCarver(pCarving, pCarver);
        return this;
    }

    public BiomeGenerationSettingsHelper addFeature(GenerationStep.Decoration generationStep, Holder<PlacedFeature> feature) {
        biomeGenSettings.addFeature(generationStep, feature);
        return this;
    }

    public BiomeGenerationSettings finishBiomeSettings() {
        return this.biomeGenSettings.build();
    }


    @FunctionalInterface
    public interface DefaultBiomeFeature {
        void apply(BiomeGenerationSettings.Builder builder);
    }
}
