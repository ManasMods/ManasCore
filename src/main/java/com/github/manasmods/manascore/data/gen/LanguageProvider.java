package com.github.manasmods.manascore.data.gen;

import net.minecraft.data.DataGenerator;

public abstract class LanguageProvider extends net.minecraftforge.common.data.LanguageProvider {
    public LanguageProvider(DataGenerator gen, String modid) {
        super(gen, modid, "en_us");
    }

    protected abstract void generate();

    @Override
    protected void addTranslations() {
        generate();
    }
}
