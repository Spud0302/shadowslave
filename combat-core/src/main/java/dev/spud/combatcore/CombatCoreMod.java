package dev.spud.combatcore;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CombatCoreMod.MOD_ID)
public final class CombatCoreMod {
    public static final String MOD_ID = "combat_core";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CombatCoreMod() {
        LOGGER.info("Combat Core mod loaded");
    }
}
