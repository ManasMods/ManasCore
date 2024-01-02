package com.github.manasmods.manascore.skill;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.skill.*;
import com.github.manasmods.manascore.api.storage.Storage;
import com.github.manasmods.manascore.api.storage.StorageEvents;
import com.github.manasmods.manascore.api.world.entity.EntityEvents;
import com.github.manasmods.manascore.storage.StorageManager.StorageKey;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.architectury.event.EventResult;
import lombok.Getter;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

@Log4j2
public class SkillStorage extends Storage implements Skills {
    @Getter
    private static StorageKey<SkillStorage> key = null;
    public static final int INSTANCE_UPDATE = 20;
    public static final int PASSIVE_SKILL = 100;
    public static final Multimap<UUID, TickingSkill> tickingSkills = ArrayListMultimap.create();
    private static final String SKILL_LIST_KEY = "skills";

    public static void init() {
        StorageEvents.REGISTER_ENTITY_STORAGE.register(registry -> key = registry.register(new ResourceLocation(ManasCore.MOD_ID, "skill_storage"), SkillStorage.class, LivingEntity.class::isInstance, target -> new SkillStorage((LivingEntity) target)));
        EntityEvents.LIVING_POST_TICK.register(entity -> {
            Level level = entity.level();
            if (level.isClientSide()) return;
            Skills storage = SkillAPI.getSkillsFrom(entity);
            handleSkillTick(entity, level, storage);
            if (entity instanceof Player player) handleSkillHeldTick(player, storage);
            storage.markDirty();
        });
    }

    private static void handleSkillTick(LivingEntity entity, Level level, Skills storage) {
        MinecraftServer server = level.getServer();
        if (server == null) return;

        boolean shouldPassiveConsume = server.getTickCount() % INSTANCE_UPDATE == 0;
        if (!shouldPassiveConsume) return;
        checkPlayerOnlyEffects(entity, storage);

        boolean passiveSkillActivate = server.getTickCount() % PASSIVE_SKILL == 0;
        if (!passiveSkillActivate) return;

        tickSkills(entity, storage);
    }

    private static void tickSkills(LivingEntity entity, Skills storage) {
        List<ManasSkillInstance> tickingSkills = new ArrayList<>();
        for (ManasSkillInstance instance : storage.getLearnedSkills()) {
            Optional<ManasSkillInstance> optional = storage.getSkill(instance.getSkill());
            if (optional.isEmpty()) continue;

            ManasSkillInstance skillInstance = optional.get();
            if (!skillInstance.canInteractSkill(entity)) continue;
            if (!skillInstance.getSkill().canTick(skillInstance, entity)) continue;
            if (SkillEvents.SKILL_PRE_TICK.invoker().tick(skillInstance, entity).isFalse()) continue;
            tickingSkills.add(skillInstance);
        }

        for (ManasSkillInstance instance : tickingSkills) {
            instance.onTick(entity);
            SkillEvents.SKILL_POST_TICK.invoker().tick(instance, entity);
        }
    }

    private static void checkPlayerOnlyEffects(LivingEntity entity, Skills storage) {
        if (!(entity instanceof Player)) return;
        List<ManasSkillInstance> toBeRemoved = new ArrayList<>();

        for (ManasSkillInstance instance : storage.getLearnedSkills()) {
            // Update cool down
            if (instance.onCoolDown()) instance.decreaseCoolDown(1);
            // Update temporary skill timer
            if (!instance.isTemporarySkill()) continue;
            instance.decreaseRemoveTime(1);
            if (!instance.shouldRemove()) continue;
            toBeRemoved.add(instance);
        }
        // Remove temporary skills
        for (ManasSkillInstance instance : toBeRemoved) {
            storage.forgetSkill(instance);
        }
    }

    private static void handleSkillHeldTick(Player player, Skills storage) {
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

    public boolean learnSkill(@NonNull ManasSkill skill) {
        return learnSkill(new ManasSkillInstance(skill));
    }

    public boolean learnSkill(@NonNull ManasSkillInstance instance) {
        if (this.skillInstances.containsKey(instance.getSkillId())) {
            log.debug("Tried to register a deduplicate of {}.", instance.getSkillId());
            return false;
        }

        EventResult result = SkillEvents.UNLOCK_SKILL.invoker().unlockSkill(instance, getOwner());
        if (result.isFalse()) return false;

        instance.markDirty();
        this.skillInstances.put(instance.getSkillId(), instance);
        instance.onLearnSkill(getOwner());
        markDirty();
        return true;
    }

    public Optional<ManasSkillInstance> getSkill(@NonNull ResourceLocation skillId) {
        return Optional.ofNullable(this.skillInstances.get(skillId));
    }

    public void forgetSkill(ManasSkill skill) {
        getSkill(skill).ifPresent(this::forgetSkill);
    }

    public void forgetSkill(ManasSkillInstance instance) {
        if (!this.skillInstances.containsKey(instance.getSkillId())) return;

        EventResult result = SkillEvents.REMOVE_SKILL.invoker().removeSkill(instance, getOwner());
        if (result.isFalse()) return;

        instance.markDirty();
        getLearnedSkills().remove(instance);
        this.hasRemovedSkills = true;
        markDirty();
    }

    public void forEachSkill(BiConsumer<SkillStorage, ManasSkillInstance> skillInstanceConsumer) {
        List.copyOf(this.skillInstances.values()).forEach(skillInstance -> skillInstanceConsumer.accept(this, skillInstance));
        markDirty();
    }

    public void handleSkillRelease(List<ResourceLocation> skillList, int heldTick, int keyNumber) {
        for (final ResourceLocation skillId : skillList) {
            getSkill(skillId).ifPresent(skill -> {
                if (!skill.canInteractSkill(getOwner())) return;
                if (skill.onCoolDown() && !skill.canIgnoreCoolDown(getOwner())) return;
                skill.onRelease(getOwner(), heldTick, keyNumber);
                if (skill.isDirty()) markDirty();

                UUID ownerID = getOwner().getUUID();
                if (tickingSkills.containsKey(ownerID)) {
                    tickingSkills.get(ownerID).removeIf(tickingSkill -> tickingSkill.getSkill() == skill.getSkill());
                }
            });
        }
    }

    @Override
    public void save(CompoundTag data) {
        ListTag skillList = new ListTag();
        this.skillInstances.values().forEach(instance -> {
            skillList.add(instance.toNBT());
            instance.resetDirty();
        });
        data.put(SKILL_LIST_KEY, skillList);
    }

    @Override
    public void load(CompoundTag data) {
        if (data.contains("resetExistingData")) {
            this.skillInstances.clear();
        }

        for (Tag tag : data.getList(SKILL_LIST_KEY, Tag.TAG_COMPOUND)) {
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
            data.put(SKILL_LIST_KEY, skillList);
        }
    }

    protected LivingEntity getOwner() {
        return (LivingEntity) this.holder;
    }
}
