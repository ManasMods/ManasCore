package com.github.manasmods.manascore.core;

import net.minecraft.data.loot.BlockLootSubProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockLootSubProvider.class)
public interface BlockLootSubProviderAccessor {

    @Accessor("NORMAL_LEAVES_SAPLING_CHANCES")
    static float[] getNormalLeavesSaplingChances() {
        throw new AssertionError("Could not access NORMAL_LEAVES_SAPLING_CHANCES in Block Loot class");
    }

    @Accessor("NORMAL_LEAVES_STICK_CHANCES")
    static float[] getNormalStickChances() {
        throw new AssertionError("Could not access NORMAL_LEAVES_SAPLING_CHANCES in Block Loot class");
    }
}
