package dev.spud.shadowslave.item;

import dev.spud.shadowslave.memory.BlackwaterHookAnchorData;
import dev.spud.shadowslave.memory.BlackwaterHookAnchorService;
import dev.spud.shadowslave.memory.MemoryOwnershipService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Physical execution adapter for the authored Blackwater Hook Memory. */
public final class BlackwaterHookMemoryItem extends Item {
    public static final ResourceLocation MEMORY_ID = ResourceLocation.fromNamespaceAndPath("shadowslave", "memory/blackwater_hook");
    static final double MAX_LINE_DISTANCE = 16.0;
    static final double ARRIVAL_DISTANCE = 2.0;
    static final double PULL_STRENGTH = 0.85;
    static final double MAX_VERTICAL_PULL = 0.35;

    public BlackwaterHookMemoryItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
        }
        if (!MemoryOwnershipService.owns(player, MEMORY_ID)) {
            player.sendSystemMessage(Component.literal("The imitation has no place in your soul.")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return InteractionResult.FAIL;
        }

        BlockPos anchor = context.getClickedPos();
        BlackwaterHookAnchorService.anchor(player, context.getLevel().dimension().location().toString(), anchor);
        player.sendSystemMessage(Component.literal("Blackwater Hook: undertow line fixed to "
                        + anchor.getX() + ", " + anchor.getY() + ", " + anchor.getZ() + ".")
                .withStyle(ChatFormatting.DARK_AQUA));
        player.getCooldowns().addCooldown(this, 10);
        return InteractionResult.sidedSuccess(false);
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

        if (serverPlayer.isShiftKeyDown()) {
            BlackwaterHookAnchorService.clear(serverPlayer);
            serverPlayer.sendSystemMessage(Component.literal("Blackwater Hook: undertow line released.")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        BlackwaterHookAnchorData.Anchor anchor = BlackwaterHookAnchorService.get(serverPlayer).anchor().orElse(null);
        if (anchor == null) {
            serverPlayer.sendSystemMessage(Component.literal("Blackwater Hook: fix the line to terrain first.")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.fail(stack);
        }
        if (!anchor.dimension().equals(level.dimension().location().toString())) {
            serverPlayer.sendSystemMessage(Component.literal("Blackwater Hook: the fixed line lies beyond this realm.")
                    .withStyle(ChatFormatting.DARK_AQUA));
            return InteractionResultHolder.fail(stack);
        }

        Vec3 target = Vec3.atCenterOf(BlockPos.of(anchor.blockPos()));
        Vec3 delta = target.subtract(serverPlayer.position());
        double distance = delta.length();
        if (!Double.isFinite(distance) || distance > MAX_LINE_DISTANCE) {
            serverPlayer.sendSystemMessage(Component.literal("Blackwater Hook: the undertow line is too distant to draw tight.")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.fail(stack);
        }
        if (distance <= ARRIVAL_DISTANCE) {
            serverPlayer.sendSystemMessage(Component.literal("Blackwater Hook: the line is already tight.")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        Vec3 pull = pullVector(delta);
        serverPlayer.setDeltaMovement(serverPlayer.getDeltaMovement().add(pull));
        serverPlayer.hurtMarked = true;
        serverPlayer.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    static boolean withinRange(double distance) {
        return Double.isFinite(distance) && distance > ARRIVAL_DISTANCE && distance <= MAX_LINE_DISTANCE;
    }

    static Vec3 pullVector(Vec3 delta) {
        double length = delta.length();
        if (!Double.isFinite(length) || length <= 0.0) {
            return Vec3.ZERO;
        }
        Vec3 normalized = delta.scale(1.0 / length);
        return new Vec3(
                normalized.x * PULL_STRENGTH,
                Math.max(-MAX_VERTICAL_PULL, Math.min(MAX_VERTICAL_PULL, normalized.y * PULL_STRENGTH)),
                normalized.z * PULL_STRENGTH
        );
    }
}
