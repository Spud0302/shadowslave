package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.world.block.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
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
        if (!event.getLevel().dimension().equals(DreamRealmPreviewService.DREAM_REALM_LEVEL)) return;

        BlockPos local = event.getPos().subtract(PREVIEW_ORIGIN);
        var interaction = DreamRealmResourceInteractionBinding.ashenExpanseResources().stream()
                .filter(candidate -> DreamRealmResourceInteractionBinding.isPhysicalClusterOffset(
                        candidate, local.getX(), local.getY(), local.getZ()))
                .findFirst()
                .orElse(null);
        if (interaction == null) return;
        if (!event.getLevel().getBlockState(event.getPos()).is(expectedPhysicalBlock(interaction.physicalBlockId()))) return;

        // Consume the same bounded interaction on both logical sides so vanilla does not
        // fall through to off-hand/item use. Presentation remains server-only.
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        player.sendSystemMessage(Component.literal("[Ashen Expanse] " + interaction.inspection())
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal(interaction.boundary()).withStyle(ChatFormatting.DARK_GRAY));
    }

    private static Block expectedPhysicalBlock(String physicalBlockId) {
        return switch (physicalBlockId) {
            case "minecraft:bone_block" -> Blocks.BONE_BLOCK;
            case "shadowslave:ruin_metal" -> ModBlocks.RUIN_METAL.get();
            case "minecraft:brown_mushroom_block" -> Blocks.BROWN_MUSHROOM_BLOCK;
            default -> throw new IllegalStateException("Unmapped Dream Realm resource presentation block " + physicalBlockId);
        };
    }
}
