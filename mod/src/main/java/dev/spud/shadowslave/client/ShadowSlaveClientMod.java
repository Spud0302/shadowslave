package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/** Loads only on the physical client, keeping rendering classes off servers. */
@Mod(value = ShadowSlaveMod.MOD_ID, dist = Dist.CLIENT)
public final class ShadowSlaveClientMod {
    public ShadowSlaveClientMod(IEventBus modEventBus) {
        modEventBus.addListener(ClientModPayloads::register);
        ShadowSlaveMod.LOGGER.info("Shadow Slave client is loading");
    }
}
