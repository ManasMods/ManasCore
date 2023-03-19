package com.github.manasmods.manascore.api.client.animation.renderer;

import com.github.manasmods.manascore.ManasCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class RendererRegistry {

    private static final ResourceLocation REGISTRY_KEY = new ResourceLocation(ManasCore.MOD_ID, "animation_renderer");
    public static final DeferredRegister<IAnimationRenderer> RENDERERS = DeferredRegister.create(REGISTRY_KEY, ManasCore.MOD_ID);
    public static final Supplier<IForgeRegistry<IAnimationRenderer>> REGISTRY = RENDERERS.makeRegistry(RegistryBuilder::new);

    public static void register(IEventBus modBus) {

    }

}
