package com.github.manasmods.manascore.api.skill;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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

    /**
     * Used to get the {@link ResourceLocation} of this skill's icon texture.
     */
    @Nullable
    public ResourceLocation getSkillIcon() {
        ResourceLocation id = this.getRegistryName();
        if (id == null) return null;
        return new ResourceLocation(id.getNamespace(), "icons/skills/" + id.getPath());
    }

    /**
     * Used to get the {@link MutableComponent} description of this skill for translation.
     */
    public Component getSkillDescription() {
        ResourceLocation id = this.getRegistryName();
        if (id == null) return Component.empty();
        return Component.translatable(String.format("%s.skill.%s.description", id.getNamespace(), id.getPath().replace('/', '.')));
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
     * @return false will stop {@link LivingEntity} from using any feature of the skill.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param living   Affected {@link LivingEntity} owning this Skill.
     */
    public boolean canInteractSkill(ManasSkillInstance instance , LivingEntity living) {
        return true;
    }

    /**
     * @return the maximum number of ticks that this skill can be held down with the skill activation button.
     * </p>
     */
    public int getMaxHeldTime() {
        return 72000;
    }

    /**
     * Determine if this skill can be toggled.
     * @return false if this skill is not toggleable.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public boolean canBeToggled(ManasSkillInstance instance, LivingEntity entity) {
        return false;
    }

    /**
     * Determine if this skill can still be activated when on cooldown.
     * @return false if this skill cannot ignore cooldown.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public boolean canIgnoreCoolDown(ManasSkillInstance instance, LivingEntity entity) {
        return false;
    }

    /**
     * Determine if this skill's {@link ManasSkill#onTick} can be executed.
     * @return false if this skill cannot tick.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public boolean canTick(ManasSkillInstance instance, LivingEntity entity) {
        return false;
    }

    /**
     * @return the maximum mastery points that this skill can have.
     * </p>
     */
    public int getMaxMastery() {
        return 100;
    }

    /**
     * Determine if the {@link ManasSkillInstance} of this Skill is mastered by {@link LivingEntity} owning it.
     * @return true to will mark this Skill is mastered, which can be used for increase stats or additional features/modes.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public boolean isMastered(ManasSkillInstance instance, LivingEntity entity) {
        return instance.getMastery() >= getMaxMastery();
    }

    /**
     * Increase the mastery points for {@link ManasSkillInstance} of this Skill if not mastered.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public void addMasteryPoint(ManasSkillInstance instance, LivingEntity entity) {
        if (isMastered(instance, entity)) return;
        instance.setMastery(instance.getMastery() + 1);
        if (isMastered(instance, entity)) onSkillMastered(instance, entity);
    }

    /**
     * Called when the {@link LivingEntity} owing this Skill toggles it on.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public void onToggleOn(ManasSkillInstance instance, LivingEntity entity) {
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill toggles it off.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public void onToggleOff(ManasSkillInstance instance, LivingEntity entity) {
    }

    /**
     * Called every tick of the {@link LivingEntity} owning this Skill.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param living   Affected {@link LivingEntity} owning this Skill.
     */
    public void onTick(ManasSkillInstance instance, LivingEntity living) {
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill presses the skill activation button.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public void onPressed(ManasSkillInstance instance, LivingEntity entity) {
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill holds the skill activation button.
     * @return true to continue ticking this Skill.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param living   Affected {@link LivingEntity} owning this Skill.
     */
    public boolean onHeld(ManasSkillInstance instance, LivingEntity living, int heldTicks) {
        return false;
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill releases the skill activation button after {@param heldTicks}.
     *
     * @param instance  Affected {@link ManasSkillInstance}
     * @param entity    Affected {@link LivingEntity} owning this Skill.
     */
    public void onRelease(ManasSkillInstance instance, LivingEntity entity, int heldTicks) {
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill scrolls the mouse when holding the skill activation buttons.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param living   Affected {@link LivingEntity} owning this Skill.
     * @param delta    The scroll delta of the mouse scroll.
     */
    public void onScroll(ManasSkillInstance instance, LivingEntity living, double delta) {
    }

    /**
     * Called when the {@link LivingEntity} learns this Skill.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param living   Affected {@link LivingEntity} learning this Skill.
     * @param event    Triggered {@link UnlockSkillEvent}
     */
    public void onLearnSkill(ManasSkillInstance instance, LivingEntity living, UnlockSkillEvent event) {
    }

    /**
     * Called when the {@link LivingEntity} masters this skill.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param living   Affected {@link LivingEntity} owning this Skill.
     */
    public void onSkillMastered(ManasSkillInstance instance, LivingEntity living) {
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill right-clicks a block.
     *
     * @param instance  Affected {@link ManasSkillInstance}
     * @param entity    Affected {@link LivingEntity} owning this Skill.
     * @param hitResult Triggered {@link BlockHitResult}
     */
    public void onRightClickBlock(ManasSkillInstance instance, LivingEntity entity, BlockHitResult hitResult) {
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill starts to be targeted by a mob.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param target   Affected {@link LivingEntity} owning this Skill.
     * @param event    Triggered {@link LivingChangeTargetEvent}
     */
    public void onBeingTargeted(ManasSkillInstance instance, LivingEntity target, LivingChangeTargetEvent event) {
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill starts to be attacked.
     * Canceling {@link LivingAttackEvent} will make the owner immune to the Damage Source.
     * Therefore, cancel the hurt sound, animation and knock back, but cannot change the damage amount like {@link LivingHurtEvent}
     *
     * Executing Order: This method gets invoked first before any Damage method.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param event    Triggered {@link LivingAttackEvent}
     */
    public void onBeingDamaged(ManasSkillInstance instance, LivingAttackEvent event) {
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill damage another {@link LivingEntity}.
     * Canceling {@link LivingHurtEvent} will not cancel the hurt sound, animation and knock back.
     * <p>
     * Executing Order: This method gets invoked after {@link ManasSkill#onBeingDamaged}
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this SKill.
     * @param event    Triggered {@link LivingHurtEvent}
     */
    public void onDamageEntity(ManasSkillInstance instance, LivingEntity entity, LivingHurtEvent event) {
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill damage another {@link LivingEntity}, triggered after {@link SkillDamageEvent.Calculation}
     * Canceling {@link LivingHurtEvent} will not cancel the hurt sound, animation and knock back.
     * <p>
     * Executing Order: This method gets invoked after {@link ManasSkill#onDamageEntity}
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     * @param event    Triggered {@link LivingHurtEvent}
     *
     * @see SkillDamageEvent.Calculation
     */
    public void onTouchEntity(ManasSkillInstance instance, LivingEntity entity, LivingHurtEvent event) {
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill takes damage.
     * Canceling {@link LivingDamageEvent} will not cancel the hurt sound, animation and knock back.
     * <p>
     * Executing Order: This method gets invoked after {@link ManasSkill#onTouchEntity}
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param event    Triggered {@link LivingDamageEvent}
     */
    public void onTakenDamage(ManasSkillInstance instance, LivingDamageEvent event) {
    }

    /**
     * Called when the {@link LivingEntity} is hit by a projectile.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param living   Affected {@link LivingEntity} owning this instance.
     * @param event    Triggered {@link ProjectileImpactEvent}
     */
    public void onProjectileHit(ManasSkillInstance instance, LivingEntity living, ProjectileImpactEvent event) {
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill dies
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param event    Triggered {@link LivingDeathEvent}
     */
    public void onDeath(ManasSkillInstance instance, LivingDeathEvent event) {
    }

    /**
     * {@link PlayerEvent.PlayerRespawnEvent} invoking this callback
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param event    Triggered {@link PlayerEvent.PlayerRespawnEvent}
     */
    public void onRespawn(ManasSkillInstance instance, PlayerEvent.PlayerRespawnEvent event) {
    }
}
