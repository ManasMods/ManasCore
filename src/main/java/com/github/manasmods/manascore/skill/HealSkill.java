package com.github.manasmods.manascore.skill;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.listener.LivingHurtListener;
import lombok.extern.log4j.Log4j2;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Log4j2
public class HealSkill extends ManasSkill implements LivingHurtListener {
    public HealSkill() {
        MinecraftForge.EVENT_BUS.addListener(this::unlock);
    }

    @Override
    public void onLivingHurt(ManasSkillInstance instance, LivingHurtEvent event) {
        float amount = event.getAmount() / 2;
        event.getEntityLiving().heal(amount);
        log.debug("Healed {} by {} health", event.getEntityLiving().getName().getString(), amount);
    }

    private void unlock(final LivingEntityUseItemEvent.Finish e) {
        if (e.getEntity() instanceof ServerPlayer player) {
            if (e.getItem().is(Items.APPLE)) {
                if (SkillAPI.getSkillsFrom(player).learnSkill(this)) {
                    log.debug("Unlocked example Heal skill for player {}", player.getName().getString());
                }
            }
        }
    }
}
