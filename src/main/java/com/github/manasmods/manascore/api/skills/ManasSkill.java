package com.github.manasmods.manascore.api.skills;

import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.ApiStatus;

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
     * Called when the {@link LivingEntity} owning this Skill gets hurt
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param event    Triggered {@link LivingHurtEvent}
     */
    public void onEntityHurt(ManasSkillInstance instance, LivingHurtEvent event) {
    }

    /**
     * Called when the {@link Player} toggles this Skill on.
     * Triggered by mods using this lib, not the lib itself.
     *
     * @param instance Affected {@link ManasSkillInstance}
     */
    public void onToggleOn(ManasSkillInstance instance, Player player) {
    }

    /**
     * Called when the {@link Player} toggles this Skill off.
     * Triggered by mods using this lib, not the lib itself.
     *
     * @param instance Affected {@link ManasSkillInstance}
     */
    public void onToggleOff(ManasSkillInstance instance, Player player) {
    }

    /**
     * Called every tick if this Skill is toggled.
     * Triggered by mods using this lib, not the lib itself.
     *
     * @param instance Affected {@link ManasSkillInstance}
     */
    public void onTick(ManasSkillInstance instance, Player player) {
    }

    /**
     * Called when the {@link Player} presses the skill activation button.
     * Triggered by mods using this lib, not the lib itself.
     *
     * @param instance Affected {@link ManasSkillInstance}
     */
    public void onActivation(ManasSkillInstance instance, Player player) {
    }

    /**
     * Called when the {@link Player} releases the skill activation button.
     * Triggered by mods using this lib, not the lib itself.
     *
     * @param instance Affected {@link ManasSkillInstance}
     */
    public void onRelease(ManasSkillInstance instance, Player player) {
    }

    /**
     * Called when the {@link Player} scrolls the mouse when holding the skill activation buttons.
     * Triggered by mods using this lib, not the lib itself.
     *
     * @param instance Affected {@link ManasSkillInstance}
     */
    public boolean onScroll(ManasSkillInstance instance, Player player, int direction) {
        return false;
    }

    /**
     * Called when the {@link Player} right-clicks a block.
     * Triggered by mods using this lib, not the lib itself.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param hitResult Triggered {@link BlockHitResult}
     */
    public void onRightClickBlock(ManasSkillInstance instance, Player player, BlockHitResult hitResult) {
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill gets hurt
     * Change the amount of the damage that the owner takes.
     * Triggered by mods using this lib, not the lib itself.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param event    Triggered {@link LivingHurtEvent}
     */
    public float onDamageEntity(ManasSkillInstance instance, LivingEntity living, LivingEntity entity, LivingHurtEvent event) {
        return event.getAmount();
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill gets hurt (after effects like Barriers is consumed the damage amount)
     * Change the amount of the damage that the owner takes.
     * Triggered by mods using this lib, not the lib itself.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param event    Triggered {@link LivingHurtEvent}
     */
    public float onTouchEntity(ManasSkillInstance instance, LivingEntity living, LivingEntity entity, LivingHurtEvent event) {
        return event.getAmount();
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill gets damaged (after armor, potion effects, etc. is calculated)
     * Change the amount of the damage that the owner takes.
     * Triggered by mods using this lib, not the lib itself.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param event    Triggered {@link LivingDamageEvent}
     */
    public float onTakenDamage(ManasSkillInstance instance, LivingEntity living, LivingDamageEvent event) {
        return event.getAmount();
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill dies
     * Cancel the death event when return true.
     * Triggered by mods using this lib, not the lib itself.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param event    Triggered {@link LivingDeathEvent}
     */
    public boolean onDeath(ManasSkillInstance instance, LivingEntity living, LivingDeathEvent event) {
        return false;
    }

    /**
     * Called when the {@link Player} owning this Skill respawns
     * Triggered by mods using this lib, not the lib itself.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param event    Triggered {@link PlayerEvent.PlayerRespawnEvent}
     */
    public void onRespawn(ManasSkillInstance instance, PlayerEvent.PlayerRespawnEvent event) {
    }
}