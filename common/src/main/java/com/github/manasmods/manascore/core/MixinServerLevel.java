package com.github.manasmods.manascore.core;

import com.github.manasmods.manascore.api.world.chunk.ChunkEvents;
import com.github.manasmods.manascore.api.world.chunk.ChunkEvents.ChunkTickPhase;
import com.github.manasmods.manascore.storage.StorageManager;
import com.github.manasmods.manascore.world.StoragePersistentState;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel extends Level {
    @Shadow
    @Final
    List<ServerPlayer> players;

    @Shadow
    public abstract DimensionDataStorage getDataStorage();

    @Shadow
    public abstract List<ServerPlayer> players();

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
        if (pChunk.manasCore$getCombinedStorage().isDirty()) {
            pChunk.setUnsaved(true);
            StorageManager.syncTracking(pChunk, true);
        }
        profiler.popPush("manascoreChunkTickEventPost");
        ChunkEvents.CHUNK_TICK.invoker().tick(ChunkTickPhase.END, pChunk, pRandomTickSpeed);
        profiler.pop();
    }

    @Override
    public Iterable<ServerPlayer> manasCore$getTrackingPlayers() {
        return this.players;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 1, shift = Shift.AFTER))
    private void onTick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        getProfiler().push("manasCoreSyncCheck");
        if (manasCore$getCombinedStorage().isDirty()) StorageManager.syncTracking(this, true);
        getProfiler().pop();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void loadStorage(MinecraftServer server, Executor dispatcher, LevelStorageAccess levelStorageAccess, ServerLevelData serverLevelData, ResourceKey dimension, LevelStem levelStem, ChunkProgressListener progressListener, boolean isDebug, long biomeZoomSeed, List customSpawners, boolean tickTime, RandomSequences randomSequences, CallbackInfo ci) {
        try {
            StoragePersistentState.LOADING.set(true);
            this.getDataStorage().computeIfAbsent(StoragePersistentState.getFactory(manasCore$getCombinedStorage()), "manascore_world_storage");
        } finally {
            StoragePersistentState.LOADING.set(false);
        }
    }
}
