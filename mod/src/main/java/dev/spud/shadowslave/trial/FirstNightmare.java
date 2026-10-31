package dev.spud.shadowslave.trial;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.awakening.AwakeningService;
import dev.spud.shadowslave.awakening.DreamCandleRitual;
import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.dreamrealm.DreamCrossing;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.core.CoreService;
import dev.spud.shadowslave.trial.content.FirstNightmareSettings;
import dev.spud.shadowslave.trial.content.NightmarePack;
import dev.spud.shadowslave.trial.content.TrialContent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Optional;

/**
 * The First Nightmare (CANON: it grows from the Spell's seed inside the
 * carrier, and only it is faced alone, ch. 1 and 23; DESIGN, Jack 2026-10-29
 * and 2026-10-31). With the {@code shadowslaveFirstNightmare} rule on, a
 * Dormant player whose core is saturated and who sleeps beside a lit Dream
 * Candle is drawn into a solo Category 1 Nightmare picked from
 * {@code first_nightmare} in the settings. Conquering it is what lets them
 * Awaken. A death inside reverts them like any Nightmare, but no Seed judges
 * them: the one inside them grows back and blooms the next time they sleep by
 * the candle. With {@code shadowslaveFirstNightmareBreach} also on, a death lets
 * the pack's {@code breach} creature out beside them.
 */
public final class FirstNightmare {
    /** How long the carrier sleeps before the Seed blooms; the same wait as a dreamer's crossing. */
    static final int BLOOM_TICKS = DreamCrossing.ENTRY_TICKS;

    private FirstNightmare() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.isSleeping() && player.getSleepTimer() == BLOOM_TICKS) {
            tryBloom(player);
        }
    }

    /** Blooms the Seed of a sleeping carrier if a lit Dream Candle burns by their bed and it is due. */
    public static boolean tryBloom(ServerPlayer player) {
        Optional<BlockPos> bed = player.getSleepingPos();
        if (bed.isEmpty() || DreamCandleRitual.findLitCandle(player.serverLevel(), bed.get()).isEmpty() || !due(player)) {
            return false;
        }
        return bloom(player).isPresent();
    }

    /**
     * Whether the Seed inside this player is ready: the rule is on, they are
     * Dormant with a revealed Aspect and a saturated core, and they have not
     * conquered their First Nightmare yet.
     */
    public static boolean due(ServerPlayer player) {
        return TrialRules.firstNightmare(player.server)
                && AwakeningService.nextRank(player).filter(SoulRank.AWAKENED::equals).isPresent()
                && CoreService.saturated(player)
                && !conquered(player)
                && !TrialService.inside(player);
    }

    /** Whether this player has conquered a First Nightmare (or any Nightmare, which counts as much). */
    public static boolean conquered(ServerPlayer player) {
        return TrialData.get(player.server).find(player.getUUID()).filter(record -> record.cleared(1)).isPresent();
    }

    /** Tells a carrier whose core has just saturated that the Seed in them is ready. */
    public static void stirs(ServerPlayer player) {
        if (due(player)) {
            TrialService.spell(player, Component.literal(settings().stirs()).withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    /**
     * Draws a player alone into their First Nightmare now, waking them if they
     * sleep. Operators can call it without the rule; returns empty, with the
     * player left where they are, if no First Nightmare can start.
     */
    public static Optional<TrialInstance> bloom(ServerPlayer player) {
        if (TrialService.inside(player)) {
            return Optional.empty();
        }
        Optional<ResourceLocation> nightmare = pick(player);
        if (nightmare.isEmpty()) {
            player.displayClientMessage(Component.literal("The Seed in you stirs, but its Nightmare is missing from the data packs.")
                    .withStyle(ChatFormatting.GRAY), true);
            return Optional.empty();
        }
        if (player.isSleeping()) {
            player.stopSleepInBed(true, true);
        }
        for (String line : settings().bloom()) {
            TrialService.spell(player, Component.literal(line).withStyle(ChatFormatting.DARK_PURPLE));
        }
        Optional<TrialInstance> run = TrialService.begin(List.of(player), nightmare.get(), Optional.empty(), Optional.empty(), 1);
        run.ifPresent(instance -> instance.first = true);
        run.ifPresent(instance -> ShadowSlaveMod.LOGGER.info("{}'s First Nightmare bloomed: {}", player.getScoreboardName(),
                instance.nightmareId));
        return run;
    }

    /** One of the First Nightmares the settings list that the data packs hold. */
    static Optional<ResourceLocation> pick(ServerPlayer player) {
        List<ResourceLocation> loaded = settings().nightmares().stream()
                .filter(id -> TrialContent.get().nightmare(id).isPresent())
                .toList();
        if (loaded.isEmpty() || TrialWorld.level(player.server) == null) {
            return Optional.empty();
        }
        return Optional.of(loaded.get(player.getRandom().nextInt(loaded.size())));
    }

    /** A carrier died in their First Nightmare and is home again: the Seed grows back, and something may have followed them. */
    static void fell(ServerPlayer player, NightmarePack pack) {
        TrialService.spell(player, Component.literal(settings().withered()).withStyle(ChatFormatting.DARK_PURPLE));
        if (TrialRules.firstNightmareBreach(player.server)) {
            pack.breach().ifPresent(breach -> breach(player, breach));
        }
    }

    /** Lets the pack's breach creature out into the waking world beside the fallen carrier. */
    static Optional<Mob> breach(ServerPlayer player, NightmarePack.Breach breach) {
        ServerLevel level = player.serverLevel();
        Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(breach.entity());
        if (type.isEmpty() || !(type.get().create(level) instanceof Mob mob)) {
            ShadowSlaveMod.LOGGER.error("A First Nightmare breaches with {}, which is not a mob", breach.entity());
            return Optional.empty();
        }
        double angle = player.getRandom().nextDouble() * Math.PI * 2;
        mob.moveTo(player.getX() + Math.cos(angle) * 4, player.getY(), player.getZ() + Math.sin(angle) * 4,
                player.getRandom().nextFloat() * 360.0F, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.EVENT, null);
        if (mob instanceof NightmareCreature creature) {
            creature.setRankAndClass(breach.rank().orElse(creature.nativeRank()), breach.creatureClass().orElse(creature.nativeClass()));
        }
        mob.setTarget(player);
        level.addFreshEntity(mob);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, mob.getX(), mob.getY() + 0.8, mob.getZ(), 60, 0.4, 0.8, 0.4, 0.06);
        level.playSound(null, mob.blockPosition(), SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE, 1.5F, 0.5F);
        if (!breach.spell().isEmpty()) {
            TrialService.spell(player, Component.literal(breach.spell()).withStyle(ChatFormatting.DARK_RED));
        }
        return Optional.of(mob);
    }

    private static FirstNightmareSettings settings() {
        return TrialContent.get().firstNightmare();
    }
}
