package com.github.manasmods.manascore.api.world.chunk;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.world.level.chunk.LevelChunk;

public interface ChunkEvents {
    Event<ChunkTick> CHUNK_TICK = EventFactory.createLoop();

    @FunctionalInterface
    interface ChunkTick {
        void tick(ChunkTickPhase phase, LevelChunk chunk, int randomTickSpeed);
    }

    enum ChunkTickPhase {
        START,
        END
    }
}
