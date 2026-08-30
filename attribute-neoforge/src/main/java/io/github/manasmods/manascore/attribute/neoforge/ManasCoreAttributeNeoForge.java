package io.github.manasmods.manascore.attribute.neoforge;

import io.github.manasmods.manascore.attribute.ManasCoreAttribute;
import io.github.manasmods.manascore.attribute.ModuleConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(ModuleConstants.MOD_ID)
public final class ManasCoreAttributeNeoForge {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, ModuleConstants.MOD_ID);
    public ManasCoreAttributeNeoForge(IEventBus bus) {
        ManasCoreAttribute.init();
        ATTRIBUTES.register(bus);
    }
}
