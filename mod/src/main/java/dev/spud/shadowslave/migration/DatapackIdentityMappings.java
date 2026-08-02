package dev.spud.shadowslave.migration;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** Exact score/name/mechanics mappings from the frozen datapack v1.0.0. */
public final class DatapackIdentityMappings {
    private static final String[] ASPECT_NATURE_IDS = {"veiled", "ashen", "pale", "restless"};
    private static final String[] ASPECT_NATURE_NAMES = {"Veiled", "Ashen", "Pale", "Restless"};
    private static final String[] ASPECT_ARCHETYPE_IDS = {"witness", "bearer", "warden", "wanderer"};
    private static final String[] ASPECT_ARCHETYPE_NAMES = {"Witness", "Bearer", "Warden", "Wanderer"};
    private static final String[] ASPECT_ROOTS = {"shadow", "flame", "bone", "wind"};

    private static final String[][] FLAW_NAMES = {
            {"Nightbound", "Pale Dawn", "Sunshy", "Dusk's Debt"},
            {"Brittle Vessel", "Cracked Heart", "Borrowed Blood", "Thin Thread"},
            {"Hollow Maw", "Bottomless Hunger", "Empty Feast", "Gnawing Soul"},
            {"Leadbound", "Heavy Step", "Shackled Pace", "Burdened Road"}
    };
    private static final String[] FLAW_FAMILIES = {
            "daylight_burden", "fragile_vessel", "ravenous_hunger", "burdened_movement"
    };
    private static final String[] FLAW_EFFECTS = {"daylight", "fragile", "ravenous", "burdened"};

    /**
     * The frozen generator writes one mechanics tag alongside every score,
     * including modern two-digit identities. A missing tag is inconsistent
     * evidence and must fail closed rather than inventing the mechanics family.
     */
    private static final String[] ASPECT_MECHANICS_TAGS = {
            "ss_aspect_shadow", "ss_aspect_flame", "ss_aspect_bone", "ss_aspect_wind"
    };
    private static final String[] FLAW_MECHANICS_TAGS = {
            "ss_flaw_shadow_slave", "ss_flaw_fragile", "ss_flaw_ravenous", "ss_flaw_weightless"
    };

    private DatapackIdentityMappings() {
    }

    public static ImportedAspect aspect(LegacyDatapackSnapshot snapshot) {
        int score = snapshot.aspectScore();
        if (score >= 1 && score <= 4) {
            int index = score - 1;
            requireTag(snapshot, ASPECT_MECHANICS_TAGS[index], "legacy Aspect score " + score);
            String root = ASPECT_ROOTS[index];
            return new ImportedAspect(
                    instanceId(snapshot.playerId(), "aspect", score),
                    capitalize(root),
                    SoulRank.DORMANT,
                    root,
                    "legacy_prototype",
                    id("prototype/aspect_root/" + root),
                    score,
                    true
            );
        }

        int family = score / 10;
        int variant = score % 10;
        if (family < 1 || family > 4 || variant < 1 || variant > 4) {
            throw new IllegalArgumentException("Unsupported datapack Aspect score: " + score);
        }

        int familyIndex = family - 1;
        int variantIndex = variant - 1;
        requireTag(snapshot, ASPECT_MECHANICS_TAGS[familyIndex], "generated Aspect score " + score);
        return new ImportedAspect(
                instanceId(snapshot.playerId(), "aspect", score),
                ASPECT_NATURE_NAMES[familyIndex] + " " + ASPECT_ARCHETYPE_NAMES[variantIndex],
                SoulRank.DORMANT,
                ASPECT_NATURE_IDS[familyIndex],
                ASPECT_ARCHETYPE_IDS[variantIndex],
                id("prototype/aspect_root/" + ASPECT_ROOTS[familyIndex]),
                score,
                false
        );
    }

    public static ImportedFlaw flaw(LegacyDatapackSnapshot snapshot) {
        int score = snapshot.flawScore();
        if (score >= 1 && score <= 4) {
            int index = score - 1;
            requireTag(snapshot, FLAW_MECHANICS_TAGS[index], "legacy Flaw score " + score);
            return new ImportedFlaw(
                    instanceId(snapshot.playerId(), "flaw", score),
                    FLAW_NAMES[index][0],
                    FLAW_FAMILIES[index],
                    id("prototype/flaw_effect/" + FLAW_EFFECTS[index]),
                    score,
                    true
            );
        }

        int family = score / 10;
        int variant = score % 10;
        if (family < 1 || family > 4 || variant < 1 || variant > 4) {
            throw new IllegalArgumentException("Unsupported datapack Flaw score: " + score);
        }

        int familyIndex = family - 1;
        int variantIndex = variant - 1;
        requireTag(snapshot, FLAW_MECHANICS_TAGS[familyIndex], "generated Flaw score " + score);
        return new ImportedFlaw(
                instanceId(snapshot.playerId(), "flaw", score),
                FLAW_NAMES[familyIndex][variantIndex],
                FLAW_FAMILIES[familyIndex],
                id("prototype/flaw_effect/" + FLAW_EFFECTS[familyIndex]),
                score,
                false
        );
    }

    private static ResourceLocation instanceId(UUID playerId, String type, int score) {
        return id("import/datapack/" + playerId + "/" + type + "_" + score);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, path);
    }

    private static void requireTag(LegacyDatapackSnapshot snapshot, String tag, String description) {
        if (!snapshot.hasTag(tag)) {
            throw new IllegalArgumentException(description + " requires compatibility tag " + tag);
        }
    }

    private static String capitalize(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
