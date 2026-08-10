package dev.spud.shadowslave.dreamrealm;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
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

        BlockPos local = event.getPos().subtract(PREVIEW_ORIGIN);
        var interaction = DreamRealmResourceInteractionBinding.ashenExpanseResources().stream()
                .filter(candidate -> DreamRealmResourceInteractionBinding.isPhysicalClusterOffset(
                        candidate, local.getX(), local.getY(), local.getZ()))
                .findFirst()
                .orElse(null);
        if (interaction == null) return;
        if (!event.getLevel().getBlockState(event.getPos()).is(expectedPlaceholder(interaction.placeholderBlockId()))) return;

        player.sendSystemMessage(Component.literal("[Ashen Expanse] " + interaction.inspection())
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal(interaction.boundary()).withStyle(ChatFormatting.DARK_GRAY));
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static Block expectedPlaceholder(String placeholderBlockId) {
        return switch (placeholderBlockId) {
            case "bone_block" -> Blocks.BONE_BLOCK;
            case "raw_iron_block" -> Blocks.RAW_IRON_BLOCK;
            case "brown_mushroom_block" -> Blocks.BROWN_MUSHROOM_BLOCK;
            default -> throw new IllegalStateException("Unmapped Dream Realm resource placeholder " + placeholderBlockId);
        };
    }
}
