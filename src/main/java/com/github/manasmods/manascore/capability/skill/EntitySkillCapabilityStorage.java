package com.github.manasmods.manascore.capability.skill;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
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
        this.skillInstances.put(updatedInstance.getSkill().getRegistryName(), updatedInstance);
        if (sync) syncChanges();
    }

    @Override
    public void learnSkill(ManasSkillInstance instance) {
        if (this.owner == null) return;
        if (this.skillInstances.containsKey(instance.getSkill().getRegistryName())) {
            log.debug("Tried to register a deduplicate of {} to {}.", instance.getSkill().getRegistryName(), this.owner.getName().getString());
            return;
        }

        if (!MinecraftForge.EVENT_BUS.post(new UnlockSkillEvent(instance, this.owner))) {
            instance.markDirty();
            this.skillInstances.put(instance.getSkill().getRegistryName(), instance);
            syncChanges();
        }
    }

    @Override
    public Optional<ManasSkillInstance> getSkill(ManasSkill skill) {
        return this.skillInstances.values()
            .parallelStream()
            .filter(skillInstance -> skillInstance.getSkill().equals(skill))
            .findFirst();
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
        ListTag skillList = nbt.getList("skills", Tag.TAG_LIST);
        skillList.forEach(tag -> {
            try {
                if (tag instanceof CompoundTag compoundTag) {
                    ManasSkillInstance instance = ManasSkillInstance.fromNBT(compoundTag);
                    this.skillInstances.put(instance.getSkill().getRegistryName(), instance);
                } else {
                    log.error("Tag is not a Compound! Exception while deserializing tag {}.", tag);
                }
            } catch (Exception exception) {
                log.error("Exception while deserializing tag {}.\n{}", tag, exception);
            }
        });
    }
}
