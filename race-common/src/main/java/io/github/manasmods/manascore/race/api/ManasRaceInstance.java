/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.race.api;

import com.mojang.datafixers.util.Pair;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.manasmods.manascore.skill.api.ManasSkill;
import io.github.manasmods.manascore.network.api.util.Changeable;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ManasRaceInstance {
    @Nullable
    private CompoundTag tag = null;
    @Getter
    private boolean dirty = false;
    protected final RegistrySupplier<ManasRace> raceRegistryObject;

    protected ManasRaceInstance(ManasRace race) {
        this.raceRegistryObject = RaceAPI.getRaceRegistry().delegate(RaceAPI.getRaceRegistry().getId(race));
    }

    /**
     * Used to get the {@link ManasRace} type of this Instance.
     */
    public ManasRace getRace() {
        return raceRegistryObject.get();
    }

    public ResourceLocation getRaceId() {
        return this.raceRegistryObject.getId();
    }

    /**
     * Used to get the difficulty of this {@link ManasRace}.
     */
    public ManasRace.Difficulty getDifficulty() {
        return this.getRace().getDifficulty();
    }

    /**
     * Used to create an exact copy of the current instance.
     */
    public ManasRaceInstance copy() {
        ManasRaceInstance clone = new ManasRaceInstance(getRace());
        clone.dirty = this.dirty;
        if (this.tag != null) clone.tag = this.tag.copy();
        return clone;
    }

    /**
     * This method is used to ensure that all required information are stored.
     * <p>
     * Override {@link ManasRaceInstance#serialize(CompoundTag)} to store your custom Data.
     */
    public final CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("race", this.getRaceId().toString());
        serialize(nbt);
        return nbt;
    }

    /**
     * Can be used to save custom data.
     *
     * @param nbt Tag with data from {@link ManasRaceInstance#fromNBT(CompoundTag)}
     */
    public CompoundTag serialize(CompoundTag nbt) {
        if (this.tag != null) nbt.put("tag", this.tag.copy());
        return nbt;
    }

    /**
     * Can be used to load custom data.
     */
    public void deserialize(CompoundTag tag) {
        if (tag.contains("tag", 10)) this.tag = tag.getCompound("tag");
    }

    /**
     * Can be used to load a {@link ManasRaceInstance} from a {@link CompoundTag}.
     * <p>
     * The {@link CompoundTag} has to be created though {@link ManasRaceInstance#toNBT()}
     */
    public static ManasRaceInstance fromNBT(CompoundTag tag) throws NullPointerException {
        ResourceLocation location = ResourceLocation.tryParse(tag.getString("race"));
        ManasRace race = RaceAPI.getRaceRegistry().get(location);
        if (race == null) throw new NullPointerException("No race found for location: " + location);
        ManasRaceInstance instance = race.createDefaultInstance();
        instance.deserialize(tag);
        return instance;
    }

    /**
     * Marks the current instance as dirty.
     */
    public void markDirty() {
        this.dirty = true;
    }

    /**
     * This Method is invoked to indicate that a {@link ManasRaceInstance} has been synced with the clients.
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
        ManasRaceInstance instance = (ManasRaceInstance) o;
        return this.getRaceId().equals(instance.getRaceId()) &&
                raceRegistryObject.getRegistryKey().equals(instance.raceRegistryObject.getRegistryKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(raceRegistryObject);
    }

    public boolean is(TagKey<ManasRace> tag) {
        return this.raceRegistryObject.is(tag);
    }

    public MutableComponent getDisplayName() {
        return this.getRace().getName();
    }

    public MutableComponent getChatDisplayName(boolean withDescription) {
        Style style = Style.EMPTY.withColor(ChatFormatting.GRAY);
        if (withDescription) {
            MutableComponent hoverMessage = getDisplayName().append("\n");
            hoverMessage.append(this.getRace().getRaceDescription().withStyle(ChatFormatting.GRAY));
            style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverMessage));
        }

        MutableComponent component = Component.literal("[").append(getDisplayName()).append("]");
        return component.withStyle(style);
    }

    /**
     * Determine if this instance can be used by {@link LivingEntity}.
     *
     * @param user Affected {@link LivingEntity}
     * @return false will stop {@link LivingEntity} from using any ability of the race.
     */
    public boolean canActivateAbility(LivingEntity user) {
        return this.getRace().canActivateAbility(this, user);
    }

    /**
     * Determine if this instance's {@link ManasRaceInstance#onTick} can be executed.
     *
     * @param entity Affected {@link LivingEntity} being this Race.
     * @return false if this race cannot tick.
     */
    public boolean canTick(LivingEntity entity) {
        return this.getRace().canTick(this, entity);
    }

    /**
     * @return compound tag of this instance.
     */
    @Nullable
    public CompoundTag getTag() {
        return this.tag;
    }

    /**
     * Used to add/create additional tags for this instance.
     *
     * @return compound tag of this instance or create if null.
     */
    public CompoundTag getOrCreateTag() {
        if (this.tag == null) {
            this.setTag(new CompoundTag());
        }
        return this.tag;
    }

    /**
     * Used to add/create additional tags for this instance.
     * Set the tag of this instance.
     */
    public void setTag(@Nullable CompoundTag tag) {
        this.tag = tag;
        markDirty();
    }

    /**
     * Applies the attribute modifiers of this instance on the {@link LivingEntity} when set.
     *
     * @param entity   Affected {@link LivingEntity} being thisRace.
     */
    public void addAttributeModifiers(LivingEntity entity) {
        this.getRace().addAttributeModifiers(this, entity);
    }

    /**
     * Removes the attribute modifiers of this instance from the {@link LivingEntity} when changing race.
     *
     * @param entity   Affected {@link LivingEntity} being this Race.
     */
    public void removeAttributeModifiers(LivingEntity entity) {
        this.getRace().removeAttributeModifiers(this, entity);
    }

    /**
     * Called every tick if this instance is set for {@link LivingEntity}.
     *
     * @param living Affected {@link LivingEntity} being this Race.
     */
    public void onTick(LivingEntity living) {
        this.getRace().onTick(this, living);
    }

    /**
     * Called when the {@link LivingEntity} uses this Race's ability.
     *
     * @param entity Affected {@link LivingEntity} being this Race.
     */
    public void onActivateAbility(LivingEntity entity) {
        this.getRace().onActivateAbility(this, entity);
    }

    /**
     * Called when the {@link LivingEntity} sets to this Race.
     *
     * @param living Affected {@link LivingEntity} sets to this Race.
     */
    public void onRaceSet(LivingEntity living) {
        this.getRace().onRaceSet(this, living);
    }

    /**
     * Called when the {@link LivingEntity} owning this instance gains an effect.
     *
     * @param entity owning this instance.
     */
    public boolean onEffectAdded(LivingEntity entity, @Nullable Entity source, Changeable<MobEffectInstance> instance) {
        return this.getRace().onEffectAdded(this, entity, source, instance);
    }

    /**
     * Called when the {@link LivingEntity} owning this instance starts to be targeted by a mob.
     *
     * @return false will stop the mob from targeting the owner.
     */
    public boolean onBeingTargeted(Changeable<LivingEntity> owner, LivingEntity mob) {
        return this.getRace().onBeingTargeted(this, owner, mob);
    }

    /**
     * Called when the {@link LivingEntity} being this race hurts another {@link LivingEntity}.
     * <p>
     * Gets executed before {@link ManasRaceInstance#onHurt}
     *
     * @return false will prevent the owner from dealing damage.
     */
    public boolean onAttackEntity(LivingEntity owner, LivingEntity target, DamageSource source, Changeable<Float> amount) {
        return this.getRace().onAttackEntity(this, owner, target, source, amount);
    }

    /**
     * Called when the {@link LivingEntity} being this Race takes damage.
     * <p>
     * Gets executed after {@link ManasRaceInstance#onAttackEntity}
     *
     * @return false will prevent the owner from taking damage.
     */
    public boolean onHurt(LivingEntity owner, DamageSource source, Changeable<Float> amount) {
        return this.getRace().onHurt(this, owner, source, amount);
    }

    /**
     * Called when the {@link LivingEntity} being this Race dies.
     *
     * @return false will prevent the owner from dying.
     */
    public boolean onDeath(LivingEntity owner, DamageSource source) {
        return this.getRace().onDeath(this, owner, source);
    }

    /**
     * Called when the {@link ServerPlayer} being this Race respawns.
     */
    public void onRespawn(ServerPlayer owner, boolean conqueredEnd) {
        this.getRace().onRespawn(this, owner, conqueredEnd);
    }

    /**
     * Returns the dimension that {@link LivingEntity} respawns at as this Race.
     * Decides whether if the game should spawn a 3x3 platform of {@link BlockState} when no valid spawn is found.
     * </p>
     * @param player   Affected {@link LivingEntity} being this race.
     */
    public Pair<ResourceKey<Level>, BlockState> getRespawnDimension(LivingEntity player) {
        return this.getRace().getRespawnDimension(this, player);
    }

    /**
     * Returns a list of all {@link ManasSkill} that {@link LivingEntity} gains on changing to this Instance.
     * </p>
     * @param entity   Affected {@link LivingEntity} being this race.
     */
    public List<ManasSkill> getIntrinsicSkills(LivingEntity entity) {
        return this.getRace().getIntrinsicSkills(this, entity);
    }

    public boolean isIntrinsicSkill(LivingEntity entity, ManasSkill skill) {
        return this.getRace().isIntrinsicSkill(this, entity, skill);
    }

    /**
     * Returns a list of all {@link ManasSkill} that {@link LivingEntity} gains on changing to this Race.
     * </p>
     * @param entity   Affected {@link LivingEntity} being this race.
     */
    public void learnIntrinsicSkills(LivingEntity entity) {
        this.getRace().learnIntrinsicSkills(this, entity);
    }

    /**
     * Returns a list of all {@link ManasRace} that this Race can evolve into.
     * </p>
     * @param entity   Affected {@link LivingEntity} evolving this race.
     */
    public List<ManasRace> getNextEvolutions(LivingEntity entity) {
        return this.getRace().getNextEvolutions(this, entity);
    }

    /**
     * Returns a list of all {@link ManasRace} that evolve into this Race.
     * </p>
     * @param entity   Affected {@link LivingEntity} being this race.
     */
    public List<ManasRace> getPreviousEvolutions(LivingEntity entity) {
        return this.getRace().getPreviousEvolutions(this, entity);
    }

    /**
     * Returns the default {@link ManasRace} that this Race evolves into.
     * </p>
     * @param entity   Affected {@link LivingEntity} evolving this race.
     */
    public @Nullable ManasRace getDefaultEvolution(LivingEntity entity) {
        return this.getRace().getDefaultEvolution(this, entity);
    }

    /**
     * Returns the float progress for this {@link ManasRace} to evolve into its evolution.
     * Acceptable values: 0 - 1.0
     * </p>
     * @param entity      Affected {@link LivingEntity} evolving this race.
     * @param evolution   Affected {@link ManasRace} that this Race evolves into.
     */
    public float getEvolutionProgress(LivingEntity entity, ManasRace evolution) {
        return this.getRace().getEvolutionProgress(this, entity, evolution);
    }

    /**
     * Called when the {@link LivingEntity} evolves this Race.
     * </p>
     * @param entity    Affected {@link LivingEntity} evolving this Race.
     * @param evolution Affected {@link ManasRaceInstance} that this Race evolves into.
     */
    public void onRaceEvolution(LivingEntity entity, ManasRaceInstance evolution) {
        this.getRace().onRaceEvolution(this, entity, evolution);
    }
}
