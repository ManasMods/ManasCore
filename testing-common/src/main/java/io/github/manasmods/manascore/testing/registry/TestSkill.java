/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.testing.registry;

import io.github.manasmods.manascore.config.ConfigRegistry;
import io.github.manasmods.manascore.network.api.util.Changeable;
import io.github.manasmods.manascore.skill.api.EntityEvents;
import io.github.manasmods.manascore.skill.api.ManasSkill;
import io.github.manasmods.manascore.skill.api.ManasSkillInstance;
import io.github.manasmods.manascore.testing.ManasCoreTesting;
import io.github.manasmods.manascore.testing.configs.SkillConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

public class TestSkill extends ManasSkill {
    public TestSkill() {
        super();
        ManasCoreTesting.LOG.info("Created skill!");
        this.addHeldAttributeModifier(Attributes.MOVEMENT_SPEED, "test_speed", -0.95, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    public int getModes() {
        return 2;
    }

    public int getMaxHeldTime(ManasSkillInstance instance, LivingEntity entity) {
        return 100;
    }

    public boolean canBeToggled(ManasSkillInstance instance, LivingEntity entity) {
        return entity.isShiftKeyDown();
    }

    public boolean canTick(ManasSkillInstance instance, LivingEntity entity) {
        return instance.isToggled();
    }

    public boolean canIgnoreCoolDown(ManasSkillInstance instance, LivingEntity entity, int mode) {
        return mode == 1 && entity.isShiftKeyDown();
    }

    public void onToggleOn(ManasSkillInstance instance, LivingEntity entity) {
        ManasCoreTesting.LOG.info("Toggled On");
    }

    public void onToggleOff(ManasSkillInstance instance, LivingEntity entity) {
        ManasCoreTesting.LOG.info("Toggled Off");
    }

    public void onPressed(ManasSkillInstance instance, LivingEntity entity, int keyNumber, int mode) {
        ManasCoreTesting.LOG.info("I'm pressed");
        if (mode == 1) ManasCoreTesting.LOG.info("In second mode");
    }

    public boolean onHeld(ManasSkillInstance instance, LivingEntity living, int heldTicks, int mode) {
        ManasCoreTesting.LOG.info("Held for {} ticks", heldTicks);
        if (mode == 1) ManasCoreTesting.LOG.info("In second mode");
        return true;
    }

    public void onRelease(ManasSkillInstance instance, LivingEntity entity, int heldTicks, int keyNumber, int mode) {
        ManasCoreTesting.LOG.info("I'm released after {} ticks", heldTicks);
        if (mode == 1) {
            ManasCoreTesting.LOG.info("In second mode");
            instance.setCoolDown(5, mode);
        }
    }

    public void addHeldAttributeModifiers(ManasSkillInstance instance, LivingEntity entity, int mode) {
        if (mode == 1) return;
        super.addHeldAttributeModifiers(instance, entity, mode);
    }

    public void onTick(ManasSkillInstance instance, LivingEntity living) {
        if (living.isShiftKeyDown()) ManasCoreTesting.LOG.info("You're sneaky");
    }

    public void onScroll(ManasSkillInstance instance, LivingEntity living, double delta, int mode) {
        ManasCoreTesting.LOG.info("Scroll delta: {}", delta);
    }

    public void onLearnSkill(ManasSkillInstance instance, LivingEntity entity) {
        ManasCoreTesting.LOG.info("Learnt test skill");
    }

    public void onForgetSkill(ManasSkillInstance instance, LivingEntity entity) {
        ManasCoreTesting.LOG.info("Forgot test skill");
    }

    public boolean onEffectAdded(ManasSkillInstance instance, LivingEntity entity, @Nullable Entity source, Changeable<MobEffectInstance> effect) {
        MobEffectInstance effectInstance = effect.get();
        if (effectInstance == null) return false;
        if (effectInstance.getEffect().equals(MobEffects.BLINDNESS)) return false;

        if (effectInstance.getEffect().equals(MobEffects.POISON)) {
            ManasCoreTesting.LOG.info("Poison is bad!");
            effect.set(new MobEffectInstance(MobEffects.GLOWING, effectInstance.getDuration(), effectInstance.getAmplifier()));
        }
        return true;
    }

    public boolean onBeingTargeted(ManasSkillInstance instance, Changeable<LivingEntity> target, LivingEntity mob) {
        if (mob instanceof Spider) ManasCoreTesting.LOG.info("Targeted by {}", mob.getName());
        return true;
    }

    public boolean onBeingDamaged(ManasSkillInstance instance, LivingEntity entity, DamageSource source, float amount) {
        if (source.equals(entity.level().damageSources().cactus())) {
            ManasCoreTesting.LOG.info("No cactus touchy");
            return false;
        }
        return true;
    }

    public boolean onDamageEntity(ManasSkillInstance instance, LivingEntity owner, LivingEntity target, DamageSource source, Changeable<Float> amount) {
        SkillConfig skillConfig = ConfigRegistry.getConfig(SkillConfig.class);
        if (target instanceof Creeper creeper && skillConfig.instaKillCreeper) {
            creeper.hurt(owner.level().damageSources().dragonBreath(), 100F);
            ManasCoreTesting.LOG.info("No creeper");
        } else if (target instanceof IronGolem) {
            amount.set(amount.get() * skillConfig.ironGolemDamageMultiplier);
        } else if (target instanceof Axolotl) {
            amount.set(0F);
        } else if (target instanceof Player) amount.set(amount.get() * 10F);
        ManasCoreTesting.LOG.info("Dealt {} damage.", amount.get());
        return true;
    }

    public boolean onTouchEntity(ManasSkillInstance instance, LivingEntity owner, LivingEntity target, DamageSource source, Changeable<Float> amount) {
        if (owner.isShiftKeyDown() && target instanceof Villager) {
            instance.setMastery(instance.getMastery() + 1);
            ManasCoreTesting.LOG.info("My mastery is {}", instance.getMastery());
            amount.set(amount.get() * 100F);
        } else if (target instanceof Pig) amount.set(0F);
        ManasCoreTesting.LOG.info("Dealt {} damage.", amount.get());
        return true;
    }

    public boolean onTakenDamage(ManasSkillInstance instance, LivingEntity owner, DamageSource source, Changeable<Float> amount) {
        if (source.equals(owner.level().damageSources().lava())) amount.set(0F);
        if (owner.isShiftKeyDown()) {
            owner.heal(amount.get());
            ManasCoreTesting.LOG.info("Healed {} by {} health", owner.getName().getString(), amount.get());
        }
        return true;
    }

    public void onProjectileHit(ManasSkillInstance instance, LivingEntity living, EntityHitResult hitResult, Projectile projectile, Changeable<ProjectileDeflection> deflectionChangeable, Changeable<EntityEvents.ProjectileHitResult> result) {
        if (projectile instanceof ThrownTrident) {
            ManasCoreTesting.LOG.info("Dodged");
            result.set(EntityEvents.ProjectileHitResult.PASS);
        } else if (projectile instanceof Arrow) {
            if (living.isShiftKeyDown()) {
                result.set(EntityEvents.ProjectileHitResult.PASS);
            } else {
                result.set(EntityEvents.ProjectileHitResult.DEFAULT);
                deflectionChangeable.set(ProjectileDeflection.AIM_DEFLECT);
            }
        }
    }

    public boolean onDeath(ManasSkillInstance instance, LivingEntity owner, DamageSource source) {
        ManasCoreTesting.LOG.info("Welcome to the phantom realm");
        return true;
    }

    public void onRespawn(ManasSkillInstance instance, ServerPlayer owner, boolean conqueredEnd) {
        ManasCoreTesting.LOG.info("Welcome to the living realm");
        if (instance.is(TestTags.TEST_SKILL_TAG)) ManasCoreTesting.LOG.info("Im in the Tag!");
    }
}
