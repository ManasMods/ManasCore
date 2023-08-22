package com.github.manasmods.manascore.api.skills;

import com.github.manasmods.manascore.network.toclient.SyncSkillsPacket;
import lombok.Getter;
import net.minecraft.core.Holder.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

/**
 * Mutable instance of a {@link ManasSkill}.
 * This instances will be stored in the {@link Entity} capability.
 * <p>
 * Instances are created though the {@link ManasSkill#createDefaultInstance()} method.
 */
@ApiStatus.AvailableSince("1.0.2.0")
public class ManasSkillInstance implements Cloneable {
    private final Reference<ManasSkill> skillRegistryObject;
    @Getter
    private boolean dirty = false;

    public ManasSkillInstance(ManasSkill skill) {
        this.skillRegistryObject = SkillAPI.getSkillRegistry().getDelegateOrThrow(skill);
    }

    /**
     * Used to get the {@link ManasSkill} type of this Instance.
     */
    public ManasSkill getSkill() {
        return skillRegistryObject.get();
    }

    public ResourceLocation getSkillId() {
        return this.skillRegistryObject.key().location();
    }

    /**
     * Used to create an exact copy of the current instance.
     */
    @Override
    public ManasSkillInstance clone() {
        ManasSkillInstance clone = new ManasSkillInstance(getSkill());
        return clone;
    }

    /**
     * This method is used to ensure that all required information are stored.
     * <p>
     * Override {@link ManasSkillInstance#serialize(CompoundTag)} to store your custom Data.
     */
    @ApiStatus.NonExtendable
    public final CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("skill", this.getSkillId().toString());
        serialize(tag);
        return tag;
    }

    /**
     * Can be used to save custom data.
     *
     * @param tag Tag with data from {@link ManasSkillInstance#fromNBT(CompoundTag)}
     */
    public CompoundTag serialize(CompoundTag tag) {
        return tag;
    }

    /**
     * Can be used to load custom data.
     */
    public void deserialize(CompoundTag tag) {
    }

    /**
     * Can be used to load a {@link ManasSkillInstance} from a {@link CompoundTag}.
     * <p>
     * The {@link CompoundTag} has to be created though {@link ManasSkillInstance#toNBT()}
     */
    @ApiStatus.NonExtendable
    public static ManasSkillInstance fromNBT(CompoundTag tag) {
        ResourceLocation skillLocation = ResourceLocation.tryParse(tag.getString("skill"));
        ManasSkillInstance instance = Objects.requireNonNull(SkillAPI.getSkillRegistry().getValue(skillLocation)).createDefaultInstance();
        instance.deserialize(tag);
        return instance;
    }

    /**
     * Marks the current instance as dirty.
     * <p>
     * This causes the Instance to get synced on the next {@link SyncSkillsPacket}
     */
    public void markDirty() {
        this.dirty = true;
    }

    /**
     * This Method is invoked to indicate that a {@link ManasSkillInstance} has been synced with the clients.
     * <p>
     * Do <strong>NOT</strong> use that method on our own!
     */
    @ApiStatus.Internal
    public void resetDirty() {
        this.dirty = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManasSkillInstance instance = (ManasSkillInstance) o;
        return this.getSkillId().equals(instance.getSkillId()) &&
                skillRegistryObject.key().registry().equals(instance.skillRegistryObject.key().registry());
    }

    @Override
    public int hashCode() {
        return Objects.hash(skillRegistryObject);
    }

    public void onHurt(LivingHurtEvent event) {
        this.getSkill().onEntityHurt(this, event);
    }
}