package com.github.manasmods.testmod.registry;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.skill.ManasSkill;
import com.github.manasmods.manascore.api.skill.ManasSkillInstance;
import com.github.manasmods.manascore.api.world.entity.EntityEvents;
import com.github.manasmods.manascore.utils.Changeable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.EntityHitResult;

public class TestSkill extends ManasSkill {
    public TestSkill() {
        super();
        ManasCore.Logger.info("Created skill!");
    }

    public boolean canTick(ManasSkillInstance instance, LivingEntity entity) {
        return instance.isToggled();
    }
    public void onToggleOn(ManasSkillInstance instance, LivingEntity entity) {
        ManasCore.Logger.debug("Toggled On");
    }

    public void onToggleOff(ManasSkillInstance instance, LivingEntity entity) {
        ManasCore.Logger.debug("Toggled Off");
    }

    public void onPressed(ManasSkillInstance instance, LivingEntity entity) {
        ManasCore.Logger.debug("I'm pressed");
    }

    public boolean onHeld(ManasSkillInstance instance, LivingEntity living, int heldTicks) {
        ManasCore.Logger.debug("Held for {} ticks", heldTicks);
        return true;
    }

    public void onRelease(ManasSkillInstance instance, LivingEntity entity, int heldTicks) {
        ManasCore.Logger.debug("I'm released after {} ticks", heldTicks);
    }

    public void onTick(ManasSkillInstance instance, LivingEntity living) {
        if (living instanceof Player player && player.isSecondaryUseActive())
            ManasCore.Logger.debug("You're sneaky");
    }

    public void onScroll(ManasSkillInstance instance, LivingEntity living, double delta) {
        ManasCore.Logger.debug("Scroll delta: {}", delta);
    }

    public void onLearnSkill(ManasSkillInstance instance, LivingEntity living) {
        ManasCore.Logger.debug("Learnt test skill");
    }

    public void onRightClickBlock(ManasSkillInstance instance, Player player, InteractionHand hand, BlockPos pos, Direction face) {
        ManasCore.Logger.debug("Block: {}", player.level().getBlockState(pos).getBlock().getName());
    }

    public boolean onBeingTargeted(ManasSkillInstance instance, LivingEntity owner, Changeable<LivingEntity> target) {
        if (target.get() instanceof Spider) ManasCore.Logger.debug("Targeted by {}", target.get().getName());
        return true;
    }

    public boolean onBeingDamaged(ManasSkillInstance instance, LivingEntity entity, DamageSource source, float amount) {
        if (source.equals(entity.level().damageSources().cactus())) {
            ManasCore.Logger.debug("No cactus touchy");
            return false;
        }
        return true;
    }

    public boolean onDamageEntity(ManasSkillInstance instance, LivingEntity owner, DamageSource source, Changeable<Float> amount) {
        if (owner instanceof Creeper creeper) {
            creeper.kill();
            ManasCore.Logger.debug("No creeper");
        }
        return true;
    }

    public boolean onTouchEntity(ManasSkillInstance instance, LivingEntity owner, DamageSource source, Changeable<Float> amount) {
        instance.setMastery(instance.getMastery() + 1);
        ManasCore.Logger.debug("My mastery is {}", instance.getMastery());
        return true;
    }

    public boolean onTakenDamage(ManasSkillInstance instance, LivingEntity owner, DamageSource source, Changeable<Float> amount) {
        amount.set(amount.get() / 2F);
        owner.heal(amount.get());
        ManasCore.Logger.debug("Healed {} by {} health", owner.getName().getString(), amount);
        return true;
    }

    public void onProjectileHit(ManasSkillInstance instance, LivingEntity living, EntityHitResult hitResult, Projectile projectile, Changeable<EntityEvents.ProjectileHitResult> result) {
        if (projectile instanceof ThrownTrident) {
            ManasCore.Logger.debug("Dodged");
            result.set(EntityEvents.ProjectileHitResult.PASS);
        }
    }

    public boolean onDeath(ManasSkillInstance instance, LivingEntity owner, DamageSource source) {
        ManasCore.Logger.debug("Welcome to the phantom realm");
        return true;
    }

    public void onRespawn(ManasSkillInstance instance, ServerPlayer owner, boolean conqueredEnd) {
        ManasCore.Logger.debug("Welcome to the living realm");
    }
}
