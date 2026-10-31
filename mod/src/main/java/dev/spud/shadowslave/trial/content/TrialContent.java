package dev.spud.shadowslave.trial.content;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

/**
 * Nightmares and echo sites as data packs describe them. {@link TrialContentLoader}
 * replaces it on every reload.
 */
public record TrialContent(TrialSettings settings, ScarSettings scar, LongNightmareSettings longNightmares,
                           FirstNightmareSettings firstNightmare, AppraisalSettings appraisal,
                           Map<ResourceLocation, NightmarePack> nightmares, Map<ResourceLocation, EchoSite> sites) {
    private static volatile TrialContent current = new TrialContent(TrialSettings.DEFAULT, Map.of(), Map.of());

    public TrialContent {
        nightmares = Map.copyOf(nightmares);
        sites = Map.copyOf(sites);
    }

    public TrialContent(TrialSettings settings, Map<ResourceLocation, NightmarePack> nightmares, Map<ResourceLocation, EchoSite> sites) {
        this(settings, ScarSettings.DEFAULT, LongNightmareSettings.DEFAULT, FirstNightmareSettings.DEFAULT, AppraisalSettings.DEFAULT,
                nightmares, sites);
    }

    public static TrialContent get() {
        return current;
    }

    public static void set(TrialContent content) {
        current = content;
    }

    public Optional<NightmarePack> nightmare(ResourceLocation id) {
        return Optional.ofNullable(nightmares.get(id));
    }

    public Optional<EchoSite> site(ResourceLocation id) {
        return Optional.ofNullable(sites.get(id));
    }

    /** The Category of Seeds at a site. */
    public int category(EchoSite site) {
        return settings.categoryFor(site.weight());
    }
}
