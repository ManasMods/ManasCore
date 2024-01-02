package com.github.manasmods.manascore.api.skill;

import com.github.manasmods.manascore.skill.InternalSkillPacketActions;
import com.github.manasmods.manascore.skill.SkillRegistry;
import com.github.manasmods.manascore.skill.SkillStorage;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.utils.Env;
import lombok.NonNull;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;

public final class SkillAPI {
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
     * It can be used to create {@link DeferredRegister} instances
     */
    public static ResourceKey<Registry<ManasSkill>> getSkillRegistryKey() {
        return SkillRegistry.KEY;
    }

    /**
     * Can be used to load the {@link SkillStorage} from an {@link LivingEntity}.
     *
     * @see SkillStorage
     */
    public static SkillStorage getSkillsFrom(@NonNull LivingEntity entity) {
        return entity.manasCore$getStorage(SkillStorage.KEY);
    }

    /**
     * Send {@link InternalSkillPacketActions#sendSkillActivationPacket} with a DistExecutor on client side.
     * Used when player press a skill activation key bind.
     *
     * @see InternalSkillPacketActions#sendSkillActivationPacket
     */
    public static void skillActivationPacket(int keyNumber) {
        if (Platform.getEnvironment() == Env.CLIENT) {
            InternalSkillPacketActions.sendSkillActivationPacket(keyNumber);
        }
    }

    /**
     * Send {@link InternalSkillPacketActions#sendSkillReleasePacket} with a DistExecutor on client side.
     * Used when player release a skill activation key bind.
     *
     * @see InternalSkillPacketActions#sendSkillReleasePacket
     */
    public static void skillReleasePacket(int keyNumber, int heldTicks) {
        if (Platform.getEnvironment() == Env.CLIENT) {
            InternalSkillPacketActions.sendSkillReleasePacket(keyNumber, heldTicks);
        }
    }

    /**
     * Send {@link InternalSkillPacketActions#sendSkillTogglePacket} with a DistExecutor on client side.
     * Used when player press a skill toggle key bind.
     *
     * @see InternalSkillPacketActions#sendSkillTogglePacket
     */
    public static void skillTogglePacket() {
        if (Platform.getEnvironment() == Env.CLIENT) {
            InternalSkillPacketActions.sendSkillTogglePacket();
        }
    }
}
