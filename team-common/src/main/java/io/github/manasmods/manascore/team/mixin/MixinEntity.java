/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.mixin;

import io.github.manasmods.manascore.team.api.TeamAPI;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes {@link Entity#isAlliedTo(Entity)} team-aware, one-way.
 * {@code x.isAlliedTo(y)} is true when x considers y an ally: vanilla scoreboard teams first, then {@link TeamAPI#isAllied(LivingEntity, LivingEntity)} in that direction.
 * Call it actor-first ({@code attacker.isAlliedTo(victim)}); the reverse call protects the victim only because the victim allied the attacker.
 */
@Mixin(Entity.class)
public abstract class MixinEntity {
    @Unique
    private static final ThreadLocal<Boolean> manascore$resolving = ThreadLocal.withInitial(() -> false);

    @Inject(method = "isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z", at = @At("RETURN"), cancellable = true)
    void isAlliedTo(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) return;
        if (!((Entity) (Object) this instanceof LivingEntity self) || !(entity instanceof LivingEntity target)) return;
        if (manascore$resolving.get()) return;
        manascore$resolving.set(true);
        try {
            if (TeamAPI.isAllied(self, target)) cir.setReturnValue(true);
        } finally {
            manascore$resolving.set(false);
        }
    }
}
