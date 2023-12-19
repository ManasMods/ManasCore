package com.github.manasmods.manascore.capability.skill;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.event.RemoveSkillEvent;
import com.github.manasmods.manascore.api.skills.event.UnlockSkillEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

@Log4j2
@ApiStatus.Internal
public class EntitySkillCapabilityStorage implements InternalSkillStorage {
    private final HashMap<ResourceLocation, ManasSkillInstance> skillInstances = new HashMap<>();
    @Getter
    @Setter
    @Nullable
    private Entity owner;

    @Override
    public Collection<ManasSkillInstance> getLearnedSkills() {
        return this.skillInstances.values();
    }

    @Override
    public void updateSkill(ManasSkillInstance updatedInstance, boolean sync) {
        updatedInstance.markDirty();
        this.skillInstances.put(updatedInstance.getSkillId(), updatedInstance);
        if (sync) syncChanges();
    }

    @Override
    public boolean learnSkill(ManasSkillInstance instance) {
        if (this.owner == null) return false;
        if (this.skillInstances.containsKey(instance.getSkillId())) {
            log.debug("Tried to register a deduplicate of {} to {}.", instance.getSkillId(), this.owner.getName().getString());
            return false;
        }

        if (!MinecraftForge.EVENT_BUS.post(new UnlockSkillEvent(instance, this.owner))) {
            instance.markDirty();
            this.skillInstances.put(instance.getSkillId(), instance);
            syncChanges();
            return true;
        }

        return false;
    }

    @Override
    public Optional<ManasSkillInstance> getSkill(ManasSkill skill) {
        return this.skillInstances.values()
                .parallelStream()
                .filter(skillInstance -> skillInstance.getSkill().equals(skill))
                .findFirst();
    }

    @Override
    public void forgetSkill(ManasSkillInstance instance) {
        if (this.owner == null) return;
        if (!this.skillInstances.containsKey(instance.getSkillId())) return;

        if (!MinecraftForge.EVENT_BUS.post(new RemoveSkillEvent(instance, this.owner))) {
            instance.markDirty();
            getLearnedSkills().remove(instance);
            syncChanges();
        }
    }

    @Override
    public void forgetSkill(ManasSkill skill) {
        if (this.owner == null) return;

        Optional<ManasSkillInstance> optional = getSkill(skill);
        if (optional.isEmpty()) return;

        if (!MinecraftForge.EVENT_BUS.post(new RemoveSkillEvent(optional.get(), this.owner))) {
            optional.get().markDirty();
            getLearnedSkills().remove(optional.get());
            syncChanges();
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        ListTag skillList = new ListTag();
        this.skillInstances.values().forEach(instance -> skillList.add(instance.toNBT()));

        tag.put("skills", skillList);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("resetExistingData")) {
            this.skillInstances.clear();
        }

        for (Tag tag : nbt.getList("skills", Tag.TAG_COMPOUND)) {
            if (!(tag instanceof CompoundTag compoundTag)) {
                log.error("Tag is not a Compound! Exception while deserializing tag {}.", tag);
                continue;
            }

            try {
                ManasSkillInstance instance = ManasSkillInstance.fromNBT(compoundTag);
                this.skillInstances.put(instance.getSkillId(), instance);
            } catch (Exception exception) {
                log.error("Exception while deserializing tag {}.\n{}", tag, exception);
            }
        }
    }
}
