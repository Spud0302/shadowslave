package dev.spud.shadowslave.spell;

import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.core.CoreService;
import dev.spud.shadowslave.spell.calling.SpellCallings;
import dev.spud.shadowslave.trial.content.TrialSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The Spell's Recorder (DESIGN, Jack 2026-10-29): writes notable world events
 * into the {@link SpellRecord} as they happen. Always on, plain game code.
 * Each event also counts as progress for the player, which quiets the Spell's
 * gentle "no progress" Calling.
 */
public final class SpellRecorder {
    private SpellRecorder() {
    }

    public static SpellEvent record(MinecraftServer server, SpellEvent.Kind kind, Optional<ServerPlayer> player, String subject, String detail) {
        SpellEvent event = new SpellEvent(server.overworld().getGameTime(), kind, player.map(ServerPlayer::getUUID),
                player.map(each -> each.getGameProfile().getName()).orElse(""), subject, detail);
        SpellRecord.get(server).add(event, SpellContentLoader.settings().recordCap());
        player.ifPresent(SpellCallings::progressed);
        return event;
    }

    // ---- kills and deaths ----------------------------------------------------------------------------------------

    /** Runs after the Nightmares and Citadels have seen a death, and only if nothing cancelled it. */
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Entity killer = event.getSource().getEntity();
            String by = killer == null ? event.getSource().type().msgId() : killer.getType().getDescription().getString();
            record(player.server, SpellEvent.Kind.DEATH, Optional.of(player), by, player.level().dimension().location().toString());
            SpellRecord.get(player.server).tally(player.getUUID()).deaths++;
            return;
        }
        if (!(event.getEntity() instanceof NightmareCreature creature) || creature.level().isClientSide()) {
            return;
        }
        ServerPlayer killer = event.getSource().getEntity() instanceof ServerPlayer player ? player
                : creature.getKillCredit() instanceof ServerPlayer player ? player : null;
        if (killer != null) {
            kill(killer, creature);
        }
    }

    /** A player slew a Nightmare Creature. */
    public static void kill(ServerPlayer killer, NightmareCreature creature) {
        boolean unique = SpellJudge.unique(creature.creatureClass(), creature.hasCustomName());
        String kind = creature.rank().getSerializedName() + " " + creature.creatureClass().getSerializedName();
        String name = creature.hasCustomName() ? creature.getCustomName().getString() : creature.getType().getDescription().getString();
        record(killer.server, unique ? SpellEvent.Kind.UNIQUE_KILL : SpellEvent.Kind.KILL, Optional.of(killer), name, kind);
        SpellRecord.Tally tally = SpellRecord.get(killer.server).tally(killer.getUUID());
        tally.kills.merge(kind, 1, Integer::sum);
        if (unique) {
            tally.uniqueKills++;
        }
        String what = SpellWords.withArticle(creature.rank().displayName() + " " + creature.creatureClass().displayName());
        first(killer, "kill_" + kind.replace(' ', '_'), what, "kill", "slay " + what);
    }

    // ---- Nightmares ----------------------------------------------------------------------------------------------

    /** A Nightmare conquered and appraised. */
    public static void cleared(ServerPlayer player, String nightmare, int category, SpellJudge.Judgement judgement, TrialSettings settings) {
        record(player.server, SpellEvent.Kind.SEED_CLEARED, Optional.of(player), nightmare,
                "category " + category + ", " + judgement.gradeKey() + ", " + judgement.behaviour().id());
        SpellRecord.Tally tally = SpellRecord.get(player.server).tally(player.getUUID());
        tally.cleared++;
        SpellRecord.Tally.push(tally.grades, judgement.gradeKey());
        SpellRecord.Tally.push(tally.behaviours, judgement.behaviour().id());
        tally.lastOutcome = "cleared";
        String nightmareWhat = "a Nightmare of the " + SpellWords.ordinal(category) + " Category";
        first(player, "nightmare_" + category, nightmareWhat, "nightmare", "conquer " + nightmareWhat);
        int glorious = settings.standing("glorious");
        if (glorious >= 0 && settings.standing(judgement.grade().name()) >= glorious) {
            first(player, "grade_" + judgement.gradeKey(), judgement.grade().name(), "grade", "be judged " + judgement.grade().name());
        }
    }

    /** A challenger died inside and was judged. */
    public static void failed(ServerPlayer player, String nightmare) {
        record(player.server, SpellEvent.Kind.SEED_FAILED, Optional.of(player), nightmare, "");
        SpellRecord.Tally tally = SpellRecord.get(player.server).tally(player.getUUID());
        tally.failed++;
        tally.lastOutcome = "failed";
    }

    /** A challenger left a Nightmare without finishing it or dying in it. */
    public static void fled(ServerPlayer player, String nightmare) {
        record(player.server, SpellEvent.Kind.SEED_FLED, Optional.of(player), nightmare, "");
        SpellRecord.Tally tally = SpellRecord.get(player.server).tally(player.getUUID());
        tally.fled++;
        tally.lastOutcome = "fled";
    }

    // ---- Citadels and ranks --------------------------------------------------------------------------------------

    public static void citadelClaimed(ServerPlayer player, String citadel) {
        record(player.server, SpellEvent.Kind.CITADEL_CLAIMED, Optional.of(player), citadel, "");
        SpellRecord.get(player.server).tally(player.getUUID()).citadels++;
        first(player, "citadel", "Citadel", "citadel", "claim a Citadel");
    }

    /** A Citadel fell open because the clan holding it is no more. */
    public static void citadelLost(MinecraftServer server, String citadel, UUID clan) {
        record(server, SpellEvent.Kind.CITADEL_LOST, Optional.empty(), citadel, "clan " + clan);
    }

    public static void rankUp(ServerPlayer player, SoulRank rank) {
        record(player.server, SpellEvent.Kind.RANK_UP, Optional.of(player), rank.serializedName(), "");
        if (rank != SoulRank.DORMANT) {
            first(player, "rank_" + rank.serializedName(), capitalised(rank.serializedName()), "rank", "rise to " + capitalised(rank.serializedName()));
        }
    }

    // ---- firsts --------------------------------------------------------------------------------------------------

    /**
     * Marks a server first if nobody has done it before: records it and lets the
     * Spell tell the player. Returns whether it was a first.
     *
     * @param what     the thing done, for the voice's {@code {what}} ("Fallen Demon")
     * @param voiceKey the line table key in the {@code first} topic
     * @param deed     the whole deed, for the record ("slay a Fallen Demon")
     */
    public static boolean first(ServerPlayer player, String key, String what, String voiceKey, String deed) {
        SpellRecord record = SpellRecord.get(player.server);
        if (record.firsts.containsKey(key)) {
            return false;
        }
        long time = player.server.overworld().getGameTime();
        record.firsts.put(key, new SpellRecord.First(player.getUUID(), player.getGameProfile().getName(), time, deed));
        record(player.server, SpellEvent.Kind.FIRST, Optional.of(player), key, deed);
        SpellWords.tell(player, "first", voiceKey, Map.of("what", what, "rank", rankName(player)), ChatFormatting.GOLD);
        return true;
    }

    static String rankName(ServerPlayer player) {
        return CoreService.rank(player).map(rank -> capitalised(rank.serializedName())).orElse("Sleeper");
    }

    static String capitalised(String id) {
        return id.isEmpty() ? id : Character.toUpperCase(id.charAt(0)) + id.substring(1);
    }
}
