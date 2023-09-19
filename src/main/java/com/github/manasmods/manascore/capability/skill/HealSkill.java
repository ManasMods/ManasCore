package com.github.manasmods.manascore.capability.skill;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import lombok.extern.log4j.Log4j2;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Log4j2
public class HealSkill extends ManasSkill {
    public HealSkill(){
        MinecraftForge.EVENT_BUS.addListener(this::unlock);
    }

    @Override
    public float onTakenDamage(ManasSkillInstance instance, LivingEntity living, LivingDamageEvent event) {
        float amount = event.getAmount() / 2;
        event.getEntity().heal(amount);
        log.debug("Healed {} by {} health", event.getEntity().getName().getString(), amount);
        return amount;
    }

    private void unlock(final LivingEntityUseItemEvent.Finish e){
        if (e.getEntity() instanceof ServerPlayer player) {
            if (e.getItem().is(Items.APPLE)) {
                if (SkillAPI.getSkillsFrom(player).learnSkill(this)) {
                    log.debug("Unlocked example Heal skill for player {}", player.getName().getString());
                }
            }
        }
    }
}
