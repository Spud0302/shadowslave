package dev.spud.shadowslave.item;

import dev.spud.shadowslave.dreamrealm.DreamRealmPreviewService;
import dev.spud.shadowslave.memory.MemoryOwnershipService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Physical execution adapter for the authored Ash Compass Memory. */
public final class AshCompassMemoryItem extends Item {
    public static final ResourceLocation MEMORY_ID = ResourceLocation.fromNamespaceAndPath("shadowslave", "memory/ash_compass");

    public AshCompassMemoryItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (!MemoryOwnershipService.owns(serverPlayer, MEMORY_ID)) {
            serverPlayer.sendSystemMessage(Component.literal("The imitation has no place in your soul.")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return InteractionResultHolder.fail(stack);
        }

        if (!level.dimension().equals(DreamRealmPreviewService.DREAM_REALM_LEVEL)) {
            serverPlayer.sendSystemMessage(Component.literal("Ash Compass: Cinder Rest lies beyond this realm.")
                    .withStyle(ChatFormatting.AQUA));
        } else {
            String reading = reading(serverPlayer.blockPosition(), DreamRealmPreviewService.cinderRestAnchor());
            serverPlayer.sendSystemMessage(Component.literal("Ash Compass: " + reading)
                    .withStyle(ChatFormatting.AQUA));
        }
        serverPlayer.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    static String reading(BlockPos from, BlockPos refuge) {
        int dx = refuge.getX() - from.getX();
        int dz = refuge.getZ() - from.getZ();
        int distance = (int) Math.round(Math.sqrt((double) dx * dx + (double) dz * dz));
        if (distance <= 3) {
            return "Cinder Rest is here";
        }
        return "Cinder Rest " + direction(dx, dz) + ", about " + distance + " blocks away";
    }

    static String direction(int dx, int dz) {
        double angle = Math.atan2(-dx, dz);
        int sector = Math.floorMod((int) Math.round(angle / (Math.PI / 4.0)), 8);
        return switch (sector) {
            case 0 -> "south";
            case 1 -> "southwest";
            case 2 -> "west";
            case 3 -> "northwest";
            case 4 -> "north";
            case 5 -> "northeast";
            case 6 -> "east";
            default -> "southeast";
        };
    }
}
