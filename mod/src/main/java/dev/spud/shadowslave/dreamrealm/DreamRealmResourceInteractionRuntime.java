package dev.spud.shadowslave.dreamrealm;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Replaceable NeoForge executor for bounded Dream Realm resource interactions. */
public final class DreamRealmResourceInteractionRuntime {
    // Mirrors the development slice anchor in DreamRealmPreviewService. Kept local so
    // the pure Java binding does not depend on Minecraft classes.
    private static final BlockPos PREVIEW_ORIGIN = new BlockPos(0, 160, 0);

    private DreamRealmResourceInteractionRuntime() {}

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ServerPlayer player)) return;
        if (!DreamRealmPreviewService.isInside(player)) return;

        var interaction = DreamRealmResourceInteractionBinding.ashenExpanseRuinMetal();
        BlockPos local = event.getPos().subtract(PREVIEW_ORIGIN);
        if (!DreamRealmResourceInteractionBinding.isPhysicalClusterOffset(
                interaction, local.getX(), local.getY(), local.getZ())) return;
        if (!event.getLevel().getBlockState(event.getPos()).is(Blocks.RAW_IRON_BLOCK)) return;

        player.sendSystemMessage(Component.literal("[Ashen Expanse] " + interaction.inspection())
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal(interaction.boundary()).withStyle(ChatFormatting.DARK_GRAY));
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
