/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.api;

import dev.architectury.event.Event;
import io.github.manasmods.manascore.network.api.util.Changeable;
import io.github.manasmods.manascore.skill.ModuleConstants;
import io.github.manasmods.manascore.skill.impl.SkillStorage;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This is the Registry Object for Skills.
 * Extend from this Class to create your own Skills.
 * <p>
 * To add functionality to the {@link ManasSkill}, you need to implement a listener interface.
 * Those interfaces allow you to invoke a Method when an {@link Event} happens.
 * The Method will only be invoked for an {@link Entity} that learned the {@link ManasSkill}.
 * <p>
 * Skills can be learned by calling the {@link SkillStorage#learnSkill} method.
 * You can simply use {@link SkillAPI#getSkillsFrom(LivingEntity)} to get the {@link SkillStorage} of an {@link Entity}.
 * <p>
 * You're also allowed to override the {@link ManasSkill#createDefaultInstance()} method to create your own implementation
 * of a {@link ManasSkillInstance}. This is required if you want to attach additional data to the {@link ManasSkill}
 * (for example to allow to disable a skill or make the skill gain exp on usage).
 */
public class ManasSkill {
    protected final Map<Holder<Attribute>, AttributeTemplate> attributeModifiers = new Object2ObjectOpenHashMap<>();
    public ManasSkill() {

    }

    /**
     * Used to create a {@link ManasSkillInstance} of this Skill.
     * <p>
     * Override this Method to use your extended version of {@link ManasSkillInstance}
     */
    public ManasSkillInstance createDefaultInstance() {
        return new ManasSkillInstance(this);
    }

    /**
     * Used to get the {@link ResourceLocation} id of this skill.
     */
    @Nullable
    public ResourceLocation getRegistryName() {
        return SkillAPI.getSkillRegistry().getId(this);
    }

    /**
     * Used to get the {@link MutableComponent} name of this skill for translation.
     */
    @Nullable
    public MutableComponent getName() {
        final ResourceLocation id = getRegistryName();
        if (id == null) return null;
        return Component.translatable(String.format("%s.skill.%s", id.getNamespace(), id.getPath().replace('/', '.')));
    }

    public MutableComponent getChatDisplayName(boolean withDescription) {
        Style style = Style.EMPTY.withColor(ChatFormatting.GRAY);
        if (withDescription) {
            MutableComponent hoverMessage = this.getName().append("\n");
            hoverMessage.append(this.getSkillDescription().withStyle(ChatFormatting.GRAY));
            hoverMessage.append("\n").append(Component.literal(SkillAPI.getSkillRegistry().getId(this).toString()).withStyle(ChatFormatting.DARK_GRAY));
            style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverMessage));
        }

        MutableComponent component = Component.literal("[").append(this.getName()).append("]");
        return component.withStyle(style);
    }

    /**
     * Used to get the {@link ResourceLocation} of this skill's icon texture.
     */
    @Nullable
    public ResourceLocation getSkillIcon() {
        ResourceLocation id = this.getRegistryName();
        if (id == null) return null;
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "icons/skills/" + id.getPath());
    }

    /**
     * Used to get the {@link MutableComponent} description of this skill for translation.
     */
    public MutableComponent getSkillDescription() {
        ResourceLocation id = this.getRegistryName();
        if (id == null) return Component.empty();
        return Component.translatable(String.format("%s.skill.%s.description", id.getNamespace(), id.getPath().replace('/', '.')));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManasSkill skill = (ManasSkill) o;
        return Objects.equals(this.getRegistryName(), skill.getRegistryName());
    }

    /**
     * Determine if the {@link ManasSkillInstance} of this Skill can be used by {@link LivingEntity}.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param user   Affected {@link LivingEntity} owning this Skill.
     * @return false will stop {@link LivingEntity} from using any feature of the skill.
     */
    public boolean canInteractSkill(ManasSkillInstance instance, LivingEntity user) {
        return true;
    }

    /**
     * @return the maximum number of ticks that this skill can be held down with the skill activation button.
     * </p>
     */
    public int getMaxHeldTime(ManasSkillInstance instance, LivingEntity entity) {
        return 72000;
    }

    /**
     * Determine if this skill can be toggled.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     * @return false if this skill is not toggleable.
     */
    public boolean canBeToggled(ManasSkillInstance instance, LivingEntity entity) {
        return false;
    }

    /**
     * Determine if a mode of this skill can still be activated when on cooldown
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     * @return false if this skill cannot ignore cooldown.
     */
    public boolean canIgnoreCoolDown(ManasSkillInstance instance, LivingEntity entity, int mode) {
        return false;
    }

    /**
     * Determine if this skill's {@link ManasSkill#onTick} can be executed.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     * @return false if this skill cannot tick.
     */
    public boolean canTick(ManasSkillInstance instance, LivingEntity entity) {
        return false;
    }

    /**
     * Determine if this skill's {@link ManasSkill#onScroll} can be executed.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     * @return false if this skill cannot be scrolled.
     */
    public boolean canScroll(ManasSkillInstance instance, LivingEntity entity) {
        return false;
    }

    /**
     * @return the number of modes that this skill can have.
     */
    public int getModes() {
        return 1;
    }

    /**
     * @return the maximum mastery points that this skill can have.
     */
    public int getMaxMastery() {
        return 100;
    }

    /**
     * Determine if the {@link ManasSkillInstance} of this Skill is mastered by {@link LivingEntity} owning it.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     * @return true to will mark this Skill is mastered, which can be used for increase stats or additional features/modes.
     */
    public boolean isMastered(ManasSkillInstance instance, LivingEntity entity) {
        return instance.getMastery() >= getMaxMastery();
    }

    /**
     * Increase the mastery points for {@link ManasSkillInstance} of this Skill if not mastered.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public void addMasteryPoint(ManasSkillInstance instance, LivingEntity entity) {
        if (isMastered(instance, entity)) return;
        instance.setMastery(instance.getMastery() + 1);
        if (isMastered(instance, entity)) instance.onSkillMastered(entity);
    }

    /**
     * Adds an attribute modifier to this skill. This method can be called for more than one attribute.
     * The attributes are applied to an entity when the skill is held and removed when it stops being held.
     * </p>
     */
    public void addHeldAttributeModifier(Holder<Attribute> holder, ResourceLocation resourceLocation, double amount, AttributeModifier.Operation operation) {
        this.attributeModifiers.put(holder, new AttributeTemplate(resourceLocation, amount, operation));
    }

    public void addHeldAttributeModifier(Holder<Attribute> holder, String id, double amount, AttributeModifier.Operation operation) {
        this.attributeModifiers.put(holder, new AttributeTemplate(id, amount, operation));
    }

    /**
     * @return the amplifier for each attribute template that this skill applies.
     * </p>
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     * @param instance Affected {@link ManasSkillInstance}
     * @param holder   Affected {@link Holder<Attribute>} that this skill provides.
     * @param template Affected {@link AttributeTemplate} that this skill provides for an attribute.
     */
    public double getAttributeModifierAmplifier(ManasSkillInstance instance, LivingEntity entity, Holder<Attribute> holder, AttributeTemplate template, int mode) {
        return 1;
    }

    /**
     * Applies the attribute modifiers of this skill on the {@link LivingEntity} holding the skill activation button.
     *
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     * @param instance Affected {@link ManasSkillInstance}
     */
    public void addHeldAttributeModifiers(ManasSkillInstance instance, LivingEntity entity, int mode) {
        if (this.attributeModifiers.isEmpty()) return;

        AttributeMap attributeMap = entity.getAttributes();
        for (Map.Entry<Holder<Attribute>, AttributeTemplate> entry : this.attributeModifiers.entrySet()) {
            AttributeInstance attributeInstance = attributeMap.getInstance(entry.getKey());

            if (attributeInstance == null) continue;
            attributeInstance.removeModifier(entry.getValue().id());
            attributeInstance.addOrUpdateTransientModifier(entry.getValue().create(instance.getAttributeModifierAmplifier(entity, entry.getKey(), entry.getValue(), mode)));
        }
    }

    /**
     * Removes the attribute modifiers of this skill from the {@link LivingEntity} holding the skill activation button.
     *
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public void removeAttributeModifiers(ManasSkillInstance instance, LivingEntity entity, int mode) {
        if (this.attributeModifiers.isEmpty()) return;
        AttributeMap map = entity.getAttributes();
        List<AttributeInstance> dirtyInstances = new ArrayList<>();

        for (Map.Entry<Holder<Attribute>, AttributeTemplate> entry : this.attributeModifiers.entrySet()) {
            AttributeInstance attributeInstance = map.getInstance(entry.getKey());
            if (attributeInstance == null) continue;
            attributeInstance.removeModifier(entry.getValue().id());
            dirtyInstances.add(attributeInstance);
        }

        if (!dirtyInstances.isEmpty() && entity instanceof ServerPlayer player) {
            ClientboundUpdateAttributesPacket packet = new ClientboundUpdateAttributesPacket(player.getId(), dirtyInstances);
            player.connection.send(packet);
        }
    }

    /**
     * Called when the {@link LivingEntity} owing this Skill toggles it on.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public void onToggleOn(ManasSkillInstance instance, LivingEntity entity) {
        // Override this method to add your own logic
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill toggles it off.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public void onToggleOff(ManasSkillInstance instance, LivingEntity entity) {
        // Override this method to add your own logic
    }

    /**
     * Called every tick of the {@link LivingEntity} owning this Skill.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param living   Affected {@link LivingEntity} owning this Skill.
     */
    public void onTick(ManasSkillInstance instance, LivingEntity living) {
        // Override this method to add your own logic
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill presses the skill activation button.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public void onPressed(ManasSkillInstance instance, LivingEntity entity, int keyNumber, int mode) {
        // Override this method to add your own logic
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill holds the skill activation button.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param living   Affected {@link LivingEntity} owning this Skill.
     * @return true to continue ticking this Skill.
     */
    public boolean onHeld(ManasSkillInstance instance, LivingEntity living, int heldTicks, int mode) {
        // Override this method to add your own logic
        return false;
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill releases the skill activation button after {@param heldTicks}.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public void onRelease(ManasSkillInstance instance, LivingEntity entity, int heldTicks, int keyNumber, int mode) {
        // Override this method to add your own logic
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill scrolls the mouse when holding the skill activation buttons.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param living   Affected {@link LivingEntity} owning this Skill.
     * @param delta    The scroll delta of the mouse scroll.
     */
    public void onScroll(ManasSkillInstance instance, LivingEntity living, double delta, int mode) {
        // Override this method to add your own logic
    }

    /**
     * Called when the {@link LivingEntity} learns this Skill.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} learning this Skill.
     */
    public void onLearnSkill(ManasSkillInstance instance, LivingEntity entity) {
        // Override this method to add your own logic
    }

    /**
     * Called when the {@link LivingEntity} forgets this Skill.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} learning this Skill.
     */
    public void onForgetSkill(ManasSkillInstance instance, LivingEntity entity) {
        // Override this method to add your own logic
    }

    /**
     * Called when the {@link LivingEntity} masters this skill.
     *
     * @param instance Affected {@link ManasSkillInstance}
     * @param entity   Affected {@link LivingEntity} owning this Skill.
     */
    public void onSkillMastered(ManasSkillInstance instance, LivingEntity entity) {
        // Override this method to add your own logic
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill gains an effect.
     *
     * @see ManasSkillInstance#onEffectAdded(LivingEntity, Entity, Changeable)
     */
    public boolean onEffectAdded(ManasSkillInstance instance, LivingEntity entity, @Nullable Entity source, Changeable<MobEffectInstance> effect) {
        // Override this method to add your own logic
        return true;
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill starts to be targeted by a mob.
     *
     * @see ManasSkillInstance#onBeingTargeted(Changeable, LivingEntity)
     */
    public boolean onBeingTargeted(ManasSkillInstance instance, Changeable<LivingEntity> target, LivingEntity owner) {
        // Override this method to add your own logic
        return true;
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill starts to be attacked.
     *
     * @see ManasSkillInstance#onBeingDamaged(LivingEntity, DamageSource, float)
     */
    public boolean onBeingDamaged(ManasSkillInstance instance, LivingEntity entity, DamageSource source, float amount) {
        // Override this method to add your own logic
        return true;
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill damage another {@link LivingEntity}.
     *
     * @see ManasSkillInstance#onDamageEntity(LivingEntity, LivingEntity, DamageSource, Changeable)
     */
    public boolean onDamageEntity(ManasSkillInstance instance, LivingEntity owner, LivingEntity target, DamageSource source, Changeable<Float> amount) {
        // Override this method to add your own logic
        return true;
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill damage another {@link LivingEntity},
     *
     * @see ManasSkillInstance#onTouchEntity(LivingEntity, LivingEntity, DamageSource, Changeable)
     */
    public boolean onTouchEntity(ManasSkillInstance instance, LivingEntity owner, LivingEntity target, DamageSource source, Changeable<Float> amount) {
        // Override this method to add your own logic
        return true;
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill takes damage.
     *
     * @see ManasSkillInstance#onTakenDamage(LivingEntity, DamageSource, Changeable)
     */
    public boolean onTakenDamage(ManasSkillInstance instance, LivingEntity owner, DamageSource source, Changeable<Float> amount) {
        // Override this method to add your own logic
        return true;
    }

    /**
     * Called when the {@link LivingEntity} is hit by a projectile.
     */
    public void onProjectileHit(ManasSkillInstance instance, LivingEntity living, EntityHitResult hitResult, Projectile projectile, Changeable<ProjectileDeflection> deflection, Changeable<EntityEvents.ProjectileHitResult> result) {
        // Override this method to add your own logic
    }

    /**
     * Called when the {@link LivingEntity} owning this Skill dies.
     *
     * @see ManasSkillInstance#onDeath(LivingEntity, DamageSource)
     */
    public boolean onDeath(ManasSkillInstance instance, LivingEntity owner, DamageSource source) {
        // Override this method to add your own logic
        return true;
    }

    /**
     * Called when the {@link ServerPlayer} owning this Skill respawns.
     */
    public void onRespawn(ManasSkillInstance instance, ServerPlayer owner, boolean conqueredEnd) {
        // Override this method to add your own logic
    }

    /**
     * Attribute Template for easier attribute modifier implementation.
     */
    public static record AttributeTemplate(ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        public AttributeTemplate(ResourceLocation id, double amount, AttributeModifier.Operation operation) {
            this.id = id;
            this.amount = amount;
            this.operation = operation;
        }

        public AttributeTemplate(String id, double amount, AttributeModifier.Operation operation) {
            this(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, id), amount, operation);
        }

        public AttributeModifier create(double i) {
            return new AttributeModifier(this.id, this.amount * i, this.operation);
        }

        public AttributeModifier create(ResourceLocation location, double i) {
            return new AttributeModifier(location, this.amount * i, this.operation);
        }

        public ResourceLocation id() {
            return this.id;
        }

        public double amount() {
            return this.amount;
        }

        public AttributeModifier.Operation operation() {
            return this.operation;
        }
    }
}
