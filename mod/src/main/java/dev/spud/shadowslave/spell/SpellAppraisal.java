package dev.spud.shadowslave.spell;

import dev.spud.shadowslave.spell.calling.SpellCallings;
import dev.spud.shadowslave.spell.voice.SpellVoices;
import dev.spud.shadowslave.spell.voice.VoiceRequest;
import dev.spud.shadowslave.trial.content.NightmarePack;
import dev.spud.shadowslave.trial.content.TrialSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The Spell's words on an appraisal: what the Judge decided, phrased by the
 * voice. Up to three lines: the grade (the Nightmare's own comment for it, or
 * the voice's), how the challenger played, and what the Spell remembers of
 * them from before ("Again, you flee."). The grade and boons are never the
 * voice's to choose.
 */
public final class SpellAppraisal {
    private SpellAppraisal() {
    }

    /** The comment lines for a conquered Nightmare. {@code before} is the player's tally as it stood before this one. */
    public static List<String> comments(ServerPlayer player, NightmarePack pack, SpellJudge.Judgement judgement,
                                        Optional<SpellRecord.Tally> before, TrialSettings settings) {
        Map<String, String> facts = facts(player, pack, judgement);
        long seed = player.getRandom().nextLong();
        List<String> lines = new ArrayList<>();
        String own = pack.comments().getOrDefault(judgement.grade().name(), "");
        if (!own.isEmpty()) {
            lines.add(own);
        } else {
            SpellVoices.say(VoiceRequest.of("appraisal_grade", judgement.gradeKey(), facts, seed)).ifPresent(lines::add);
        }
        SpellVoices.say(VoiceRequest.of("appraisal_behaviour", judgement.behaviour().id(), facts, seed)).ifPresent(lines::add);
        String memory = memoryKey(before, judgement, settings);
        if (!memory.isEmpty()) {
            SpellVoices.say(VoiceRequest.of("appraisal_memory", memory, facts, seed)).ifPresent(lines::add);
        }
        return lines;
    }

    /** What the Spell recalls of a player's earlier Nightmares, as a line table key; empty if nothing stands out. */
    public static String memoryKey(Optional<SpellRecord.Tally> before, SpellJudge.Judgement judgement, TrialSettings settings) {
        if (before.isEmpty() || before.get().cleared() + before.get().failed() + before.get().fled() == 0) {
            return "first_conquest";
        }
        SpellRecord.Tally tally = before.get();
        switch (tally.lastOutcome()) {
            case "fled" -> {
                return "after_fleeing";
            }
            case "failed" -> {
                return "after_failing";
            }
            default -> {
            }
        }
        if (judgement.behaviour() != SpellJudge.Behaviour.STEADFAST
                && tally.lastBehaviour().filter(judgement.behaviour().id()::equals).isPresent()) {
            return "again_behaviour";
        }
        Optional<String> last = tally.lastGrade();
        if (last.isEmpty()) {
            return "";
        }
        int was = settings.standing(last.get());
        int now = settings.standing(judgement.grade().name());
        if (now > was) {
            return "rose";
        }
        if (now < was) {
            return "fell";
        }
        return "same_grade";
    }

    /** A challenger died inside: the Spell judges them Failed, and may recall that it is not the first time. */
    public static void failed(ServerPlayer player, NightmarePack pack, TrialSettings settings) {
        Map<String, String> facts = new java.util.HashMap<>(SpellCallings.playerFacts(player));
        facts.put("nightmare", pack.displayName());
        facts.put("grade", settings.failedGrade());
        SpellWords.tell(player, "appraisal_grade", "failed", facts, ChatFormatting.DARK_RED);
        if (SpellRecord.get(player.server).find(player.getUUID()).map(SpellRecord.Tally::failed).orElse(0) >= 2) {
            SpellWords.tell(player, "appraisal_memory", "again_failed", facts, ChatFormatting.DARK_RED);
        }
    }

    /** A challenger left a Nightmare unfinished; if it is a habit, the Spell says so. */
    public static void fled(ServerPlayer player, NightmarePack pack) {
        if (SpellRecord.get(player.server).find(player.getUUID()).map(SpellRecord.Tally::fled).orElse(0) >= 2) {
            Map<String, String> facts = new java.util.HashMap<>(SpellCallings.playerFacts(player));
            facts.put("nightmare", pack.displayName());
            SpellWords.tell(player, "appraisal_memory", "again_fled", facts, ChatFormatting.GRAY);
        }
    }

    static Map<String, String> facts(ServerPlayer player, NightmarePack pack, SpellJudge.Judgement judgement) {
        Map<String, String> facts = new java.util.HashMap<>(SpellCallings.playerFacts(player));
        facts.put("nightmare", pack.displayName());
        facts.put("grade", judgement.grade().name());
        facts.put("behaviour", judgement.behaviour().id());
        return facts;
    }
}
