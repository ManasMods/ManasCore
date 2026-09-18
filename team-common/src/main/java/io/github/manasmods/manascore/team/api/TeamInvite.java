/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * Pending invitation. For GROUP types {@code teamId} is the team id. For RELATION types
 * {@code teamId} is a unique id for this invite; the inviter is {@code inviter}.
 */
public record TeamInvite(UUID teamId, ResourceLocation typeId, UUID inviter, UUID invitee, long expiresAtTick) {
    public boolean isExpired(long currentTick) {
        return currentTick >= this.expiresAtTick;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("team", this.teamId);
        tag.putString("type", this.typeId.toString());
        tag.putUUID("inviter", this.inviter);
        tag.putUUID("invitee", this.invitee);
        tag.putLong("expires", this.expiresAtTick);
        return tag;
    }

    public static TeamInvite fromNBT(CompoundTag tag) {
        return new TeamInvite(
                tag.getUUID("team"),
                ResourceLocation.parse(tag.getString("type")),
                tag.getUUID("inviter"),
                tag.getUUID("invitee"),
                tag.getLong("expires")
        );
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.teamId);
        buf.writeResourceLocation(this.typeId);
        buf.writeUUID(this.inviter);
        buf.writeUUID(this.invitee);
        buf.writeLong(this.expiresAtTick);
    }

    public static TeamInvite read(FriendlyByteBuf buf) {
        return new TeamInvite(buf.readUUID(), buf.readResourceLocation(), buf.readUUID(), buf.readUUID(), buf.readLong());
    }
}
