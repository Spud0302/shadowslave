package dev.spud.combatcore;

import com.mojang.logging.LogUtils;
import dev.spud.combatcore.runtime.BasicPlayerMeleeExecutor;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(CombatCoreMod.MOD_ID)
public final class CombatCoreMod {
    public static final String MOD_ID = "combat_core";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CombatCoreMod() {
        NeoForge.EVENT_BUS.addListener(BasicPlayerMeleeExecutor::onAttack);
        NeoForge.EVENT_BUS.addListener(BasicPlayerMeleeExecutor::onPlayerTick);
        LOGGER.info("Combat Core mod loaded");
    }
}
