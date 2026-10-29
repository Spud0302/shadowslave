package dev.spud.shadowslave.weaving;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.EssenceService;
import dev.spud.shadowslave.soul.SoulService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Iterator;
import java.util.Optional;

/**
 * Memories made by Weaving (DESIGN, after ch. 3024): a piece that holds the
 * Memory weave can be dismissed into its holder's soul and summoned back,
 * mends while dismissed, and stays with its holder through death. Any
 * Memory can be handed to another player; a Bound one answers only to the
 * player it bonded with.
 */
public final class MemoryWeave {
    public static final ResourceLocation MEMORY_WEAVE = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "memory_weave");
    public static final ResourceLocation BOUND = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "bound");
    /** How often a player's hands and armour are checked for Bound pieces. */
    static final int BOND_CHECK_TICKS = 10;

    private static final EquipmentSlot[] HELD = {
            EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private MemoryWeave() {
    }

    public static boolean isMemory(ItemStack stack) {
        return !stack.isEmpty() && StrandService.holds(WeavingContent.current(), stack, StrandPattern.Kind.MEMORY);
    }

    public static boolean isBound(ItemStack stack) {
        return !stack.isEmpty() && StrandService.holds(WeavingContent.current(), stack, StrandPattern.Kind.BOUND);
    }

    /** Whether {@code player} may wield or wear the piece: anyone, unless it is Bound to someone else. */
    public static boolean answers(ItemStack stack, ServerPlayer player) {
        BoundTo bond = stack.get(WeavingRegistry.BOUND_TO.get());
        return bond == null || bond.owner().equals(player.getUUID()) || !isBound(stack);
    }

    public static SoulMemories held(ServerPlayer player) {
        return player.getData(WeavingRegistry.SOUL_MEMORIES.get());
    }

    static void setHeld(ServerPlayer player, SoulMemories memories) {
        player.setData(WeavingRegistry.SOUL_MEMORIES.get(), memories);
    }

    public static int capacity(ServerPlayer player) {
        return StrandRules.soulCapacity(SoulService.get(player).soulRank(), perk(player, WeavingBranch.PerkKind.SOUL_CAPACITY));
    }

    static int perk(ServerPlayer player, WeavingBranch.PerkKind kind) {
        return WeavingRules.perk(WeavingContent.current(), WeavingService.get(player), kind);
    }

    /** What summoning a Memory costs this player: nothing to a soul with no essence core yet, else a little essence. */
    public static int summonCost(ServerPlayer player) {
        return EssenceService.capacity(player) > 0
                ? StrandRules.summonCost(StrandService.essencePercent(player), perk(player, WeavingBranch.PerkKind.SUMMON_DISCOUNT)) : 0;
    }

    // The Memory key

    public enum Outcome { DISMISSED, SUMMONED, CYCLED, REFUSED, NONE }

    /**
     * The Memory key: dismisses the Memory in the main hand, or else summons
     * the one dismissed last. With {@code cycle} (sneaking) it instead turns
     * the soul's line of Memories so a different one comes next. Returns
     * {@link Outcome#NONE} when there was nothing for Weaving to do, so the
     * key can fall through to the Spell's own Memories.
     */
    public static Outcome toggle(ServerPlayer player, boolean cycle) {
        SoulMemories memories = held(player);
        if (cycle) {
            if (memories.size() < 2) {
                return memories.isEmpty() ? Outcome.NONE : tell(player, Outcome.CYCLED,
                        name(memories.next()) + " is the only Memory in your soul.");
            }
            SoulMemories turned = memories.cycled();
            setHeld(player, turned);
            return tell(player, Outcome.CYCLED, "Next to answer: " + name(turned.next()) + ".");
        }
        ItemStack hand = player.getMainHandItem();
        if (isMemory(hand)) {
            return dismiss(player, hand);
        }
        if (!memories.isEmpty()) {
            return summon(player);
        }
        return Outcome.NONE;
    }

    static Outcome dismiss(ServerPlayer player, ItemStack hand) {
        if (!answers(hand, player)) {
            return tell(player, Outcome.REFUSED, "It is Bound to someone else and won't sink into your soul.");
        }
        SoulMemories memories = held(player);
        if (memories.size() >= capacity(player)) {
            return tell(player, Outcome.REFUSED, "Your soul holds " + memories.size() + " Memories already. A higher Rank holds more.");
        }
        ItemStack dismissed = hand.copy();
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        return sink(player, dismissed, " dissolves into your soul");
    }

    /** Takes a Memory into the soul, counts it toward any deed for holding many, and tells the player. */
    private static Outcome sink(ServerPlayer player, ItemStack stack, String how) {
        SoulMemories memories = held(player).with(stack);
        setHeld(player, memories);
        java.util.List<String> deeds = new java.util.ArrayList<>();
        WeavingData data = WeavingRules.holdDeeds(WeavingContent.current(), WeavingService.get(player), memories.size(), deeds);
        if (!deeds.isEmpty()) {
            WeavingService.set(player, data);
            deeds.forEach(deed -> player.sendSystemMessage(Component.literal("Deed done. " + deed)
                    .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.8F, 1.4F);
        WeavingService.sync(player);
        return tell(player, Outcome.DISMISSED, name(stack) + how + " (" + memories.size() + "/" + capacity(player) + ").");
    }

    static Outcome summon(ServerPlayer player) {
        return summon(player, held(player).size() - 1);
    }

    /** Summons the Memory at {@code index} in the soul's line, as the Soul screen's Memories page asks. */
    public static Outcome summon(ServerPlayer player, int index) {
        SoulMemories memories = held(player);
        if (index < 0 || index >= memories.size()) {
            return Outcome.NONE;
        }
        ItemStack next = memories.held().get(index);
        if (!player.getMainHandItem().isEmpty() && player.getInventory().getFreeSlot() < 0) {
            return tell(player, Outcome.REFUSED, "Your hands and pack are full.");
        }
        // A soul with no essence core yet calls its Memories for nothing; anyone else rebuilds them from essence.
        int cost = summonCost(player);
        if (cost > 0 && !EssenceService.covers(player, cost)) {
            return tell(player, Outcome.REFUSED, "Summoning a Memory takes " + cost + " essence.");
        }
        if (cost > 0) {
            EssenceService.spend(player, cost);
        }
        setHeld(player, memories.without(index));
        WeavingService.sync(player);
        ItemStack stack = next.copy();
        if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        } else {
            player.getInventory().add(stack);
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.1F);
        return tell(player, Outcome.SUMMONED, name(next) + " takes shape in your hand.");
    }

    private static Outcome tell(ServerPlayer player, Outcome outcome, String message) {
        player.displayClientMessage(Component.literal(message).withStyle(outcome == Outcome.REFUSED ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE), true);
        return outcome;
    }

    private static String name(ItemStack stack) {
        return stack.getHoverName().getString();
    }

    // Mending, bonds and death

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        long time = player.level().getGameTime();
        if (time % StrandRules.MEND_TICKS == 0) {
            mend(player);
        }
        if (time % BOND_CHECK_TICKS == 0) {
            checkBonds(player);
        }
    }

    /**
     * Each dismissed Memory that is worn mends a little (lore: Memories repair
     * themselves while dismissed). A soul with an essence core pays one
     * essence for each mend and mends nothing while empty.
     */
    static void mend(ServerPlayer player) {
        SoulMemories memories = held(player);
        boolean paying = EssenceService.capacity(player) > 0;
        int bonus = perk(player, WeavingBranch.PerkKind.MEND);
        boolean changed = false;
        for (ItemStack stack : memories.held()) {
            if (!stack.isDamaged()) {
                continue;
            }
            if (paying) {
                if (!EssenceService.covers(player, 1)) {
                    break;
                }
                EssenceService.spend(player, 1);
            }
            stack.setDamageValue(Math.max(0, stack.getDamageValue() - StrandRules.mend(stack.getMaxDamage(), bonus)));
            changed = true;
        }
        if (changed) {
            setHeld(player, new SoulMemories(memories.held()));
            WeavingService.sync(player);
        }
    }

    /** A Bound piece bonds to the first player to hold or wear it, and slips from anyone else's hands. */
    static void checkBonds(ServerPlayer player) {
        for (EquipmentSlot slot : HELD) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!isBound(stack)) {
                continue;
            }
            BoundTo bond = stack.get(WeavingRegistry.BOUND_TO.get());
            if (bond == null) {
                stack.set(WeavingRegistry.BOUND_TO.get(), new BoundTo(player.getUUID(), player.getGameProfile().getName()));
                player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 0.7F);
                tell(player, Outcome.SUMMONED, name(stack) + " bonds to your soul. It answers only to you now.");
            } else if (!bond.owner().equals(player.getUUID())) {
                ItemStack loose = stack.copy();
                player.setItemSlot(slot, ItemStack.EMPTY);
                if (!stowAway(player, loose)) {
                    player.drop(loose, false);
                }
                tell(player, Outcome.REFUSED, name(loose) + " won't answer your hand. It is Bound to " + bond.name() + ".");
            }
        }
    }

    /** Puts a piece into the pack, outside the hotbar's selected slot, if there is room. */
    private static boolean stowAway(ServerPlayer player, ItemStack stack) {
        var inventory = player.getInventory();
        for (int slot = 9; slot < 36; slot++) {
            if (inventory.getItem(slot).isEmpty()) {
                inventory.setItem(slot, stack);
                return true;
            }
        }
        return false;
    }

    /**
     * When a player dies, the Memories they carried sink into their soul
     * instead of falling (lore: Memories return to the soul). A Memory Bound to
     * someone else falls as usual. The soul keeps them even past its
     * capacity, so nothing is lost; it just can't take more until some are
     * summoned.
     */
    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        SoulMemories memories = held(player);
        int kept = 0;
        for (Iterator<ItemEntity> drops = event.getDrops().iterator(); drops.hasNext(); ) {
            ItemStack stack = drops.next().getItem();
            if (isMemory(stack) && answers(stack, player)) {
                memories = memories.with(stack);
                drops.remove();
                kept++;
            }
        }
        if (kept > 0) {
            setHeld(player, memories);
            player.sendSystemMessage(Component.literal(kept == 1 ? "A Memory sinks into your soul as you fall."
                    : kept + " Memories sink into your soul as you fall.").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    // Passing Memories on

    /**
     * A Memory is never dropped on the ground (lore: Memories pass hand to
     * hand, ch. 54). Tossing one sends it back into the soul, or into the
     * pack if the soul is full; only a Memory Bound to someone else falls.
     */
    public static void onToss(ItemTossEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack stack = event.getEntity().getItem();
        if (!isMemory(stack) || !answers(stack, player)) {
            return;
        }
        if (held(player).size() < capacity(player)) {
            event.setCanceled(true);
            sink(player, stack.copy(), " won't touch the ground and sinks back into your soul");
        } else if (player.getInventory().add(stack.copy())) {
            event.setCanceled(true);
            tell(player, Outcome.REFUSED, "A Memory won't lie on the ground. Hand it over to another player instead.");
        }
    }

    /**
     * Handing a Memory over (lore: it takes touch, ch. 54): sneak and use
     * another player while holding it, and it passes to their hands. A Memory
     * Bound to you would not answer them, so it stays.
     */
    public static void onInteractPlayer(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer giver) || !(event.getTarget() instanceof ServerPlayer taker)
                || !giver.isShiftKeyDown() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        ItemStack hand = giver.getMainHandItem();
        if (!isMemory(hand)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        if (isBound(hand) && hand.has(WeavingRegistry.BOUND_TO.get())) {
            tell(giver, Outcome.REFUSED, name(hand) + " is Bound to " + bond(hand).map(BoundTo::name).orElse("someone")
                    + " and would not answer " + taker.getGameProfile().getName() + ".");
            return;
        }
        ItemStack given = hand.copy();
        if (!taker.getInventory().add(given)) {
            tell(giver, Outcome.REFUSED, taker.getGameProfile().getName() + "'s hands and pack are full.");
            return;
        }
        giver.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        giver.level().playSound(null, taker.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.3F);
        tell(giver, Outcome.SUMMONED, "You hand " + name(given) + " to " + taker.getGameProfile().getName() + ".");
        tell(taker, Outcome.SUMMONED, giver.getGameProfile().getName() + " hands you " + name(given) + ".");
    }

    /** What a Memory reads as for the tooltip: empty for a piece that is not one. */
    public static Optional<BoundTo> bond(ItemStack stack) {
        return Optional.ofNullable(stack.get(WeavingRegistry.BOUND_TO.get()));
    }
}
