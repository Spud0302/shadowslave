package dev.spud.shadowslave.item;

import dev.spud.shadowslave.memory.MemoryOwnershipService;
import dev.spud.shadowslave.world.entity.AshBurrowerEntity;
import dev.spud.shadowslave.world.entity.ChainbackEntity;
import dev.spud.shadowslave.world.entity.DrownedListenerEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/** Physical execution adapter for the authored Bellglass Token Memory. */
public final class BellglassTokenMemoryItem extends Item {
    public static final ResourceLocation MEMORY_ID = ResourceLocation.fromNamespaceAndPath("shadowslave", "memory/bellglass_token");
    static final double WARNING_RANGE = 10.0D;

    public BellglassTokenMemoryItem(Properties properties) {
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

        LivingEntity hiddenMovement = nearestHiddenMovement(level, serverPlayer);
        if (hiddenMovement == null) {
            serverPlayer.sendSystemMessage(Component.literal("Bellglass Token: the glass remains still.")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            level.playSound(null, serverPlayer.blockPosition(), SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 0.7F, 1.6F);
            serverPlayer.sendSystemMessage(Component.literal("Bellglass Token: a clear vibration answers hidden movement nearby.")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        serverPlayer.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    static LivingEntity nearestHiddenMovement(Level level, ServerPlayer player) {
        AABB area = player.getBoundingBox().inflate(WARNING_RANGE);
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, area, entity ->
                entity.isAlive()
                        && isBoundThreat(entity)
                        && isMoving(entity)
                        && !player.hasLineOfSight(entity));
        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : candidates) {
            double distance = player.distanceToSqr(candidate);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = candidate;
            }
        }
        return nearest;
    }

    static boolean isBoundThreat(LivingEntity entity) {
        return entity instanceof AshBurrowerEntity
                || entity instanceof ChainbackEntity
                || entity instanceof DrownedListenerEntity;
    }

    static boolean isMoving(LivingEntity entity) {
        return entity.getDeltaMovement().lengthSqr() > 0.0025D;
    }
}