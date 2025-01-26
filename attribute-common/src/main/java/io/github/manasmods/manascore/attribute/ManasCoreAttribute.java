package io.github.manasmods.manascore.attribute;

import io.github.manasmods.manascore.attribute.impl.network.ManasCoreAttributeNetwork;

public class ManasCoreAttribute {
    public static void init() {
        ManasCoreAttributeRegister.init();
        ManasCoreAttributeNetwork.init();
    }
}
