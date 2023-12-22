package com.github.manasmods.manascore.world;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.world.chunk.LevelChunkTickEvent;

public class ChunkTickHandler {
    public static void tick(final LevelChunkTickEvent e) {
        ManasCore.getLogger().info("Chunk tick event fired at {} at phase {}!", e.getChunk().getPos(), e.phase.name());
    }
}
