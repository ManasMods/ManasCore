package com.github.manasmods.manascore.attribute;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class ManasCoreAttributeUtils {
    public static double getEntityReachAddition(Player player) {
        double range = player.getAttributeValue(ManasCoreAttributes.ENTITY_REACH.get());
        return range == 0 ? 0 : range + (player.isCreative() ? 3 : 0);
    }

    public static double getBlockReachAddition(Player player) {
        double reach = player.getAttributeValue(ManasCoreAttributes.BLOCK_REACH.get());
        return reach == 0 ? 0 : reach + (player.isCreative() ? 0.5 : 0);
    }

    public static boolean cantHit(Player player, Entity entity, double padding) {
        Vec3 eye = player.getEyePosition();
        Vec3 targetCenter = entity.getPosition(1.0F).add(0, entity.getBbHeight() / 2, 0);
        Optional<Vec3> hit = entity.getBoundingBox().clip(eye, targetCenter);

        double dist = getEntityReachAddition(player) + padding;
        return !(hit.map(eye::distanceToSqr).orElseGet(() -> player.distanceToSqr(entity)) < dist * dist);
    }
}
