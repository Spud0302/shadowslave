package dev.spud.shadowslave.nightmare;

import dev.spud.shadowslave.appraisal.GeneratedAppraisalRecoveryService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** NeoForge event bridge; all actual state changes route through Java-owned services. */
public final class NightmareEvents {
    private NightmareEvents() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (NightmareService.resolveScenarioInteraction(player, event.getPos())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && player.serverLevel().dimension().equals(NightmareService.NIGHTMARE_LEVEL)
                && NightmareService.activeFor(player).isPresent()) {
            NightmareService.canonicalDeath(player);
        }
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // A durable successful-completion receipt outranks ordinary active-instance reconciliation.
        // Replay is exact and idempotent; contradictory player state fails closed while retaining the receipt.
        if (GeneratedAppraisalRecoveryService.replayPending(player)) {
            return;
        }

        NightmareService.activeFor(player).ifPresent(instance -> {
            if (player.serverLevel().dimension().equals(NightmareService.NIGHTMARE_LEVEL)) {
                player.sendSystemMessage(Component.literal(NightmareService.resumeHint(instance))
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            } else {
                NightmareService.technicalRecover(player);
            }
        });
    }
}
