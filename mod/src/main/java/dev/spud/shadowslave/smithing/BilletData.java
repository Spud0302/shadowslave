package dev.spud.shadowslave.smithing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.spud.shadowslave.weaving.WeavingRecipe;
import net.minecraft.resources.ResourceLocation;

/**
 * A piece being forged, carried on the billet item. It holds everything the
 * work needs so the client can draw the hammer bar and heat without the
 * server's pattern data.
 *
 * @param pattern     the Smithing pattern
 * @param name        the pattern's name, for the tooltip
 * @param heat        the heat when last measured, in degrees
 * @param stamp       the game time it was measured at
 * @param coolPercent how fast it cools from there, 100 being normal
 * @param heatMin     the working band's floor; 0 with {@code heatMax} 0 is cold work
 * @param heatMax     the working band's ceiling
 * @param blows       landed blows that shape it
 * @param quench      whether it is quenched once shaped
 * @param quenchTicks how long a perfect quench is held
 * @param zone        the clean zone's half width, in thousandths of the bar
 * @param seed        varies where each blow wants to land
 * @param clean       clean blows so far
 * @param rough       rough blows so far
 * @param misses      missed blows so far
 * @param inBand      landed blows struck inside the working band
 * @param burnTicks   ticks spent burning in the hearth
 * @param burned      whether it burned, which costs heat score once
 * @param masterwork  a Forge's Masterwork was spent on it
 * @param smith       who laid it
 */
public record BilletData(
        ResourceLocation pattern, String name, int heat, long stamp, int coolPercent,
        int heatMin, int heatMax, int blows, boolean quench, int quenchTicks, int zone, int seed,
        int clean, int rough, int misses, int inBand, int burnTicks, boolean burned, boolean masterwork, String smith
) {
    private static final Codec<BilletData> FIRST = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("pattern").forGetter(BilletData::pattern),
            Codec.STRING.fieldOf("name").forGetter(BilletData::name),
            Codec.INT.fieldOf("heat").forGetter(BilletData::heat),
            Codec.LONG.fieldOf("stamp").forGetter(BilletData::stamp),
            Codec.INT.optionalFieldOf("cool_percent", 100).forGetter(BilletData::coolPercent),
            Codec.INT.fieldOf("heat_min").forGetter(BilletData::heatMin),
            Codec.INT.fieldOf("heat_max").forGetter(BilletData::heatMax),
            Codec.INT.fieldOf("blows").forGetter(BilletData::blows),
            Codec.BOOL.fieldOf("quench").forGetter(BilletData::quench),
            Codec.INT.fieldOf("quench_ticks").forGetter(BilletData::quenchTicks),
            Codec.INT.fieldOf("zone").forGetter(BilletData::zone),
            Codec.INT.fieldOf("seed").forGetter(BilletData::seed)
    ).apply(instance, (pattern, name, heat, stamp, cool, min, max, blows, quench, ticks, zone, seed) -> new BilletData(
            pattern, name, heat, stamp, cool, min, max, blows, quench, ticks, zone, seed, 0, 0, 0, 0, 0, false, false, "")));

    private record Progress(int clean, int rough, int misses, int inBand, int burnTicks, boolean burned,
                            boolean masterwork, String smith) {
        static final Codec<Progress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.optionalFieldOf("clean", 0).forGetter(Progress::clean),
                Codec.INT.optionalFieldOf("rough", 0).forGetter(Progress::rough),
                Codec.INT.optionalFieldOf("misses", 0).forGetter(Progress::misses),
                Codec.INT.optionalFieldOf("in_band", 0).forGetter(Progress::inBand),
                Codec.INT.optionalFieldOf("burn_ticks", 0).forGetter(Progress::burnTicks),
                Codec.BOOL.optionalFieldOf("burned", false).forGetter(Progress::burned),
                Codec.BOOL.optionalFieldOf("masterwork", false).forGetter(Progress::masterwork),
                Codec.STRING.optionalFieldOf("smith", "").forGetter(Progress::smith)
        ).apply(instance, Progress::new));
    }

    /** Split in two because a record codec takes at most sixteen fields. */
    public static final Codec<BilletData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FIRST.fieldOf("work").forGetter(billet -> billet),
            Progress.CODEC.fieldOf("progress").forGetter(billet -> new Progress(billet.clean, billet.rough, billet.misses,
                    billet.inBand, billet.burnTicks, billet.burned, billet.masterwork, billet.smith))
    ).apply(instance, BilletData::withProgress));

    private BilletData withProgress(Progress progress) {
        return new BilletData(pattern, name, heat, stamp, coolPercent, heatMin, heatMax, blows, quench, quenchTicks, zone, seed,
                progress.clean(), progress.rough(), progress.misses(), progress.inBand(), progress.burnTicks(), progress.burned(),
                progress.masterwork(), progress.smith());
    }

    /** A fresh billet of {@code recipe}, laid at {@code time}. Cold work starts as warm as it needs to be. */
    public static BilletData lay(ResourceLocation id, String name, WeavingRecipe.Forging forging, long time, int zone, int seed,
                                 boolean masterwork, String smith) {
        return new BilletData(id, name, SmithingRules.AMBIENT, time, 100, forging.heatMin(), forging.heatMax(), forging.blows(),
                forging.quench(), forging.quenchTicks(), zone, seed, 0, 0, 0, 0, 0, false, masterwork, smith);
    }

    public boolean cold() {
        return heatMax == 0;
    }

    /** The heat now, cooling from the last measure. */
    public int heatAt(long time) {
        return SmithingRules.cooled(heat, time - stamp, coolPercent);
    }

    public int landed() {
        return clean + rough;
    }

    /** All the blows it needs are in. */
    public boolean shaped() {
        return landed() >= blows;
    }

    public boolean workable(long time) {
        return cold() || heatAt(time) >= heatMin;
    }

    public boolean inBand(int heatNow) {
        return cold() || heatNow >= heatMin && heatNow <= heatMax;
    }

    /** Where the next blow wants to land. */
    public float target() {
        return SmithingRules.target(seed, landed() + misses);
    }

    BilletData measured(int heatNow, long time, int cool) {
        return new BilletData(pattern, name, heatNow, time, cool, heatMin, heatMax, blows, quench, quenchTicks, zone, seed,
                clean, rough, misses, inBand, burnTicks, burned, masterwork, smith);
    }

    BilletData burning(int heatNow, long time) {
        int ticks = burnTicks + 1;
        return new BilletData(pattern, name, heatNow, time, coolPercent, heatMin, heatMax, blows, quench, quenchTicks, zone, seed,
                clean, rough, misses, inBand, ticks, burned || ticks >= SmithingRules.BURN_TICKS, masterwork, smith);
    }

    BilletData struck(SmithingRules.Blow blow, int heatNow, long time, int cool) {
        boolean band = blow != SmithingRules.Blow.MISS && inBand(heatNow);
        int after = cold() ? heatNow : Math.max(SmithingRules.AMBIENT, heatNow - SmithingRules.BLOW_COOL);
        return new BilletData(pattern, name, after, time, cool, heatMin, heatMax, blows, quench, quenchTicks, zone, seed,
                clean + (blow == SmithingRules.Blow.CLEAN ? 1 : 0), rough + (blow == SmithingRules.Blow.ROUGH ? 1 : 0),
                misses + (blow == SmithingRules.Blow.MISS ? 1 : 0), inBand + (band ? 1 : 0), burnTicks, burned, masterwork, smith);
    }
}
