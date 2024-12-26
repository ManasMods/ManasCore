/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.register;

import net.fabricmc.api.ModInitializer;

public class ManasCoreRegisterFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ManasCoreRegister.init();
    }
}