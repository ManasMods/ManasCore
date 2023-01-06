package com.github.manasmods.manascore.core;

import net.minecraft.data.loot.packs.VanillaBlockLoot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VanillaBlockLoot.class)
public interface VanillaBlockLootAccessor {
    @Accessor("JUNGLE_LEAVES_SAPLING_CHANGES")
    static float[] getJungleLeavesSaplingChances() {
        throw new AssertionError("Could not access NORMAL_LEAVES_SAPLING_CHANCES in Block Loot class");
    }
}
