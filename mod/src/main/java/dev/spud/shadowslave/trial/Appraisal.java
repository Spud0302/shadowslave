package dev.spud.shadowslave.trial;

import dev.spud.shadowslave.trial.content.NightmarePack;
import dev.spud.shadowslave.trial.content.TrialSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * How the Spell appraises one challenger at the end of a Nightmare (after
 * ch. 15-16, where every notable kill is listed, then a grade with commentary).
 * The ending sets the base of the score; each kill adds by the creature's
 * rank; fulfilling your role's fate, going in alone and losing nobody add more.
 * Pure arithmetic, so it can be tested without a world.
 */
public record Appraisal(List<String> deeds, int score, TrialSettings.Grade grade, String comment) {

    /** What one challenger did, as the appraisal needs it. */
    public record Run(Map<String, Integer> kills, int killScore, String roleId, int cohortSize, int falls) {
    }

    /** Appraises one run that ended with {@code ending}; {@code comments} are the Nightmare's comment per grade. */
    public static Appraisal of(Map<String, String> comments, NightmarePack.Ending ending, Run run, TrialSettings settings) {
        List<String> deeds = new ArrayList<>();
        int score = ending.score() + run.killScore();
        run.kills().forEach((what, count) -> deeds.add(killDeed(what, count)));
        if (!ending.deed().isEmpty()) {
            deeds.add(ending.deed());
        }
        if (ending.role().filter(run.roleId()::equals).isPresent()) {
            score += ending.roleScore();
            if (!ending.roleDeed().isEmpty()) {
                deeds.add(ending.roleDeed());
            }
        }
        if (run.cohortSize() == 1) {
            score += settings.soloBonus();
            deeds.add("You entered the Nightmare alone.");
        } else if (run.falls() == 0) {
            score += settings.unbrokenBonus();
            deeds.add("Not one of your cohort fell.");
        }
        TrialSettings.Grade grade = settings.gradeFor(score);
        String comment = comments.getOrDefault(grade.name(), "");
        return new Appraisal(List.copyOf(deeds), score, grade, comment);
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
