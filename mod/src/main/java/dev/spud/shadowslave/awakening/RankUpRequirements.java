package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.core.CoreService;
import dev.spud.shadowslave.trial.FirstNightmare;
import dev.spud.shadowslave.trial.TrialData;
import dev.spud.shadowslave.trial.TrialRules;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The requirement for reaching each rank (DESIGN). Awakening needs a saturated
 * core; with the First Nightmare switched on it also needs the First
 * Nightmare conquered. Every rank after that needs a saturated core and the
 * next Category's Nightmare conquered (Jack, 2026-10-29; ch. 17 and 459: the
 * Second Nightmare makes a Master, the Third a Saint).
 */
public final class RankUpRequirements {
    /** The core must be full. */
    public static final RankUpRequirement SATURATED_CORE = new RankUpRequirement() {
        @Override
        public boolean met(ServerPlayer player) {
            return CoreService.saturated(player);
        }

        @Override
        public String describe(ServerPlayer player) {
            if (met(player)) {
                return "Your core is saturated.";
            }
            return "Saturate your core: " + CoreService.saturation(player) + " / " + CoreService.limit(player) + ".";
        }
    };

    /** A Nightmare of at least this Category conquered. */
    public static RankUpRequirement nightmareConquered(int category) {
        return new RankUpRequirement() {
            @Override
            public boolean met(ServerPlayer player) {
                return TrialData.get(player.server).find(player.getUUID()).filter(record -> record.cleared(category)).isPresent();
            }

            @Override
            public String describe(ServerPlayer player) {
                return met(player) ? "You have conquered a Category " + category + " Nightmare."
                        : "Conquer a Category " + category + " Nightmare.";
            }
        };
    }

    /** The First Nightmare conquered: born from the Seed inside the carrier, and faced alone (ch. 1, 23). */
    public static final RankUpRequirement FIRST_NIGHTMARE_CONQUERED = new RankUpRequirement() {
        @Override
        public boolean met(ServerPlayer player) {
            return FirstNightmare.conquered(player);
        }

        @Override
        public String describe(ServerPlayer player) {
            return met(player) ? "You have conquered your First Nightmare."
                    : "Conquer your First Nightmare: sleep beside a lit Dream Candle.";
        }
    };

    /** Every one of {@code parts}. */
    public static RankUpRequirement allOf(RankUpRequirement... parts) {
        return new RankUpRequirement() {
            @Override
            public boolean met(ServerPlayer player) {
                for (RankUpRequirement part : parts) {
                    if (!part.met(player)) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public String describe(ServerPlayer player) {
                StringBuilder text = new StringBuilder();
                for (RankUpRequirement part : parts) {
                    if (!text.isEmpty()) {
                        text.append(' ');
                    }
                    text.append(part.describe(player));
                }
                return text.toString();
            }
        };
    }

    /** A saturated core, and, if the server switched the First Nightmare on, the First Nightmare conquered. */
    public static final RankUpRequirement AWAKENING = new RankUpRequirement() {
        private final RankUpRequirement withFirstNightmare = allOf(SATURATED_CORE, FIRST_NIGHTMARE_CONQUERED);

        @Override
        public boolean met(ServerPlayer player) {
            return current(player).met(player);
        }

        @Override
        public String describe(ServerPlayer player) {
            return current(player).describe(player);
        }

        private RankUpRequirement current(ServerPlayer player) {
            return TrialRules.firstNightmare(player.server) ? withFirstNightmare : SATURATED_CORE;
        }
    };

    private static final Map<SoulRank, RankUpRequirement> REQUIREMENTS = new EnumMap<>(SoulRank.class);

    static {
        REQUIREMENTS.put(SoulRank.AWAKENED, AWAKENING);
        REQUIREMENTS.put(SoulRank.ASCENDED, allOf(SATURATED_CORE, nightmareConquered(2)));
        REQUIREMENTS.put(SoulRank.TRANSCENDENT, allOf(SATURATED_CORE, nightmareConquered(3)));
    }

    private RankUpRequirements() {
    }

    /** The requirement for reaching {@code rank}; empty for ranks that cannot be reached yet. */
    public static Optional<RankUpRequirement> forRank(SoulRank rank) {
        return Optional.ofNullable(REQUIREMENTS.get(rank));
    }

    /** Swaps the requirement for a rank, for a new trigger or a test. */
    public static RankUpRequirement replace(SoulRank rank, RankUpRequirement requirement) {
        return REQUIREMENTS.put(Objects.requireNonNull(rank, "rank"), Objects.requireNonNull(requirement, "requirement"));
    }
}
