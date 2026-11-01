/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.command.internal;

import org.jetbrains.annotations.Nullable;

public interface IPermissionSource {
    boolean isPlayer();

    boolean isOwnCommand();

    boolean hasLevel(int level);

    boolean hasOwnLevel(int level);

    @Nullable Boolean getValue(String node);
}
