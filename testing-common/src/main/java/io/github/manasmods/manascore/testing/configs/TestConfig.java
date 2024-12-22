package io.github.manasmods.manascore.testing.configs;

import io.github.manasmods.manascore.command.api.Permission;
import io.github.manasmods.manascore.config.api.ManasConfig;
import io.github.manasmods.manascore.testing.ModuleConstants;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class TestConfig extends ManasConfig {
    public String initialMessage = "Config working!";
    public Permission.PermissionLevel permissionLevel = Permission.PermissionLevel.GAMEMASTER;
    public List<Integer> intList = List.of(69, 420);
    public List<Double> doubleList = List.of(1.0, 2D, 3d);
    public List<Long> longList = List.of(1L, 2L, 3L);
    public List<String> stringList = List.of("I", "Hate", "Bugs", "soooooo much!");
    public List<ResourceLocation> resourceLocationList = List.of(ResourceLocation.withDefaultNamespace("test"),
            ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "test"));
}
