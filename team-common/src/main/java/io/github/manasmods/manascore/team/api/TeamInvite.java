/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.team.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * Pending invitation into a {@code GROUP} team.
 * <p>
 * {@code expiresAtTick} is overworld game time ({@code ServerLevel#getGameTime}), which survives restarts.
 *
 * @param teamId        Id of the {@link Team} the invitee is invited into.
 * @param typeId        Registry id of the {@link TeamType} of the team.
 * @param inviter       Id of the inviting entity, {@link net.minecraft.Util#NIL_UUID} for a system invite.
 * @param invitee       Id of the invited entity.
 * @param expiresAtTick Overworld game time at which the invite runs out.
 */
public record TeamInvite(UUID teamId, ResourceLocation typeId, UUID inviter, UUID invitee, long expiresAtTick) {
    /**
     * Determine if this invite ran out at the given overworld game time.
     */
    public boolean isExpired(long currentTick) {
        return currentTick >= this.expiresAtTick;
    }

    /**
     * Used to save this invite to a {@link CompoundTag}.
     *
     * @see #fromNBT(CompoundTag)
     */
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("team", this.teamId);
        tag.putString("type", this.typeId.toString());
        tag.putUUID("inviter", this.inviter);
        tag.putUUID("invitee", this.invitee);
        tag.putLong("expires", this.expiresAtTick);
        return tag;
    }

    /**
     * Used to load an invite from a {@link CompoundTag}.
     *
     * @see #toNBT()
     */
    public static TeamInvite fromNBT(CompoundTag tag) {
        return new TeamInvite(
                tag.getUUID("team"),
                ResourceLocation.parse(tag.getString("type")),
                tag.getUUID("inviter"),
                tag.getUUID("invitee"),
                tag.getLong("expires")
        );
    }

    /**
     * Used to write this invite to the network buffer.
     *
     * @see #read(FriendlyByteBuf)
     */
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.teamId);
        buf.writeResourceLocation(this.typeId);
        buf.writeUUID(this.inviter);
        buf.writeUUID(this.invitee);
        buf.writeLong(this.expiresAtTick);
    }

    /**
     * Used to read an invite from the network buffer.
     *
     * @see #write(FriendlyByteBuf)
     */
    public static TeamInvite read(FriendlyByteBuf buf) {
        return new TeamInvite(buf.readUUID(), buf.readResourceLocation(), buf.readUUID(), buf.readUUID(), buf.readLong());
    }
}
