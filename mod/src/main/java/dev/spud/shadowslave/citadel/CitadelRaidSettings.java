package dev.spud.shadowslave.citadel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Raids on Citadels (DESIGN, Run C, Jack 2026-10-30: raids come only from
 * Seeds blooming near a Citadel), read from {@code data/<ns>/citadel/raid.json}.
 * A Seed that blooms within {@code radius} of a claimed Citadel's Gateway opens
 * its Gate there, in the Dream Realm, at its region's Category, and its waves
 * march on the Gateway. The clan holds the Citadel by slaying the Gate's
 * Guardian. It loses the raid if raiders stand at the Gateway long enough to
 * fill the breach, or if nobody answers in time; a lost raid damages upgrades
 * and eats provisions, and never loses the Citadel.
 *
 * @param radius            how close to a Gateway a Seed must bloom to raid it
 * @param defendRadius      defenders are members who fight within this of the Gateway or the Gate
 * @param breachRadius      raiders this close to the Gateway press the breach
 * @param breachLimit       raider-seconds at the Gateway that lose the raid
 * @param unansweredMinutes how long a raid may wait for a defender before it is lost unanswered
 * @param damaged           how many built upgrades a lost raid damages
 * @param provisionsLost    the share of provisions a lost raid spoils
 * @param shardsPerCategory soul shards each defender earns per Category when the raid is held
 * @param echoRadius        how far from the Gateway the Guardian's echo strikes raiders
 * @param echoSeconds       seconds between the echo's strikes
 * @param echoDamage        damage of a strike, times (the Citadel's rank + 1)
 * @param echoFailsAt       the stage of neglect from which the echo no longer defends
 * @param repairShare       the share of a tier's cost that repairs it after a lost raid
 */
public record CitadelRaidSettings(int radius, int defendRadius, int breachRadius, int breachLimit, int unansweredMinutes,
                                  int damaged, double provisionsLost, int shardsPerCategory, int echoRadius, int echoSeconds,
                                  float echoDamage, Neglect echoFailsAt, double repairShare) {
    public static final CitadelRaidSettings DEFAULT = new CitadelRaidSettings(96, 64, 5, 60, 20, 2, 0.25, 2, 16, 3, 4.0F,
            Neglect.FORSAKEN, 0.5);

    public static final Codec<CitadelRaidSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(8, 512).optionalFieldOf("radius", DEFAULT.radius).forGetter(CitadelRaidSettings::radius),
            Codec.intRange(8, 256).optionalFieldOf("defend_radius", DEFAULT.defendRadius).forGetter(CitadelRaidSettings::defendRadius),
            Codec.intRange(1, 32).optionalFieldOf("breach_radius", DEFAULT.breachRadius).forGetter(CitadelRaidSettings::breachRadius),
            Codec.intRange(1, 100_000).optionalFieldOf("breach_limit", DEFAULT.breachLimit).forGetter(CitadelRaidSettings::breachLimit),
            Codec.intRange(1, 100_000).optionalFieldOf("unanswered_minutes", DEFAULT.unansweredMinutes).forGetter(CitadelRaidSettings::unansweredMinutes),
            Codec.intRange(0, 16).optionalFieldOf("damaged", DEFAULT.damaged).forGetter(CitadelRaidSettings::damaged),
            Codec.doubleRange(0, 1).optionalFieldOf("provisions_lost", DEFAULT.provisionsLost).forGetter(CitadelRaidSettings::provisionsLost),
            Codec.intRange(0, 64).optionalFieldOf("shards_per_category", DEFAULT.shardsPerCategory).forGetter(CitadelRaidSettings::shardsPerCategory),
            Codec.intRange(0, 64).optionalFieldOf("echo_radius", DEFAULT.echoRadius).forGetter(CitadelRaidSettings::echoRadius),
            Codec.intRange(1, 600).optionalFieldOf("echo_seconds", DEFAULT.echoSeconds).forGetter(CitadelRaidSettings::echoSeconds),
            Codec.floatRange(0, 1000).optionalFieldOf("echo_damage", DEFAULT.echoDamage).forGetter(CitadelRaidSettings::echoDamage),
            Neglect.CODEC.optionalFieldOf("echo_fails_at", DEFAULT.echoFailsAt).forGetter(CitadelRaidSettings::echoFailsAt),
            Codec.doubleRange(0, 1).optionalFieldOf("repair_share", DEFAULT.repairShare).forGetter(CitadelRaidSettings::repairShare)
    ).apply(instance, CitadelRaidSettings::new));
}
