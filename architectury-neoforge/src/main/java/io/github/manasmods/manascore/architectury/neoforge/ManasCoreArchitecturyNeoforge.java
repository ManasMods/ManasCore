package io.github.manasmods.manascore.architectury.neoforge;

import io.github.manasmods.manascore.architectury.ManasCoreArchitectury;
import io.github.manasmods.manascore.architectury.ModuleConstants;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ModuleConstants.MOD_ID)
public final class ManasCoreArchitecturyNeoforge {
    public ManasCoreArchitecturyNeoforge(IEventBus bus) {
        ManasCoreArchitectury.init();
    }
}
