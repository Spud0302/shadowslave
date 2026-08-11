package dev.spud.shadowslave.item;

import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
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
import net.minecraft.world.phys.Vec3;

/** Physical executor for the authored Stonewake Shield Memory. */
public final class StonewakeShieldMemoryItem extends Item {
    public static final ResourceLocation MEMORY_ID = ResourceLocation.fromNamespaceAndPath("shadowslave", "memory/stonewake_shield");
    public static final ResourceLocation SETTLE_ID = ResourceLocation.fromNamespaceAndPath("shadowslave", "memory_enchantment/settle");
    static final double MIN_HORIZONTAL_FORCE = 0.08D;
    static final double SETTLED_HORIZONTAL_MULTIPLIER = 0.25D;

    public StonewakeShieldMemoryItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (!MemoryOwnershipService.owns(serverPlayer, MEMORY_ID)) {
            serverPlayer.sendSystemMessage(Component.literal("The imitation carries no Stonewake weight bound to your soul.").withStyle(ChatFormatting.DARK_GRAY));
            return InteractionResultHolder.fail(stack);
        }
        if (!serverPlayer.isCrouching()) {
            serverPlayer.sendSystemMessage(Component.literal("Brace low before asking Stonewake to settle.").withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.fail(stack);
        }

        Vec3 movement = serverPlayer.getDeltaMovement();
        if (!canSettle(movement.x, movement.z)) {
            serverPlayer.sendSystemMessage(Component.literal("Stonewake finds no incoming force to settle against.").withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.fail(stack);
        }

        serverPlayer.setDeltaMovement(settledVelocity(movement.x), movement.y, settledVelocity(movement.z));
        serverPlayer.sendSystemMessage(Component.literal("Stonewake settles your stance and bleeds the shove into the ground.").withStyle(ChatFormatting.AQUA));
        serverPlayer.getCooldowns().addCooldown(this, 40);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    static boolean canSettle(double x, double z) {
        if (!Double.isFinite(x) || !Double.isFinite(z)) return false;
        return Math.hypot(x, z) >= MIN_HORIZONTAL_FORCE;
    }

    static double settledVelocity(double component) {
        if (!Double.isFinite(component)) throw new IllegalArgumentException("movement component must be finite");
        return component * SETTLED_HORIZONTAL_MULTIPLIER;
    }

    static boolean bindsExistingCatalogueDefinition() {
        MemoryContentCatalog.MemoryProfile memory = MemoryContentCatalog.waveOne().memories().stream()
                .filter(profile -> profile.id().equals(MEMORY_ID))
                .findFirst()
                .orElseThrow();
        return memory.enchantments().stream().anyMatch(enchantment -> enchantment.id().equals(SETTLE_ID));
    }
}
