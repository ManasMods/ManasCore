/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import io.github.manasmods.manascore.network.api.util.Changeable;
import io.github.manasmods.manascore.skill.ManasCoreSkill;
import io.github.manasmods.manascore.skill.ModuleConstants;
import io.github.manasmods.manascore.skill.api.*;
import io.github.manasmods.manascore.storage.api.Storage;
import io.github.manasmods.manascore.storage.api.StorageEvents;
import io.github.manasmods.manascore.storage.api.StorageKey;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

@Log4j2
public class SkillStorage  extends Storage implements Skills {
    @Getter
    private static StorageKey<SkillStorage> key = null;
    public static final int INSTANCE_UPDATE = 20;
    public static final int PASSIVE_SKILL = 100;
    public static final Multimap<UUID, TickingSkill> tickingSkills = ArrayListMultimap.create();
    private static final String SKILL_LIST_KEY = "skills";

    public static void init() {
        StorageEvents.REGISTER_ENTITY_STORAGE.register(registry -> key = registry.register(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "skill_storage"), SkillStorage.class, LivingEntity.class::isInstance, target -> new SkillStorage((LivingEntity) target)));

        EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
            if (EntityEvents.LIVING_PRE_DAMAGED.invoker().hurt(entity, source, amount).isFalse()) return EventResult.interruptFalse();
            if (EntityEvents.LIVING_ON_BEING_DAMAGED.invoker().hurt(entity, source, amount).isFalse()) return EventResult.interruptFalse();
            return EventResult.pass();
        });

        EntityEvents.LIVING_HURT.register((entity, source, changeable) -> {
            Skills skills = SkillAPI.getSkillsFrom(entity);
            if (SkillEvents.SKILL_DAMAGE_PRE_CALCULATION.invoker().calculate(skills, entity, source, changeable).isFalse()) return EventResult.interruptFalse();
            if (SkillEvents.SKILL_DAMAGE_CALCULATION.invoker().calculate(skills, entity, source, changeable).isFalse()) return EventResult.interruptFalse();
            if (SkillEvents.SKILL_DAMAGE_POST_CALCULATION.invoker().calculate(skills, entity, source, changeable).isFalse()) return EventResult.interruptFalse();
            return EventResult.pass();
        });

        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            if (EntityEvents.DEATH_EVENT_FIRST.invoker().die(entity, source).isFalse()) return EventResult.interruptFalse();
            if (EntityEvents.DEATH_EVENT_HIGH.invoker().die(entity, source).isFalse()) return EventResult.interruptFalse();
            if (EntityEvents.DEATH_EVENT_NORMAL.invoker().die(entity, source).isFalse()) return EventResult.interruptFalse();
            if (EntityEvents.DEATH_EVENT_LOW.invoker().die(entity, source).isFalse()) return EventResult.interruptFalse();
            if (EntityEvents.DEATH_EVENT_LAST.invoker().die(entity, source).isFalse()) return EventResult.interruptFalse();
            return EventResult.pass();
        });

        EntityEvents.LIVING_POST_TICK.register(entity -> {
            Level level = entity.level();
            if (level.isClientSide()) return;
            SkillStorage storage = SkillAPI.getSkillsFrom(entity);
            handleSkillTick(entity, level, storage);
            if (entity instanceof Player player) handleSkillHeldTick(player, storage);
        });

        PlayerEvent.PLAYER_QUIT.register(SkillStorage::removeTickingSkill);
        PlayerEvent.CHANGE_DIMENSION.register((player, resourceKey, resourceKey1) -> SkillStorage.removeTickingSkill(player));
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
            if (!skillInstance.canTick(entity)) continue;
            if (SkillEvents.SKILL_PRE_TICK.invoker().tick(skillInstance, entity).isFalse()) continue;
            tickingSkills.add(skillInstance);
        }

        for (ManasSkillInstance instance : tickingSkills) {
            instance.onTick(entity);
            SkillEvents.SKILL_POST_TICK.invoker().tick(instance, entity);
            storage.checkAndMarkDirty(instance);
        }
    }

    private static void checkPlayerOnlyEffects(LivingEntity entity, Skills storage) {
        if (!(entity instanceof Player)) return;
        List<ManasSkillInstance> toBeRemoved = new ArrayList<>();

        for (ManasSkillInstance instance : storage.getLearnedSkills()) {
            // Update cooldown
            for (int i = 0; i < instance.getModes(); i++) {
                if (!instance.onCoolDown(i)) continue;
                int currentCooldown = instance.getCoolDown(i);
                Changeable<Integer> newCooldown = Changeable.of(Math.max(0, currentCooldown - 1));
                if (!SkillEvents.SKILL_UPDATE_COOLDOWN.invoker().cooldown(instance, entity, i, currentCooldown, newCooldown).isFalse())
                    instance.setCoolDown(newCooldown.get(), i);
                storage.checkAndMarkDirty(instance);
            }

            // Update temporary skill timer
            if (!instance.isTemporarySkill()) continue;
            instance.decreaseRemoveTime(1);
            storage.checkAndMarkDirty(instance);

            if (!instance.shouldRemove()) continue;
            toBeRemoved.add(instance);
        }

        // Remove temporary skills
        for (ManasSkillInstance instance : toBeRemoved) {
            storage.forgetSkill(instance);
        }
    }

    private static void handleSkillHeldTick(Player player, SkillStorage storage) {
        if (!tickingSkills.containsKey(player.getUUID())) return;
        tickingSkills.get(player.getUUID()).removeIf(skill -> {
            if (!skill.tick(storage, player)) {
                Optional<ManasSkillInstance> instance = storage.getSkill(skill.getSkill());
                if (instance.isEmpty()) return true;
                skill.getSkill().removeAttributeModifiers(instance.get(), player, skill.getMode());
                storage.checkAndMarkDirty(instance.get());
                return true;
            } else storage.markDirty();
            return false;
        });
    }

    private final Map<ResourceLocation, ManasSkillInstance> skillInstances = new ConcurrentHashMap<>();
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

    public boolean learnSkill(@NonNull ManasSkillInstance instance, MutableComponent component) {
        if (this.skillInstances.containsKey(instance.getSkillId())) {
            log.debug("Tried to register a deduplicate of {}.", instance.getSkillId());
            return false;
        }

        Changeable<MutableComponent> unlockMessage = Changeable.of(component);
        EventResult result = SkillEvents.UNLOCK_SKILL.invoker().unlockSkill(instance, getOwner(), unlockMessage);
        if (result.isFalse()) return false;

        instance.markDirty();
        this.skillInstances.put(instance.getSkillId(), instance);
        if (unlockMessage.isPresent()) getOwner().sendSystemMessage(unlockMessage.get());
        instance.onLearnSkill(this.getOwner());
        markDirty();
        return true;
    }

    public Optional<ManasSkillInstance> getSkill(@NonNull ResourceLocation skillId) {
        return Optional.ofNullable(this.skillInstances.get(skillId));
    }

    public void forgetSkill(@NotNull ResourceLocation skillId, @Nullable MutableComponent component) {
        if (!this.skillInstances.containsKey(skillId)) return;
        ManasSkillInstance instance = this.skillInstances.get(skillId);

        Changeable<MutableComponent> forgetMessage = Changeable.of(component);
        EventResult result = SkillEvents.REMOVE_SKILL.invoker().removeSkill(instance, getOwner(), forgetMessage);
        if (result.isFalse()) return;

        if (forgetMessage.isPresent()) getOwner().sendSystemMessage(forgetMessage.get());
        instance.onForgetSkill(this.getOwner());
        instance.markDirty();

        this.getLearnedSkills().remove(instance);
        this.hasRemovedSkills = true;
        markDirty();
    }

    public void forEachSkill(BiConsumer<SkillStorage, ManasSkillInstance> skillInstanceConsumer) {
        List.copyOf(this.skillInstances.values()).forEach(skillInstance -> skillInstanceConsumer.accept(this, skillInstance));
        markDirty();
    }

    public void handleSkillRelease(ManasSkillInstance skillInstance, int heldTick, int keyNumber, int mode, boolean heldInterrupt) {
        Changeable<ManasSkillInstance> changeable = Changeable.of(skillInstance);
        if (SkillEvents.RELEASE_SKILL.invoker().releaseSkill(changeable, this.getOwner(), keyNumber, mode, heldTick).isFalse()) return;
        ManasSkillInstance skill = changeable.get();
        if (skill == null) return;

        if ((heldInterrupt || skill.canInteractSkill(getOwner())) && mode < skill.getModes()) {
            if (!skill.onCoolDown(mode) || skill.canIgnoreCoolDown(getOwner(), mode)) {
                skill.onRelease(getOwner(), heldTick, keyNumber, mode);
                this.checkAndMarkDirty(skillInstance);
            }
        }

        skill.removeAttributeModifiers(getOwner(), mode);
        if (!heldInterrupt) {
            UUID ownerID = getOwner().getUUID();
            if (tickingSkills.containsKey(ownerID))
                tickingSkills.get(ownerID).removeIf(tickingSkill -> tickingSkill.matches(skill.getSkill(), mode));
        }
        this.checkAndMarkDirty(skillInstance);
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
                ManasCoreSkill.LOG.error("Failed to load skill instance from NBT", e);
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

    public static void removeTickingSkill(Player player) {
        Multimap<UUID, TickingSkill> multimap = tickingSkills;
        if (multimap.containsKey(player.getUUID())) {
            for (TickingSkill skill : multimap.get(player.getUUID())) {
                Optional<ManasSkillInstance> instance = SkillAPI.getSkillsFrom(player).getSkill(skill.getSkill());
                if (instance.isEmpty()) continue;
                skill.getSkill().removeAttributeModifiers(instance.get(), player, skill.getMode());
            }
            multimap.removeAll(player.getUUID());
        }
    }
}
