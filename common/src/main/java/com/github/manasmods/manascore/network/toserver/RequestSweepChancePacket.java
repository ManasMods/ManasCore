package com.github.manasmods.manascore.network.toserver;

import com.github.manasmods.manascore.attribute.ManasCoreAttributeUtils;
import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class RequestSweepChancePacket {
    public RequestSweepChancePacket(FriendlyByteBuf buf) {
        // No data to read
    }

    public void toBytes(FriendlyByteBuf buf) {
        // No data to write
    }

    public void handle(Supplier<NetworkManager.PacketContext> ctx) {
        NetworkManager.PacketContext context = ctx.get();
        if (context.getEnvironment() == Env.CLIENT) return;
        context.queue(() -> sweepAttack(context.getPlayer()));
    }

    private void sweepAttack(Player player) {
        float attack = ManasCoreAttributeUtils.getAttackDamage(player);
        double radiusAddition = player.isCreative() ? 3 : 1.5F; //TODO Entity Reach Attribute
        float sweepAttack = 1.0F + EnchantmentHelper.getSweepingDamageRatio(player) * attack;

        AABB sweepArea = player.getBoundingBox().inflate(1.0 + radiusAddition, 0.25, 1.0 + radiusAddition)
                .move(ManasCoreAttributeUtils.getLookTowardVec(player, 1 + radiusAddition));
        List<LivingEntity> list = player.level().getEntitiesOfClass(LivingEntity.class, sweepArea);
        if (list.isEmpty()) return;

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
        player.sweepAttack();

        for (LivingEntity target : list) {
            if (target == player) continue;
            if (target.isAlliedTo(player)) continue;
            if (target instanceof ArmorStand armorStand && armorStand.isMarker()) continue;
            if (target.distanceToSqr(player) > 9 + radiusAddition * radiusAddition) continue;

            target.knockback(0.4000000059604645, Mth.sin(player.getYRot() * 0.017453292F), -Mth.cos(player.getYRot() * 0.017453292F));
            target.hurt(player.level().damageSources().playerAttack(player), sweepAttack);
        }
    }
}
