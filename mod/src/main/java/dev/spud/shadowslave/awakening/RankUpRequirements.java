package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.core.CoreService;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** The requirement for reaching each rank. For now a saturated core is enough (DESIGN). */
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

    private static final Map<SoulRank, RankUpRequirement> REQUIREMENTS = new EnumMap<>(SoulRank.class);

    static {
        REQUIREMENTS.put(SoulRank.AWAKENED, SATURATED_CORE);
        REQUIREMENTS.put(SoulRank.ASCENDED, SATURATED_CORE);
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
