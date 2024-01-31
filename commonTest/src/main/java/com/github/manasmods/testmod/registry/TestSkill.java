package com.github.manasmods.testmod.registry;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.skill.ManasSkill;
import com.github.manasmods.manascore.api.skill.ManasSkillInstance;
import com.github.manasmods.manascore.api.skill.SkillRarity;
import com.github.manasmods.manascore.api.world.entity.EntityEvents;
import com.github.manasmods.manascore.utils.Changeable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.EntityHitResult;

public class TestSkill extends ManasSkill {
    public TestSkill() {
        super(SkillRarity.Unique);
        ManasCore.Logger.info("Created skill!");
    }

    public boolean canBeToggled(ManasSkillInstance instance, LivingEntity entity) {
        return entity.isShiftKeyDown();
    }

    public boolean canTick(ManasSkillInstance instance, LivingEntity entity) {
        return instance.isToggled();
    }

    public void onToggleOn(ManasSkillInstance instance, LivingEntity entity) {
        ManasCore.Logger.info("Toggled On");
    }

    public void onToggleOff(ManasSkillInstance instance, LivingEntity entity) {
        ManasCore.Logger.info("Toggled Off");
    }

    public void onPressed(ManasSkillInstance instance, LivingEntity entity, int keyNumber) {
        ManasCore.Logger.info("I'm pressed");
    }

    public boolean onHeld(ManasSkillInstance instance, LivingEntity living, int heldTicks) {
        ManasCore.Logger.info("Held for {} ticks", heldTicks);
        return true;
    }

    public void onRelease(ManasSkillInstance instance, LivingEntity entity, int heldTicks, int keyNumber) {
        ManasCore.Logger.info("I'm released after {} ticks", heldTicks);
    }

    public void onTick(ManasSkillInstance instance, LivingEntity living) {
        if (living.isShiftKeyDown()) ManasCore.Logger.info("You're sneaky");
    }

    public void onScroll(ManasSkillInstance instance, LivingEntity living, double delta) {
        ManasCore.Logger.info("Scroll delta: {}", delta);
    }

    public void onLearnSkill(ManasSkillInstance instance, LivingEntity living) {
        ManasCore.Logger.info("Learnt test skill");
    }

    public void onRightClickBlock(ManasSkillInstance instance, Player player, InteractionHand hand, BlockPos pos, Direction face) {
        ManasCore.Logger.info("Block: {}", player.level().getBlockState(pos).getBlock().getName());
    }

    public boolean onBeingTargeted(ManasSkillInstance instance, Changeable<LivingEntity> target, LivingEntity mob) {
        if (mob instanceof Spider) ManasCore.Logger.info("Targeted by {}", mob.getName());
        return true;
    }

    public boolean onBeingDamaged(ManasSkillInstance instance, LivingEntity entity, DamageSource source, float amount) {
        if (source.equals(entity.level().damageSources().cactus())) {
            ManasCore.Logger.info("No cactus touchy");
            return false;
        }
        return true;
    }

    public boolean onDamageEntity(ManasSkillInstance instance, LivingEntity owner, LivingEntity target, DamageSource source, Changeable<Float> amount) {
        if (target instanceof Creeper creeper) {
            creeper.kill();
            ManasCore.Logger.info("No creeper");
        } else if (target instanceof IronGolem) {
            amount.set(amount.get() * 100F);
            ManasCore.Logger.info("Dealt {} damage.", amount.get());
        }
        return true;
    }

    public boolean onTouchEntity(ManasSkillInstance instance, LivingEntity owner, LivingEntity target, DamageSource source, Changeable<Float> amount) {
        if (owner.isShiftKeyDown() && target instanceof Villager) {
            instance.setMastery(instance.getMastery() + 1);
            ManasCore.Logger.info("My mastery is {}", instance.getMastery());
        }
        return true;
    }

    public boolean onTakenDamage(ManasSkillInstance instance, LivingEntity owner, DamageSource source, Changeable<Float> amount) {
        if (instance.is(RegisterTest.TEST_SKILL_TAG)) {
            owner.heal(amount.get());
            ManasCore.Logger.info("Healed {} by {} health", owner.getName(), amount.get());
        }
        return true;
    }

    public void onProjectileHit(ManasSkillInstance instance, LivingEntity living, EntityHitResult hitResult, Projectile projectile, Changeable<EntityEvents.ProjectileHitResult> result) {
        if (projectile instanceof ThrownTrident) {
            ManasCore.Logger.info("Dodged");
            result.set(EntityEvents.ProjectileHitResult.PASS);
        }
    }

    public boolean onDeath(ManasSkillInstance instance, LivingEntity owner, DamageSource source) {
        ManasCore.Logger.info("Welcome to the phantom realm");
        return true;
    }

    public void onRespawn(ManasSkillInstance instance, ServerPlayer owner, boolean conqueredEnd) {
        ManasCore.Logger.info("Welcome to the living realm");
    }
}
