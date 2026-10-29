package dev.spud.shadowslave.trial.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * A place in the Dream Realm where history is heavy enough for Nightmares to
 * grow (DESIGN, Jack 2026-10-29; after ch. 23 and 522, where Nightmares are born
 * of the Dream Realm's past and a Seed grows from the remains of something
 * momentous). Read from {@code data/<ns>/dream_realm/echo_site/*.json}.
 *
 * <p>The site stands at {@code x}, {@code z} in the Dream Realm. Its
 * {@code weight} sets the Category of every Seed that grows there (see
 * {@link TrialSettings#categoryFor}), and {@code nightmare} names the hand-made
 * Nightmare inside, which replays the site's past. {@code ruin} is the
 * structure raised there the first time a dreamer comes near: what is left of
 * the place today. {@code lore} is what the Spell says about the place when its
 * Seed is read.</p>
 */
public record EchoSite(String displayName, int x, int z, int weight, ResourceLocation nightmare,
                       Optional<ResourceLocation> ruin, int seedOffsetX, int seedOffsetZ, String lore) {
    public static final Codec<EchoSite> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("display_name").forGetter(EchoSite::displayName),
            Codec.INT.fieldOf("x").forGetter(EchoSite::x),
            Codec.INT.fieldOf("z").forGetter(EchoSite::z),
            Codec.intRange(0, 100).fieldOf("weight").forGetter(EchoSite::weight),
            ResourceLocation.CODEC.fieldOf("nightmare").forGetter(EchoSite::nightmare),
            ResourceLocation.CODEC.optionalFieldOf("ruin").forGetter(EchoSite::ruin),
            Codec.intRange(-64, 64).optionalFieldOf("seed_offset_x", 0).forGetter(EchoSite::seedOffsetX),
            Codec.intRange(-64, 64).optionalFieldOf("seed_offset_z", 0).forGetter(EchoSite::seedOffsetZ),
            Codec.STRING.optionalFieldOf("lore", "").forGetter(EchoSite::lore)
    ).apply(instance, EchoSite::new));
}
