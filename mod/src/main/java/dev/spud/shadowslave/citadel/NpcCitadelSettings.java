package dev.spud.shadowslave.citadel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * NPC-held Citadels (DESIGN, Jack 2026-10-31), read from
 * {@code data/<ns>/citadel/npc.json}; a server retunes the world's mix and
 * how many residents walk about without code. Lesser Citadels are run by
 * Lesser Legacy Clans, each under a Great Clan's influence (CANON, ch. 365);
 * a Citadel is a settlement whose residents share the wall watch (CANON,
 * ch. 398).
 *
 * @param innerRadius     how far from the realm's centre the inner ring reaches (the Ashen Expanse)
 * @param inner           the inner ring's mix of held, wild and fallen Citadels
 * @param outer           the mix beyond it
 * @param maxPresent      most residents of one Citadel who stand in the world at once (the rest are counted, not spawned)
 * @param presenceRadius  how close a player must come for a held Citadel's people to stand at their posts
 * @param guards          guards a held Citadel starts with
 * @param residents       residents (traders among them) it starts with
 * @param regrowMinutes   real minutes for one lost guard or resident to be replaced
 * @param clanReach       how far apart two Citadels of one NPC clan may stand
 * @param clanMaxCitadels most Citadels one NPC clan holds at the start
 * @param fallenCreatures Nightmare Creatures prowling a fallen Citadel when it wakes
 * @param standing        how standing with an NPC clan is earned and lost
 * @param greatClans      the Great Clans every NPC clan answers to (original names, Jack 2026-10-31)
 * @param clanNames       names an NPC Lesser Legacy Clan can take
 * @param givenNames      names its people can take
 * @param natures         Natures its lord can be named as holding
 */
public record NpcCitadelSettings(int innerRadius, Mix inner, Mix outer, int maxPresent, int presenceRadius, int guards, int residents,
                                 int regrowMinutes, int clanReach, int clanMaxCitadels, int fallenCreatures, Standing standing,
                                 List<String> greatClans, List<String> clanNames, List<String> givenNames, List<String> natures) {
    public static final NpcCitadelSettings DEFAULT = new NpcCitadelSettings(1000, new Mix(50, 35, 15), new Mix(20, 60, 20), 40, 96, 6, 8,
            20, 1500, 3, 3, Standing.DEFAULT,
            List.of("The Pale Crown", "House of Embers", "The Iron Vigil"),
            List.of("Ashveil", "Greymantle", "Vesper", "Thornhall", "Cindermark"),
            List.of("Aren", "Brisa", "Corvin", "Dael", "Edda", "Fen", "Hale", "Ines"),
            List.of("Warden", "Seer", "Forge", "Stone", "Tide", "Ember"));

    public static final Codec<NpcCitadelSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, 100_000).optionalFieldOf("inner_radius", DEFAULT.innerRadius).forGetter(NpcCitadelSettings::innerRadius),
            Mix.CODEC.optionalFieldOf("inner", DEFAULT.inner).forGetter(NpcCitadelSettings::inner),
            Mix.CODEC.optionalFieldOf("outer", DEFAULT.outer).forGetter(NpcCitadelSettings::outer),
            Codec.intRange(0, 200).optionalFieldOf("max_present", DEFAULT.maxPresent).forGetter(NpcCitadelSettings::maxPresent),
            Codec.intRange(16, 256).optionalFieldOf("presence_radius", DEFAULT.presenceRadius).forGetter(NpcCitadelSettings::presenceRadius),
            Codec.intRange(0, 64).optionalFieldOf("guards", DEFAULT.guards).forGetter(NpcCitadelSettings::guards),
            Codec.intRange(0, 200).optionalFieldOf("residents", DEFAULT.residents).forGetter(NpcCitadelSettings::residents),
            Codec.intRange(1, 10_000).optionalFieldOf("regrow_minutes", DEFAULT.regrowMinutes).forGetter(NpcCitadelSettings::regrowMinutes),
            Codec.intRange(0, 100_000).optionalFieldOf("clan_reach", DEFAULT.clanReach).forGetter(NpcCitadelSettings::clanReach),
            Codec.intRange(1, 64).optionalFieldOf("clan_max_citadels", DEFAULT.clanMaxCitadels).forGetter(NpcCitadelSettings::clanMaxCitadels),
            Codec.intRange(0, 16).optionalFieldOf("fallen_creatures", DEFAULT.fallenCreatures).forGetter(NpcCitadelSettings::fallenCreatures),
            Standing.CODEC.optionalFieldOf("standing", DEFAULT.standing).forGetter(NpcCitadelSettings::standing),
            Codec.STRING.listOf(1, 64).optionalFieldOf("great_clans", DEFAULT.greatClans).forGetter(NpcCitadelSettings::greatClans),
            Codec.STRING.listOf(1, 256).optionalFieldOf("clan_names", DEFAULT.clanNames).forGetter(NpcCitadelSettings::clanNames),
            Codec.STRING.listOf(1, 512).optionalFieldOf("given_names", DEFAULT.givenNames).forGetter(NpcCitadelSettings::givenNames),
            Codec.STRING.listOf(1, 64).optionalFieldOf("natures", DEFAULT.natures).forGetter(NpcCitadelSettings::natures)
    ).apply(instance, NpcCitadelSettings::new));

    /** The ring a Citadel at this distance from the centre belongs to. */
    public Mix mixAt(double distance) {
        return distance < innerRadius ? inner : outer;
    }

    /** Weights for what a generated Citadel wakes as: held by an NPC clan, wild under its Guardian, or fallen to ruin. */
    public record Mix(int held, int wild, int fallen) {
        public static final Codec<Mix> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, 1000).optionalFieldOf("held", 0).forGetter(Mix::held),
                Codec.intRange(0, 1000).optionalFieldOf("wild", 1).forGetter(Mix::wild),
                Codec.intRange(0, 1000).optionalFieldOf("fallen", 0).forGetter(Mix::fallen)
        ).apply(instance, Mix::new));

        /** Picks a kind for a roll in [0, 1). */
        public NpcCitadels.Kind pick(double roll) {
            int total = held + wild + fallen;
            if (total <= 0) {
                return NpcCitadels.Kind.WILD;
            }
            double at = roll * total;
            if (at < held) {
                return NpcCitadels.Kind.HELD;
            }
            return at < held + wild ? NpcCitadels.Kind.WILD : NpcCitadels.Kind.FALLEN;
        }
    }

    /**
     * Standing with an NPC clan: thresholds for each step, and what moves it.
     *
     * @param known   points to be Known
     * @param trusted points to be Trusted
     * @param sworn   points to be Sworn
     * @param trade   gained for each trade with its trader
     * @param hunt    gained for each Nightmare Creature slain on its land, per rank above Dormant plus one
     * @param hurt    lost for striking one of its people
     * @param kill    lost for killing one of them
     */
    public record Standing(int known, int trusted, int sworn, int trade, int hunt, int hurt, int kill) {
        public static final Standing DEFAULT = new Standing(10, 30, 60, 1, 1, 5, 25);

        public static final Codec<Standing> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(1, 100_000).optionalFieldOf("known", DEFAULT.known).forGetter(Standing::known),
                Codec.intRange(1, 100_000).optionalFieldOf("trusted", DEFAULT.trusted).forGetter(Standing::trusted),
                Codec.intRange(1, 100_000).optionalFieldOf("sworn", DEFAULT.sworn).forGetter(Standing::sworn),
                Codec.intRange(0, 1000).optionalFieldOf("trade", DEFAULT.trade).forGetter(Standing::trade),
                Codec.intRange(0, 1000).optionalFieldOf("hunt", DEFAULT.hunt).forGetter(Standing::hunt),
                Codec.intRange(0, 1000).optionalFieldOf("hurt", DEFAULT.hurt).forGetter(Standing::hurt),
                Codec.intRange(0, 1000).optionalFieldOf("kill", DEFAULT.kill).forGetter(Standing::kill)
        ).apply(instance, Standing::new));
    }
}
