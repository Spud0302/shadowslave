package dev.spud.shadowslave;

import com.mojang.logging.LogUtils;
import dev.spud.shadowslave.attachment.ModAttachments;
import dev.spud.shadowslave.command.ShadowSlaveCommands;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(ShadowSlaveMod.MOD_ID)
public final class ShadowSlaveMod {
    public static final String MOD_ID = "shadowslave";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ShadowSlaveMod(IEventBus modEventBus) {
        ModAttachments.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(ShadowSlaveCommands::register);
        LOGGER.info("Shadow Slave Java core is loading");
    }
}
