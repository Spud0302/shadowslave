package dev.spud.shadowslave.nightmare;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** NeoForge event bridge; all actual state changes route through NightmareService. */
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
            NightmareService.canonicalDeath(player);
        }
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        boolean completionPresent = NightmareService.successfulCompletionFor(player).isPresent();
        boolean activePresent = NightmareService.activeFor(player).isPresent();
        boolean playerInNightmare = player.serverLevel().dimension().equals(NightmareService.NIGHTMARE_LEVEL);

        switch (NightmareLoginRecoveryPolicy.select(completionPresent, activePresent, playerInNightmare)) {
            case SUCCESSFUL_COMPLETION -> {
                if (!NightmareService.resumeSuccessfulCompletion(player)) {
                    throw new IllegalStateException(
                            "Successful Nightmare receipt disappeared after login recovery selected it"
                    );
                }
            }
            case ACTIVE_IN_NIGHTMARE -> player.sendSystemMessage(Component.literal(
                    "Active First Nightmare restored: The Last Signal. Reach and right-click the unlit soul campfire."
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
            case TECHNICAL_RECOVERY -> NightmareService.technicalRecover(player);
            case NONE -> {
                // No Nightmare-owned recovery state exists for this player.
            }
        }
    }
}
