package dev.spud.shadowslave.item;

import dev.spud.shadowslave.memory.BorrowedDawnChargeService;
import dev.spud.shadowslave.memory.MemoryOwnershipService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Physical executor for the authored Borrowed Dawn Memory. */
public final class BorrowedDawnMemoryItem extends Item {
    public static final ResourceLocation MEMORY_ID = ResourceLocation.fromNamespaceAndPath("shadowslave", "memory/borrowed_dawn");
    public static final ResourceLocation FIRST_LIGHT_ID = ResourceLocation.fromNamespaceAndPath("shadowslave", "memory_enchantment/first_light");
    static final int CAPTURE_BRIGHTNESS = 12;
    static final float RESTORATIVE_HEAL = 4.0F;

    public BorrowedDawnMemoryItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (!MemoryOwnershipService.owns(serverPlayer, MEMORY_ID)) {
            serverPlayer.sendSystemMessage(Component.literal("The imitation holds no light for your soul.").withStyle(ChatFormatting.DARK_GRAY));
            return InteractionResultHolder.fail(stack);
        }

        if (BorrowedDawnChargeService.get(serverPlayer).charged()) {
            if (serverPlayer.getHealth() >= serverPlayer.getMaxHealth()) {
                serverPlayer.sendSystemMessage(Component.literal("Borrowed Dawn is warm, but you need no restoration.").withStyle(ChatFormatting.GOLD));
                return InteractionResultHolder.fail(stack);
            }
            serverPlayer.heal(RESTORATIVE_HEAL);
            BorrowedDawnChargeService.clear(serverPlayer);
            serverPlayer.sendSystemMessage(Component.literal("Borrowed Dawn releases its stored warmth.").withStyle(ChatFormatting.GOLD));
            serverPlayer.getCooldowns().addCooldown(this, 40);
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        int brightness = level.getMaxLocalRawBrightness(serverPlayer.blockPosition());
        if (!canCapture(brightness)) {
            serverPlayer.sendSystemMessage(Component.literal("Borrowed Dawn finds too little ambient light to hold.").withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.fail(stack);
        }
        BorrowedDawnChargeService.storeLight(serverPlayer);
        serverPlayer.sendSystemMessage(Component.literal("Borrowed Dawn gathers the surrounding light.").withStyle(ChatFormatting.YELLOW));
        serverPlayer.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    static boolean canCapture(int brightness) {
        return brightness >= CAPTURE_BRIGHTNESS && brightness <= 15;
    }
}
