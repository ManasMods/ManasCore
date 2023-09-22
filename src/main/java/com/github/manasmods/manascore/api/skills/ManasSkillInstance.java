package com.github.manasmods.manascore.api.skills;

import com.github.manasmods.manascore.network.toclient.SyncSkillsPacket;
import lombok.Getter;
import net.minecraft.core.Holder.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Mutable instance of a {@link ManasSkill}.
 * This instances will be stored in the {@link Entity} capability.
 * <p>
 * Instances are created though the {@link ManasSkill#createDefaultInstance()} method.
 */
@ApiStatus.AvailableSince("1.0.2.0")
public class ManasSkillInstance implements Cloneable {
    private int mode = 1;
    private int coolDown;
    private int removeTime = -1;
    private int masteryPoint;
    private boolean toggled;
    @Nullable
    private CompoundTag tag;
    private final Reference<ManasSkill> skillRegistryObject;
    @Getter
    private boolean dirty = false;

    public ManasSkillInstance(ManasSkill skill) {
        this.skillRegistryObject = SkillAPI.getSkillRegistry().getDelegateOrThrow(skill);
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
        tag.putByte("Mode", (byte) this.mode);
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
        this.mode = tag.getByte("Mode");
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
        ManasSkillInstance instance = Objects.requireNonNull(SkillAPI.getSkillRegistry().getValue(skillLocation)).createDefaultInstance();
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
     * Determine if the {@link ManasSkill} type of this instance can be toggled.
     * @return false if this skill is not toggleable.
     */
    public boolean canBeToggled() {
        return this.getSkill().canBeToggled();
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
    }

    /**
     * Determine if the {@link ManasSkill} type of this instance is mastered.
     */
    public boolean isMastered() {
        return this.getSkill().isMastered(this);
    }

    /**
     * Increase the mastery point of the {@link ManasSkill} type of this instance.
     */
    public void addMasteryPoint(Player player) {
        this.getSkill().addMasteryPoint(this, player);
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
    }

    /**
     * Decrease the cooldown of this instance.
     */
    public void decreaseCoolDown(int coolDown) {
        this.coolDown -= coolDown;
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
    }

    /**
     * Decrease the remove time of this instance.
     */
    public void decreaseRemoveTime(int time) {
        this.removeTime -= time;
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
    }

    /**
     * Called when the {@link Player} owning this Skill toggles this {@link ManasSkill} type of this instance on.
     *
     * @param player   Affected {@link Player}
     */
    public void onToggleOn(Player player) {
        this.getSkill().onToggleOn(this, player);
    }

    /**
     * Called when the {@link Player} owning this Skill toggles this {@link ManasSkill} type of this instance off.
     *
     * @param player   Affected {@link Player}
     */
    public void onToggleOff(Player player) {
        this.getSkill().onToggleOff(this, player);
    }

    /**
     * Called every tick if this instance is obtained by {@link Player}.
     *
     * @param player   Affected {@link Player}
     */
    public void onTick(Player player) {
        this.getSkill().onTick(this, player);
    }

    /**
     * Called when the {@link Player} owning this Skill presses the skill activation button.
     *
     * @param player   Affected {@link Player}
     */
    public void onPressed(Player player) {
        this.getSkill().onPressed(this, player);
    }

    /**
     * Called when the {@link Player} owning this Skill holds the skill activation button.
     * @return true to continue ticking this instance.
     *
     * @param player   Affected {@link Player}
     */
    public boolean onHeld(Player player, int heldTicks) {
        return this.getSkill().onHeld(this, player, heldTicks);
    }

    /**
     * Called when the {@link Player} owning this Skill releases the skill activation button after {@param heldTicks}.
     *
     * @param player    Affected {@link Player}
     * @param heldTicks - the number of ticks the skill activation button is held down.
     */
    public void onRelease(Player player, int heldTicks) {
        this.getSkill().onRelease(this, player, heldTicks);
    }

    /**
     * Called when the {@link Player} owning this Skill scrolls the mouse when holding the skill activation buttons.
     *
     * @param player   Affected {@link Player}
     * @param delta    The scroll delta of the mouse scroll.
     */
    public void onScroll(Player player, double delta) {
        this.getSkill().onScroll(this, player, delta);
    }

    /**
     * Called when the {@link Player} owning this Skill right-clicks a block.
     *
     * @param player    Affected {@link Player}
     * @param hitResult Triggered {@link BlockHitResult}
     */
    public void onRightClickBlock(Player player, BlockHitResult hitResult) {
        this.getSkill().onRightClickBlock(this, player, hitResult);
    }

    /**
     * Called when the {@link LivingEntity} owning this instance starts to be targeted by a mob.
     *
     * @param target   Affected {@link LivingEntity} owning this instance.
     * @param attacker Affected {@link LivingEntity} targeting the owner of this instance.
     * @param event    Triggered {@link LivingChangeTargetEvent}
     */
    public void onBeingTargeted(LivingEntity target, LivingEntity attacker, LivingChangeTargetEvent event) {
        this.getSkill().onBeingTargeted(this, target, attacker, event);
    }

    /**
     * Called when the {@link LivingEntity} owning this instance starts to be attacked.
     * Canceling {@link LivingAttackEvent} will make the owner immune to the Damage Source.
     * Therefore, cancel the hurt sound, animation and knock back, but cannot change the damage amount like {@link LivingHurtEvent}
     *
     * @param event    Triggered {@link LivingAttackEvent}
     */
    public void onBeingDamaged(LivingAttackEvent event) {
        this.getSkill().onBeingDamaged(this, event);
    }

    /**
     * Called when the {@link LivingEntity} owning this instance gets hurt
     * Change the amount of the damage that the owner takes.
     *
     * @param entity   Affected {@link LivingEntity} being attacked by the owner of this SKill.
     * @param event    Triggered {@link LivingHurtEvent}
     */
    public void onDamageEntity(LivingEntity entity, LivingHurtEvent event) {
        this.getSkill().onDamageEntity(this, entity, event);
    }

    /**
     * Called when the {@link LivingEntity} owning this intance gets hurt (after effects like Barriers is consumed the damage amount)
     * Change the amount of the damage that the owner takes.
     *
     * @param entity   Affected {@link LivingEntity} being attacked by the owner of this SKill.
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
     * @param living   Affected {@link LivingEntity}
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