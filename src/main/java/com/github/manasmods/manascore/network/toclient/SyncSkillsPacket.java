package com.github.manasmods.manascore.network.toclient;

import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.capability.skill.InternalSkillStorage;
import lombok.RequiredArgsConstructor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class SyncSkillsPacket {
    private final int entityId;
    private final SyncType syncType;
    private final CompoundTag skills;

    public SyncSkillsPacket(Entity source, InternalSkillStorage internalSkillStorage, SyncType syncType) {
        this.entityId = source.getId();
        this.syncType = syncType;
        this.skills = syncType.factory.create(internalSkillStorage);
    }

    public SyncSkillsPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.syncType = buf.readEnum(SyncType.class);
        this.skills = buf.readAnySizeNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeEnum(this.syncType);
        buf.writeNbt(this.skills);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> this.syncType.handler.update(this.entityId, this.skills));
        ctx.get().setPacketHandled(true);
    }

    @RequiredArgsConstructor
    public enum SyncType {
        FULL(skillStorage -> skillStorage.serializeNBT(), (entityId1, updateTag) -> {
            ClientLevelAccessor.execute(level -> {
                Entity target = level.getEntity(entityId1);
                if (target == null) return;
                SkillAPI.getSkillsFrom(target).deserializeNBT(updateTag);
            });
        }),
        CHANGES_ONLY(skillStorage -> {
            CompoundTag tag = new CompoundTag();
            ListTag skills = new ListTag();
            skillStorage.getDirtySkills().forEach(skillInstance -> {
                skillInstance.resetDirty();
                skills.add(skillInstance.toNBT());
            });

            tag.put("skills", skills);
            return tag;
        }, (entityId1, updateTag) -> {
            ClientLevelAccessor.execute(level -> {
                Entity target = level.getEntity(entityId1);
                if (target == null) return;
                if (!updateTag.contains("skills")) return;
                SkillAPI.getSkillsFrom(target).updateSkills(updateTag.getList("skills", Tag.TAG_LIST)
                        .parallelStream()
                        .map(tag -> {
                            if (tag instanceof CompoundTag compoundTag) {
                                return compoundTag;
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .map(ManasSkillInstance::fromNBT)
                        .toList()
                );
            });
        });

        private final UpdateFactory factory;
        private final UpdateHandler handler;
    }

    @FunctionalInterface
    private interface UpdateFactory {
        CompoundTag create(InternalSkillStorage skillStorage);
    }

    @FunctionalInterface
    private interface UpdateHandler {
        void update(int entityId, CompoundTag updateTag);
    }
}