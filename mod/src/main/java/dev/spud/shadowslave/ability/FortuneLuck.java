package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The always-on halves of Fortune and the luck its abilities lay down:
 * Favoured (a point of Luck, a chance at an extra drop from what you kill and
 * at an extra boon from a Nightmare), the Coin of Fate's next hit, Twist Fate's
 * ward, Borrowed Luck's marks, and Ill Omen, the mishap that follows a lucky
 * streak.
 */
public final class FortuneLuck {
    static final ResourceLocation LUCK_ID = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "fortune_favoured");
    /** Favoured: one point of vanilla Luck, which loot tables, chests and fishing already read. */
    static final double LUCK = 1.0;
    /** Favoured: each stack a Fortune player's kill drops has this chance to drop one more. */
    static final float EXTRA_DROP_CHANCE = 0.2F;
    /** Favoured: the chance a Nightmare's boons come with one more, drawn from a second roll. */
    static final float EXTRA_BOON_CHANCE = 0.25F;
    static final float COIN_HEADS = 2.0F;
    static final float COIN_TAILS = 0.5F;
    static final float TWIST_MISS_CHANCE = 0.5F;
    /** Ill Omen: this many lucky breaks within {@link #STREAK_TICKS} and a mishap follows. */
    static final int STREAK = 3;
    static final long STREAK_TICKS = 20 * 120;
    static final int MISHAP_TICKS = 20 * 5;
    static final int MISHAP_ESSENCE = 15;

    /** Armed coins, Twist Fate wards and Borrowed Luck marks: each until a game time. */
    private static final Map<UUID, Long> COINS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> TWISTS = new ConcurrentHashMap<>();
    /** Marked creature to the Fortune player who marked it and when the mark fades. */
    private static final Map<UUID, Mark> MARKS = new ConcurrentHashMap<>();
    private static final Map<UUID, Deque<Long>> STREAKS = new ConcurrentHashMap<>();
    /** Tests can fix the coin and the odds; null leaves them to chance. */
    static Boolean forcedLuck;

    record Mark(UUID owner, long until) {
    }

    private FortuneLuck() {
    }

    static boolean favoured(ServerPlayer player) {
        return KitRules.hasInnate(player, AspectKitCatalog.FORTUNE.innateId());
    }

    static boolean omened(ServerPlayer player) {
        return KitRules.hasFlaw(player, AspectKitCatalog.FORTUNE.flawEffectId());
    }

    private static boolean chance(RandomSource random, float odds) {
        return forcedLuck != null ? forcedLuck : random.nextFloat() < odds;
    }

    // ---- Favoured --------------------------------------------------------------------------------------------------

    /** Keeps the Luck point on Favoured players and off everyone else, once a second. */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 20 == 0) {
            applyLuck(player);
        }
    }

    static void applyLuck(ServerPlayer player) {
        AttributeInstance luck = player.getAttribute(Attributes.LUCK);
        if (luck == null) {
            return;
        }
        if (favoured(player)) {
            luck.addOrUpdateTransientModifier(new AttributeModifier(LUCK_ID, LUCK, AttributeModifier.Operation.ADD_VALUE));
        } else if (luck.hasModifier(LUCK_ID)) {
            luck.removeModifier(LUCK_ID);
        }
    }

    /** What a creature drops: a Fortune player's kill may drop a little more, and a Borrowed Luck mark doubles it all. */
    public static void onDrops(LivingDropsEvent event) {
        LivingEntity dead = event.getEntity();
        if (!(dead.level() instanceof ServerLevel level) || dead instanceof Player) {
            return;
        }
        Mark mark = MARKS.remove(dead.getUUID());
        Optional<ServerPlayer> marker = Optional.ofNullable(mark).filter(found -> found.until() > level.getGameTime())
                .map(found -> level.getServer().getPlayerList().getPlayer(found.owner()));
        if (marker.isPresent()) {
            for (ItemEntity drop : event.getDrops()) {
                ItemStack stack = drop.getItem();
                stack.setCount(Math.min(stack.getMaxStackSize(), stack.getCount() * 2));
                drop.setItem(stack);
            }
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, dead.getX(), dead.getY() + 0.5, dead.getZ(), 20, 0.5, 0.5, 0.5, 0.1);
            level.playSound(null, dead.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.6F);
            AbilityService.feedback(marker.get(), "Borrowed Luck pays out.", ChatFormatting.GOLD);
            lucky(marker.get());
        }
        if (event.getSource().getEntity() instanceof ServerPlayer killer && favoured(killer)) {
            boolean any = false;
            for (ItemEntity drop : event.getDrops()) {
                ItemStack stack = drop.getItem();
                if (stack.getCount() < stack.getMaxStackSize() && chance(level.random, EXTRA_DROP_CHANCE)) {
                    stack.grow(1);
                    drop.setItem(stack);
                    any = true;
                }
            }
            if (any) {
                level.sendParticles(ParticleTypes.WAX_OFF, dead.getX(), dead.getY() + 0.5, dead.getZ(), 6, 0.3, 0.3, 0.3, 0.05);
                lucky(killer);
            }
        }
    }

    /** A marked creature gives up twice the experience too. */
    public static void onExperience(LivingExperienceDropEvent event) {
        Mark mark = MARKS.get(event.getEntity().getUUID());
        if (mark != null && mark.until() > event.getEntity().level().getGameTime()) {
            event.setDroppedExperience(event.getDroppedExperience() * 2);
        }
    }

    /**
     * Favoured, for a Nightmare's boons: sometimes one more, drawn from a second
     * roll of the same table. {@code reroll} rolls the table again.
     */
    public static List<ItemStack> boons(ServerPlayer player, List<ItemStack> rolled, java.util.function.Supplier<List<ItemStack>> reroll) {
        if (!favoured(player) || !chance(player.getRandom(), EXTRA_BOON_CHANCE)) {
            return rolled;
        }
        List<ItemStack> again = reroll.get();
        if (again.isEmpty()) {
            return rolled;
        }
        List<ItemStack> out = new ArrayList<>(rolled);
        out.add(again.get(player.getRandom().nextInt(again.size())));
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Fortune favours you: the Spell grants one boon more.")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        lucky(player);
        return out;
    }

    // ---- Coin of Fate ----------------------------------------------------------------------------------------------

    static void armCoin(ServerPlayer player, int ticks) {
        COINS.put(player.getUUID(), player.serverLevel().getGameTime() + ticks);
    }

    static boolean coinArmed(ServerPlayer player) {
        Long until = COINS.get(player.getUUID());
        return until != null && until > player.serverLevel().getGameTime();
    }

    static void dropCoin(UUID player) {
        COINS.remove(player);
    }

    // ---- Twist Fate ------------------------------------------------------------------------------------------------

    static void twist(ServerPlayer player, int ticks) {
        TWISTS.put(player.getUUID(), player.serverLevel().getGameTime() + ticks);
    }

    static boolean twisted(ServerPlayer player) {
        Long until = TWISTS.get(player.getUUID());
        return until != null && until > player.serverLevel().getGameTime();
    }

    static void untwist(UUID player) {
        TWISTS.remove(player);
    }

    // ---- Borrowed Luck ---------------------------------------------------------------------------------------------

    static void mark(ServerPlayer owner, LivingEntity target, int ticks) {
        MARKS.values().removeIf(found -> found.owner().equals(owner.getUUID()));
        MARKS.put(target.getUUID(), new Mark(owner.getUUID(), owner.serverLevel().getGameTime() + ticks));
    }

    static Optional<UUID> marked(ServerPlayer owner) {
        long now = owner.serverLevel().getGameTime();
        return MARKS.entrySet().stream()
                .filter(entry -> entry.getValue().owner().equals(owner.getUUID()) && entry.getValue().until() > now)
                .map(Map.Entry::getKey).findFirst();
    }

    static void unmark(UUID owner) {
        MARKS.values().removeIf(found -> found.owner().equals(owner));
    }

    // ---- the coin and the ward, on each blow ------------------------------------------------------------------------

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        // Twist Fate: the next attack aimed at a warded player may simply miss.
        if (event.getEntity() instanceof ServerPlayer target && event.getSource().getEntity() != null
                && event.getSource().getEntity() != target && twisted(target)) {
            untwist(target.getUUID());
            AbilityService.syncStatus(target);
            if (chance(level.random, TWIST_MISS_CHANCE)) {
                event.setCanceled(true);
                level.sendParticles(ParticleTypes.ENCHANT, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.4, 0.6, 0.4, 0.4);
                level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.8F);
                AbilityService.feedback(target, "Fate twists: the blow goes wide.", ChatFormatting.GOLD);
                lucky(target);
                return;
            }
            AbilityService.feedback(target, "Fate would not twist.", ChatFormatting.GRAY);
        }
        // Coin of Fate: the flipper's next hit lands double or half.
        Entity source = event.getSource().getEntity();
        if (source instanceof ServerPlayer flipper && source != event.getEntity() && coinArmed(flipper)) {
            dropCoin(flipper.getUUID());
            boolean heads = chance(level.random, 0.5F);
            event.setAmount(event.getAmount() * (heads ? COIN_HEADS : COIN_TAILS));
            level.sendParticles(heads ? ParticleTypes.WAX_OFF : ParticleTypes.SMOKE, event.getEntity().getX(),
                    event.getEntity().getY() + event.getEntity().getBbHeight() * 0.6, event.getEntity().getZ(), 10, 0.3, 0.3, 0.3, 0.05);
            level.playSound(null, flipper.blockPosition(), heads ? SoundEvents.PLAYER_LEVELUP : SoundEvents.ITEM_BREAK,
                    SoundSource.PLAYERS, 0.6F, heads ? 1.6F : 1.0F);
            AbilityService.feedback(flipper, heads ? "Heads: double." : "Tails: half.", heads ? ChatFormatting.GOLD : ChatFormatting.GRAY);
            AbilityService.syncStatus(flipper);
            if (heads) {
                lucky(flipper);
            }
        }
    }

    // ---- Ill Omen --------------------------------------------------------------------------------------------------

    /** Counts a lucky break; for the Omened, enough of them close together bring a mishap. */
    static void lucky(ServerPlayer player) {
        if (!omened(player)) {
            return;
        }
        long now = player.serverLevel().getGameTime();
        Deque<Long> streak = STREAKS.computeIfAbsent(player.getUUID(), id -> new ArrayDeque<>());
        streak.addLast(now);
        while (!streak.isEmpty() && streak.peekFirst() < now - STREAK_TICKS) {
            streak.pollFirst();
        }
        if (streak.size() >= STREAK) {
            streak.clear();
            mishap(player);
        }
    }

    static int streak(ServerPlayer player) {
        Deque<Long> streak = STREAKS.get(player.getUUID());
        return streak == null ? 0 : streak.size();
    }

    /** A small mishap: a stumble, a fumble or spilled essence. */
    static void mishap(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        String what;
        switch (level.random.nextInt(3)) {
            case 0 -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, MISHAP_TICKS, 1));
                what = "you stumble";
            }
            case 1 -> {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, MISHAP_TICKS, 1));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, MISHAP_TICKS, 0));
                what = "your grip slips";
            }
            default -> {
                EssenceService.spend(player, Math.min(MISHAP_ESSENCE, (int) EssenceService.current(player)));
                what = "your essence spills";
            }
        }
        level.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.3, 0.5, 0.3, 0.02);
        level.playSound(null, player.blockPosition(), SoundEvents.CAT_HISS, SoundSource.PLAYERS, 0.6F, 0.8F);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Ill Omen: luck runs out, and " + what + ".")
                .withStyle(ChatFormatting.DARK_GRAY), true);
        AbilityService.syncStatus(player);
    }

    public static void forget(UUID player) {
        COINS.remove(player);
        TWISTS.remove(player);
        STREAKS.remove(player);
        unmark(player);
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        COINS.clear();
        TWISTS.clear();
        MARKS.clear();
        STREAKS.clear();
    }
}
