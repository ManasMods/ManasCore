package com.github.manasmods.manascore.core;

import net.minecraft.data.loot.BlockLoot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockLoot.class)
public interface BlockLootAccessor {

    @Accessor("NORMAL_LEAVES_SAPLING_CHANCES")
    static float[] getNormalLeavesSaplingChances() {
        throw new AssertionError("Could not access NORMAL_LEAVES_SAPLING_CHANCES in Block Loot class");
    }

    @Accessor("JUNGLE_LEAVES_SAPLING_CHANGES")
    static float[] getJungleLeavesSaplingChances() {
        throw new AssertionError("Could not access NORMAL_LEAVES_SAPLING_CHANCES in Block Loot class");
    }

    @Accessor("NORMAL_LEAVES_STICK_CHANCES")
    static float[] getNormalStickChances() {
        throw new AssertionError("Could not access NORMAL_LEAVES_SAPLING_CHANCES in Block Loot class");
    }
}
