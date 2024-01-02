package com.github.manasmods.manascore.api.skill;

import com.github.manasmods.manascore.api.world.entity.EntityEvents.ProjectileHitResult;
import com.github.manasmods.manascore.utils.Changeable;
import dev.architectury.registry.registries.RegistrySupplier;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ManasSkillInstance {
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
        return this.skillRegistryObject.getId();
    }

    /**
     * Used to create an exact copy of the current instance.
     */
    public ManasSkillInstance copy() {
        ManasSkillInstance clone = new ManasSkillInstance(getSkill());
        clone.dirty = this.dirty;
        clone.mode = this.mode;
        clone.coolDown = this.coolDown;
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
    public static ManasSkillInstance fromNBT(CompoundTag tag) {
        ResourceLocation skillLocation = ResourceLocation.tryParse(tag.getString("skill"));
        ManasSkillInstance instance = Objects.requireNonNull(SkillAPI.getSkillRegistry().get(skillLocation)).createDefaultInstance();
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
        return Objects.hash(skillRegistryObject);
    }

    /**
     * Determine if this instance can be used by {@link LivingEntity}.
     *
     * @param living Affected {@link LivingEntity}
     * @return false will stop {@link LivingEntity} from using any feature of the skill.
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
    public boolean canIgnoreCoolDown(LivingEntity entity) {
        return this.getSkill().canIgnoreCoolDown(this, entity);
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
     * One skill can have many modes at once and each mode can have their own different features.
     *
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
     * @return if this skill instance is temporary, which should be removed when its time runs out.
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
     * @param entity Affected {@link LivingEntity} owning this instance.
     */
    public void onPressed(LivingEntity entity) {
        this.getSkill().onPressed(this, entity);
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill holds the skill activation button.
     *
     * @param entity Affected {@link LivingEntity} owning this instance.
     * @return true to continue ticking this instance.
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
     * @param entity Affected {@link LivingEntity} owning this instance.
     * @param delta  The scroll delta of the mouse scroll.
     */
    public void onScroll(LivingEntity entity, double delta) {
        this.getSkill().onScroll(this, entity, delta);
    }

    /**
     * Called when the {@link LivingEntity} learns this instance.
     *
     * @param living Affected {@link LivingEntity} learning this instance.
     */
    public void onLearnSkill(LivingEntity living) {
        this.getSkill().onLearnSkill(this, living);
    }

    /**
     * Called when the {@link LivingEntity} masters this instance.
     *
     * @param living Affected {@link LivingEntity} owning this Skill.
     */
    public void onSkillMastered(LivingEntity living) {
        this.getSkill().onSkillMastered(this, living);
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill right-clicks a block.
     *
     * @param player owning this instance.
     */
    public void onRightClickBlock(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        this.getSkill().onRightClickBlock(this, player, hand, pos, face);
    }

    /**
     * Called when the {@link LivingEntity} owning this instance starts to be targeted by a mob.
     *
     * @return false will stop the mob from targeting the owner.
     */
    public boolean onBeingTargeted(Changeable<LivingEntity> target) {
        return this.getSkill().onBeingTargeted(this, target);
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
     * Called when the {@link LivingEntity} owning this instance gets hurt.
     * <p>
     * Gets executed after {@link ManasSkillInstance#onBeingDamaged}<br>
     * Gets executed before {@link ManasSkillInstance#onTouchEntity}
     *
     * @return false will prevent the owner from taking damage.
     */
    public boolean onDamageEntity(LivingEntity owner, DamageSource source, Changeable<Float> amount) {
        return this.getSkill().onDamageEntity(this, owner, source, amount);
    }

    /**
     * Called when the {@link LivingEntity} owning this instance gets hurt (after effects like Barriers are consumed the damage amount).
     * <p>
     * Gets executed after {@link ManasSkillInstance#onDamageEntity}
     * Gets executed before {@link ManasSkillInstance#onTakenDamage}
     *
     * @return false will prevent the owner from taking damage.
     */
    public boolean onTouchEntity(LivingEntity owner, DamageSource source, Changeable<Float> amount) {
        return this.getSkill().onTouchEntity(this, owner, source, amount);
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
    public void onProjectileHit(LivingEntity living, EntityHitResult hitResult, Projectile projectile, Changeable<ProjectileHitResult> result) {
        this.getSkill().onProjectileHit(this, living, hitResult, projectile, result);
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
}
