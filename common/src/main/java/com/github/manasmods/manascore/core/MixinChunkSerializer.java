package com.github.manasmods.manascore.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
public class MixinChunkSerializer {
    @Inject(method = "read", at = @At("RETURN"))
    private static void onChunkRead(ServerLevel level, PoiManager poiManager, ChunkPos pos, CompoundTag tag, CallbackInfoReturnable<ProtoChunk> cir) {
        if (!(cir.getReturnValue() instanceof ImposterProtoChunk protoChunk)) return;
        // Apply loaded data to initial storage
        protoChunk.getWrapped().manasCore$getCombinedStorage().handleUpdatePacket(tag.getCompound("ManasCoreStorage"));
    }

    @Inject(method = "write", at = @At("RETURN"))
    private static void onChunkWrite(ServerLevel level, ChunkAccess chunk, CallbackInfoReturnable<CompoundTag> cir) {
        if (!(chunk instanceof LevelChunk levelChunk)) return;
        CompoundTag tag = cir.getReturnValue();
        tag.put("ManasCoreStorage", levelChunk.manasCore$getCombinedStorage().toNBT());
    }
}
