package dev.spud.shadowslave.item;

import dev.spud.shadowslave.memory.MemoryOwnershipService;
import dev.spud.shadowslave.memory.RedThreadCompanionData;
import dev.spud.shadowslave.memory.RedThreadCompanionService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Locale;
import java.util.UUID;

/** Physical execution adapter for Red Thread Bracelet's authored tethered_pulse enchantment. */
public final class RedThreadBraceletMemoryItem extends Item {
    public static final ResourceLocation MEMORY_ID = ResourceLocation.fromNamespaceAndPath("shadowslave", "memory/red_thread_bracelet");
    static final double TETHER_RANGE = 128.0D;

    public RedThreadBraceletMemoryItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.sidedSuccess(player.level().isClientSide());
        }
        if (!MemoryOwnershipService.owns(serverPlayer, MEMORY_ID)) {
            serverPlayer.sendSystemMessage(Component.literal("The imitation has no place in your soul.")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return InteractionResult.FAIL;
        }
        if (!(target instanceof ServerPlayer companion) || companion.getUUID().equals(serverPlayer.getUUID())) {
            return InteractionResult.PASS;
        }

        RedThreadCompanionService.mark(serverPlayer, companion.getUUID());
        serverPlayer.sendSystemMessage(Component.literal("Red Thread Bracelet: " + companion.getGameProfile().getName() + " is marked by the thread.")
                .withStyle(ChatFormatting.RED));
        serverPlayer.getCooldowns().addCooldown(this, 10);
        return InteractionResult.SUCCESS;
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

        RedThreadCompanionData data = RedThreadCompanionService.get(serverPlayer);
        if (!data.hasCompanion()) {
            serverPlayer.sendSystemMessage(Component.literal("Red Thread Bracelet: no companion is marked.")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        UUID companionId = data.companionId().orElseThrow();
        ServerPlayer companion = serverPlayer.serverLevel().getServer().getPlayerList().getPlayer(companionId);
        if (companion == null) {
            serverPlayer.sendSystemMessage(Component.literal("Red Thread Bracelet: the thread has no answer.")
                    .withStyle(ChatFormatting.GRAY));
        } else if (companion.level().dimension() != serverPlayer.level().dimension()) {
            serverPlayer.sendSystemMessage(Component.literal("Red Thread Bracelet: the marked companion lies beyond this realm.")
                    .withStyle(ChatFormatting.DARK_RED));
        } else {
            double distanceSqr = serverPlayer.distanceToSqr(companion);
            if (!withinRangeSqr(distanceSqr)) {
                serverPlayer.sendSystemMessage(Component.literal("Red Thread Bracelet: the thread is too faint to read.")
                        .withStyle(ChatFormatting.GRAY));
            } else {
                double dx = companion.getX() - serverPlayer.getX();
                double dz = companion.getZ() - serverPlayer.getZ();
                int approximateDistance = (int) Math.round(Math.sqrt(distanceSqr));
                serverPlayer.sendSystemMessage(Component.literal("Red Thread Bracelet: the thread pulls "
                                + direction(dx, dz) + " (~" + approximateDistance + " blocks).")
                        .withStyle(ChatFormatting.RED));
            }
        }

        serverPlayer.getCooldowns().addCooldown(this, 10);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    static boolean withinRangeSqr(double distanceSqr) {
        return distanceSqr >= 0.0D && distanceSqr <= TETHER_RANGE * TETHER_RANGE;
    }

    static String direction(double dx, double dz) {
        if (!Double.isFinite(dx) || !Double.isFinite(dz)) {
            throw new IllegalArgumentException("direction components must be finite");
        }
        if (Math.abs(dx) < 0.5D && Math.abs(dz) < 0.5D) return "here";

        double absX = Math.abs(dx);
        double absZ = Math.abs(dz);
        if (absX > absZ * 2.0D) return dx > 0.0D ? "east" : "west";
        if (absZ > absX * 2.0D) return dz > 0.0D ? "south" : "north";

        String northSouth = dz > 0.0D ? "south" : "north";
        String eastWest = dx > 0.0D ? "east" : "west";
        return (northSouth + "-" + eastWest).toLowerCase(Locale.ROOT);
    }
}
