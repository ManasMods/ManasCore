package com.github.manasmods.manascore.core;

import com.github.manasmods.manascore.api.world.chunk.LevelChunkTickEvent;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel extends Level {

    protected MixinServerLevel(WritableLevelData pLevelData, ResourceKey<Level> pDimension, Holder<DimensionType> pDimensionTypeRegistration, Supplier<ProfilerFiller> pProfiler, boolean pIsClientSide, boolean pIsDebug, long pBiomeZoomSeed, int pMaxChainedNeighborUpdates) {
        super(pLevelData, pDimension, pDimensionTypeRegistration, pProfiler, pIsClientSide, pIsDebug, pBiomeZoomSeed, pMaxChainedNeighborUpdates);
    }

    @Inject(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getProfiler()Lnet/minecraft/util/profiling/ProfilerFiller;", shift = Shift.AFTER))
    private void onPreTickChunk(LevelChunk pChunk, int pRandomTickSpeed, CallbackInfo ci) {
        ProfilerFiller profiler = getProfiler();
        profiler.push("manascore_chunk_tick_pre");
        MinecraftForge.EVENT_BUS.post(new LevelChunkTickEvent(Phase.START, pChunk));
        profiler.pop();
    }

    @Inject(method = "tickChunk", at = @At("RETURN"))
    private void onPostTickChunk(LevelChunk pChunk, int pRandomTickSpeed, CallbackInfo ci) {
        ProfilerFiller profiler = getProfiler();
        profiler.push("manascore_chunk_tick_post");
        MinecraftForge.EVENT_BUS.post(new LevelChunkTickEvent(Phase.END, pChunk));
        profiler.pop();
    }
}
