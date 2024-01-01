package com.github.manasmods.manascore.api.skill;

import com.github.manasmods.manascore.api.world.entity.EntityEvents.ProjectileHitResult;
import com.github.manasmods.manascore.utils.Changeable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
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
        return SkillAPI.getSkillRegistry().getId(this);
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
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param living   Affected {@link LivingEntity} owning this Skill.
     * @return false will stop {@link LivingEntity} from using any feature of the skill.
     */
    public boolean canInteractSkill(ManasSkillInstance instance, LivingEntity living) {
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
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     * @return false if this skill is not toggleable.
     */
    public boolean canBeToggled(ManasSkillInstance instance, LivingEntity entity) {
        return false;
    }

    /**
     * Determine if this skill can still be activated when on cooldown.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     * @return false if this skill cannot ignore cooldown.
     */
    public boolean canIgnoreCoolDown(ManasSkillInstance instance, LivingEntity entity) {
        return false;
    }

    /**
     * Determine if this skill's {@link ManasSkill#onTick} can be executed.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     * @return false if this skill cannot tick.
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
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     * @return true to will mark this Skill is mastered, which can be used for increase stats or additional features/modes.
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
        if (isMastered(instance, entity)) instance.onSkillMastered(entity);
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
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param living   Affected {@link LivingEntity} owning this Skill.
     * @return true to continue ticking this Skill.
     */
    public boolean onHeld(ManasSkillInstance instance, LivingEntity living, int heldTicks) {
        return false;
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill releases the skill activation button after {@param heldTicks}.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
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
     */
    public void onLearnSkill(ManasSkillInstance instance, LivingEntity living) {
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
     * Called when the {@link Player} owning this Skill right-clicks a block.
     *
     * @see ManasSkillInstance#onRightClickBlock(Player, InteractionHand, BlockPos, Direction)
     */
    public void onRightClickBlock(ManasSkillInstance instance, Player player, InteractionHand hand, BlockPos pos, Direction face) {
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill starts to be targeted by a mob.
     *
     * @see ManasSkillInstance#onBeingTargeted(LivingEntity, Changeable)
     */
    public boolean onBeingTargeted(ManasSkillInstance instance, LivingEntity owner, Changeable<LivingEntity> target) {
        return true;
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill starts to be attacked.
     *
     * @see ManasSkillInstance#onBeingDamaged(LivingEntity, DamageSource, float)
     */
    public boolean onBeingDamaged(ManasSkillInstance instance, LivingEntity entity, DamageSource source, float amount) {
        return true;
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill damage another {@link LivingEntity}.
     *
     * @see ManasSkillInstance#onDamageEntity(LivingEntity, DamageSource, Changeable)
     */
    public boolean onDamageEntity(ManasSkillInstance instance, LivingEntity owner, DamageSource source, Changeable<Float> amount) {
        return true;
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill damage another {@link LivingEntity},
     *
     * @see ManasSkillInstance#onTouchEntity(LivingEntity, DamageSource, Changeable)
     */
    public boolean onTouchEntity(ManasSkillInstance instance, LivingEntity owner, DamageSource source, Changeable<Float> amount) {
        return true;
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill takes damage.
     *
     * @see ManasSkillInstance#onTakenDamage(LivingEntity, DamageSource, Changeable)
     */
    public boolean onTakenDamage(ManasSkillInstance instance, LivingEntity owner, DamageSource source, Changeable<Float> amount) {
        return true;
    }

    /**
     * Called when the {@link LivingEntity} is hit by a projectile.
     */
    public void onProjectileHit(ManasSkillInstance instance, LivingEntity living, EntityHitResult hitResult, Projectile projectile, Changeable<ProjectileHitResult> result) {

    }

    /**
     * Called when the {@link LivingEntity} owning this Skill dies.
     *
     * @see ManasSkillInstance#onDeath(LivingEntity, DamageSource)
     */
    public boolean onDeath(ManasSkillInstance instance, LivingEntity owner, DamageSource source) {
        return true;
    }

    /**
     * Called when the {@link ServerPlayer} owning this Skill respawns.
     */
    public void onRespawn(ManasSkillInstance instance, ServerPlayer owner, boolean conqueredEnd) {
    }
}
