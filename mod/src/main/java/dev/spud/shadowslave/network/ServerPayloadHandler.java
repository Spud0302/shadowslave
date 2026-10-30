package dev.spud.shadowslave.network;

import dev.spud.shadowslave.network.payload.CallingFrequencyPayload;
import dev.spud.shadowslave.network.payload.TrueNameActionPayload;
import dev.spud.shadowslave.spell.truename.TrueNames;
import dev.spud.shadowslave.spell.calling.CallingFrequency;
import dev.spud.shadowslave.spell.calling.SpellCallings;
import dev.spud.shadowslave.ability.AbilityService;
import dev.spud.shadowslave.ability.ChannelService;
import dev.spud.shadowslave.awakening.SoulSeaService;
import dev.spud.shadowslave.echo.EchoInstanceData;
import dev.spud.shadowslave.echo.EchoManifestationService;
import dev.spud.shadowslave.echo.EchoOwnershipService;
import dev.spud.shadowslave.echo.content.EchoContentCatalog;
import dev.spud.shadowslave.fieldguide.FieldGuideService;
import dev.spud.shadowslave.memory.MemoryManifestationService;
import dev.spud.shadowslave.network.payload.MemoryKeyPayload;
import dev.spud.shadowslave.network.payload.MemorySummonPayload;
import dev.spud.shadowslave.network.payload.StrandCreatePayload;
import dev.spud.shadowslave.network.payload.WeaveTracePayload;
import dev.spud.shadowslave.network.payload.FieldGuidePinRequestPayload;
import dev.spud.shadowslave.network.payload.AwakenRequestPayload;
import dev.spud.shadowslave.network.payload.ChannelRequestPayload;
import dev.spud.shadowslave.network.payload.IntentionRequestPayload;
import dev.spud.shadowslave.network.payload.OpenSoulScreenRequestPayload;
import dev.spud.shadowslave.network.payload.RefineRequestPayload;
import dev.spud.shadowslave.network.payload.ToggleAshBurrowerEchoModeRequestPayload;
import dev.spud.shadowslave.network.payload.ToggleAshBurrowerEchoRequestPayload;
import dev.spud.shadowslave.network.payload.ToggleAshCompassRequestPayload;
import dev.spud.shadowslave.network.payload.UseAbilityRequestPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Handles client intent without trusting client-provided character state. */
public final class ServerPayloadHandler {
    private ServerPayloadHandler() {
    }

    public static void handleOpenSoulScreen(OpenSoulScreenRequestPayload payload, IPayloadContext context) {
        ServerPlayer player = serverPlayer(context, "Soul screen");
        if (player != null) {
            SoulSyncService.openScreen(player);
        }
    }

    public static void handleAwaken(AwakenRequestPayload payload, IPayloadContext context) {
        ServerPlayer player = serverPlayer(context, "Awakening");
        if (player != null) {
            SoulSeaService.requestAwakening(player, payload.abilityId());
        }
    }

    public static void handleChannel(ChannelRequestPayload payload, IPayloadContext context) {
        ServerPlayer player = serverPlayer(context, "Essence channeling");
        if (player != null) {
            ChannelService.channel(player, payload.strike());
        }
    }

    public static void handleRefine(RefineRequestPayload payload, IPayloadContext context) {
        ServerPlayer player = serverPlayer(context, "Refinement");
        if (player != null) {
            SoulSeaService.requestRefinement(player, payload.action());
        }
    }

    public static void handleCallingFrequency(CallingFrequencyPayload payload, IPayloadContext context) {
        ServerPlayer player = serverPlayer(context, "Calling frequency");
        if (player != null) {
            SpellCallings.setFrequency(player, CallingFrequency.byOrdinal(payload.frequency()));
        }
    }

    public static void handleTrueNameAction(TrueNameActionPayload payload, IPayloadContext context) {
        ServerPlayer player = serverPlayer(context, "True Name");
        if (player == null) {
            return;
        }
        switch (payload.action()) {
            case TrueNameActionPayload.TOGGLE_SHOWN -> TrueNames.setShown(player, !TrueNames.data(player).shown());
            case TrueNameActionPayload.SENSE -> TrueNames.sense(player, payload.name());
            default -> {
            }
        }
    }

    public static void handleFieldGuidePin(FieldGuidePinRequestPayload payload, IPayloadContext context) {
        ServerPlayer player = serverPlayer(context, "Field Guide pin");
        if (player != null) {
            FieldGuideService.requestPin(player, payload);
        }
    }

    public static void handleWeaveTrace(WeaveTracePayload payload, IPayloadContext context) {
        ServerPlayer player = serverPlayer(context, "Weaving trace");
        if (player != null) {
            dev.spud.shadowslave.weaving.WeavingService.weaveTrace(player, payload);
        }
    }

    public static void handleMemorySummon(MemorySummonPayload payload, IPayloadContext context) {
        ServerPlayer player = serverPlayer(context, "Memory summon");
        if (player != null) {
            dev.spud.shadowslave.weaving.MemoryWeave.summon(player, payload.index());
        }
    }

    public static void handleStrandCreate(StrandCreatePayload payload, IPayloadContext context) {
        ServerPlayer player = serverPlayer(context, "Strand create");
        if (player != null) {
            dev.spud.shadowslave.weaving.WeavingService.createStrand(player, payload);
        }
    }

    /** The Memory key: woven Memories first, then the Spell's own Memory. */
    public static void handleMemoryKey(MemoryKeyPayload payload, IPayloadContext context) {
        ServerPlayer player = serverPlayer(context, "Memory key");
        if (player == null || dev.spud.shadowslave.weaving.MemoryWeave.toggle(player, payload.cycle())
                != dev.spud.shadowslave.weaving.MemoryWeave.Outcome.NONE) {
            return;
        }
        MemoryManifestationService.ManifestResult result = MemoryManifestationService.summonAshCompass(player);
        if (result == MemoryManifestationService.ManifestResult.ALREADY_SUMMONED) {
            result = MemoryManifestationService.dismissAshCompass(player);
        }
        sendMemoryResult(player, result);
    }

    public static void handleIntention(IntentionRequestPayload payload, IPayloadContext context) {
        ServerPlayer player = serverPlayer(context, "Intention offering");
        if (player != null) {
            SoulSeaService.requestIntention(player, payload.longing());
        }
    }

    public static void handleUseAbility(UseAbilityRequestPayload payload, IPayloadContext context) {
        ServerPlayer player = serverPlayer(context, "Aspect ability");
        if (player != null) {
            AbilityService.useAbility(player, payload.slot());
        }
    }

    public static void handleToggleAshCompass(ToggleAshCompassRequestPayload payload, IPayloadContext context) {
        ServerPlayer player = serverPlayer(context, "Memory toggle");
        if (player == null) {
            return;
        }

        MemoryManifestationService.ManifestResult result = MemoryManifestationService.summonAshCompass(player);
        if (result == MemoryManifestationService.ManifestResult.ALREADY_SUMMONED) {
            result = MemoryManifestationService.dismissAshCompass(player);
        }
        sendMemoryResult(player, result);
    }

    public static void handleToggleAshBurrowerEcho(
            ToggleAshBurrowerEchoRequestPayload payload,
            IPayloadContext context
    ) {
        ServerPlayer player = serverPlayer(context, "Echo toggle");
        if (player == null) {
            return;
        }

        EchoManifestationService.ManifestResult result = EchoManifestationService.summonAshBurrower(player);
        if (result == EchoManifestationService.ManifestResult.ALREADY_SUMMONED) {
            result = EchoManifestationService.dismissAshBurrower(player);
        }
        sendEchoManifestResult(player, result);
    }

    public static void handleToggleAshBurrowerEchoMode(
            ToggleAshBurrowerEchoModeRequestPayload payload,
            IPayloadContext context
    ) {
        ServerPlayer player = serverPlayer(context, "Echo command toggle");
        if (player == null) {
            return;
        }

        EchoInstanceData state = EchoOwnershipService.get(player)
                .find(EchoManifestationService.ASH_BURROWER_ID)
                .orElse(null);
        if (state == null) {
            player.sendSystemMessage(Component.literal("Your soul does not contain [Ash Burrower].")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        EchoContentCatalog.CommandMode nextMode = state.commandMode() == EchoContentCatalog.CommandMode.FOLLOW
                ? EchoContentCatalog.CommandMode.HOLD
                : EchoContentCatalog.CommandMode.FOLLOW;
        EchoManifestationService.CommandResult result = EchoManifestationService.commandAshBurrower(player, nextMode);
        if (result == EchoManifestationService.CommandResult.COMMAND_SET) {
            player.sendSystemMessage(Component.literal("[Ash Burrower] — "
                            + (nextMode == EchoContentCatalog.CommandMode.FOLLOW ? "Follow." : "Hold here."))
                    .withStyle(ChatFormatting.AQUA));
        } else if (result == EchoManifestationService.CommandResult.NOT_OWNED) {
            player.sendSystemMessage(Component.literal("Your soul does not contain [Ash Burrower].")
                    .withStyle(ChatFormatting.RED));
        } else {
            player.sendSystemMessage(Component.literal("[Ash Burrower] cannot execute that command yet.")
                    .withStyle(ChatFormatting.RED));
        }
    }

    private static ServerPlayer serverPlayer(IPayloadContext context, String action) {
        if (context.player() instanceof ServerPlayer player) {
            return player;
        }
        context.disconnect(Component.literal(action + " request reached a non-server player context"));
        return null;
    }

    private static void sendMemoryResult(ServerPlayer player, MemoryManifestationService.ManifestResult result) {
        switch (result) {
            case SUMMONED -> player.sendSystemMessage(Component.literal("[Ash Compass] manifests.")
                    .withStyle(ChatFormatting.AQUA));
            case DISMISSED -> player.sendSystemMessage(Component.literal("[Ash Compass] dissolves back into your soul.")
                    .withStyle(ChatFormatting.DARK_AQUA));
            case INVENTORY_FULL -> player.sendSystemMessage(Component.literal(
                    "[Ash Compass] cannot manifest while your inventory is full.").withStyle(ChatFormatting.RED));
            case NOT_OWNED -> player.sendSystemMessage(Component.literal("Your soul does not contain [Ash Compass].")
                    .withStyle(ChatFormatting.RED));
            case NOT_SUMMONED -> player.sendSystemMessage(Component.literal("[Ash Compass] is not currently manifested.")
                    .withStyle(ChatFormatting.GRAY));
            case ALREADY_SUMMONED -> throw new IllegalStateException("Memory toggle did not resolve manifested state");
        }
    }

    private static void sendEchoManifestResult(ServerPlayer player, EchoManifestationService.ManifestResult result) {
        switch (result) {
            case SUMMONED -> player.sendSystemMessage(Component.literal("[Ash Burrower] takes shape beside you.")
                    .withStyle(ChatFormatting.AQUA));
            case DISMISSED -> player.sendSystemMessage(Component.literal(
                    "[Ash Burrower] dissolves out of the physical world.").withStyle(ChatFormatting.DARK_AQUA));
            case NOT_SUMMONED -> player.sendSystemMessage(Component.literal("[Ash Burrower] is not currently manifested.")
                    .withStyle(ChatFormatting.GRAY));
            case NOT_OWNED -> player.sendSystemMessage(Component.literal("Your soul does not contain [Ash Burrower].")
                    .withStyle(ChatFormatting.RED));
            case SPAWN_FAILED -> player.sendSystemMessage(Component.literal("[Ash Burrower] could not take physical form here.")
                    .withStyle(ChatFormatting.RED));
            case ALREADY_SUMMONED -> throw new IllegalStateException("Echo toggle did not resolve manifested state");
        }
    }
}
