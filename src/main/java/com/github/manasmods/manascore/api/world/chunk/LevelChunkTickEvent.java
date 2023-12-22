package com.github.manasmods.manascore.api.world.chunk;

import lombok.Getter;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.Event;

public class LevelChunkTickEvent extends Event {
    @Getter
    private final LevelChunk chunk;
    public final Phase phase;

    public LevelChunkTickEvent(Phase phase, LevelChunk chunk) {
        this.chunk = chunk;
        this.phase = phase;
    }
}
