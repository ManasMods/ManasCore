package com.github.manasmods.manascore.skill;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.skill.ManasSkill;
import com.github.manasmods.manascore.api.skill.ManasSkillInstance;
import com.github.manasmods.manascore.api.skill.SkillEvents;
import com.github.manasmods.manascore.api.storage.Storage;
import com.github.manasmods.manascore.api.storage.StorageEvents;
import com.github.manasmods.manascore.storage.StorageManager.StorageKey;
import dev.architectury.event.EventResult;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Log4j2
public class SkillStorage extends Storage {
    public static StorageKey<SkillStorage> KEY = null;

    public static void init() {
        StorageEvents.REGISTER_ENTITY_STORAGE.register(registry -> {
            registry.register(new ResourceLocation(ManasCore.MOD_ID, "skill_storage"), SkillStorage.class, entity -> entity instanceof LivingEntity, target -> new SkillStorage((LivingEntity) target));
        });
    }

    private final Map<ResourceLocation, ManasSkillInstance> skillInstances = new HashMap<>();

    protected SkillStorage(LivingEntity holder) {
        super(holder);
    }

    public Collection<ManasSkillInstance> getLearnedSkills() {
        return this.skillInstances.values();
    }

    public void updateSkill(@NonNull ManasSkillInstance updatedInstance, boolean sync) {
        updatedInstance.markDirty();
        this.skillInstances.put(updatedInstance.getSkillId(), updatedInstance);
        if (sync) markDirty();
    }

    public boolean learnSkill(@NonNull ManasSkillInstance instance) {
        if (this.skillInstances.containsKey(instance.getSkillId())) {
            log.debug("Tried to register a deduplicate of {}.", instance.getSkillId());
            return false;
        }

        EventResult result = SkillEvents.UNLOCK_SKILL.invoker().unlockSkill(instance, (LivingEntity) this.holder);
        if (result.isFalse()) return false;

        instance.markDirty();
        this.skillInstances.put(instance.getSkillId(), instance);
        markDirty();
        return true;
    }

    public Optional<ManasSkillInstance> getSkill(@NonNull ManasSkill skill) {
        return Optional.ofNullable(this.skillInstances.get(skill.getRegistryName()));
    }

    public Optional<ManasSkillInstance> getSkill(@NonNull ResourceLocation skillId) {
        return Optional.ofNullable(this.skillInstances.get(skillId));
    }

    public void forgetSkill(ManasSkillInstance instance) {
        if (!this.skillInstances.containsKey(instance.getSkillId())) return;

        EventResult result = SkillEvents.REMOVE_SKILL.invoker().removeSkill(instance, (LivingEntity) this.holder);
        if (result.isFalse()) return;

        instance.markDirty();
        getLearnedSkills().remove(instance);
        markDirty();
    }

    @Override
    public void save(CompoundTag data) {
        ListTag skillList = new ListTag();
        this.skillInstances.values().forEach(instance -> skillList.add(instance.toNBT()));
        data.put("skills", skillList);
    }

    @Override
    public void load(CompoundTag data) {
        if (data.contains("resetExistingData")) {
            this.skillInstances.clear();
        }

        for (Tag tag : data.getList("skills", Tag.TAG_COMPOUND)) {
            try {
                ManasSkillInstance instance = ManasSkillInstance.fromNBT((CompoundTag) tag);
                this.skillInstances.put(instance.getSkillId(), instance);
            } catch (Exception e) {
                ManasCore.Logger.error("Failed to load skill instance from NBT", e);
            }
        }
    }
}
