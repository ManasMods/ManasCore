package com.github.manasmods.manascore.api.world.chunk;

import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired when a chunk is ticked.
 * Only fired on the server side.
 */
public class LevelChunkTickEvent extends Event {
    @Getter
    public final LevelChunk chunk;
    public final Phase phase;
    @Getter
    public final ServerLevel level;

    public LevelChunkTickEvent(Phase phase, LevelChunk chunk) {
        this.chunk = chunk;
        this.phase = phase;
        this.level = (ServerLevel) chunk.getLevel();
    }

    public Phase getTickPhase() {
        return phase;
    }
}
