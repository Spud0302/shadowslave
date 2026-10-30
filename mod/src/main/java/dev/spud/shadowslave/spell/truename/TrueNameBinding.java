package dev.spud.shadowslave.spell.truename;

import dev.spud.shadowslave.ability.AbilityService;
import dev.spud.shadowslave.ability.EssenceService;
import dev.spud.shadowslave.weaving.BoundTo;
import dev.spud.shadowslave.weaving.MemoryWeave;
import dev.spud.shadowslave.weaving.WeavingRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * True-Name Binding (ch. 696): a True Name woven into a Memory is the beacon
 * that ties it to its owner's soul. A bearer binds any Memory they hold this
 * way, with no need for the Bound pattern or the Binding branch: sneak and use
 * a Weaving Table with the Memory in hand. It then answers only to them, as a
 * Bound Memory does.
 */
public final class TrueNameBinding {
    public enum Outcome { BOUND, NO_NAME, NOT_A_MEMORY, ALREADY_BOUND, NO_ESSENCE }

    private TrueNameBinding() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isShiftKeyDown() || event.getHand() != InteractionHand.MAIN_HAND
                || !player.level().getBlockState(event.getPos()).is(WeavingRegistry.WEAVING_TABLE.get())
                || !MemoryWeave.isMemory(player.getMainHandItem())) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        bind(player, player.getMainHandItem());
    }

    /** Weaves the player's True Name into the Memory, if it can be. */
    public static Outcome bind(ServerPlayer player, ItemStack stack) {
        TrueNameData data = TrueNames.data(player);
        if (!data.named()) {
            return tell(player, Outcome.NO_NAME, "You have no True Name to weave into it.");
        }
        if (!MemoryWeave.isMemory(stack)) {
            return tell(player, Outcome.NOT_A_MEMORY, "Only a Memory can hold a True Name.");
        }
        if (MemoryWeave.isBound(stack)) {
            return tell(player, Outcome.ALREADY_BOUND, stack.getHoverName().getString() + " is Bound already.");
        }
        int cost = EssenceService.capacity(player) > 0 ? TrueNames.settings().bindEssence() : 0;
        if (cost > 0 && !EssenceService.covers(player, cost)) {
            return tell(player, Outcome.NO_ESSENCE, "Weaving your True Name into it takes " + cost + " essence.");
        }
        if (cost > 0) {
            EssenceService.spend(player, cost);
            AbilityService.syncStatus(player);
        }
        stack.set(WeavingRegistry.NAME_WOVEN.get(), Unit.INSTANCE);
        stack.set(WeavingRegistry.BOUND_TO.get(), new BoundTo(player.getUUID(), player.getGameProfile().getName()));
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 0.6F);
        player.serverLevel().sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.2, player.getZ(), 40, 0.4, 0.5, 0.4, 0.6);
        return tell(player, Outcome.BOUND, "You weave your True Name into " + stack.getHoverName().getString()
                + ". It answers only to you now.");
    }

    private static Outcome tell(ServerPlayer player, Outcome outcome, String message) {
        player.displayClientMessage(Component.literal(message).withStyle(outcome == Outcome.BOUND ? ChatFormatting.GOLD : ChatFormatting.RED), true);
        return outcome;
    }
}
