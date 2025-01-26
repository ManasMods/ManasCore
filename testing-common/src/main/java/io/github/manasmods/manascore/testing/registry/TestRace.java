/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.testing.registry;

import io.github.manasmods.manascore.race.api.ManasRace;
import io.github.manasmods.manascore.race.api.ManasRaceInstance;
import io.github.manasmods.manascore.skill.api.ManasSkill;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import io.github.manasmods.manascore.skill.api.Skills;
import io.github.manasmods.manascore.skill.utils.Changeable;
import io.github.manasmods.manascore.testing.ManasCoreTesting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TestRace extends ManasRace {
    public TestRace() {
        super(Difficulty.INTERMEDIATE);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, ResourceLocation.withDefaultNamespace("race.attack"),
                50, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(Attributes.ARMOR, ResourceLocation.withDefaultNamespace("race.armor"),
                10, AttributeModifier.Operation.ADD_VALUE);
    }

    public boolean canTick(ManasRaceInstance instance, LivingEntity entity) {
        return entity.isShiftKeyDown();
    }

    public void onActivateAbility(ManasRaceInstance instance, LivingEntity entity) {
        entity.level().explode(entity, entity.getX(), entity.getY(), entity.getZ(), 4F, Level.ExplosionInteraction.BLOCK);
    }

    public void onTick(ManasRaceInstance instance, LivingEntity living) {
        if (living.getItemBySlot(EquipmentSlot.MAINHAND).getItem().equals(Items.SALMON))
            ManasCoreTesting.LOG.info("Fishy fishy!");
    }

    public void onRaceSet(ManasRaceInstance instance, LivingEntity living) {
        ManasCoreTesting.LOG.info("You are a Test Race!");
        living.playSound(SoundEvents.PLAYER_LEVELUP, 1, 1);
    }

    public boolean onEffectAdded(ManasRaceInstance instance, LivingEntity entity, @Nullable Entity source, Changeable<MobEffectInstance> effect) {
        MobEffectInstance effectInstance = effect.get();
        if (effectInstance == null) return false;
        if (effectInstance.getEffect().equals(MobEffects.WEAKNESS)) return false;

        if (effectInstance.getEffect().equals(MobEffects.CONFUSION)) {
            ManasCoreTesting.LOG.info("Poison is bad!");
            effect.set(new MobEffectInstance(MobEffects.DAMAGE_BOOST, effectInstance.getDuration(), effectInstance.getAmplifier()));
        }
        return true;
    }

    public boolean onBeingTargeted(ManasRaceInstance instance, Changeable<LivingEntity> target, LivingEntity mob) {
        if (mob.getType().is(EntityTypeTags.SENSITIVE_TO_SMITE)) target.set(null);
        return true;
    }

    public boolean onAttackEntity(ManasRaceInstance instance, LivingEntity owner, LivingEntity target, DamageSource source, Changeable<Float> amount) {
        if (owner.isShiftKeyDown() && target instanceof Pillager) {
            amount.set(amount.get() * 100F);
            ManasCoreTesting.LOG.info("Dealt {} damage.", amount.get());
        }
        return true;
    }

    public boolean onHurt(ManasRaceInstance instance, LivingEntity owner, DamageSource source, Changeable<Float> amount) {
        return !source.equals(owner.level().damageSources().fall());
    }

    public boolean onDeath(ManasRaceInstance instance, LivingEntity owner, DamageSource source) {
        ManasCoreTesting.LOG.info("AWWWWW MANNNNN");
        return true;
    }

    public void onRespawn(ManasRaceInstance instance, ServerPlayer owner, boolean conqueredEnd) {
        ManasCoreTesting.LOG.info("CREEPER");
    }

    public List<ManasSkill> getIntrinsicSkills(ManasRaceInstance instance, LivingEntity entity) {
        List<ManasSkill> list = new ArrayList<>();
        list.add(RegistryTest.TEST_SKILL.get());
        return list;
    }

    public void learnIntrinsicSkills(ManasRaceInstance instance, LivingEntity entity) {
        Skills storage = SkillAPI.getSkillsFrom(entity);
        for (ManasSkill skill : instance.getIntrinsicSkills(entity)) {
            if (storage.learnSkill(skill)) ManasCoreTesting.LOG.info("LEARNT SKILL FROM RACE?!?!");
        }
    }

    public float getEvolutionProgress(ManasRaceInstance instance, LivingEntity entity, ManasRace evolution) {
        return entity.getMainHandItem().getCount() / 16F;
    }

    public void onRaceEvolution(ManasRaceInstance instance, LivingEntity living, ManasRaceInstance evolution) {
        ManasCoreTesting.LOG.info("YOU EVOLVED TO" + evolution.getRace().getName() + "!?!");
    }

    public List<ManasRace> getNextEvolutions(ManasRaceInstance instance, LivingEntity entity) {
        List<ManasRace> list = new ArrayList<>();
        list.add(RegistryTest.TEST_RACE_EVOLVED.get());
        return list;
    }

    public @Nullable ManasRace getDefaultEvolution(ManasRaceInstance instance, LivingEntity entity) {
        return RegistryTest.TEST_RACE_EVOLVED.get();
    }
}
