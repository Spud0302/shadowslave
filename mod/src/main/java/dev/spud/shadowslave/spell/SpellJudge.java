package dev.spud.shadowslave.spell;

import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.trial.content.AppraisalSettings;
import dev.spud.shadowslave.trial.content.NightmarePack;
import dev.spud.shadowslave.trial.content.TrialContent;
import dev.spud.shadowslave.trial.content.TrialSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * The Spell's Judge (DESIGN, Jack 2026-10-29): plain game code that decides a
 * Nightmare's grade from what the challenger did. It never asks a voice; the
 * voice only phrases what the Judge decided.
 *
 * <p>What counts (CANON: how far the challenger strayed from the predestined
 * outcome, ch. 744; Jack, 2026-10-31): each hand-made Nightmare has a fated
 * ending the Spell never reveals, and the score is how far the ending reached
 * strays from it, more when a lowly role changed it, plus playing the part the
 * ending asked of your role, going in alone or with a small cohort, cohort
 * members lost, and striking your own. Kills are listed, but by default do not
 * count ({@link AppraisalSettings#killWeight}). The grade ladder runs Poor,
 * Adequate, Good, Excellent and Glorious (only glorious and excellent are CANON
 * words, ch. 15 and 743); the best glorious runs also hear "You have achieved
 * the impossible!" before the grade. A challenger who dies inside is judged
 * Failed.</p>
 *
 * <p>The Judge also reads how the challenger played, so the Spell's commentary
 * can be keyed to it (ch. 15: "Your treachery truly knows no bounds").</p>
 */
public final class SpellJudge {

    /** How a challenger played, as the Spell comments on it. The first that applies wins. */
    public enum Behaviour {
        /** Struck one of their own cohort. */
        TREACHERY,
        /** Slew creatures that were hunting others of the cohort. */
        PROTECTOR,
        /** Came close to death again and again. */
        RECKLESS,
        /** Slew creatures above their own rank. */
        DEFIANT,
        /** Took no lives at all. */
        MERCIFUL,
        /** Went in alone. */
        ALONE,
        /** None of the above stood out. */
        STEADFAST;

        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * What one challenger did in a run, as the Judge needs it.
     *
     * @param kills         kills by what was slain ("Awakened Monster, Chainback"), in order
     * @param killScore     points from kills, each from {@link #killPoints}
     * @param uniqueKills   Tyrants or stronger, and named creatures, slain
     * @param aboveRank     kills of creatures above the challenger's own rank
     * @param roleId        the role the Spell gave them
     * @param roleDeviation how far beneath the story's notice that role stands
     * @param cohortSize    how many went in together
     * @param falls         how many of the cohort died inside
     * @param alliesStruck  times they hurt one of their own
     * @param alliesSaved   creatures they slew that were hunting another of the cohort
     * @param closeCalls    times they fell to a quarter of their health or less
     */
    public record Run(Map<String, Integer> kills, int killScore, int uniqueKills, int aboveRank, String roleId, int roleDeviation,
                      int cohortSize, int falls, int alliesStruck, int alliesSaved, int closeCalls) {
        public Run {
            kills = Map.copyOf(kills);
        }

        /** A run with only kills, role and cohort, as older callers and tests give it. */
        public static Run simple(Map<String, Integer> kills, int killScore, String roleId, int cohortSize, int falls) {
            return new Run(kills, killScore, 0, 0, roleId, 0, cohortSize, falls, 0, 0, 0);
        }

        public int totalKills() {
            return kills.values().stream().mapToInt(Integer::intValue).sum();
        }
    }

    /**
     * The Judge's verdict: the deeds it counted, the score, the grade, how they
     * played, whether it was the impossible, and the line said before the grade.
     */
    public record Judgement(List<String> deeds, int score, TrialSettings.Grade grade, Behaviour behaviour, boolean impossible,
                            String acclaim) {
        public Judgement {
            deeds = List.copyOf(deeds);
        }

        /** Grades a score: the ladder's grade for it, and the impossible when it is the top grade and high enough. */
        public static Judgement of(List<String> deeds, int score, Behaviour behaviour, TrialSettings settings, AppraisalSettings appraisal) {
            TrialSettings.Grade grade = settings.gradeFor(score);
            List<TrialSettings.Grade> ladder = settings.grades();
            boolean impossible = grade.equals(ladder.get(ladder.size() - 1)) && score >= appraisal.impossibleMin();
            String acclaim = impossible ? appraisal.impossibleLine() : appraisal.acclaim().getOrDefault(grade.name(), "");
            return new Judgement(deeds, score, grade, behaviour, impossible, acclaim);
        }

        public String gradeKey() {
            return TrialSettings.key(grade.name());
        }
    }

    private SpellJudge() {
    }

    /** Judges one run with the loaded appraisal settings. */
    public static Judgement judge(NightmarePack.Ending ending, Run run, TrialSettings settings) {
        return judge(ending, run, settings, TrialContent.get().appraisal());
    }

    /** Judges one run that ended with {@code ending}. Pure arithmetic, so it can be tested without a world. */
    public static Judgement judge(NightmarePack.Ending ending, Run run, TrialSettings settings, AppraisalSettings appraisal) {
        List<String> deeds = new ArrayList<>();
        int score = ending.deviation() + Math.round(run.killScore() * appraisal.killWeight());
        if (!ending.fated()) {
            // The fate was changed: the lower the role that changed it, the further it strayed.
            score += run.roleDeviation();
        }
        run.kills().forEach((what, count) -> deeds.add(killDeed(what, count)));
        if (run.aboveRank() > 0) {
            deeds.add(run.aboveRank() == 1 ? "You slew a creature above your own rank."
                    : "You slew " + run.aboveRank() + " creatures above your own rank.");
        }
        if (!ending.deed().isEmpty()) {
            deeds.add(ending.deed());
        }
        if (ending.role().filter(run.roleId()::equals).isPresent()) {
            score += ending.roleScore();
            if (!ending.roleDeed().isEmpty()) {
                deeds.add(ending.roleDeed());
            }
        }
        if (run.cohortSize() <= 1) {
            score += settings.soloBonus();
            deeds.add("You entered the Nightmare alone.");
        } else {
            if (run.cohortSize() <= settings.smallCohortSize()) {
                score += settings.smallCohortBonus();
            }
            if (run.falls() == 0) {
                score += settings.unbrokenBonus();
                deeds.add("Not one of your cohort fell.");
            } else {
                score -= settings.lossPenalty() * run.falls();
                deeds.add(run.falls() == 1 ? "One of your cohort fell." : run.falls() + " of your cohort fell.");
            }
        }
        if (run.alliesSaved() > 0) {
            deeds.add("You stood between your cohort and death.");
        }
        if (run.alliesStruck() > 0) {
            score -= settings.treacheryPenalty() * run.alliesStruck();
            deeds.add("You struck one of your own.");
        }
        score = Math.max(0, score);
        return Judgement.of(deeds, score, behaviour(run), settings, appraisal);
    }

    /** How the challenger played: the first behaviour that applies, in {@link Behaviour}'s order. */
    public static Behaviour behaviour(Run run) {
        if (run.alliesStruck() > 0) {
            return Behaviour.TREACHERY;
        }
        if (run.alliesSaved() >= 2) {
            return Behaviour.PROTECTOR;
        }
        if (run.closeCalls() >= 3) {
            return Behaviour.RECKLESS;
        }
        if (run.aboveRank() > 0) {
            return Behaviour.DEFIANT;
        }
        if (run.totalKills() == 0) {
            return Behaviour.MERCIFUL;
        }
        if (run.cohortSize() <= 1) {
            return Behaviour.ALONE;
        }
        return Behaviour.STEADFAST;
    }

    /**
     * What one kill is worth: its rank's points, times its soul cores (one for
     * a Beast, five for a Tyrant), plus the unique and above-rank bonuses.
     */
    public static int killPoints(TrialSettings settings, CreatureRank rank, CreatureClass creatureClass, boolean unique, boolean aboveRank) {
        int points = settings.pointsFor(rank) * creatureClass.cores();
        if (unique) {
            points += settings.uniqueBonus();
        }
        if (aboveRank) {
            points += settings.aboveRankBonus();
        }
        return points;
    }

    /** Whether a creature of this rank stands above a soul of {@code soul}'s rank (Dormant against Dormant does not). */
    public static boolean aboveRank(CreatureRank creature, Optional<SoulRank> soul) {
        return creature.ordinal() > soul.map(Enum::ordinal).orElse(0);
    }

    /** Unique creatures: a Tyrant or stronger, or one with a name of its own. */
    public static boolean unique(CreatureClass creatureClass, boolean named) {
        return named || creatureClass.ordinal() >= CreatureClass.TYRANT.ordinal();
    }

    /**
     * "You have slain an Awakened Monster, Chainback." for one kill, or
     * "You have slain 3 Dormant Beasts, Ash Burrower." for several.
     * {@code what} is "Rank Class, Name".
     */
    public static String killDeed(String what, int count) {
        if (count <= 1) {
            String article = what.isEmpty() || "AEIOU".indexOf(Character.toUpperCase(what.charAt(0))) < 0 ? "a " : "an ";
            return "You have slain " + article + what + ".";
        }
        int comma = what.indexOf(',');
        String kind = comma < 0 ? what : what.substring(0, comma);
        String name = comma < 0 ? "" : what.substring(comma);
        return "You have slain " + count + " " + kind + "s" + name + ".";
    }
}
