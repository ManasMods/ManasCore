package com.github.manasmods.manascore.datagen.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.advancements.AdvancementHolder;

import java.util.function.Consumer;

public interface DatagenEvents {

    Event<RegisterAdvancements> registerAdvancementsEvent = EventFactory.createLoop();

    interface RegisterAdvancements {
        void registerAdvancements(Consumer<AdvancementHolder> advancementConsumer);
    }


}
