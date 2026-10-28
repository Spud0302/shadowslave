package dev.spud.shadowslave.smithing;

import net.minecraft.util.Mth;

/**
 * Pure Smithing rules (DESIGN): heat colours, cooling, the hammer's timing
 * marker, how a piece is scored and the Quality grade the score earns. The
 * server decides every outcome with these; the client uses the same numbers
 * only to draw them.
 */
public final class SmithingRules {
    /** A billet left alone ends up here. */
    public static final int AMBIENT = 20;
    /** How hot a hearth burns on its own, and with the bellows working. */
    public static final int HEARTH_TEMPERATURE = 1000;
    public static final int BELLOWS_TEMPERATURE = 1450;
    /** Degrees a hearth gains or loses each tick while it moves toward its target. */
    public static final int HEARTH_RISE = 10;
    public static final int HEARTH_FALL = 3;
    /** Degrees a billet in the hearth gains each tick toward the hearth's heat. */
    public static final int BILLET_RISE = 10;
    /** Degrees a billet loses each tick out of the hearth, at a cooling rate of 100%. */
    public static final int BILLET_COOL = 3;
    /** Degrees each hammer blow takes out of the billet. */
    public static final int BLOW_COOL = 15;
    /** Above the working band by this much, the billet starts to burn. */
    public static final int BURN_MARGIN = 150;
    /** Ticks a billet may spend burning before it is marked burned. */
    public static final int BURN_TICKS = 40;
    /** Below this a shaped blade is too cold to quench. */
    public static final int QUENCH_MIN = 700;
    /** Ticks between hammer blows. */
    public static final int HAMMER_COOLDOWN = 8;
    /** Ticks the marker takes to cross the bar once. */
    public static final int SWEEP_TICKS = 20;
    /** The clean zone's half width, in thousandths of the bar, at Forging level 1, and what each level adds. */
    public static final int BASE_ZONE = 70;
    public static final int ZONE_PER_LEVEL = 10;
    /** The rough zone is this many times as wide as the clean one. */
    public static final float ROUGH_SCALE = 2.5F;
    /** A quench held this close to the mark is perfect; this far off scores nothing. */
    public static final int QUENCH_PERFECT = 4;
    public static final int QUENCH_ZERO = 20;

    private SmithingRules() {
    }

    // Heat

    public enum Glow {
        COLD("cold", 0x5A5A5A),
        DULL_RED("dull red", 0x7A1E12),
        CHERRY("cherry", 0xB8261A),
        ORANGE("orange", 0xE8661E),
        YELLOW("yellow", 0xF6C33C),
        WHITE("white", 0xFFF6DC);

        private final String label;
        private final int colour;

        Glow(String label, int colour) {
            this.label = label;
            this.colour = colour;
        }

        public String label() {
            return label;
        }

        public int colour() {
            return colour;
        }
    }

    public static Glow glow(int heat) {
        if (heat >= 1300) {
            return Glow.WHITE;
        }
        if (heat >= 1100) {
            return Glow.YELLOW;
        }
        if (heat >= 900) {
            return Glow.ORANGE;
        }
        if (heat >= 700) {
            return Glow.CHERRY;
        }
        if (heat >= 500) {
            return Glow.DULL_RED;
        }
        return Glow.COLD;
    }

    /** A billet's heat {@code ticks} after it was last measured at {@code heat}, cooling at {@code coolPercent}. */
    public static int cooled(int heat, long ticks, int coolPercent) {
        if (ticks <= 0 || heat <= AMBIENT) {
            return Math.max(AMBIENT, heat);
        }
        long lost = ticks * BILLET_COOL * coolPercent / 100;
        return (int) Math.max(AMBIENT, heat - lost);
    }

    /** One step of a hearth's own temperature toward its target. */
    public static int hearthStep(int temperature, boolean burning, boolean bellows) {
        if (!burning) {
            return Math.max(AMBIENT, temperature - HEARTH_FALL);
        }
        int target = bellows ? BELLOWS_TEMPERATURE : HEARTH_TEMPERATURE;
        if (temperature < target) {
            return Math.min(target, temperature + HEARTH_RISE);
        }
        return Math.max(target, temperature - HEARTH_FALL);
    }

    /** One step of a billet in the hearth toward the hearth's temperature. */
    public static int billetStep(int heat, int hearth) {
        if (heat < hearth) {
            return Math.min(hearth, heat + BILLET_RISE);
        }
        return Math.max(hearth, heat - BILLET_COOL);
    }

    public static boolean burning(int heat, int heatMax) {
        return heatMax > 0 && heat > heatMax + BURN_MARGIN;
    }

    // The hammer

    /** The clean zone's half width in thousandths, for a Forging level, doubled by a Masterwork. */
    public static int zone(int forgingLevel, boolean masterwork) {
        int zone = BASE_ZONE + ZONE_PER_LEVEL * Math.max(0, forgingLevel - 1);
        return masterwork ? zone * 2 : zone;
    }

    /** Where the marker is, 0 to 1, at a game time (with a partial tick on the client). */
    public static float marker(double time, int seed) {
        double phase = (time + (seed & 0xFF)) / SWEEP_TICKS;
        double within = phase - Math.floor(phase / 2.0) * 2.0;
        return (float) (within <= 1.0 ? within : 2.0 - within);
    }

    /** Where blow {@code blow} wants to land, kept clear of the bar's ends. */
    public static float target(int seed, int blow) {
        int mixed = Mth.murmurHash3Mixer(seed * 31 + blow * 977 + 0x5BD1E995);
        float unit = (mixed >>> 8) / (float) (1 << 24);
        return 0.2F + unit * 0.6F;
    }

    public enum Blow {
        CLEAN, ROUGH, MISS
    }

    public static Blow judge(float marker, float target, int zone) {
        float off = Math.abs(marker - target);
        float clean = zone / 1000.0F;
        if (off <= clean) {
            return Blow.CLEAN;
        }
        return off <= clean * ROUGH_SCALE ? Blow.ROUGH : Blow.MISS;
    }

    // Scoring

    /** How a quench held for {@code held} ticks scores, 0 to 1, against the mark {@code ideal}. */
    public static float quench(int held, int ideal) {
        int off = Math.abs(held - ideal);
        if (off <= QUENCH_PERFECT) {
            return 1.0F;
        }
        return Math.max(0.0F, 1.0F - (off - QUENCH_PERFECT) / (float) (QUENCH_ZERO - QUENCH_PERFECT));
    }

    /** Held so long the blade turned brittle. */
    public static boolean cracked(int held, int ideal) {
        return held > ideal + QUENCH_ZERO;
    }

    /**
     * The piece's score out of 100: heat 30 (blows struck inside the band,
     * less 10 if it burned), hammer 50 (clean blows full, rough half, misses
     * nothing) and quench 20. A pattern with no quench scales the other two
     * up to 100.
     */
    public static int score(BilletData billet, float quench) {
        int landed = Math.max(1, billet.clean() + billet.rough());
        float heat = 30.0F * billet.inBand() / landed - (billet.burned() ? 10.0F : 0.0F);
        float hammer = 50.0F * (billet.clean() + 0.5F * billet.rough()) / Math.max(1, landed + billet.misses());
        float total = billet.quench() ? heat + hammer + 20.0F * quench : (heat + hammer) * 100.0F / 80.0F;
        return Mth.clamp(Math.round(total), 0, 100);
    }

    /** The grade a finished piece earns: its score's grade, one up for a Masterwork, one down if it cracked. */
    public static Quality grade(int score, boolean masterwork, boolean cracked) {
        int index = Quality.forScore(score).ordinal() + (masterwork ? 1 : 0) - (cracked ? 1 : 0);
        Quality[] grades = Quality.values();
        return grades[Mth.clamp(index, 0, grades.length - 1)];
    }
}
