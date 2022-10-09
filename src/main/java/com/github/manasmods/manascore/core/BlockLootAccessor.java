package com.github.manasmods.manascore.core;

import net.minecraft.data.loot.BlockLoot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockLoot.class)
public interface BlockLootAccessor {
    @Accessor("NORMAL_LEAVES_SAPLING_CHANCES")
    static float[] getNormalLeavesSaplingChanges() {
        throw new AssertionError("Accessor Mixin could not be applied to BlockLoot.NORMAL_LEAVES_SAPLING_CHANCES Field");
    }
}
