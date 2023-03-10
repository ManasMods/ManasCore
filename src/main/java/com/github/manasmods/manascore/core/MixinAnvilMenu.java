package com.github.manasmods.manascore.core;

import com.github.manasmods.manascore.config.ManasCoreConfig;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilMenu.class)
public class MixinAnvilMenu {
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40, ordinal = 2))
    private int maxEnchantmentLevel(int original) {
        int limit = ManasCoreConfig.INSTANCE.getAnvilExpLimit().get();
        if (limit == -1) return 1_000_000;
        return limit;
    }
}
