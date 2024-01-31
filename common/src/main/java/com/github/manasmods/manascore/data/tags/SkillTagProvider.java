package com.github.manasmods.manascore.data.tags;

import com.github.manasmods.manascore.api.skill.ManasSkill;
import com.github.manasmods.manascore.skill.SkillRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;

import java.util.concurrent.CompletableFuture;

public abstract class SkillTagProvider extends IntrinsicHolderTagsProvider<ManasSkill> {
    public SkillTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, SkillRegistry.KEY, lookupProvider, manasSkill -> SkillRegistry.SKILLS.getKey(manasSkill).orElseThrow());
    }

    public SkillTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<ManasSkill>> parentProvider) {
        super(output, SkillRegistry.KEY, lookupProvider, parentProvider, manasSkill -> SkillRegistry.SKILLS.getKey(manasSkill).orElseThrow());
    }
}
