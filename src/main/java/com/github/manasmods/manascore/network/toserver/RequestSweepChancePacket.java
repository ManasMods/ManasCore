package com.github.manasmods.manascore.network.toserver;

import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.SweepingEdgeEnchantment;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class RequestSweepChancePacket {
    public RequestSweepChancePacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) sweepAttack(player);
        });
        ctx.get().setPacketHandled(true);
    }

    private void sweepAttack(Player player) {
        double radiusAddition = player.getAttackRange() / 2;
        float sweepAttack = 1.0F + getSweepingDamageRatio(player) * getAttackDamage(player);

        AABB sweepArea = player.getBoundingBox().inflate(1.0 + radiusAddition, 0.25, 1.0 + radiusAddition)
                .move(getLookTowardVec(player, 1 + radiusAddition));
        List<LivingEntity> list = player.level.getEntitiesOfClass(LivingEntity.class, sweepArea);
        if (list.isEmpty()) return;

        player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
        player.sweepAttack();

        for (LivingEntity target : list) {
            if (target == player) continue;
            if (target.isAlliedTo(player)) continue;
            if (target instanceof ArmorStand armorStand && armorStand.isMarker()) continue;
            if (!player.canHit(target, 0)) continue;

            target.knockback(0.4000000059604645, Mth.sin(player.getYRot() * 0.017453292F), -Mth.cos(player.getYRot() * 0.017453292F));
            target.hurt(DamageSource.playerAttack(player), sweepAttack);
        }
    }

    private float getSweepingDamageRatio(LivingEntity pEntity) {
        int i = 1 + EnchantmentHelper.getEnchantmentLevel(Enchantments.SWEEPING_EDGE, pEntity);
        return i > 0 ? SweepingEdgeEnchantment.getSweepingDamageRatio(i) : 0.0F;
    }

    private float getAttackDamage(Player player) {
        float f = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float h = player.getAttackStrengthScale(0.5F);
        f *= 0.2F + h * h * 0.8F;
        return f;
    }

    private Vec3 getLookTowardVec(Player player, double distance) {
        float f = player.getXRot();
        float g = player.getYRot();
        float h = Mth.cos(-g * 0.017453292F - 3.1415927F);
        float i = Mth.sin(-g * 0.017453292F - 3.1415927F);
        float j = -Mth.cos(-f * 0.017453292F);
        float k = Mth.sin(-f * 0.017453292F);
        return new Vec3(i * j * distance, k * distance, h * j * distance);
    }
}
