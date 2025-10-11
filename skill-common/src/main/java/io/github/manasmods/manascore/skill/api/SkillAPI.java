/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.api;

import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.skill.impl.network.InternalSkillPacketActions;
import io.github.manasmods.manascore.skill.impl.SkillRegistry;
import io.github.manasmods.manascore.skill.impl.SkillStorage;
import lombok.NonNull;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class SkillAPI {
    private SkillAPI() {
    }

    /**
     * This Method returns the {@link ManasSkill} Registry.
     * It can be used to load {@link ManasSkill}s from the Registry.
     */
    public static Registrar<ManasSkill> getSkillRegistry() {
        return SkillRegistry.SKILLS;
    }

    /**
     * This Method returns the Registry Key of the {@link SkillRegistry}.
     * It can be used to create {@link dev.architectury.registry.registries.DeferredRegister} instances
     */
    public static ResourceKey<Registry<ManasSkill>> getSkillRegistryKey() {
        return SkillRegistry.KEY;
    }

    /**
     * Can be used to load the {@link SkillStorage} from an {@link LivingEntity}.
     */
    public static SkillStorage getSkillsFrom(@NonNull LivingEntity entity) {
        return entity.manasCore$getStorage(SkillStorage.getKey());
    }

    /**
     * Send {@link InternalSkillPacketActions#sendSkillActivationPacket} with a DistExecutor on client side.
     * Used when player press a skill activation key bind.
     *
     * @see InternalSkillPacketActions#sendSkillActivationPacket
     */
    public static void skillActivationPacket(ResourceLocation skill, int keyNumber, int mode) {
        if (Platform.getEnvironment() == Env.CLIENT) {
            InternalSkillPacketActions.sendSkillActivationPacket(skill, keyNumber, mode);
        }
    }

    /**
     * Send {@link InternalSkillPacketActions#sendSkillReleasePacket} with a DistExecutor on client side.
     * Used when player release a skill activation key bind.
     *
     * @see InternalSkillPacketActions#sendSkillReleasePacket
     */
    public static void skillReleasePacket(ResourceLocation skill, int keyNumber, int mode, int heldTicks) {
        if (Platform.getEnvironment() == Env.CLIENT) {
            InternalSkillPacketActions.sendSkillReleasePacket(skill, keyNumber, mode, heldTicks);
        }
    }

    /**
     * Send {@link InternalSkillPacketActions#sendSkillTogglePacket} with a DistExecutor on client side.
     * Used when player press a skill toggle key bind.
     *
     * @see InternalSkillPacketActions#sendSkillTogglePacket
     */
    public static void skillTogglePacket(ResourceLocation skill) {
        if (Platform.getEnvironment() == Env.CLIENT) {
            InternalSkillPacketActions.sendSkillTogglePacket(skill);
        }
    }
}
