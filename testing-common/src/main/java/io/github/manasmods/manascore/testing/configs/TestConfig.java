package io.github.manasmods.manascore.testing.configs;

import io.github.manasmods.manascore.config.api.ManasConfig;
import io.github.manasmods.manascore.testing.ModuleConstants;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class TestConfig extends ManasConfig {
    public String initialMessage = "";
    public int ironGolemDamageMultiplier = 0;
    public boolean instaKillCreeper = false;
    public List<ResourceLocation> list = List.of(ResourceLocation.withDefaultNamespace("test"),
            ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "test"));
}
