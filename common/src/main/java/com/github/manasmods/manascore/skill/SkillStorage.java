package com.github.manasmods.manascore.skill;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.skill.ManasSkill;
import com.github.manasmods.manascore.api.skill.ManasSkillInstance;
import com.github.manasmods.manascore.api.skill.SkillAPI;
import com.github.manasmods.manascore.api.skill.SkillEvents;
import com.github.manasmods.manascore.api.storage.Storage;
import com.github.manasmods.manascore.api.storage.StorageEvents;
import com.github.manasmods.manascore.api.world.entity.EntityEvents;
import com.github.manasmods.manascore.storage.StorageManager.StorageKey;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.architectury.event.EventResult;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Log4j2
public class SkillStorage extends Storage {
    public static StorageKey<SkillStorage> KEY = null;
    public static final int INSTANCE_UPDATE = 20;
    public static final int PASSIVE_SKILL = 100;
    public static final Multimap<UUID, TickingSkill> tickingSkills = ArrayListMultimap.create();

    public static void init() {
        StorageEvents.REGISTER_ENTITY_STORAGE.register(registry -> {
            registry.register(new ResourceLocation(ManasCore.MOD_ID, "skill_storage"), SkillStorage.class, entity -> entity instanceof LivingEntity, target -> new SkillStorage((LivingEntity) target));
        });
        EntityEvents.LIVING_POST_TICK.register(entity -> {
            Level level = entity.level();
            if (level.isClientSide()) return;
            SkillStorage storage = SkillAPI.getSkillsFrom(entity);
            handleSkillTick(entity, level, storage);
            if (entity instanceof Player player) handleSkillHeldTick(player, level, storage);
            storage.markDirty();
        });
    }

    private static void handleSkillTick(LivingEntity entity, Level level, SkillStorage storage) {
        MinecraftServer server = level.getServer();

        boolean shouldPassiveConsume = server.getTickCount() % INSTANCE_UPDATE == 0;
        if (!shouldPassiveConsume) return;

        if (entity instanceof Player) {
            for (ManasSkillInstance instance : storage.getLearnedSkills()) {
                // Update cool down
                if (instance.onCoolDown()) instance.decreaseCoolDown(1);
                // Update temporary skill timer
                if (!instance.isTemporarySkill()) continue;
                instance.decreaseRemoveTime(1);
                if (!instance.shouldRemove()) continue;
                storage.forgetSkill(instance);
            }
        }

        boolean passiveSkillActivate = server.getTickCount() % PASSIVE_SKILL == 0;
        if (!passiveSkillActivate) return;

        for (ManasSkillInstance instance : List.copyOf(storage.getLearnedSkills())) {
            Optional<ManasSkillInstance> optional = storage.getSkill(instance.getSkill());
            if (optional.isEmpty()) continue;

            ManasSkillInstance skillInstance = optional.get();
            if (!skillInstance.canInteractSkill(entity)) continue;
            if (!skillInstance.getSkill().canTick(skillInstance, entity)) continue;
            if (SkillEvents.SKILL_TICK.invoker().tick(skillInstance, entity).isFalse()) continue;
            skillInstance.onTick(entity);
        }
    }

    private static void handleSkillHeldTick(Player player, Level level, SkillStorage storage) {
        if (!tickingSkills.containsKey(player.getUUID())) return;
        tickingSkills.get(player.getUUID()).removeIf(skill -> !skill.tick(storage, player));
    }

    private final Map<ResourceLocation, ManasSkillInstance> skillInstances = new HashMap<>();
    private boolean hasRemovedSkills = false;

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
        this.hasRemovedSkills = true;
        markDirty();
    }

    @Override
    public void save(CompoundTag data) {
        ListTag skillList = new ListTag();
        this.skillInstances.values().forEach(instance -> {
            skillList.add(instance.toNBT());
            instance.resetDirty();
        });
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

    @Override
    public void saveOutdated(CompoundTag data) {
        if (this.hasRemovedSkills) {
            this.hasRemovedSkills = false;
            data.putBoolean("resetExistingData", true);
            super.saveOutdated(data);
        } else {
            ListTag skillList = new ListTag();
            for (ManasSkillInstance instance : this.skillInstances.values()) {
                if (!instance.isDirty()) continue;
                skillList.add(instance.toNBT());
                instance.resetDirty();
            }
            data.put("skills", skillList);
        }
    }
}
