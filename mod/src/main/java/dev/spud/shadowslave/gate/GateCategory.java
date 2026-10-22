package dev.spud.shadowslave.gate;

import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.CreatureRank;

import java.util.List;

/**
 * What a Gate of each Category holds (DESIGN). Canon names a Gate by Category
 * and forecasts it with odds (ch. 484); how Category maps to creatures is
 * ours. Higher Categories bring stronger Ranks, more waves and a stronger
 * Guardian. Creatures are named by key so this class stays free of Minecraft
 * registries: {@code ash_burrower}, {@code chainback}, {@code hollow_mimic}.
 */
public final class GateCategory {
    public static final int MIN = 1;
    public static final int MAX = 5;

    /** One kind of creature in a wave, at a Rank and Class. */
    public record Spawn(String creature, CreatureRank rank, CreatureClass creatureClass, int count) {
    }

    private GateCategory() {
    }

    public static int clamp(int category) {
        return Math.max(MIN, Math.min(MAX, category));
    }

    /** The Guardian is always an Ashen Tyrant, at this Rank. */
    public static CreatureRank guardianRank(int category) {
        return switch (clamp(category)) {
            case 1 -> CreatureRank.DORMANT;
            case 2, 3 -> CreatureRank.AWAKENED;
            case 4 -> CreatureRank.FALLEN;
            default -> CreatureRank.CORRUPTED;
        };
    }

    /** Ticks between waves. */
    public static int waveInterval(int category) {
        return 20 * (24 - 2 * clamp(category));
    }

    public static List<List<Spawn>> waves(int category) {
        CreatureRank d = CreatureRank.DORMANT;
        CreatureRank a = CreatureRank.AWAKENED;
        CreatureRank f = CreatureRank.FALLEN;
        CreatureClass beast = CreatureClass.BEAST;
        CreatureClass monster = CreatureClass.MONSTER;
        CreatureClass demon = CreatureClass.DEMON;
        return switch (clamp(category)) {
            case 1 -> List.of(
                    List.of(new Spawn("ash_burrower", d, beast, 3)),
                    List.of(new Spawn("ash_burrower", d, beast, 4)));
            case 2 -> List.of(
                    List.of(new Spawn("ash_burrower", d, beast, 4)),
                    List.of(new Spawn("ash_burrower", d, beast, 3), new Spawn("chainback", d, monster, 1)),
                    List.of(new Spawn("ash_burrower", a, beast, 2), new Spawn("chainback", a, monster, 1)));
            case 3 -> List.of(
                    List.of(new Spawn("ash_burrower", a, beast, 4)),
                    List.of(new Spawn("ash_burrower", a, beast, 2), new Spawn("chainback", a, monster, 2)),
                    List.of(new Spawn("chainback", a, monster, 1), new Spawn("hollow_mimic", a, demon, 1)));
            case 4 -> List.of(
                    List.of(new Spawn("ash_burrower", a, beast, 5), new Spawn("chainback", a, monster, 1)),
                    List.of(new Spawn("chainback", a, monster, 2), new Spawn("hollow_mimic", a, demon, 1)),
                    List.of(new Spawn("ash_burrower", f, beast, 3), new Spawn("hollow_mimic", a, demon, 2)),
                    List.of(new Spawn("chainback", f, monster, 1), new Spawn("hollow_mimic", f, demon, 1)));
            default -> List.of(
                    List.of(new Spawn("ash_burrower", f, beast, 6)),
                    List.of(new Spawn("chainback", f, monster, 2), new Spawn("ash_burrower", f, beast, 3)),
                    List.of(new Spawn("hollow_mimic", f, demon, 2), new Spawn("chainback", f, monster, 1)),
                    List.of(new Spawn("hollow_mimic", f, demon, 2), new Spawn("chainback", f, monster, 2)));
        };
    }
}
