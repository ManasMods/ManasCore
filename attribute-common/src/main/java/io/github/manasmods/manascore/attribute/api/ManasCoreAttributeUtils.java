/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.attribute.api;

import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ManasCoreAttributeUtils {
    public static float getAttackDamage(Player player) {
        float f = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float h = player.getAttackStrengthScale(0.5F);
        f *= 0.2F + h * h * 0.8F;
        return f;
    }

    public static float getWeaponDamage(LivingEntity attacker, @Nullable Entity target, @Nullable DamageSource source) {
        AttributeInstance attack = attacker.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack == null) return 0;

        float damage = 1F;
        AttributeModifier modifier = attack.getModifier(Item.BASE_ATTACK_DAMAGE_ID);
        if (modifier != null) damage += (float) modifier.amount();

        if (target != null && source != null && attacker.level() instanceof ServerLevel serverLevel)
            damage = EnchantmentHelper.modifyDamage(serverLevel, attacker.getWeaponItem(), target, source, damage);
        return damage;
    }

    public static void triggerCriticalAttackEffect(Entity target, Entity attacker) {
        target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT, attacker.getSoundSource(), 1.0F, 1.0F);
        if (target.level() instanceof ServerLevel level)
            level.getChunkSource().broadcastAndSend(target, new ClientboundAnimatePacket(target, 4));
    }

    public static Vec3 getLookTowardVec(Player player, double distance) {
        float f = player.getXRot();
        float g = player.getYRot();
        float h = Mth.cos(-g * 0.017453292F - 3.1415927F);
        float i = Mth.sin(-g * 0.017453292F - 3.1415927F);
        float j = -Mth.cos(-f * 0.017453292F);
        float k = Mth.sin(-f * 0.017453292F);
        return new Vec3(i * j * distance, k * distance, h * j * distance);
    }
}
