package io.github.manasmods.manascore.attribute.neoforge;

import io.github.manasmods.manascore.attribute.ManasCoreAttribute;
import io.github.manasmods.manascore.attribute.ModuleConstants;
import net.neoforged.fml.common.Mod;

@Mod(ModuleConstants.MOD_ID)
public final class ManasCoreAttributeNeoForge {
    public ManasCoreAttributeNeoForge() {
        ManasCoreAttribute.init();
    }
}
