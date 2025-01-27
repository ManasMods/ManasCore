/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.testing.registry;

import com.mojang.datafixers.util.Pair;
import io.github.manasmods.manascore.network.api.util.Changeable;
import io.github.manasmods.manascore.race.api.ManasRace;
import io.github.manasmods.manascore.race.api.ManasRaceInstance;
import io.github.manasmods.manascore.race.api.SpawnPointHelper;
import io.github.manasmods.manascore.testing.ManasCoreTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class TestRaceEvolved extends ManasRace {
    public TestRaceEvolved() {
        super(Difficulty.EASY);
        this.addAttributeModifier(Attributes.JUMP_STRENGTH, ResourceLocation.withDefaultNamespace("race.speed"),
                2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.MAX_HEALTH, ResourceLocation.withDefaultNamespace("race.health"),
                100, AttributeModifier.Operation.ADD_VALUE);
    }

    public void onActivateAbility(ManasRaceInstance instance, LivingEntity entity) {
        entity.level().explode(entity, entity.getX(), entity.getY(), entity.getZ(), 10F, Level.ExplosionInteraction.BLOCK);
    }

    public void onRaceSet(ManasRaceInstance instance, LivingEntity living) {
        ManasCoreTesting.LOG.info("You are a Test Race Evolved!");
        living.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1, 1);
    }

    public boolean onBeingTargeted(ManasRaceInstance instance, Changeable<LivingEntity> target, LivingEntity mob) {
        if (mob.getType().is(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)) target.set(null);
        return true;
    }

    public boolean onAttackEntity(ManasRaceInstance instance, LivingEntity owner, LivingEntity target, DamageSource source, Changeable<Float> amount) {
        if (owner.isShiftKeyDown()) {
            BlockPos pos = ServerLevel.END_SPAWN_POINT;
            SpawnPointHelper.teleportToAcrossDimensions(target,
                    this.getRespawnDimension(instance, owner).getFirst(), pos.getX(), pos.getY(), pos.getZ(), 0, 0);
        }
        return true;
    }

    public boolean onDeath(ManasRaceInstance instance, LivingEntity owner, DamageSource source) {
        ManasCoreTesting.LOG.info("AWWWWW MANNNNN x2");
        return true;
    }

    public void onRespawn(ManasRaceInstance instance, ServerPlayer owner, boolean conqueredEnd) {
        ManasCoreTesting.LOG.info("CREEPER x2");
    }

    public List<ManasRace> getPreviousEvolutions(ManasRaceInstance instance, LivingEntity entity) {
        List<ManasRace> list = new ArrayList<>();
        list.add(RegistryTest.TEST_RACE.get());
        return list;
    }

    public Pair<ResourceKey<Level>, BlockState> getRespawnDimension(ManasRaceInstance instance, LivingEntity owner) {
        return Pair.of(Level.END, Blocks.END_STONE.defaultBlockState());
    }
}
