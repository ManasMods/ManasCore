package com.github.manasmods.manascore.data.gen;

import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

public abstract class LanguageProvider extends net.minecraftforge.common.data.LanguageProvider {
    public LanguageProvider(GatherDataEvent gatherDataEvent, String modid) {
        this(gatherDataEvent, modid, "en_us");
    }

    public LanguageProvider(GatherDataEvent gatherDataEvent, String modid, String localeCode) {
        super(gatherDataEvent.getGenerator(), modid, localeCode);
    }

    protected abstract void generate();

    @Override
    protected void addTranslations() {
        generate();
    }
}
