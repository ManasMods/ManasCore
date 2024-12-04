/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.command.neoforge;

import io.github.manasmods.manascore.command.ManasCoreCommand;
import io.github.manasmods.manascore.command.ModuleConstants;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;

@Mod(ModuleConstants.MOD_ID)
public final class ManasCoreCommandNeoForge {
    public ManasCoreCommandNeoForge() {
        ManasCoreCommand.init();

        NeoForge.EVENT_BUS.addListener(PermissionGatherEvent.Nodes.class, event -> PlatformCommandUtilsImpl.PERMISSIONS.values().forEach(event::addNodes));
    }
}
