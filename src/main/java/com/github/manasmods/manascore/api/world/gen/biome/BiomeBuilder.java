/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.world.gen.biome;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.sounds.Music;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import org.jetbrains.annotations.ApiStatus.AvailableSince;

import java.awt.Color;
import java.util.Optional;

@AvailableSince("2.0.0.0")
@SuppressWarnings("unused")
@RequiredArgsConstructor
public class BiomeBuilder {
    private final BiomeGenerationSettingsHelper generationSettingsHelper;
    private final MobSpawnHelper mobSpawnHelper;
    private final Biome.BiomeBuilder biomeBuilder = new Biome.BiomeBuilder();
    @Setter
    private float temperature = 0.8F;
    private float downfall = 0.4F;
    private int waterColor = 4159204;
    private int waterFogColor = 329011;
    private int fogColor = 12638463;
    private Music backgroundMusic = null;
    private Optional<Integer> grassColorOverride = Optional.empty();
    private BiomeSpecialEffects.GrassColorModifier grassColorModifier = BiomeSpecialEffects.GrassColorModifier.NONE;

    public BiomeBuilder grassColor(Color color) {
        grassColorOverride = Optional.of(color.getRGB());
        return this;
    }

    public BiomeBuilder grassModifier(BiomeSpecialEffects.GrassColorModifier grassColorModifier) {
        this.grassColorModifier = grassColorModifier;
        return this;
    }

    public BiomeBuilder downfall(float downfall) {
        this.downfall = downfall;
        return this;
    }

    public BiomeBuilder waterColor(Color color) {
        this.waterColor = color.getRGB();
        return this;
    }

    public BiomeBuilder waterFogColor(Color color) {
        this.waterFogColor = color.getRGB();
        return this;
    }

    public BiomeBuilder fogColor(Color color) {
        this.fogColor = color.getRGB();
        return this;
    }

    public BiomeBuilder backgroundMusic(Music music) {
        this.backgroundMusic = music;
        return this;
    }

    public BiomeBuilder temperature(float temperature) {
        this.temperature = temperature;
        return this;
    }

    //If you're looking to set the rain type, it seems to have been removed in 1.19.4. It is now based on the temperature of a biome and is not manually set.

    public Biome build() {
        BiomeSpecialEffects.Builder specialEffects = new BiomeSpecialEffects.Builder()
            .waterColor(waterColor)
            .waterFogColor(waterFogColor)
            .fogColor(fogColor)
            .skyColor(calculateSkyColor(temperature))
            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
            .backgroundMusic(backgroundMusic)
            .grassColorModifier(grassColorModifier);

        this.grassColorOverride.ifPresent(specialEffects::grassColorOverride);

        return biomeBuilder
            .temperature(temperature)
            .downfall(downfall)
            .specialEffects(specialEffects.build())
            .mobSpawnSettings(mobSpawnHelper.finishMobSpawnSettings())
            .generationSettings(generationSettingsHelper.finishBiomeSettings())
            .build();
    }

    public static BiomeBuilder forest(BiomeGenerationSettingsHelper generationSettingsHelper, MobSpawnHelper mobSpawnHelper) {
        return new BiomeBuilder(generationSettingsHelper, mobSpawnHelper)
            .temperature(0.7F)
            .downfall(0.8F);
    }

    private static int calculateSkyColor(float p_194844_) {
        float $$1 = p_194844_ / 3.0F;
        $$1 = Mth.clamp($$1, -1.0F, 1.0F);
        return Mth.hsvToRgb(0.62222224F - $$1 * 0.05F, 0.5F + $$1 * 0.1F, 1.0F);
    }
}
