package com.github.manasmods.manascore.capability.skill.event;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import com.github.manasmods.manascore.api.skills.event.SkillTickEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ManasCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TickEventListenerHandler {
    public static final int INSTANCE_UPDATE = 20;
    public static final int PASSIVE_SKILL = 100;
    @SubscribeEvent
    public static void onTick(final TickEvent.LevelTickEvent event) {
        if (event.level.isClientSide) return;
        if (event.phase == TickEvent.Phase.START) return;

        Level level = event.level;
        if (level.getServer() == null) return;
        boolean shouldPassiveConsume = level.getServer().getTickCount() % INSTANCE_UPDATE == 0;
        boolean passiveSkillActivate = level.getServer().getTickCount() % PASSIVE_SKILL == 0;

        level.players().forEach(player -> {
            if (player instanceof ServerPlayer serverPlayer) {
                if (!shouldPassiveConsume) return;
                updateSkillInstance(serverPlayer);
                updateTemporarySkill(serverPlayer);

                if (!passiveSkillActivate) return;
                SkillStorage skillStorage = SkillAPI.getSkillsFrom(serverPlayer);
                for (ManasSkillInstance skillInstance : skillStorage.getLearnedSkills()) {
                    if (MinecraftForge.EVENT_BUS.post(new SkillTickEvent(skillInstance, serverPlayer))) continue;
                    skillInstance.onTick(serverPlayer);
                }
            }
        });
    }

    public static void updateSkillInstance(ServerPlayer serverPlayer) {
        SkillStorage skillStorage = SkillAPI.getSkillsFrom(serverPlayer);
        for (ManasSkillInstance instance : skillStorage.getLearnedSkills()) {
            if (!instance.onCoolDown()) continue;
            instance.decreaseCoolDown(1);
        }
        skillStorage.syncChanges();
    }

    public static void updateTemporarySkill(ServerPlayer serverPlayer) {
        SkillStorage skillStorage = SkillAPI.getSkillsFrom(serverPlayer);
        for (ManasSkillInstance instance : skillStorage.getLearnedSkills()) {
            if (!instance.isTemporarySkill()) continue;

            instance.decreaseRemoveTime(1);
            if (!instance.shouldRemove()) continue;

            skillStorage.forgetSkill(instance);
            skillStorage.syncChanges();
            break;
        }
    }
}
