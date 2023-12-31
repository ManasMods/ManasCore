package com.github.manasmods.manascore.api.skill;

import dev.architectury.registry.registries.RegistrySupplier;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ManasSkillInstance implements Cloneable {
    private int mode = 1;
    private int coolDown;
    private int removeTime = -1;
    private int masteryPoint;
    private boolean toggled;
    @Nullable
    private CompoundTag tag;
    @Getter
    private boolean dirty = false;
    protected final RegistrySupplier<ManasSkill> skillRegistryObject;
    public ManasSkillInstance(ManasSkill skill) {
        this.skillRegistryObject = SkillAPI.getSkillRegistry().delegate(SkillAPI.getSkillRegistry().getId(skill));
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
        clone.dirty = this.dirty;
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
        tag.putInt("Mode", this.mode);
        tag.putInt("CoolDown", this.coolDown);
        tag.putInt("RemoveTime", this.removeTime);
        tag.putInt("Mastery", this.masteryPoint);
        tag.putBoolean("Toggled", this.toggled);
        if (this.tag != null) tag.put("tag", this.tag.copy());
        return tag;
    }

    /**
     * Can be used to load custom data.
     */
    public void deserialize(CompoundTag tag) {
        this.mode = tag.getInt("Mode");
        this.coolDown = tag.getInt("CoolDown");
        this.removeTime = tag.getInt("RemoveTime");
        this.masteryPoint = tag.getInt("Mastery");
        this.toggled = tag.getBoolean("Toggled");
        if (tag.contains("tag", 10)) this.tag = tag.getCompound("tag");
    }

    /**
     * Can be used to load a {@link ManasSkillInstance} from a {@link CompoundTag}.
     * <p>
     * The {@link CompoundTag} has to be created though {@link ManasSkillInstance#toNBT()}
     */
    @ApiStatus.NonExtendable
    public static ManasSkillInstance fromNBT(CompoundTag tag) {
        ResourceLocation skillLocation = ResourceLocation.tryParse(tag.getString("skill"));
        ManasSkillInstance instance = Objects.requireNonNull(SkillAPI.getSkillRegistry().get(skillLocation)).createDefaultInstance();
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

    /**
     * Determine if this instance can be used by {@link LivingEntity}.
     * @return false will stop {@link LivingEntity} from using any feature of the skill.
     *
     * @param living   Affected {@link LivingEntity}
     */
    public boolean canInteractSkill(LivingEntity living) {
        return this.getSkill().canInteractSkill(this, living);
    }

    /**
     * @return the maximum number of ticks that this skill can be held down with the skill activation button.
     * </p>
     */
    public int getMaxHeldTime() {
        return this.getSkill().getMaxHeldTime();
    }

    /**
     * Determine if the {@link ManasSkill} type of this instance can be toggled.
     * @return false if this skill is not toggleable.
     *
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public boolean canBeToggled(LivingEntity entity) {
        return this.getSkill().canBeToggled(this, entity);
    }

    /**
     * Determine if the {@link ManasSkill} type of this instance can still be activated when on cooldown.
     * @return false if this skill cannot ignore cooldown.
     *
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public boolean canIgnoreCoolDown(LivingEntity entity) {
        return this.getSkill().canIgnoreCoolDown(this, entity);
    }

    /**
     * Determine if this instance's {@link ManasSkillInstance#onTick} can be executed.
     * @return false if this skill cannot tick.
     *
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public boolean canTick(LivingEntity entity) {
        return this.getSkill().canTick(this, entity);
    }

    /**
     * One skill can have many modes at once and each mode can have their own different features.
     * @return the current mode of this instance.
     */
    public int getMode() {
        return this.mode;
    }

    /**
     * Change the mode of this instance.
     *
     * @see ManasSkillInstance#getMode()
     */
    public void setMode(int mode) {
        this.mode = mode;
        markDirty();
    }

    /**
     * @return the maximum mastery points that this skill instance can have.
     * </p>
     */
    public int getMaxMastery() {
        return this.getSkill().getMaxMastery();
    }

    /**
     * Determine if the {@link ManasSkill} type of this instance is mastered by {@link LivingEntity} owning it.
     *
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public boolean isMastered(LivingEntity entity) {
        return this.getSkill().isMastered(this, entity);
    }

    /**
     * Increase the mastery point of the {@link ManasSkill} type of this instance.
     *
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public void addMasteryPoint(LivingEntity entity) {
        this.getSkill().addMasteryPoint(this, entity);
    }

    /**
     * @return the mastery point of the {@link ManasSkill} type of this instance.
     */
    public int getMastery() {
        return this.masteryPoint;
    }

    /**
     * Set the mastery point of the {@link ManasSkill} type of this instance.
     */
    public void setMastery(int point) {
        this.masteryPoint = point;
        markDirty();
    }

    /**
     * @return the cooldown of this instance.
     */
    public int getCoolDown() {
        return this.coolDown;
    }

    /**
     * @return if this instance is on cooldown.
     */
    public boolean onCoolDown() {
        return this.coolDown > 0;
    }

    /**
     * Set the cooldown of this instance.
     */
    public void setCoolDown(int coolDown) {
        this.coolDown = coolDown;
        markDirty();
    }

    /**
     * Decrease the cooldown of this instance.
     */
    public void decreaseCoolDown(int coolDown) {
        this.coolDown -= coolDown;
        markDirty();
    }

    /**
     * @return  if this skill instance is temporary, which should be removed when its time runs out.
     */
    public boolean isTemporarySkill() {
        return this.removeTime != -1;
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
        this.removeTime -= time;
        markDirty();
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
     * Called when the {@link LivingEntity} owning this Skill toggles this {@link ManasSkill} type of this instance on.
     *
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public void onToggleOn(LivingEntity entity) {
        this.getSkill().onToggleOn(this, entity);
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill toggles this {@link ManasSkill} type of this instance off.
     *
     * @param entity   Affected {@link LivingEntity} owning this instance.
     */
    public void onToggleOff(LivingEntity entity) {
        this.getSkill().onToggleOff(this, entity);
    }

    /**
     * Called every tick if this instance is obtained by {@link LivingEntity}.
     *
     * @param living   Affected {@link LivingEntity} owning this instance.
     */
    public void onTick(LivingEntity living) {
        this.getSkill().onTick(this, living);
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill presses the skill activation button.
     *
     * @param entity   Affected {@link LivingEntity} owning this instance.
     */
    public void onPressed(LivingEntity entity) {
        this.getSkill().onPressed(this, entity);
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill holds the skill activation button.
     * @return true to continue ticking this instance.
     *
     * @param entity   Affected {@link LivingEntity} owning this instance.
     */
    public boolean onHeld(LivingEntity entity, int heldTicks) {
        return this.getSkill().onHeld(this, entity, heldTicks);
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill releases the skill activation button after {@param heldTicks}.
     *
     * @param entity    Affected {@link LivingEntity} owning this instance.
     * @param heldTicks - the number of ticks the skill activation button is held down.
     */
    public void onRelease(LivingEntity entity, int heldTicks) {
        this.getSkill().onRelease(this, entity, heldTicks);
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill scrolls the mouse when holding the skill activation buttons.
     *
     * @param entity   Affected {@link LivingEntity} owning this instance.
     * @param delta    The scroll delta of the mouse scroll.
     */
    public void onScroll(LivingEntity entity, double delta) {
        this.getSkill().onScroll(this, entity, delta);
    }

    /**
     * Called when the {@link LivingEntity} learns this instance.
     *
     * @param living   Affected {@link LivingEntity} learning this instance.
     */
    public void onLearnSkill(LivingEntity living, UnlockSkillEvent event) {
        this.getSkill().onLearnSkill(this, living, event);
    }

    /**
     * Called when the {@link LivingEntity} masters this instance.
     *
     * @param living   Affected {@link LivingEntity} owning this Skill.
     */
    public void onSkillMastered(LivingEntity living) {
        this.getSkill().onSkillMastered(this, living);
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill right-clicks a block.
     *
     * @param entity    Affected {@link LivingEntity} owning this instance.
     * @param hitResult Triggered {@link BlockHitResult}
     */
    public void onRightClickBlock(LivingEntity entity, BlockHitResult hitResult) {
        this.getSkill().onRightClickBlock(this, entity, hitResult);
    }

    /**
     * Called when the {@link LivingEntity} owning this instance starts to be targeted by a mob.
     *
     * @param target   Affected {@link LivingEntity} owning this instance.
     * @param event    Triggered {@link LivingChangeTargetEvent}
     */
    public void onBeingTargeted(LivingEntity target, LivingChangeTargetEvent event) {
        this.getSkill().onBeingTargeted(this, target, event);
    }

    /**
     * Called when the {@link LivingEntity} owning this instance starts to be attacked.
     * Canceling {@link LivingAttackEvent} will make the owner immune to the Damage Source.
     * Therefore, cancel the hurt sound, animation and knock back, but cannot change the damage amount like {@link LivingHurtEvent}
     *
     * Executing Order: This method gets invoked first before any Damage method.
     *
     * @param event    Triggered {@link LivingAttackEvent}
     */
    public void onBeingDamaged(LivingAttackEvent event) {
        this.getSkill().onBeingDamaged(this, event);
    }

    /**
     * Called when the {@link LivingEntity} owning this instance gets hurt
     * Change the amount of the damage that the owner takes.
     * <p>
     * Executing Order: This method gets invoked after {@link ManasSkillInstance#onBeingDamaged}
     *
     * @param entity   Affected {@link LivingEntity} owning this instance.
     * @param event    Triggered {@link LivingHurtEvent}
     */
    public void onDamageEntity(LivingEntity entity, LivingHurtEvent event) {
        this.getSkill().onDamageEntity(this, entity, event);
    }

    /**
     * Called when the {@link LivingEntity} owning this intance gets hurt (after effects like Barriers is consumed the damage amount)
     * Change the amount of the damage that the owner takes.
     * <p>
     * Executing Order: This method gets invoked after {@link ManasSkillInstance#onDamageEntity}
     *
     * @param entity   Affected {@link LivingEntity} owning this instance.
     * @param event    Triggered {@link LivingHurtEvent}
     */
    public void onTouchEntity(LivingEntity entity, LivingHurtEvent event) {
        this.getSkill().onTouchEntity(this, entity, event);
    }

    /**
     * Called when the {@link LivingEntity} owning this instance takes damage.
     * Change the amount of the damage that the owner takes.
     *
     * @param event    Triggered {@link LivingDamageEvent}
     */
    public void onTakenDamage(LivingDamageEvent event) {
        this.getSkill().onTakenDamage(this, event);
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill is hit by a projectile.
     * Cancel the event when return true.
     *
     * @param living   Affected {@link LivingEntity} owning this instance.
     * @param event    Triggered {@link ProjectileImpactEvent}
     */
    public void onProjectileHit(LivingEntity living, ProjectileImpactEvent event) {
        this.getSkill().onProjectileHit(this, living, event);
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill dies
     *
     * @param event    Triggered {@link LivingDeathEvent}
     */
    public void onDeath(LivingDeathEvent event) {
        this.getSkill().onDeath(this, event);
    }

    /**
     * {@link PlayerEvent.PlayerRespawnEvent} invoking this callback
     * <p>
     * @param event    Triggered {@link PlayerEvent.PlayerRespawnEvent}
     */
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        this.getSkill().onRespawn(this, event);
    }
}