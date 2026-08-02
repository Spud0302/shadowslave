package dev.spud.shadowslave.migration;

import dev.spud.shadowslave.soul.SoulService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;

import java.util.Set;
import java.util.TreeSet;

/**
 * Read-only adapter over the frozen datapack's player tags and scoreboard state.
 * Missing objectives/scores are deliberately represented as 0 because the frozen
 * pack never writes an explicit zero. An explicit zero is rejected instead of
 * being confused with absence.
 */
public final class LegacyDatapackReader {
    private static final String RANK_OBJECTIVE = "ss_rank";
    private static final String ASPECT_OBJECTIVE = "ss_aspect";
    private static final String FLAW_OBJECTIVE = "ss_flaw";

    private static final Set<String> RELEVANT_TAGS = Set.of(
            "ss_carrier",
            "ss_in_nightmare",
            "ss_aspect_shadow",
            "ss_aspect_flame",
            "ss_aspect_bone",
            "ss_aspect_wind",
            "ss_flaw_shadow_slave",
            "ss_flaw_fragile",
            "ss_flaw_ravenous",
            "ss_flaw_weightless"
    );

    private LegacyDatapackReader() {
    }

    public static LegacyDatapackSnapshot read(ServerPlayer player) {
        Scoreboard scoreboard = player.serverLevel().getScoreboard();
        Set<String> tags = new TreeSet<>();
        for (String tag : player.getTags()) {
            if (RELEVANT_TAGS.contains(tag)) {
                tags.add(tag);
            }
        }

        try {
            return new LegacyDatapackSnapshot(
                    player.getUUID(),
                    tags.contains("ss_carrier"),
                    readOptionalScore(scoreboard, player, RANK_OBJECTIVE),
                    readOptionalScore(scoreboard, player, ASPECT_OBJECTIVE),
                    readOptionalScore(scoreboard, player, FLAW_OBJECTIVE),
                    tags,
                    tags.contains("ss_in_nightmare"),
                    SoulService.get(player).migrationVersion()
            );
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                    "Could not read frozen datapack evidence for " + player.getScoreboardName(),
                    exception
            );
        }
    }

    static int readOptionalScore(Scoreboard scoreboard, ServerPlayer player, String objectiveName) {
        Objective objective = scoreboard.getObjective(objectiveName);
        if (objective == null) {
            return normalizeOptionalScore(objectiveName, null);
        }

        ReadOnlyScoreInfo scoreInfo = scoreboard.getPlayerScoreInfo(player, objective);
        return normalizeOptionalScore(objectiveName, scoreInfo == null ? null : scoreInfo.value());
    }

    static int normalizeOptionalScore(String objectiveName, Integer storedValue) {
        if (storedValue == null) {
            return 0;
        }
        if (storedValue == 0) {
            throw new IllegalStateException(
                    "Frozen datapack objective " + objectiveName + " contains an explicit zero; "
                            + "zero is reserved for an absent score during migration"
            );
        }
        return storedValue;
    }
}
