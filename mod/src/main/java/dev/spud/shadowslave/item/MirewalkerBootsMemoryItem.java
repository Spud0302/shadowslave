package dev.spud.shadowslave.item;

import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import dev.spud.shadowslave.memory.MemoryOwnershipService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Physical executor for the authored Mirewalker Boots Memory. */
public final class MirewalkerBootsMemoryItem extends Item {
    public static final ResourceLocation MEMORY_ID = ResourceLocation.fromNamespaceAndPath("shadowslave", "memory/mirewalker_boots");
    public static final ResourceLocation SURE_FOOT_ID = ResourceLocation.fromNamespaceAndPath("shadowslave", "memory_enchantment/sure_foot");
    static final int SURE_FOOT_DURATION_TICKS = 160;
    static final int SURE_FOOT_AMPLIFIER = 0;

    public MirewalkerBootsMemoryItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (!MemoryOwnershipService.owns(serverPlayer, MEMORY_ID)) {
            serverPlayer.sendSystemMessage(Component.literal("The imitation has no sure footing bound to your soul.").withStyle(ChatFormatting.DARK_GRAY));
            return InteractionResultHolder.fail(stack);
        }
        if (!qualifiesForSureFoot(level, serverPlayer)) {
            serverPlayer.sendSystemMessage(Component.literal("Mirewalker finds no mire or unstable footing to steady.").withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.fail(stack);
        }

        serverPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, SURE_FOOT_DURATION_TICKS, SURE_FOOT_AMPLIFIER, false, false, true));
        serverPlayer.sendSystemMessage(Component.literal("Mirewalker steadies your footing against the dragging ground.").withStyle(ChatFormatting.AQUA));
        serverPlayer.getCooldowns().addCooldown(this, 100);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    static boolean qualifiesForSureFoot(Level level, Player player) {
        if (player.isInWater()) {
            return true;
        }
        BlockPos feet = player.blockPosition();
        BlockState atFeet = level.getBlockState(feet);
        BlockState below = level.getBlockState(feet.below());
        return isDraggingTerrain(atFeet) || isDraggingTerrain(below);
    }

    static boolean isDraggingTerrain(BlockState state) {
        return state.is(Blocks.MUD) || state.is(Blocks.SOUL_SAND);
    }

    static boolean bindsExistingCatalogueDefinition() {
        MemoryContentCatalog.MemoryProfile memory = MemoryContentCatalog.waveOne().memories().stream()
                .filter(profile -> profile.id().equals(MEMORY_ID))
                .findFirst()
                .orElseThrow();
        return memory.enchantments().stream().anyMatch(enchantment -> enchantment.id().equals(SURE_FOOT_ID));
    }
}
