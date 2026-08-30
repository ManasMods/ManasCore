/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.attribute.neoforge;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.github.manasmods.manascore.attribute.neoforge.ManasCoreAttributeNeoForge.ATTRIBUTES;

public class ManasCoreAttributeRegisterImpl {
    private static final List<Holder<Attribute>> GENERIC_REGISTRY = new CopyOnWriteArrayList<>();
    private static final List<Holder<Attribute>> PLAYER_REGISTRY = new CopyOnWriteArrayList<>();

    public static Holder<Attribute> registerToPlayers(Holder<Attribute> holder) {
        PLAYER_REGISTRY.add(holder);
        return holder;
    }

    public static Holder<Attribute> registerToGeneric(Holder<Attribute> holder) {
        GENERIC_REGISTRY.add(holder);
        return holder;
    }

    public static @NotNull Holder<Attribute> registerPlayerAttribute(String modID, String id, String name, double amount,
                                                                     double min, double max, boolean syncable, Attribute.Sentiment sentiment) {
        Attribute attribute = new RangedAttribute(name, amount, min, max).setSyncable(syncable).setSentiment(sentiment);
        return registerToPlayers(ATTRIBUTES.register(id, () -> attribute));
    }

    public static @NotNull Holder<Attribute> registerGenericAttribute(String modID, String id, String name, double amount,
                                                                      double min, double max, boolean syncable, Attribute.Sentiment sentiment) {
        Attribute attribute = new RangedAttribute(name, amount, min, max).setSyncable(syncable).setSentiment(sentiment);
        return registerToGeneric(ATTRIBUTES.register(id, () -> attribute));
    }

    static void registerAttributes(final EntityAttributeModificationEvent e) {
        e.getTypes().forEach(type -> {
            if (type.equals(EntityType.PLAYER)) PLAYER_REGISTRY.forEach(holder -> {
                if (!e.has(type, holder)) e.add(type, holder);
            });
            GENERIC_REGISTRY.forEach(holder -> {
                if (!e.has(type, holder)) e.add(type, holder);
            });
        });

        // Clear the registry
        PLAYER_REGISTRY.clear();
        GENERIC_REGISTRY.clear();
    }

    public static void init() {
        IEventBus modEventBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        if (modEventBus == null) return;
        modEventBus.addListener(ManasCoreAttributeRegisterImpl::registerAttributes);
    }
}
