package com.github.manasmods.manascore.capability.skill;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import com.github.manasmods.manascore.network.ManasCoreNetwork;
import com.github.manasmods.manascore.network.toclient.SyncSkillsPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public interface InternalSkillStorage extends SkillStorage {
    default List<ManasSkillInstance> getDirtySkills() {
        return getLearnedSkills().parallelStream()
            .filter(ManasSkillInstance::isDirty)
            .toList();
    }

    void setOwner(Entity entity);

    Entity getOwner();

    default void sync(SyncSkillsPacket.SyncType syncType) {
        if (getOwner() == null) return;
        if (getOwner().level.isClientSide()) return;

        if (getOwner() instanceof ServerPlayer) {
            ManasCoreNetwork.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(this::getOwner), new SyncSkillsPacket(getOwner(), this, syncType));
        } else {
            ManasCoreNetwork.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(this::getOwner), new SyncSkillsPacket(getOwner(), this, syncType));
        }
    }

    @Override
    default void syncAll() {
        sync(SyncSkillsPacket.SyncType.FULL);
    }

    @Override
    default void syncChanges() {
        sync(SyncSkillsPacket.SyncType.CHANGES_ONLY);
    }

    @Override
    default void syncPlayer(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ManasCoreNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new SyncSkillsPacket(getOwner(), this, SyncSkillsPacket.SyncType.FULL));
        }
    }

    void updateSkill(ManasSkillInstance updatedInstance, boolean sync);

    @Override
    default void updateSkill(ManasSkillInstance updatedInstance) {
        updateSkill(updatedInstance, true);
    }

    @Override
    default void updateSkills(List<ManasSkillInstance> updatedInstances) {
        updatedInstances.forEach(skillInstance -> updateSkill(skillInstance, false));
        syncChanges();
    }

    @Override
    default void updateSkills(ManasSkillInstance... updatedInstances) {
        for (ManasSkillInstance skill : updatedInstances) {
            updateSkill(skill, false);
        }
        syncChanges();
    }

    @Override
    default boolean learnSkill(ManasSkill skill) {
        return learnSkill(skill.createDefaultInstance());
    }

    @Override
    default void forgetSkill(ManasSkill skill) {
        getLearnedSkills().removeIf(instance -> instance.getSkill().getRegistryName().equals(skill.getRegistryName()));
    }

    @Override
    default void forgetSkill(ManasSkillInstance skill) {
        forgetSkill(skill.getSkill());
    }
}
