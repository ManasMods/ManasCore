package com.github.manasmods.manascore.attribute;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class ManasCoreAttributeUtils {

    public static float getAttackDamage(Player player) {
        float f = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float h = player.getAttackStrengthScale(0.5F);
        f *= 0.2F + h * h * 0.8F;
        return f;
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
