package com.github.manasmods.manascore.skill;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.skill.ManasSkill;
import com.github.manasmods.manascore.api.skill.ManasSkillInstance;
import com.github.manasmods.manascore.api.skill.SkillAPI;
import com.github.manasmods.manascore.api.skill.SkillEvents;
import com.github.manasmods.manascore.api.world.entity.EntityEvents;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class SkillRegistry {
    public static final Registrar<ManasSkill> SKILLS = RegistrarManager.get(ManasCore.MOD_ID).<ManasSkill>builder(new ResourceLocation(ManasCore.MOD_ID, "skills"))
            .build();
    public static final ResourceKey<Registry<ManasSkill>> KEY = (ResourceKey<Registry<ManasSkill>>) SKILLS.key();


    public static void init() {
        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, pos, face) -> {
            if (player.level().isClientSide()) return EventResult.pass();
            SkillAPI.getSkillsFrom(player).forEachSkill(((storage, skillInstance) -> {
                if (!skillInstance.canInteractSkill(player)) return;
                skillInstance.onRightClickBlock(player, hand, pos, face);
            }));

            return EventResult.pass();
        });

        EntityEvents.LIVING_CHANGE_TARGET.register((entity, changeableTarget) -> {
            if (!changeableTarget.isPresent()) return EventResult.pass();

            for (ManasSkillInstance instance : SkillAPI.getSkillsFrom(changeableTarget.get()).getLearnedSkills()) {
                if (!instance.canInteractSkill(entity)) continue;
                if (!instance.onBeingTargeted(changeableTarget)) return EventResult.interruptFalse();
            }

            return EventResult.pass();
        });

        EntityEvents.LIVING_ATTACK.register((entity, source, amount) -> {
            for (ManasSkillInstance instance : SkillAPI.getSkillsFrom(entity).getLearnedSkills()) {
                if (!instance.canInteractSkill(entity)) continue;
                if (!instance.onBeingDamaged(entity, source, amount)) return EventResult.interruptFalse();
            }

            return EventResult.pass();
        });

        SkillEvents.SKILL_DAMAGE_PRE_CALCULATION.register((storage, entity, source, amount) -> {
            for (ManasSkillInstance instance : storage.getLearnedSkills()) {
                if (!instance.canInteractSkill(entity)) continue;
                if (!instance.onDamageEntity(entity, source, amount)) return EventResult.interruptFalse();
            }

            return EventResult.pass();
        });
    }
}
