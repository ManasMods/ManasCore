package com.github.manasmods.manascore.api.skills;

import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * This is the Registry Object for Skills.
 * Extend from this Class to create your own Skills.
 * <p>
 * To add functionality to the {@link ManasSkill}, you need to implement a listener interface.
 * Those interfaces allow you to invoke a Method when an {@link Event} happens.
 * The Method will only be invoked for an {@link Entity} that learned the {@link ManasSkill}.
 * <p>
 * Skills can be learned by calling the {@link SkillStorage#learnSkill} method.
 * You can simply use {@link SkillAPI#getSkillsFrom(Entity)} to get the {@link SkillStorage} of an {@link Entity}.
 * <p>
 * You're also allowed to override the {@link ManasSkill#createDefaultInstance()} method to create your own implementation
 * of a {@link ManasSkillInstance}. This is required if you want to attach additional data to the {@link ManasSkill}
 * (for example to allow to disable a skill or make the skill gain exp on usage).
 */
@ApiStatus.AvailableSince("1.0.2.0")
public class ManasSkill {
    /**
     * Used to create a {@link ManasSkillInstance} of this Skill.
     * <p>
     * Override this Method to use your extended version of {@link ManasSkillInstance}
     */
    public ManasSkillInstance createDefaultInstance() {
        return new ManasSkillInstance(this);
    }

    /**
     * Used to get the {@link ResourceLocation} id of this skill.
     */
    @Nullable
    public ResourceLocation getRegistryName() {
        return SkillAPI.getSkillRegistry().getKey(this);
    }

    /**
     * Used to get the {@link MutableComponent} name of this skill for translation.
     */
    @Nullable
    public MutableComponent getName() {
        final ResourceLocation id = getRegistryName();
        if (id == null) return null;
        return Component.translatable(String.format("%s.skill.%s", id.getNamespace(), id.getPath().replace('/', '.')));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManasSkill skill = (ManasSkill) o;
        return Objects.equals(getRegistryName(), skill.getRegistryName());
    }

    /**
     * Determine if the {@link ManasSkillInstance} of this Skill can be used by {@link LivingEntity}.
     * Returning false will stop {@link LivingEntity} from using any feature of the skill.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param living   Affected {@link LivingEntity}
     */
    public boolean canInteractSkill(ManasSkillInstance instance , LivingEntity living) {
        return true;
    }

    /**
     * Determine if this skill can be toggleable.
     * Returning false if this skill is not toggleable.
     */
    public boolean canBeToggled() {
        return false;
    }

    /**
     * Determine if the {@link ManasSkillInstance} of this Skill is mastered.
     *
     * @param instance Affected {@link ManasSkillInstance}
     */
    public boolean isMastered(ManasSkillInstance instance) {
        return instance.getMastery() >= 100;
    }

    /**
     * Increase the mastery points for {@link ManasSkillInstance} of this Skill if not mastered.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param player   Affected {@link Player}
     */
    public void addMasteryPoint(ManasSkillInstance instance, Player player) {
        if (isMastered(instance)) return;
        instance.setMastery(instance.getMastery() + 1);
    }

    /**
     * Called when the {@link Player} toggles this Skill on.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param player   Affected {@link Player}
     */
    public void onToggleOn(ManasSkillInstance instance, Player player) {
    }

    /**
     * Called when the {@link Player} toggles this Skill off.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param player   Affected {@link Player}
     */
    public void onToggleOff(ManasSkillInstance instance, Player player) {
    }

    /**
     * Called every tick if this Skill is obtained.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param player   Affected {@link Player}
     */
    public void onTick(ManasSkillInstance instance, Player player) {
    }

    /**
     * Called when the {@link Player} presses the skill activation button.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param player   Affected {@link Player}
     */
    public void onActivation(ManasSkillInstance instance, Player player) {
    }

    /**
     * Called when the {@link Player} releases the skill activation button after {@param heldTicks}.
     *
     * @param instance  Affected {@link ManasSkillInstance}
     * @param player    Affected {@link Player}
     */
    public void onRelease(ManasSkillInstance instance, Player player, int heldTicks) {
    }

    /**
     * Called when the {@link Player} scrolls the mouse when holding the skill activation buttons.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param player   Affected {@link Player}
     * @param direction decides if the mouse scroll is scrolled up (positive) or down (negative).
     */
    public void onScroll(ManasSkillInstance instance, Player player, int direction) {
    }

    /**
     * Called when the {@link Player} right-clicks a block.
     *
     * @param instance  Affected {@link ManasSkillInstance}
     * @param player    Affected {@link Player}
     * @param hitResult Triggered {@link BlockHitResult}
     */
    public void onRightClickBlock(ManasSkillInstance instance, Player player, BlockHitResult hitResult) {
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill starts to be targeted by a mob.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param target   Affected {@link LivingEntity}
     * @param attacker Affected {@link LivingEntity}
     * @param event    Triggered {@link LivingChangeTargetEvent}
     */
    public void onBeingTargeted(ManasSkillInstance instance, LivingEntity target, LivingEntity attacker, LivingChangeTargetEvent event) {
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill starts to be attacked.
     * Canceling {@link LivingAttackEvent} will make the owner immune to the Damage Source.
     * Therefore, cancel the hurt sound, animation and knock back, but cannot change the damage amount like {@link LivingHurtEvent}
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity}
     * @param event    Triggered {@link LivingAttackEvent}
     */
    public void onBeingDamaged(ManasSkillInstance instance, LivingEntity entity, LivingAttackEvent event) {
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill gets hurt
     * Canceling {@link LivingHurtEvent} will not cancel the hurt sound, animation and knock back.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param attacker Affected {@link LivingEntity}
     * @param entity   Affected {@link LivingEntity}
     * @param event    Triggered {@link LivingHurtEvent}
     */
    public void onDamageEntity(ManasSkillInstance instance, LivingEntity attacker, LivingEntity entity, LivingHurtEvent event) {
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill gets hurt (after effects like Barriers is consumed the damage amount)
     * Canceling {@link LivingHurtEvent} will not cancel the hurt sound, animation and knock back.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param attacker Affected {@link LivingEntity}
     * @param entity   Affected {@link LivingEntity} - Attacked Entity
     * @param event    Triggered {@link LivingHurtEvent}
     */
    public void onTouchEntity(ManasSkillInstance instance, LivingEntity attacker, LivingEntity entity, LivingHurtEvent event) {
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill gets damaged (after armor, potion effects, etc. is calculated)
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param living   Affected {@link LivingEntity}
     * @param event    Triggered {@link LivingDamageEvent}
     */
    public void onTakenDamage(ManasSkillInstance instance, LivingEntity living, LivingDamageEvent event) {
    }

    /**
     * Called when the {@link LivingEntity} is hit by a projectile.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param living   Affected {@link LivingEntity}
     * @param event    Triggered {@link ProjectileImpactEvent}
     */
    public void onProjectileHit(ManasSkillInstance instance, LivingEntity living, ProjectileImpactEvent event) {
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill dies
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param living   Affected {@link LivingEntity}
     * @param event    Triggered {@link LivingDeathEvent}
     */
    public void onDeath(ManasSkillInstance instance, LivingEntity living, LivingDeathEvent event) {
    }

    /**
     * Called when the {@link Player} owning this Skill respawns
     *
     * @param instance Affected {@link ManasSkillInstance}
     * {@link PlayerEvent.PlayerRespawnEvent} invoking this callback
     */
    public void onRespawn(ManasSkillInstance instance, PlayerEvent.PlayerRespawnEvent event) {
    }
}