package com.github.manasmods.manascore.core;

import com.github.manasmods.manascore.api.world.chunk.ChunkEvents;
import com.github.manasmods.manascore.api.world.chunk.ChunkEvents.ChunkTickPhase;
import com.github.manasmods.manascore.storage.StorageManager;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel extends Level {
    protected MixinServerLevel(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, Supplier<ProfilerFiller> profiler, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, profiler, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Inject(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getProfiler()Lnet/minecraft/util/profiling/ProfilerFiller;", shift = Shift.AFTER))
    private void onPreTickChunk(LevelChunk pChunk, int pRandomTickSpeed, CallbackInfo ci) {
        ProfilerFiller profiler = getProfiler();
        profiler.push("manascoreChunkTickEventPost");
        ChunkEvents.CHUNK_TICK.invoker().tick(ChunkTickPhase.START, pChunk, pRandomTickSpeed);
        profiler.pop();
    }

    @Inject(method = "tickChunk", at = @At("RETURN"))
    private void onPostTickChunk(LevelChunk pChunk, int pRandomTickSpeed, CallbackInfo ci) {
        ProfilerFiller profiler = getProfiler();
        profiler.push("manasCoreSyncCheck");
        if (pChunk.manasCore$getCombinedStorage().isDirty()) StorageManager.syncTracking(pChunk, true);
        profiler.popPush("manascoreChunkTickEventPost");
        ChunkEvents.CHUNK_TICK.invoker().tick(ChunkTickPhase.END, pChunk, pRandomTickSpeed);
        profiler.pop();
    }
}
