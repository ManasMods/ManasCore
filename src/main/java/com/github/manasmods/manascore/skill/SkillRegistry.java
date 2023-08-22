package com.github.manasmods.manascore.skill;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.capability.skill.EntitySkillCapability;
import com.github.manasmods.manascore.capability.skill.HealSkill;
import com.github.manasmods.manascore.core.RegistryBuilderAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

@ApiStatus.Internal
public class SkillRegistry {
    public static final ResourceLocation REGISTRY_KEY = new ResourceLocation(ManasCore.MOD_ID, "skills");
    public static final DeferredRegister<ManasSkill> SKILLS = DeferredRegister.create(REGISTRY_KEY, ManasCore.MOD_ID);
    public static final Supplier<IForgeRegistry<ManasSkill>> REGISTRY = SKILLS.makeRegistry(() -> {
        RegistryBuilder<ManasSkill> builder = new RegistryBuilder<>();
        ((RegistryBuilderAccessor) builder).setHasWrapper(true);
        return builder;
    });

    public static void register(IEventBus modEventBus) {
        //Register Capability
        EntitySkillCapability.init(modEventBus);
        //Register Example in Dev env
        if (!FMLEnvironment.production) {
            SKILLS.register("example_self_heal", HealSkill::new);
        }
        //Register Registry to Forge
        SKILLS.register(modEventBus);
    }
}