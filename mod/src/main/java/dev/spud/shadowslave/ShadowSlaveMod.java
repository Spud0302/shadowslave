package dev.spud.shadowslave;

import com.mojang.logging.LogUtils;
import dev.spud.shadowslave.attachment.ModAttachments;
import dev.spud.shadowslave.command.ShadowSlaveCommands;
import dev.spud.shadowslave.network.ModPayloads;
import dev.spud.shadowslave.network.SoulPlayerEvents;
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
        modEventBus.addListener(ModPayloads::register);

        NeoForge.EVENT_BUS.addListener(ShadowSlaveCommands::register);
        NeoForge.EVENT_BUS.addListener(SoulPlayerEvents::onPlayerLoggedIn);
        LOGGER.info("Shadow Slave Java core is loading");
    }
}
