package dev.spud.shadowslave;

import com.mojang.logging.LogUtils;
import dev.spud.shadowslave.ability.AbilityService;
import dev.spud.shadowslave.ability.ChannelService;
import dev.spud.shadowslave.ability.KitPassives;
import dev.spud.shadowslave.attachment.ModAttachments;
import dev.spud.shadowslave.awakening.DreamCandleRitual;
import dev.spud.shadowslave.awakening.SoulSeaService;
import dev.spud.shadowslave.block.ModBlocks;
import dev.spud.shadowslave.command.ShadowSlaveCommands;
import dev.spud.shadowslave.dreamrealm.DreamRealmCommands;
import dev.spud.shadowslave.dreamrealm.DreamRealmResourceInteractionRuntime;
import dev.spud.shadowslave.dreamrealm.DreamRealmStoryNpcRuntime;
import dev.spud.shadowslave.echo.EchoCommands;
import dev.spud.shadowslave.echo.EchoManifestationService;
import dev.spud.shadowslave.item.GlassRoadMemoryItem;
import dev.spud.shadowslave.item.ModDataComponents;
import dev.spud.shadowslave.item.ModItems;
import dev.spud.shadowslave.memory.MemoryCommands;
import dev.spud.shadowslave.network.ModPayloads;
import dev.spud.shadowslave.network.SoulPlayerEvents;
import dev.spud.shadowslave.nightmare.NightmareEvents;
import dev.spud.shadowslave.soul.core.CoreService;
import dev.spud.shadowslave.soul.core.ShardDrops;
import dev.spud.shadowslave.world.entity.DrownedBellListenerEntityAdapter;
import dev.spud.shadowslave.world.entity.NightmareCreatureEntities;
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
        ModBlocks.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModItems.register(modEventBus);
        ShardDrops.bootstrap();
        NightmareCreatureEntities.register(modEventBus);

        if (physicalSide == Dist.DEDICATED_SERVER) {
            modEventBus.addListener(ModPayloads::registerDedicatedServer);
        }

        NeoForge.EVENT_BUS.addListener(ShadowSlaveCommands::register);
        NeoForge.EVENT_BUS.addListener(MemoryCommands::register);
        NeoForge.EVENT_BUS.addListener(EchoCommands::register);
        NeoForge.EVENT_BUS.addListener(DreamRealmCommands::register);
        NeoForge.EVENT_BUS.addListener(DreamRealmStoryNpcRuntime::registerCommands);
        NeoForge.EVENT_BUS.addListener(SoulPlayerEvents::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(NightmareEvents::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(NightmareEvents::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(NightmareEvents::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(DreamRealmResourceInteractionRuntime::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(DreamRealmStoryNpcRuntime::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(DrownedBellListenerEntityAdapter::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(DreamCandleRitual::onPlayerWakeUp);
        NeoForge.EVENT_BUS.addListener(AbilityService::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(AbilityService::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(ChannelService::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(ChannelService::onFall);
        NeoForge.EVENT_BUS.addListener(ChannelService::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(SoulSeaService::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(SoulSeaService::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(ShardDrops::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(CoreService::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(CoreService::onPlayerRespawn);
        NeoForge.EVENT_BUS.addListener(KitPassives::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(KitPassives::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(KitPassives::onGameEvent);
        NeoForge.EVENT_BUS.addListener(KitPassives::onKnockBack);
        NeoForge.EVENT_BUS.addListener(KitPassives::onBreathe);
        NeoForge.EVENT_BUS.addListener(KitPassives::onWakeUp);
        NeoForge.EVENT_BUS.addListener(EchoManifestationService::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(GlassRoadMemoryItem::onPlayerTick);
        LOGGER.info("Shadow Slave Java core is loading");
    }
}
