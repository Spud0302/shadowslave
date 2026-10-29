package dev.spud.shadowslave.citadel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.creature.CreatureClass;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Tunables for Citadels and clans, read from {@code data/<ns>/citadel/settings.json}
 * (DESIGN, Jack 2026-10-29). Missing keys keep their defaults. The Guardian's
 * rank is not here: it is the rank of the region the Citadel stands in.
 *
 * @param guardian              the creature that holds an unclaimed Citadel
 * @param guardianClass         the Class it holds it at
 * @param guardRadius           how far from the Gateway the Guardian will roam
 * @param guardianReturnSeconds how long a Guardian may be missing (not slain) before another takes its post
 * @param claimPrioritySeconds  how long only the clan that slew the Guardian may claim the Gateway
 * @param visitRadius           how close to its Gateway a member must come to visit a Citadel
 * @param logRange              how close you must come to a Citadel for your Field Guide to log it
 * @param clanNameMax           longest clan name allowed
 * @param names                 names a Citadel can be known by; each takes one, fixed by where it stands
 */
public record CitadelSettings(ResourceLocation guardian, CreatureClass guardianClass, int guardRadius, int guardianReturnSeconds,
                              int claimPrioritySeconds, int visitRadius, int logRange, int clanNameMax, List<String> names) {
    public static final CitadelSettings DEFAULT = new CitadelSettings(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "ashen_tyrant"), CreatureClass.TYRANT, 14, 10, 300, 24, 96, 24,
            List.of("Ashen Hold", "Greywatch", "Cinder Keep", "Hollow Bastion", "Duskwall", "Grey Spire"));

    public static final Codec<CitadelSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("guardian", DEFAULT.guardian).forGetter(CitadelSettings::guardian),
            CreatureClass.CODEC.optionalFieldOf("guardian_class", DEFAULT.guardianClass).forGetter(CitadelSettings::guardianClass),
            Codec.intRange(4, 64).optionalFieldOf("guard_radius", DEFAULT.guardRadius).forGetter(CitadelSettings::guardRadius),
            Codec.intRange(1, 3600).optionalFieldOf("guardian_return_seconds", DEFAULT.guardianReturnSeconds).forGetter(CitadelSettings::guardianReturnSeconds),
            Codec.intRange(0, 86400).optionalFieldOf("claim_priority_seconds", DEFAULT.claimPrioritySeconds).forGetter(CitadelSettings::claimPrioritySeconds),
            Codec.intRange(4, 256).optionalFieldOf("visit_radius", DEFAULT.visitRadius).forGetter(CitadelSettings::visitRadius),
            Codec.intRange(0, 512).optionalFieldOf("log_range", DEFAULT.logRange).forGetter(CitadelSettings::logRange),
            Codec.intRange(3, 48).optionalFieldOf("clan_name_max", DEFAULT.clanNameMax).forGetter(CitadelSettings::clanNameMax),
            Codec.STRING.listOf(1, 256).optionalFieldOf("names", DEFAULT.names).forGetter(CitadelSettings::names)
    ).apply(instance, CitadelSettings::new));
}
