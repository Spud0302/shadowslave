package dev.spud.shadowslave.nightmare;

import dev.spud.shadowslave.preview.PreviewResetService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** NeoForge event bridge; all actual state changes route through Nightmare services. */
public final class NightmareEvents {
    private NightmareEvents() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (NightmareService.resolveSignalFire(player, event.getPos())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && player.serverLevel().dimension().equals(NightmareService.NIGHTMARE_LEVEL)
                && NightmareService.activeFor(player).isPresent()) {
            NightmareDeathService.record(player);
        }
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (NightmareDeathService.resumePending(player)) {
            return;
        }

        if (PreviewResetService.resumePending(player)) {
            return;
        }

        if (NightmareService.resumeTechnicalExit(player)) {
            return;
        }

        if (NightmareService.resumeSuccessfulCompletion(player)) {
            return;
        }

        NightmareService.activeFor(player).ifPresent(instance -> {
            if (player.serverLevel().dimension().equals(NightmareService.NIGHTMARE_LEVEL)) {
                player.sendSystemMessage(Component.literal(
                        "Active First Nightmare restored: The Last Signal. Reach and right-click the unlit soul campfire."
                ).withStyle(ChatFormatting.LIGHT_PURPLE));
            } else {
                NightmareService.technicalRecover(player);
            }
        });
    }
}
