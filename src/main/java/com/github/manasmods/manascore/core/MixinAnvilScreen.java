package com.github.manasmods.manascore.core;

import com.github.manasmods.manascore.config.ManasCoreConfig;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilScreen.class)
public class MixinAnvilScreen {
    @ModifyConstant(method = "renderLabels", constant = @Constant(intValue = 40))
    private int maxEnchantmentLevel(int original) {
        int limit = ManasCoreConfig.INSTANCE.getAnvilExpLimit().get();
        if (limit == -1) return 1_000_000;
        return limit;
    }
}
