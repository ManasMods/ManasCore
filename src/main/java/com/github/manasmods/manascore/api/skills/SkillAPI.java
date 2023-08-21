package com.github.manasmods.manascore.api.skills;

import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import com.github.manasmods.manascore.capability.skill.EntitySkillCapability;
import com.github.manasmods.manascore.skill.SkillRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
@ApiStatus.AvailableSince("1.0.2.0")
public final class SkillAPI {
    /**
     * This Method returns the {@link ManasSkill} Registry.
     * It can be used to create {@link DeferredRegister} instances or to load {@link ManasSkill}s from the Registry.
     */
    @NotNull
    public static IForgeRegistry<ManasSkill> getSkillRegistry() {
        return SkillRegistry.REGISTRY.get();
    }

    /**
     * Can be used to load the {@link SkillStorage} from an {@link Entity}.
     *
     * @see SkillStorage
     */
    @NotNull
    public static SkillStorage getSkillsFrom(Entity entity) {
        return EntitySkillCapability.load(entity);
    }
}