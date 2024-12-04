/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.api;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.manasmods.manascore.registry.api.AbstractRegister;
import net.minecraft.core.Holder;

import java.util.function.Supplier;

public class AbstractSkillRegister<R extends AbstractSkillRegister<R>> extends AbstractRegister<R> {
    protected DeferredRegister<ManasSkill> skills = null;
    public AbstractSkillRegister(String modId) {
        super(modId);
    }

    public void init(final Runnable beforeRegistration) {
        super.init(beforeRegistration);
        if (skills != null) skills.register();
    }

    /**
     * Creates a new {@link SkillBuilder} for the given name.
     */
    public <T extends ManasSkill> SkillBuilder<R, T> skill(final String name, final Supplier<T> skillFactory) {
        if (this.skills == null) this.skills = DeferredRegister.create(this.modId, SkillAPI.getSkillRegistryKey());
        return new SkillBuilder<>(self(), name, skillFactory);
    }

    public static class SkillBuilder<R extends AbstractSkillRegister<R>, T extends ManasSkill> extends ContentBuilder<T, R> {
        protected final Supplier<T> skillFactory;

        private SkillBuilder(R register, String name, Supplier<T> skillFactory) {
            super(register, name);
            this.skillFactory = skillFactory;
        }

        @Override
        public RegistrySupplier<T> end() {
            return this.register.skills.register(this.id, this.skillFactory);
        }

        @Override
        public Holder<T> endAsHolder() {
            return this.end().getRegistrar().getHolder(this.id);
        }
    }
}
