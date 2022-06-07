package com.github.manasmods.manascore;

import com.github.manasmods.manascore.tab.InventoryTabRegistry;
import com.github.manasmods.manascore.tab.VanillaInventoryTab;
import lombok.Getter;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ManasCore.MOD_ID)
public class ManasCore {
    public static final String MOD_ID = "manascore";
    @Getter
    private static final Logger logger = LogManager.getLogger();

    public ManasCore() {
        InventoryTabRegistry.register(new VanillaInventoryTab());
    }
}
