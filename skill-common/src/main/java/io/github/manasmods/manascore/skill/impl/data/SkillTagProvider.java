/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.impl.data;

import io.github.manasmods.manascore.skill.api.ManasSkill;
import io.github.manasmods.manascore.skill.impl.SkillRegistry;
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
