/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.api;

import dev.architectury.registry.registries.RegistrySupplier;
import io.github.manasmods.manascore.network.api.util.Changeable;
import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ManasSkillInstance {
    private int removeTime = -1;
    private double masteryPoint = 0;
    private boolean toggled = false;
    private List<Integer> cooldownList;
    @Nullable
    private CompoundTag tag = null;
    @Getter
    private boolean dirty = false;
    protected final RegistrySupplier<ManasSkill> skillRegistryObject;

    protected ManasSkillInstance(ManasSkill skill) {
        this.skillRegistryObject = SkillAPI.getSkillRegistry().delegate(SkillAPI.getSkillRegistry().getId(skill));
        this.cooldownList = NonNullList.withSize(this.getModes(), 0);
    }

    /**
     * Used to get the {@link ManasSkill} type of this Instance.
     */
    public ManasSkill getSkill() {
        return skillRegistryObject.get();
    }

    public ResourceLocation getSkillId() {
        return this.skillRegistryObject.getId();
    }

    /**
     * Used to create an exact copy of the current instance.
     */
    public ManasSkillInstance copy() {
        ManasSkillInstance clone = new ManasSkillInstance(getSkill());
        clone.dirty = this.dirty;
        clone.cooldownList = new ArrayList<>(this.cooldownList);
        clone.removeTime = this.removeTime;
        clone.masteryPoint = this.masteryPoint;
        clone.toggled = this.toggled;
        if (this.tag != null) clone.tag = this.tag.copy();
        return clone;
    }

    /**
     * This method is used to ensure that all required information are stored.
     * <p>
     * Override {@link ManasSkillInstance#serialize(CompoundTag)} to store your custom Data.
     */
    public final CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("skill", this.getSkillId().toString());
        serialize(nbt);
        return nbt;
    }

    /**
     * Can be used to save custom data.
     *
     * @param nbt Tag with data from {@link ManasSkillInstance#fromNBT(CompoundTag)}
     */
    public CompoundTag serialize(CompoundTag nbt) {
        nbt.putInt("RemoveTime", this.removeTime);
        nbt.putDouble("Mastery", this.masteryPoint);
        nbt.putBoolean("Toggled", this.toggled);
        nbt.putIntArray("CooldownList", this.cooldownList);
        if (this.tag != null) nbt.put("tag", this.tag.copy());
        return nbt;
    }

    /**
     * Can be used to load custom data.
     */
    public void deserialize(CompoundTag tag) {
        this.removeTime = tag.getInt("RemoveTime");
        this.masteryPoint = tag.getDouble("Mastery");
        this.toggled = tag.getBoolean("Toggled");
        this.cooldownList = Arrays.stream(tag.getIntArray("CooldownList")).boxed().collect(Collectors.toList());
        if (tag.contains("tag", 10)) this.tag = tag.getCompound("tag");
    }

    /**
     * Can be used to load a {@link ManasSkillInstance} from a {@link CompoundTag}.
     * <p>
     * The {@link CompoundTag} has to be created though {@link ManasSkillInstance#toNBT()}
     */
    public static ManasSkillInstance fromNBT(CompoundTag tag) throws NullPointerException {
        ResourceLocation skillLocation = ResourceLocation.tryParse(tag.getString("skill"));
        ManasSkill skill = SkillAPI.getSkillRegistry().get(skillLocation);
        if (skill == null) throw new IllegalArgumentException("Skill not found in registry: " + skillLocation);
        ManasSkillInstance instance = skill.createDefaultInstance();
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
                skillRegistryObject.getRegistryKey().equals(instance.skillRegistryObject.getRegistryKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getSkillId(), skillRegistryObject.getRegistryKey());
    }

    /**
     * Determine if this instance can be used by {@link LivingEntity}.
     *
     * @param user Affected {@link LivingEntity}
     * @return false will stop {@link LivingEntity} from using any feature of the skill.
     */
    public boolean canInteractSkill(LivingEntity user) {
        return this.getSkill().canInteractSkill(this, user);
    }

    /**
     * @return the maximum number of ticks that this skill can be held down with the skill activation button.
     */
    public int getMaxHeldTime(LivingEntity entity) {
        return this.getSkill().getMaxHeldTime(this, entity);
    }

    /**
     * Determine if the {@link ManasSkill} type of this instance can be toggled.
     *
     * @param entity Affected {@link LivingEntity} owning this Skill.
     * @return false if this skill is not toggleable.
     */
    public boolean canBeToggled(LivingEntity entity) {
        return this.getSkill().canBeToggled(this, entity);
    }

    /**
     * Determine if the {@link ManasSkill} type of this instance can still be activated when on cooldown.
     *
     * @param entity Affected {@link LivingEntity} owning this Skill.
     * @return false if this skill cannot ignore cooldown.
     */
    public boolean canIgnoreCoolDown(LivingEntity entity, int mode) {
        return this.getSkill().canIgnoreCoolDown(this, entity, mode);
    }

    /**
     * Determine if this instance's {@link ManasSkillInstance#onTick} can be executed.
     *
     * @param entity Affected {@link LivingEntity} owning this Skill.
     * @return false if this skill cannot tick.
     */
    public boolean canTick(LivingEntity entity) {
        return this.getSkill().canTick(this, entity);
    }

    /**
     * Determine if this instance's {@link ManasSkillInstance#onScroll} can be executed.
     *
     * @param entity Affected {@link LivingEntity} owning this Skill.
     * @return false if this skill cannot be scrolled.
     */
    public boolean canScroll(LivingEntity entity, int mode) {
        return this.getSkill().canScroll(this, entity, mode);
    }

    /**
     * @return the number of modes that this skill instance has.
     */
    public int getModes() {
        return this.getSkill().getModes(this);
    }

    /**
     * @return the maximum mastery points that this skill instance can have.
     */
    public int getMaxMastery() {
        return this.getSkill().getMaxMastery();
    }

    /**
     * Determine if the {@link ManasSkill} type of this instance is mastered by {@link LivingEntity} owning it.
     *
     * @param entity Affected {@link LivingEntity} owning this Skill.
     */
    public boolean isMastered(LivingEntity entity) {
        return this.getSkill().isMastered(this, entity);
    }

    /**
     * Increase the mastery point of the {@link ManasSkill} type of this instance.
     *
     * @param entity Affected {@link LivingEntity} owning this Skill.
     */
    public void addMasteryPoint(LivingEntity entity) {
        this.getSkill().addMasteryPoint(this, entity);
    }

    /**
     * @return the mastery point of the {@link ManasSkill} type of this instance.
     */
    public double getMastery() {
        return this.masteryPoint;
    }

    /**
     * Set the mastery point of the {@link ManasSkill} type of this instance.
     */
    public void setMastery(double point) {
        this.masteryPoint = point;
        markDirty();
    }

    /**
     * @return the cooldown of a specific mode of this instance.
     */
    public int getCoolDown(int mode) {
        if (mode < 0 || mode >= cooldownList.size()) return 0;
        return this.cooldownList.get(mode);
    }

    /**
     * @return if a specific mode of this instance is on cooldown.
     */
    public boolean onCoolDown(int mode) {
        if (mode < 0 || mode >= cooldownList.size()) return false;
        return this.cooldownList.get(mode) > 0;
    }

    /**
     * Set the cooldown of a specific mode of this instance.
     */
    public void setCoolDown(int coolDown, int mode) {
        if (mode < 0 || mode >= cooldownList.size()) return;
        this.cooldownList.set(mode, coolDown);
        markDirty();
    }

    /**
     * Set the cooldown of every mode of this instance.
     */
    public void setCoolDowns(int coolDown) {
        Collections.fill(this.cooldownList, coolDown);
        markDirty();
    }

    /**
     * Decrease the cooldown of a specific mode of this instance.
     */
    public void decreaseCoolDown(int coolDown, int mode) {
        if (mode < 0 || mode >= cooldownList.size()) return;
        this.cooldownList.set(mode, Math.max(0, this.cooldownList.get(mode) - coolDown));
        markDirty();
    }

    /**
     * Edit the entire cooldown list of this instance.
     */
    public void setCoolDownList(List<Integer> list) {
        this.cooldownList = list;
    }

    /**
     * @return if this skill instance is temporary, which should be removed when its time runs out.
     */
    public boolean isTemporarySkill() {
        return this.removeTime != -1;
    }

    /**
     * @return the removal time of this instance.
     */
    public int getRemoveTime() {
        return this.removeTime;
    }

    /**
     * @return if this skill instance needs to be removed.
     */
    public boolean shouldRemove() {
        return this.removeTime == 0;
    }

    /**
     * Set the remove time of this instance.
     */
    public void setRemoveTime(int removeTime) {
        this.removeTime = removeTime;
        markDirty();
    }

    /**
     * Decrease the remove time of this instance.
     */
    public void decreaseRemoveTime(int time) {
        if (this.removeTime > 0) {
            this.removeTime = Math.max(0, this.removeTime - time);
            markDirty();
        }
    }

    /**
     * @return if this instance is toggled.
     */
    public boolean isToggled() {
        return this.toggled;
    }

    /**
     * Toggle on/off this instance.
     */
    public void setToggled(boolean toggled) {
        this.toggled = toggled;
        markDirty();
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
            this.markDirty();
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
     * @return the amplifier for each attribute modifier that this instance applies.
     * </p>
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     * @param holder   Affected {@link Holder<Attribute>} that this skill provides.
     * @param template Affected {@link ManasSkill.AttributeTemplate} that this skill provides for an attribute.
     */
    public double getAttributeModifierAmplifier(LivingEntity entity, Holder<Attribute> holder, ManasSkill.AttributeTemplate template, int mode) {
        return this.getSkill().getAttributeModifierAmplifier(this, entity, holder, template, mode);
    }

    /**
     * Applies the attribute modifiers of this instance on the {@link LivingEntity} holding the skill activation button.
     *
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public void addHeldAttributeModifiers(LivingEntity entity, int mode) {
        this.getSkill().addHeldAttributeModifiers(this, entity, mode);
    }

    /**
     * Removes the attribute modifiers of this instance from the {@link LivingEntity} holding the skill activation button.
     *
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public void removeAttributeModifiers(LivingEntity entity, int mode) {
        this.getSkill().removeAttributeModifiers(this, entity, mode);
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill toggles this {@link ManasSkill} type of this instance on.
     *
     * @param entity Affected {@link LivingEntity} owning this Skill.
     */
    public void onToggleOn(LivingEntity entity) {
        this.getSkill().onToggleOn(this, entity);
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill toggles this {@link ManasSkill} type of this instance off.
     *
     * @param entity Affected {@link LivingEntity} owning this instance.
     */
    public void onToggleOff(LivingEntity entity) {
        this.getSkill().onToggleOff(this, entity);
    }

    /**
     * Called every tick if this instance is obtained by {@link LivingEntity}.
     *
     * @param living Affected {@link LivingEntity} owning this instance.
     */
    public void onTick(LivingEntity living) {
        this.getSkill().onTick(this, living);
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill presses the skill activation button.
     *
     * @param entity    Affected {@link LivingEntity} owning this instance.
     * @param keyNumber The key number that was pressed.
     * @param mode      The mode that was activated.
     */
    public void onPressed(LivingEntity entity, int keyNumber, int mode) {
        this.getSkill().onPressed(this, entity, keyNumber, mode);
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill holds the skill activation button.
     *
     * @param entity    Affected {@link LivingEntity} owning this instance.
     * @param heldTicks The number of ticks the skill activation button is being held down.
     * @param mode      The mode that is being held down.
     * @return true to continue ticking this instance.
     */
    public boolean onHeld(LivingEntity entity, int heldTicks, int mode) {
        return this.getSkill().onHeld(this, entity, heldTicks, mode);
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill releases the skill activation button after {@param heldTicks}.
     *
     * @param entity    Affected {@link LivingEntity} owning this instance.
     * @param heldTicks The number of ticks the skill activation button is held down.
     * @param keyNumber The key number that was pressed.
     * @param mode      The mode that was activated.
     */
    public void onRelease(LivingEntity entity, int heldTicks, int keyNumber, int mode) {
        this.getSkill().onRelease(this, entity, heldTicks, keyNumber, mode);
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill scrolls the mouse when holding the skill activation buttons.
     *
     * @param entity    Affected {@link LivingEntity} owning this instance.
     * @param delta     The scroll delta of the mouse scroll.
     * @param mode      The mode that was activated.
     */
    public void onScroll(LivingEntity entity, double delta, int mode) {
        this.getSkill().onScroll(this, entity, delta, mode);
    }

    /**
     * Called when the {@link LivingEntity} learns this instance.
     *
     * @param entity Affected {@link LivingEntity} learning this instance.
     */
    public void onLearnSkill(LivingEntity entity) {
        this.getSkill().onLearnSkill(this, entity);
    }

    /**
     * Called when the {@link LivingEntity} forgets this instance.
     *
     * @param entity Affected {@link LivingEntity} learning this instance.
     */
    public void onForgetSkill(LivingEntity entity) {
        this.getSkill().onForgetSkill(this, entity);
    }

    /**
     * Called when the {@link LivingEntity} masters this instance.
     *
     * @param entity Affected {@link LivingEntity} owning this Skill.
     */
    public void onSkillMastered(LivingEntity entity) {
        this.getSkill().onSkillMastered(this, entity);
    }

    /**
     * Called when the {@link LivingEntity} owning this instance gains an effect.
     *
     * @param entity owning this instance.
     */
    public boolean onEffectAdded(LivingEntity entity, @Nullable Entity source, Changeable<MobEffectInstance> instance) {
        return this.getSkill().onEffectAdded(this, entity, source, instance);
    }

    /**
     * Called when the {@link LivingEntity} owning this instance starts to be targeted by a mob.
     *
     * @return false will stop the mob from targeting the owner.
     */
    public boolean onBeingTargeted(Changeable<LivingEntity> owner, LivingEntity mob) {
        return this.getSkill().onBeingTargeted(this, owner, mob);
    }

    /**
     * Called when the {@link LivingEntity} owning this instance starts to be attacked.
     * <p>
     * Gets executed before {@link ManasSkillInstance#onDamageEntity}
     *
     * @return false will prevent the owner from taking damage.
     */
    public boolean onBeingDamaged(LivingEntity entity, DamageSource source, float amount) {
        return this.getSkill().onBeingDamaged(this, entity, source, amount);
    }

    /**
     * Called when the {@link LivingEntity} owning this instance starts attacking another {@link LivingEntity}.
     * <p>
     * Gets executed after {@link ManasSkillInstance#onBeingDamaged}<br>
     * Gets executed before {@link ManasSkillInstance#onTouchEntity}
     *
     * @return false will prevent the owner from dealing damage
     */
    public boolean onDamageEntity(LivingEntity owner, LivingEntity target, DamageSource source, Changeable<Float> amount) {
        return this.getSkill().onDamageEntity(this, owner, target, source, amount);
    }

    /**
     * Called when the {@link LivingEntity} owning this instance hurts another {@link LivingEntity} (after effects like Barriers are consumed the damage amount).
     * <p>
     * Gets executed after {@link ManasSkillInstance#onDamageEntity}
     * Gets executed before {@link ManasSkillInstance#onTakenDamage}
     *
     * @return false will prevent the owner from dealing damage.
     */
    public boolean onTouchEntity(LivingEntity owner, LivingEntity target, DamageSource source, Changeable<Float> amount) {
        return this.getSkill().onTouchEntity(this, owner, target, source, amount);
    }

    /**
     * Called when the {@link LivingEntity} owning this instance takes damage.
     * <p>
     * Gets executed after {@link ManasSkillInstance#onTouchEntity}
     *
     * @return false will prevent the owner from taking damage.
     */
    public boolean onTakenDamage(LivingEntity owner, DamageSource source, Changeable<Float> amount) {
        return this.getSkill().onTakenDamage(this, owner, source, amount);
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill is hit by a projectile.
     */
    public void onProjectileHit(LivingEntity living, EntityHitResult hitResult, Projectile projectile, Changeable<ProjectileDeflection> deflection, Changeable<EntityEvents.ProjectileHitResult> result) {
        this.getSkill().onProjectileHit(this, living, hitResult, projectile, deflection, result);
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill dies.
     *
     * @return false will prevent the owner from dying.
     */
    public boolean onDeath(LivingEntity owner, DamageSource source) {
        return this.getSkill().onDeath(this, owner, source);
    }

    /**
     * Called when the {@link ServerPlayer} owning this Skill respawns.
     */
    public void onRespawn(ServerPlayer owner, boolean conqueredEnd) {
        this.getSkill().onRespawn(this, owner, conqueredEnd);
    }

    public MutableComponent getDisplayName() {
        return this.getSkill().getName();
    }

    public MutableComponent getChatDisplayName(boolean withDescription) {
        return this.getSkill().getChatDisplayName(withDescription);
    }

    public boolean is(TagKey<ManasSkill> tag) {
        return this.skillRegistryObject.is(tag);
    }
}
