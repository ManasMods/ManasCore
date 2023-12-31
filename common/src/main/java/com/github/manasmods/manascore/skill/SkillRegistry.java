package com.github.manasmods.manascore.skill;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.skill.ManasSkill;
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

    }
}
