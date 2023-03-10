package com.github.manasmods.manascore.config;

import lombok.Getter;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import org.apache.commons.lang3.tuple.Pair;

public class ManasCoreConfig {
    public static final ManasCoreConfig INSTANCE;
    public static final ForgeConfigSpec SPEC;

    static {
        Pair<ManasCoreConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(ManasCoreConfig::new);
        INSTANCE = pair.getKey();
        SPEC = pair.getValue();
    }

    @Getter
    private final IntValue anvilExpLimit;

    private ManasCoreConfig(ForgeConfigSpec.Builder builder) {
        builder.push("anvil");

        this.anvilExpLimit = builder
            .comment("Max EXP a survival player can spend in an anvil recipe", "-1 = unlimited")
            .defineInRange("expLimit", 40, -1, 1_000_000);

        builder.pop();
    }
}
