/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.race.api;

import com.mojang.datafixers.util.Pair;
import io.github.manasmods.manascore.race.ManasCoreRace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Tuple;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * This class contains a modified version of one or multiple open source code snippets.
 * <p>
 * The following sources have been used to create this class:
 * - <a href="https://github.com/EdwinMindcraft/apoli/blob/2c3969242331db054d7c5b11aa9398779c7da019/src/main/java/io/github/edwinmindcraft/apoli/common/power/configuration/ModifyPlayerSpawnConfiguration.java#L128C5-L208C3">...</a>
 */
public class SpawnPointHelper {
    /**
     * Can be used to teleport {@link Entity} to a new location among dimensions.
     */
    public static void teleportToAcrossDimensions(Entity entity, ServerLevel dimension, double x, double y, double z, float xRot, float yRot) {
        ChunkPos chunkPos = new ChunkPos(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        dimension.getChunkSource().addRegionTicket(TicketType.START, chunkPos, 11, Unit.INSTANCE);
        if (entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.teleportTo(dimension, x, y, z, xRot, yRot);
        } else {
            entity.teleportTo(dimension, x, y, z, Set.of(), xRot, yRot);
        }
    }

    public static void teleportToAcrossDimensions(Entity entity, ResourceKey<Level> dimension, double x, double y, double z, float xRot, float yRot) {
        MinecraftServer server = entity.getServer();
        if (server == null) return;
        ServerLevel level = server.getLevel(dimension);
        if (level == null) return;
        teleportToAcrossDimensions(entity, level, x, y, z, xRot, yRot);
    }

    /**
     * Can be used to teleport {@link Entity} to a new valid spawn location among dimensions.
     */
    public static void teleportToNewSpawn(Entity entity, ResourceKey<Level> dimension, BlockState platformMaterial) {
        Tuple<ServerLevel, Vec3> spawn = getSpawn(entity, dimension, platformMaterial);
        if (spawn == null) return;
        Vec3 pos = spawn.getB();
        teleportToAcrossDimensions(entity, spawn.getA(), pos.x, pos.y, pos.z, entity.getXRot(), entity.getYRot());
    }

    public static void teleportToNewSpawn(ServerPlayer player) {
        Optional<ManasRaceInstance> optional = RaceAPI.getRaceFrom(player).getRace();
        if (optional.isEmpty()) return;
        Pair<ResourceKey<Level>, BlockState> pair = optional.get().getRespawnDimension(player);
        ResourceKey<Level> dimension = pair.getFirst();
        if (player.getRespawnDimension() == dimension) return;
        teleportToNewSpawn(player, dimension, pair.getSecond());
    }

    @Nullable
    public static Tuple<ServerLevel, Vec3> getSpawn(Entity entity, ResourceKey<Level> dimension, BlockState platformMaterial) {
        MinecraftServer server = entity.getServer();
        if (server == null) return null;

        ServerLevel level = server.getLevel(dimension);
        if (level == null) {
            ManasCoreRace.LOG.warn("Could not find dimension \"{}\".", dimension.toString());
            return null;
        }

        BlockPos defaultSpawn = dimension == Level.END ? ServerLevel.END_SPAWN_POINT : Objects.requireNonNull(server.getLevel(Level.OVERWORLD)).getSharedSpawnPos();
        Vec3 validSpawn = getValidSpawn(defaultSpawn, 200, level);

        if (validSpawn != null) {
            ChunkPos chunkPos = new ChunkPos(SectionPos.blockToSectionCoord(validSpawn.x), SectionPos.blockToSectionCoord(validSpawn.z));
            level.getChunkSource().addRegionTicket(TicketType.START, chunkPos, 11, Unit.INSTANCE);
            return new Tuple<>(level, validSpawn);
        }

        if (platformMaterial.isAir()) return null;
        createSafePlatform(level, BlockPos.containing(defaultSpawn.getBottomCenter()).below(), platformMaterial, true);
        Vec3 secondSpawn = getValidSpawn(defaultSpawn, 100, level);

        if (secondSpawn != null) {
            ChunkPos chunkPos = new ChunkPos(SectionPos.blockToSectionCoord(secondSpawn.x), SectionPos.blockToSectionCoord(secondSpawn.z));
            level.getChunkSource().addRegionTicket(TicketType.START, chunkPos, 11, Unit.INSTANCE);
            return new Tuple<>(level, secondSpawn);
        }
        return null;
    }

    @Nullable
    private static Vec3 getValidSpawn(BlockPos startPos, int range, ServerLevel world) {
        //Force load the chunk in which we are working.
        //This method will generate the chunk if it needs to.
        world.getChunk(startPos.getX() >> 4, startPos.getZ() >> 4, ChunkStatus.FULL, true);

        // (di, dj) is a vector - direction in which we move right now
        // (di, dj) is a vector - direction in which we move right now
        int dx = 1;
        int dz = 0;

        // length of current segment
        int segmentLength = 1;
        BlockPos.MutableBlockPos mutable = startPos.mutable();

        // center of our starting structure, or dimension
        int center = startPos.getY();
        // Our valid spawn location
        Vec3 tpPos;

        // current position (x, z) and how much of current segment we passed
        int x = startPos.getX();
        int z = startPos.getZ();

        //position to check up, or down
        int segmentPassed = 0;
        // Increase y check
        int i = 0;
        // Decrease y check
        int d = -1;

        while (i < world.getLogicalHeight() || d > 0) {
            for (int coordinateCount = 0; coordinateCount < range; ++coordinateCount) {
                // make a step, add 'direction' vector (di, dj) to current position (i, j)
                x += dx;
                z += dz;
                ++segmentPassed;

                mutable.setX(x);
                mutable.setZ(z);
                mutable.setY(center + i);

                tpPos = DismountHelper.findSafeDismountLocation(EntityType.PLAYER, world, mutable, true);
                if (tpPos != null) return (tpPos);

                mutable.setY(center + d);
                tpPos = DismountHelper.findSafeDismountLocation(EntityType.PLAYER, world, mutable, true);
                if (tpPos != null) return (tpPos);

                if (segmentPassed == segmentLength) {
                    // done with current segment
                    segmentPassed = 0;

                    // 'rotate' directions
                    int buffer = dx;
                    dx = -dz;
                    dz = buffer;

                    // increase segment length if necessary
                    if (dz == 0) ++segmentLength;
                }
            }
            i++;
            d--;
        }
        return null;
    }

    public static void createSafePlatform(ServerLevel level, BlockPos blockPos, BlockState state, boolean replaceBlocks) {
        BlockPos.MutableBlockPos mutable = blockPos.mutable();
        for(int i = -2; i <= 2; ++i) {
            for(int j = -2; j <= 2; ++j) {
                for(int k = -1; k < 3; ++k) {

                    BlockPos pos = mutable.set(blockPos).move(j, k, i);
                    BlockState blockState = k == -1 ? state : Blocks.AIR.defaultBlockState();

                    if (level.getBlockState(pos).is(blockState.getBlock())) continue;
                    if (replaceBlocks) level.destroyBlock(pos, true, null);
                    level.setBlock(pos, blockState, 3);
                }
            }
        }
    }
}
