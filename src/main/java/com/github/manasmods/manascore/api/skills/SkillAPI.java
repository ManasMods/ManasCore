package com.github.manasmods.manascore.api.skills;

import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import com.github.manasmods.manascore.capability.skill.EntitySkillCapability;
import com.github.manasmods.manascore.capability.skill.event.InternalSkillPacketActions;
import com.github.manasmods.manascore.skill.SkillRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
@ApiStatus.AvailableSince("1.0.2.0")
public final class SkillAPI {
    /**
     * This Method returns the {@link ManasSkill} Registry.
     * It can be used to load {@link ManasSkill}s from the Registry.
     */
    @NotNull
    public static IForgeRegistry<ManasSkill> getSkillRegistry() {
        return SkillRegistry.REGISTRY.get();
    }

    /**
     * This Method returns the Registry Key of the {@link SkillRegistry}.
     * It can be used to create {@link DeferredRegister} instances
     */
    @NotNull
    public static ResourceLocation getSkillRegistryKey() {
        return SkillRegistry.REGISTRY_KEY;
    }


    /**
     * Can be used to load the {@link SkillStorage} from an {@link Entity}.
     *
     * @see SkillStorage
     */
    @NotNull
    public static SkillStorage getSkillsFrom(Entity entity) {
        return EntitySkillCapability.load(entity);
    }

    /**
     * Send {@link InternalSkillPacketActions#sendSkillActivationPacket} with a DistExecutor on client side.
     * Used when player press a skill activation key bind.
     *
     * @see InternalSkillPacketActions#sendSkillActivationPacket
     */
    public static void skillActivationPacket(int keyNumber) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> InternalSkillPacketActions.sendSkillActivationPacket(keyNumber));
    }

    /**
     * Send {@link InternalSkillPacketActions#sendSkillReleasePacket} with a DistExecutor on client side.
     * Used when player release a skill activation key bind.
     *
     * @see InternalSkillPacketActions#sendSkillReleasePacket
     */
    public static void skillReleasePacket(int keyNumber, int heldTicks) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> InternalSkillPacketActions.sendSkillReleasePacket(keyNumber, heldTicks));
    }

    /**
     * Send {@link InternalSkillPacketActions#sendSkillTogglePacket} with a DistExecutor on client side.
     * Used when player press a skill toggle key bind.
     *
     * @see InternalSkillPacketActions#sendSkillTogglePacket
     */
    public static void skillTogglePacket() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> InternalSkillPacketActions::sendSkillTogglePacket);
    }
}