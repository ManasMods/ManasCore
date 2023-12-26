package com.github.manasmods.manascore.core;

import com.github.manasmods.manascore.world.StoragePersistentState;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.DataFixTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DataFixTypes.class)
public class MixinDataFixTypes {
    @Inject(method = "update(Lcom/mojang/datafixers/DataFixer;Lcom/mojang/serialization/Dynamic;II)Lcom/mojang/serialization/Dynamic;", at = @At("RETURN"), cancellable = true)
    private <T> void cancelIfStorage(DataFixer fixer, Dynamic<T> input, int version, int newVersion, CallbackInfoReturnable<Dynamic<T>> cir) {
        if (StoragePersistentState.LOADING.get()) {
            cir.setReturnValue(input);
        }
    }
}
