/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.race.api;

import com.mojang.datafixers.util.Pair;
import dev.architectury.event.Event;
import io.github.manasmods.manascore.network.api.util.Changeable;
import io.github.manasmods.manascore.race.impl.RaceStorage;
import io.github.manasmods.manascore.skill.api.ManasSkill;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import io.github.manasmods.manascore.skill.api.Skills;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This is the Registry Object for Races.
 * Extend from this Class to create your own Races.
 * <p>
 * To add functionality to the {@link ManasRace}, you need to implement a listener interface.
 * Those interfaces allow you to invoke a Method when an {@link Event} happens.
 * The Method will only be invoked for an {@link Entity} that learned the {@link ManasRace}.
 * <p>
 * Races will be set by calling the {@link RaceStorage#setRace} method.
 * You can simply use {@link RaceAPI#getRaceFrom(LivingEntity)} to get the {@link RaceStorage} of an {@link Entity}.
 * <p>
 * You're also allowed to override the {@link ManasRace#createDefaultInstance()} method to create your own implementation
 * of a {@link ManasRaceInstance}. This is required if you want to attach additional data to the {@link ManasRace}
 * (for example to allow to disable a race or make the race gain exp on usage).
 */
public class ManasRace {
    @Getter
    private final Difficulty difficulty;
    protected final Map<Holder<Attribute>, AttributeTemplate> attributeModifiers = new Object2ObjectOpenHashMap<>();
    public ManasRace(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * Used to create a {@link ManasRaceInstance} of this Race.
     * <p>
     * Override this Method to use your extended version of {@link ManasRaceInstance}
     */
    public ManasRaceInstance createDefaultInstance() {
        return new ManasRaceInstance(this);
    }

    /**
     * Used to get the {@link ResourceLocation} id of this race.
     */
    @Nullable
    public ResourceLocation getRegistryName() {
        return RaceAPI.getRaceRegistry().getId(this);
    }

    /**
     * Used to get the {@link MutableComponent} name of this race for translation.
     */
    @Nullable
    public MutableComponent getName() {
        final ResourceLocation id = getRegistryName();
        if (id == null) return null;
        return Component.translatable(String.format("%s.race.%s", id.getNamespace(), id.getPath().replace('/', '.')));
    }

    /**
     * Used to get the {@link ResourceLocation} of this race's icon texture.
     */
    @Nullable
    public ResourceLocation getRaceIcon() {
        ResourceLocation id = this.getRegistryName();
        if (id == null) return null;
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "icons/races/" + id.getPath());
    }

    /**
     * Used to get the {@link MutableComponent} description of this race for translation.
     */
    public MutableComponent getRaceDescription() {
        ResourceLocation id = this.getRegistryName();
        if (id == null) return Component.empty();
        return Component.translatable(String.format("%s.race.%s.description", id.getNamespace(), id.getPath().replace('/', '.')));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManasRace race = (ManasRace) o;
        return Objects.equals(getRegistryName(), race.getRegistryName());
    }

    /**
     * Determine if the {@link ManasRaceInstance} of this Race's ability can be used by {@link LivingEntity}.
     *
     * @param instance Affected {@link ManasRaceInstance}
     * @param user   Affected {@link LivingEntity} using this Race's ability.
     * @return false will stop {@link LivingEntity} from using any ability of the race.
     */
    public boolean canActivateAbility(ManasRaceInstance instance, LivingEntity user) {
        return true;
    }

    /**
     * Determine if this race's {@link ManasRace#onTick} can be executed.
     *
     * @param instance Affected {@link ManasRaceInstance}
     * @param entity   Affected {@link LivingEntity} being this Race.
     * @return false if this race cannot tick.
     */
    public boolean canTick(ManasRaceInstance instance, LivingEntity entity) {
        return false;
    }

    /**
     * Adds an attribute modifier to this skillId. This method can be called for more than one attribute.
     * The attributes are applied to an entity when the race is set.
     * </p>
     */
    public void addAttributeModifier(Holder<Attribute> holder, ResourceLocation resourceLocation, double amount, AttributeModifier.Operation operation) {
        this.attributeModifiers.put(holder, new AttributeTemplate(resourceLocation, amount, operation));
    }

    /**
     * Applies the attribute modifiers of this race on the {@link LivingEntity} when set.
     *
     * @param entity   Affected {@link LivingEntity} being this Race.
     * @param instance Affected {@link ManasRaceInstance}
     */
    public void addAttributeModifiers(ManasRaceInstance instance, LivingEntity entity) {
        if (this.attributeModifiers.isEmpty()) return;

        AttributeMap attributeMap = entity.getAttributes();
        for (Map.Entry<Holder<Attribute>, AttributeTemplate> entry : this.attributeModifiers.entrySet()) {
            AttributeInstance attributeInstance = attributeMap.getInstance(entry.getKey());

            if (attributeInstance == null) continue;
            attributeInstance.removeModifier(entry.getValue().id());
            attributeInstance.addPermanentModifier(entry.getValue().create());
        }
    }

    /**
     * Removes the attribute modifiers of this skillId from the {@link LivingEntity} when changing race.
     *
     * @param entity   Affected {@link LivingEntity} being this Race.
     */
    public void removeAttributeModifiers(ManasRaceInstance instance, LivingEntity entity) {
        if (this.attributeModifiers.isEmpty()) return;
        AttributeMap map = entity.getAttributes();
        List<AttributeInstance> dirtyInstances = new ArrayList<>();

        for (Map.Entry<Holder<Attribute>, AttributeTemplate> entry : this.attributeModifiers.entrySet()) {
            AttributeInstance attributeInstance = map.getInstance(entry.getKey());
            if (attributeInstance == null) continue;
            attributeInstance.removeModifier(entry.getValue().id());
            dirtyInstances.add(attributeInstance);
        }

        if (!dirtyInstances.isEmpty() && entity instanceof ServerPlayer player) {
            ClientboundUpdateAttributesPacket packet = new ClientboundUpdateAttributesPacket(player.getId(), dirtyInstances);
            player.connection.send(packet);
        }
    }

    /**
     * Called every tick of the {@link LivingEntity} owning this Race.
     *
     * @param instance Affected {@link ManasRaceInstance}
     * @param living   Affected {@link LivingEntity} being this Race.
     */
    public void onTick(ManasRaceInstance instance, LivingEntity living) {
        // Override this method to add your own logic
    }

    /**
     * Called when the {@link LivingEntity} using this Race's ability.
     *
     * @param instance Affected {@link ManasRaceInstance}
     * @param entity   Affected {@link LivingEntity} using this Race's ability.
     */
    public void onActivateAbility(ManasRaceInstance instance, LivingEntity entity) {
        // Override this method to add your own logic
    }


    /**
     * Called when the {@link LivingEntity} sets to this Race.
     *
     * @param instance Affected {@link ManasRaceInstance}
     * @param living   Affected {@link LivingEntity} sets to this Race.
     */
    public void onRaceSet(ManasRaceInstance instance, LivingEntity living) {
        // Override this method to add your own logic
    }

    /**
     * Called when the {@link LivingEntity} being this Race gains an effect.
     *
     * @see ManasRaceInstance#onEffectAdded(LivingEntity, Entity, Changeable)
     */
    public boolean onEffectAdded(ManasRaceInstance instance, LivingEntity entity, @Nullable Entity source, Changeable<MobEffectInstance> effect) {
        // Override this method to add your own logic
        return true;
    }

    /**
     * Called when the {@link LivingEntity} being this Race starts to be targeted by a mob.
     *
     * @see ManasRaceInstance#onBeingTargeted(Changeable, LivingEntity)
     */
    public boolean onBeingTargeted(ManasRaceInstance instance, Changeable<LivingEntity> target, LivingEntity owner) {
        // Override this method to add your own logic
        return true;
    }

    /**
     * Called when the {@link LivingEntity} being this Race attack another {@link LivingEntity},
     *
     * @see ManasRaceInstance#onAttackEntity(LivingEntity, LivingEntity, DamageSource, Changeable)
     */
    public boolean onAttackEntity(ManasRaceInstance instance, LivingEntity owner, LivingEntity target, DamageSource source, Changeable<Float> amount) {
        // Override this method to add your own logic
        return true;
    }

    /**
     * Called when the {@link LivingEntity} being this Race takes damage.
     *
     * @see ManasRaceInstance#onHurt(LivingEntity, DamageSource, Changeable)
     */
    public boolean onHurt(ManasRaceInstance instance, LivingEntity owner, DamageSource source, Changeable<Float> amount) {
        // Override this method to add your own logic
        return true;
    }

    /**
     * Called when the {@link LivingEntity} being this Race dies.
     *
     * @see ManasRaceInstance#onDeath(LivingEntity, DamageSource)
     */
    public boolean onDeath(ManasRaceInstance instance, LivingEntity owner, DamageSource source) {
        // Override this method to add your own logic
        return true;
    }

    /**
     * Called when the {@link ServerPlayer} being this Race respawns.
     *
     * @see ManasRaceInstance#onRespawn(ServerPlayer, boolean)
     */
    public void onRespawn(ManasRaceInstance instance, ServerPlayer owner, boolean conqueredEnd) {
        // Override this method to add your own logic
    }

    /**
     * Returns the dimension that {@link LivingEntity} respawns at as this Race.
     * Decides whether if the game should spawn a 3x3 platform of {@link BlockState} when no valid spawn is found.
     *
     * @see ManasRaceInstance#getRespawnDimension(LivingEntity)
     */
    public Pair<ResourceKey<Level>, BlockState> getRespawnDimension(ManasRaceInstance instance, LivingEntity owner) {
        return Pair.of(Level.OVERWORLD, Blocks.AIR.defaultBlockState());
    }

    /**
     * Returns a list of all {@link ManasSkill} that {@link LivingEntity} gains on changing to this Race.
     *
     * @see ManasRaceInstance#getIntrinsicSkills(LivingEntity)
     */
    public List<ManasSkill> getIntrinsicSkills(ManasRaceInstance instance, LivingEntity entity) {
        return new ArrayList<>();
    }

    public boolean isIntrinsicSkill(ManasRaceInstance instance, LivingEntity entity, ManasSkill skill) {
        return this.getIntrinsicSkills(instance, entity).contains(skill);
    }

    /**
     * Returns a list of all {@link ManasSkill} that {@link LivingEntity} gains on changing to this Race.
     *
     * @see ManasRaceInstance#getIntrinsicSkills(LivingEntity)
     */
    public void learnIntrinsicSkills(ManasRaceInstance instance, LivingEntity entity) {
        Skills storage = SkillAPI.getSkillsFrom(entity);
        for (ManasSkill skill : instance.getIntrinsicSkills(entity)) {
            storage.learnSkill(skill);
        }
    }

    /**
     * Returns a list of all {@link ManasRace} that this Race can evolve into.
     *
     * @see ManasRaceInstance#getNextEvolutions(LivingEntity)
     */
    public List<ManasRace> getNextEvolutions(ManasRaceInstance instance, LivingEntity entity) {
        return new ArrayList<>();
    }

    /**
     * Returns a list of all {@link ManasRace} that evolve into this Race.
     *
     * @see ManasRaceInstance#getPreviousEvolutions(LivingEntity)
     */
    public List<ManasRace> getPreviousEvolutions(ManasRaceInstance instance, LivingEntity entity) {
        return new ArrayList<>();
    }

    /**
     * Returns the default {@link ManasRace} that this Race evolves into.
     *
     * @see ManasRaceInstance#getDefaultEvolution(LivingEntity)
     */
    public @Nullable ManasRace getDefaultEvolution(ManasRaceInstance instance, LivingEntity entity) {
        return null;
    }

    /**
     * Returns the float progress for this {@link ManasRace} to evolve into its evolution.
     * Acceptable values: 0 - 1.0
     *
     * @see ManasRaceInstance#getEvolutionProgress(LivingEntity, ManasRace)
     */
    public float getEvolutionProgress(ManasRaceInstance instance, LivingEntity entity, ManasRace evolution) {
        return 0;
    }

    /**
     * Called when the {@link LivingEntity} evolves this Race.
     *
     * @see ManasRaceInstance#onRaceEvolution(LivingEntity, ManasRaceInstance)
     */
    public void onRaceEvolution(ManasRaceInstance instance, LivingEntity living, ManasRaceInstance evolution) {
        // Override this method to add your own logic
    }

    /**
     * Attribute Template for easier base attribute modifier implementation.
     */
    public static record AttributeTemplate(ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        public AttributeTemplate(ResourceLocation id, double amount, AttributeModifier.Operation operation) {
            this.id = id;
            this.amount = amount;
            this.operation = operation;
        }

        public AttributeModifier create() {
            return new AttributeModifier(this.id, this.amount, this.operation);
        }

        public ResourceLocation id() {
            return this.id;
        }

        public double amount() {
            return this.amount;
        }

        public AttributeModifier.Operation operation() {
            return this.operation;
        }
    }

    @RequiredArgsConstructor
    public enum Difficulty {
        EASY(Component.translatable("manascore.race.difficulty.easy").withStyle(ChatFormatting.GREEN)),
        INTERMEDIATE(Component.translatable("manascore.race.difficulty.intermediate").withStyle(style -> style.withColor(0xFFA500))),
        HARD(Component.translatable("manascore.race.difficulty.hard").withStyle(ChatFormatting.RED)),
        EXTREME(Component.translatable("manascore.race.difficulty.extreme").withStyle(ChatFormatting.DARK_RED));

        private final MutableComponent name;

        public MutableComponent asText() {
            return name;
        }
    }
}
