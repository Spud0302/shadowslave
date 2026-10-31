package dev.spud.shadowslave.citadel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Clan wars (DESIGN, Run I, locked with Jack 2026-10-31), read from
 * {@code data/<ns>/citadel/war.json}. Every number is a server option, and
 * sieges are on unless {@code enabled} is false. War is a formal thing: a
 * clan's leader declares it at their own Gateway, pays for it, and the Spell
 * gives both clans notice before anything can be struck. Then war is a long
 * state with no time limit: skirmishes anywhere, raids on a Citadel's
 * outskirts, strikes on its supply line, and assaults on the enemy's seat.
 * Taking the seat ends the war. Times are in real hours, since a war is
 * fought between people who log in and out.
 *
 * @param enabled   whether clans may go to war at all
 * @param declare   what declaring costs, and the notice it gives
 * @param assault   how assaults on a seat are announced and fought
 * @param outskirts raids on a Citadel's outskirts
 * @param supply    strikes on a Citadel's supply line, its Link Anchor
 * @param seat      how a clan's seat may move
 * @param abandon   when a clan nobody logs into loses the protection of notice
 * @param truceDays the truce a lost war gives the two clans
 * @param warHours  fixed server-wide war hours instead of announced assaults
 */
public record CitadelWarSettings(boolean enabled, Declare declare, Assault assault, Outskirts outskirts, Supply supply, Seat seat,
                                 Abandon abandon, double truceDays, WarHours warHours) {
    public static final long HOUR = 60L * 60L * 1000L;

    /**
     * @param cost        what the declaring leader pays at their Gateway
     * @param noticeHours hours of notice before anything can be struck
     */
    public record Declare(List<Cost> cost, double noticeHours) {
        static final Declare DEFAULT = new Declare(List.of(), 24);
        static final Codec<Declare> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Cost.CODEC.listOf().optionalFieldOf("cost", DEFAULT.cost).forGetter(Declare::cost),
                Codec.doubleRange(0, 24 * 30).optionalFieldOf("notice_hours", DEFAULT.noticeHours).forGetter(Declare::noticeHours)
        ).apply(instance, Declare::new));

        public Declare {
            cost = List.copyOf(cost);
        }
    }

    /**
     * @param noticeHours    the least time ahead an assault must be announced
     * @param maxAheadDays   the furthest ahead it may be announced
     * @param windowMinutes  how long the assault lasts once it begins
     * @param channelSeconds attacker-seconds at the Gateway that take the seat
     * @param channelRadius  how close to the Gateway attackers channel and defenders break it
     * @param cooldownHours  how long after a failed assault before the same clan may announce another
     */
    public record Assault(double noticeHours, double maxAheadDays, int windowMinutes, int channelSeconds, int channelRadius,
                          double cooldownHours) {
        static final Assault DEFAULT = new Assault(12, 7, 60, 120, 6, 72);
        static final Codec<Assault> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.doubleRange(0, 24 * 30).optionalFieldOf("notice_hours", DEFAULT.noticeHours).forGetter(Assault::noticeHours),
                Codec.doubleRange(0, 365).optionalFieldOf("max_ahead_days", DEFAULT.maxAheadDays).forGetter(Assault::maxAheadDays),
                Codec.intRange(1, 24 * 60).optionalFieldOf("window_minutes", DEFAULT.windowMinutes).forGetter(Assault::windowMinutes),
                Codec.intRange(1, 3600).optionalFieldOf("channel_seconds", DEFAULT.channelSeconds).forGetter(Assault::channelSeconds),
                Codec.intRange(1, 32).optionalFieldOf("channel_radius", DEFAULT.channelRadius).forGetter(Assault::channelRadius),
                Codec.doubleRange(0, 24 * 60).optionalFieldOf("cooldown_hours", DEFAULT.cooldownHours).forGetter(Assault::cooldownHours)
        ).apply(instance, Assault::new));
    }

    /**
     * @param raidSeconds   attacker-seconds on a Citadel's land, unanswered, that make a raid
     * @param damaged       how many built upgrades a raid damages
     * @param cooldownHours how long a raided Citadel is spared further raids
     */
    public record Outskirts(int raidSeconds, int damaged, double cooldownHours) {
        static final Outskirts DEFAULT = new Outskirts(90, 1, 12);
        static final Codec<Outskirts> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(1, 3600).optionalFieldOf("raid_seconds", DEFAULT.raidSeconds).forGetter(Outskirts::raidSeconds),
                Codec.intRange(0, 16).optionalFieldOf("damaged", DEFAULT.damaged).forGetter(Outskirts::damaged),
                Codec.doubleRange(0, 24 * 30).optionalFieldOf("cooldown_hours", DEFAULT.cooldownHours).forGetter(Outskirts::cooldownHours)
        ).apply(instance, Outskirts::new));
    }

    /**
     * @param anchorDistance how far north of the Gateway, outside the walls, its Link Anchor stands
     * @param strikeSeconds  seconds an enemy must stand at the anchor, unanswered, to cut it
     * @param strikeRadius   how close to the anchor they must stand
     * @param guardRadius    a member this close to the anchor stops the strike
     * @param cutDrain       how many times faster a Gateway eats while its anchor is cut
     * @param repairCost     what a member pays at the anchor to mend it
     */
    public record Supply(int anchorDistance, int strikeSeconds, int strikeRadius, int guardRadius, double cutDrain, List<Cost> repairCost) {
        static final Supply DEFAULT = new Supply(28, 20, 3, 16, 2.0, List.of());
        static final Codec<Supply> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(4, 128).optionalFieldOf("anchor_distance", DEFAULT.anchorDistance).forGetter(Supply::anchorDistance),
                Codec.intRange(1, 3600).optionalFieldOf("strike_seconds", DEFAULT.strikeSeconds).forGetter(Supply::strikeSeconds),
                Codec.intRange(1, 16).optionalFieldOf("strike_radius", DEFAULT.strikeRadius).forGetter(Supply::strikeRadius),
                Codec.intRange(1, 128).optionalFieldOf("guard_radius", DEFAULT.guardRadius).forGetter(Supply::guardRadius),
                Codec.doubleRange(1, 100).optionalFieldOf("cut_drain", DEFAULT.cutDrain).forGetter(Supply::cutDrain),
                Cost.CODEC.listOf().optionalFieldOf("repair_cost", DEFAULT.repairCost).forGetter(Supply::repairCost)
        ).apply(instance, Supply::new));

        public Supply {
            repairCost = List.copyOf(repairCost);
        }
    }

    /** @param moveCooldownDays the least time between two moves of a clan's seat */
    public record Seat(double moveCooldownDays) {
        static final Seat DEFAULT = new Seat(7);
        static final Codec<Seat> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.doubleRange(0, 365).optionalFieldOf("move_cooldown_days", DEFAULT.moveCooldownDays).forGetter(Seat::moveCooldownDays)
        ).apply(instance, Seat::new));
    }

    /**
     * @param warnHours      hours with no member logged in before the Spell warns both clans
     * @param abandonedHours hours with no member logged in before the seat turns Abandoned
     * @param forsakenOpen   whether a seat at the last stage of neglect may be assaulted with no notice
     */
    public record Abandon(double warnHours, double abandonedHours, boolean forsakenOpen) {
        static final Abandon DEFAULT = new Abandon(24, 72, true);
        static final Codec<Abandon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.doubleRange(0, 24 * 365).optionalFieldOf("warn_hours", DEFAULT.warnHours).forGetter(Abandon::warnHours),
                Codec.doubleRange(1, 24 * 365).optionalFieldOf("abandoned_hours", DEFAULT.abandonedHours).forGetter(Abandon::abandonedHours),
                Codec.BOOL.optionalFieldOf("forsaken_open", DEFAULT.forsakenOpen).forGetter(Abandon::forsakenOpen)
        ).apply(instance, Abandon::new));
    }

    /**
     * Fixed server-wide war hours (UTC): when on, seats may be assaulted
     * only in these hours, every day, and need no announcement.
     *
     * @param startHour the hour they begin, 0 to 23
     * @param endHour   the hour they end; earlier than the start wraps past midnight
     */
    public record WarHours(boolean enabled, int startHour, int endHour) {
        static final WarHours DEFAULT = new WarHours(false, 18, 22);
        static final Codec<WarHours> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("enabled", DEFAULT.enabled).forGetter(WarHours::enabled),
                Codec.intRange(0, 23).optionalFieldOf("start_hour", DEFAULT.startHour).forGetter(WarHours::startHour),
                Codec.intRange(0, 23).optionalFieldOf("end_hour", DEFAULT.endHour).forGetter(WarHours::endHour)
        ).apply(instance, WarHours::new));

        /** Whether this moment falls in the war hours. */
        public boolean contains(long epochMillis) {
            int hour = Instant.ofEpochMilli(epochMillis).atOffset(ZoneOffset.UTC).getHour();
            if (startHour == endHour) {
                return true;
            }
            return startHour < endHour ? hour >= startHour && hour < endHour : hour >= startHour || hour < endHour;
        }
    }

    public static final CitadelWarSettings DEFAULT = new CitadelWarSettings(true, Declare.DEFAULT, Assault.DEFAULT, Outskirts.DEFAULT,
            Supply.DEFAULT, Seat.DEFAULT, Abandon.DEFAULT, 7, WarHours.DEFAULT);

    public static final Codec<CitadelWarSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("enabled", DEFAULT.enabled).forGetter(CitadelWarSettings::enabled),
            Declare.CODEC.optionalFieldOf("declare", DEFAULT.declare).forGetter(CitadelWarSettings::declare),
            Assault.CODEC.optionalFieldOf("assault", DEFAULT.assault).forGetter(CitadelWarSettings::assault),
            Outskirts.CODEC.optionalFieldOf("outskirts", DEFAULT.outskirts).forGetter(CitadelWarSettings::outskirts),
            Supply.CODEC.optionalFieldOf("supply", DEFAULT.supply).forGetter(CitadelWarSettings::supply),
            Seat.CODEC.optionalFieldOf("seat", DEFAULT.seat).forGetter(CitadelWarSettings::seat),
            Abandon.CODEC.optionalFieldOf("abandon", DEFAULT.abandon).forGetter(CitadelWarSettings::abandon),
            Codec.doubleRange(0, 365).optionalFieldOf("truce_days", DEFAULT.truceDays).forGetter(CitadelWarSettings::truceDays),
            WarHours.CODEC.optionalFieldOf("war_hours", DEFAULT.warHours).forGetter(CitadelWarSettings::warHours)
    ).apply(instance, CitadelWarSettings::new));

    /** Hours as milliseconds. */
    public static long hours(double hours) {
        return Math.round(hours * HOUR);
    }
}
