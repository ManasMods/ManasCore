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

    public boolean canInteractSkill(LivingEntity living) {
        return this.getSkill().canInteractSkill(this, living);
    }
    public boolean canBeToggled() {
        return this.getSkill().canBeToggled();
    }

    /**
     * Modes of the skill
     */
    public int getMode() {
        return this.mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * Mastery point of the skill
     */
    public boolean isMastered() {
        return this.getSkill().isMastered(this);
    }

    public void addMasteryPoint(Player player) {
        this.getSkill().addMasteryPoint(this, player);
    }

    public int getMastery() {
        return this.masteryPoint;
    }

    public void setMastery(int point) {
        this.masteryPoint = point;
    }

    /**
     * Cooldown of the skill's action
     */
    public boolean onCoolDown() {
        return this.coolDown > 0;
    }
    public void setCoolDown(int coolDown) {
        this.coolDown = coolDown;
    }
    public void decreaseCoolDown(int coolDown) {
        this.coolDown -= coolDown;
    }

    /**
     * Remove the skill after some time if temporary
     */
    public boolean isTemporarySkill() {
        return this.removeTime != -1;
    }
    public boolean shouldRemove() {
        return this.removeTime == 0;
    }
    public void setRemoveTime(int removeTime) {
        this.removeTime = removeTime;
    }

    public void decreaseRemoveTime(int time) {
        this.removeTime -= time;
    }

    /**
     * Toggling the skill
     */
    public boolean isToggled() {
        return this.toggled;
    }
    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    /**
     * Additional Tags for the instance
     */
    @Nullable
    public CompoundTag getTag() {
        return this.tag;
    }

    public CompoundTag getOrCreateTag() {
        if (this.tag == null) {
            this.setTag(new CompoundTag());
        }
        return this.tag;
    }
    public void setTag(@Nullable CompoundTag tag) {
        this.tag = tag;
    }

    /** ACTION **/
    public void onToggleOn(Player player) {
        this.getSkill().onToggleOn(this, player);
    }

    public void onToggleOff(Player player) {
        this.getSkill().onToggleOff(this, player);
    }

    public void onTick(Player player) {
        this.getSkill().onTick(this, player);
    }

    public void onActivation(Player player) {
        this.getSkill().onActivation(this, player);
    }

    public void onRelease(Player player, int heldTicks) {
        this.getSkill().onRelease(this, player, heldTicks);
    }

    public void onScroll(Player player, int direction) {
        this.getSkill().onScroll(this, player, direction);
    }

    public void onRightClickBlock(Player player, BlockHitResult hitResult) {
        this.getSkill().onRightClickBlock(this, player, hitResult);
    }

    public float onDamageEntity(LivingEntity living, LivingEntity entity, LivingHurtEvent e) {
        return this.getSkill().onDamageEntity(this, living, entity, e);
    }

    public float onTouchEntity(LivingEntity living, LivingEntity entity, LivingHurtEvent e) {
        return this.getSkill().onTouchEntity(this, living, entity, e);
    }

    public float onTakenDamage(LivingEntity living, LivingDamageEvent e) {
        return this.getSkill().onTakenDamage(this, living, e);
    }

    public boolean onProjectileHit(LivingEntity living, ProjectileImpactEvent event) {
        return this.getSkill().onProjectileHit(this, living, event);
    }

    public boolean onDeath(LivingEntity living, LivingDeathEvent e) {
        return this.getSkill().onDeath(this, living, e);
    }

    public void onRespawn(ManasSkillInstance instance, PlayerEvent.PlayerRespawnEvent event) {
        this.getSkill().onRespawn(instance, event);
    }
}