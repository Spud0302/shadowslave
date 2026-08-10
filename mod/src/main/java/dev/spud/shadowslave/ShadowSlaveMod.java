package dev.spud.shadowslave;

import com.mojang.logging.LogUtils;
import dev.spud.shadowslave.attachment.ModAttachments;
import dev.spud.shadowslave.command.ShadowSlaveCommands;
import dev.spud.shadowslave.echo.EchoCommands;
import dev.spud.shadowslave.echo.EchoManifestationService;
import dev.spud.shadowslave.item.ModItems;
import dev.spud.shadowslave.memory.MemoryCommands;
import dev.spud.shadowslave.network.ModPayloads;
import dev.spud.shadowslave.network.SoulPlayerEvents;
import dev.spud.shadowslave.nightmare.NightmareEvents;
import dev.spud.shadowslave.preview.PreviewPowerService;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(ShadowSlaveMod.MOD_ID)
public final class ShadowSlaveMod {
    public static final String MOD_ID = "shadowslave";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ShadowSlaveMod(IEventBus modEventBus, Dist physicalSide) {
        ModAttachments.register(modEventBus);
        ModItems.register(modEventBus);

        if (physicalSide == Dist.DEDICATED_SERVER) {
            modEventBus.addListener(ModPayloads::registerDedicatedServer);
        }

        NeoForge.EVENT_BUS.addListener(ShadowSlaveCommands::register);
        NeoForge.EVENT_BUS.addListener(MemoryCommands::register);
        NeoForge.EVENT_BUS.addListener(EchoCommands::register);
        NeoForge.EVENT_BUS.addListener(SoulPlayerEvents::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(NightmareEvents::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(NightmareEvents::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(NightmareEvents::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(PreviewPowerService::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(EchoManifestationService::onPlayerTick);
        LOGGER.info("Shadow Slave Java core is loading");
    }
}
