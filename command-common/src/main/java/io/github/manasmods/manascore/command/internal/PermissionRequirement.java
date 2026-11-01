/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.command.internal;

import io.github.manasmods.manascore.command.PlatformCommandUtils;
import io.github.manasmods.manascore.command.api.Permission;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;
import java.util.function.Predicate;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class PermissionRequirement implements Predicate<CommandSourceStack> {
    private final List<Permission> permissions;

    public boolean test(CommandSourceStack source) {
        if (source.getPlayer() == null) return true;
        for (Permission permission : this.permissions) if (!PlatformCommandUtils.hasPermission(source, permission)) return false;
        return true;
    }
}
