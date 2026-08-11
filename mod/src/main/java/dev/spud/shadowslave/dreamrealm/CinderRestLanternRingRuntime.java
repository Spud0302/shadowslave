package dev.spud.shadowslave.dreamrealm;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Replaceable NeoForge interaction adapter for the Cinder Rest lantern-ring presentation. */
public final class CinderRestLanternRingRuntime {
    private static final BlockPos PREVIEW_ORIGIN = new BlockPos(0, 160, 0);

    private CinderRestLanternRingRuntime() {}

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().dimension().equals(DreamRealmPreviewService.DREAM_REALM_LEVEL)) return;
        if (!event.getLevel().getBlockState(event.getPos()).is(Blocks.SOUL_LANTERN)) return;

        BlockPos local = event.getPos().subtract(PREVIEW_ORIGIN);
        var binding = CinderRestLanternRingBinding.cinderRest();
        if (!CinderRestLanternRingBinding.isLamp(binding, local.getX(), local.getY(), local.getZ())) return;

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        player.sendSystemMessage(Component.literal(binding.settlementName() + " — " + binding.factionName())
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("The hooded warning lamps mark the refuge line. Services: "
                        + String.join(", ", binding.serviceLabels()) + ".")
                .withStyle(ChatFormatting.YELLOW));
        player.sendSystemMessage(Component.literal(binding.standingRule()).withStyle(ChatFormatting.DARK_GRAY));
    }
}
