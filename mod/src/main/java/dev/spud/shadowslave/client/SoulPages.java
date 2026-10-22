package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ability.AbilityRegistry;
import dev.spud.shadowslave.ability.AspectAbility;
import dev.spud.shadowslave.awakening.AspectKit;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.network.payload.AbilityStatusPayload;
import dev.spud.shadowslave.network.payload.SoulSnapshot;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * What each tab of the Soul screen says, built from the client's read-only
 * snapshot. Pure so it can be tested without a running client; the screen
 * only lays the lines out.
 */
public final class SoulPages {
    public enum Tab {
        ASPECT("Aspect"),
        ABILITIES("Abilities"),
        FLAW("Flaw"),
        SOUL("Soul");

        public final String label;

        Tab(String label) {
            this.label = label;
        }
    }

    public enum Style {
        NAME, SUBTITLE, HEADING, BODY, VOICE, ROW, SPACER
    }

    /**
     * One laid-out line. {@code value} is only used by ROW entries; {@code icon}
     * is an ability or Flaw effect id whose icon sits beside the text, or empty.
     */
    public record Entry(Style style, String text, String value, int colour, String icon) {
        static Entry of(Style style, String text, int colour) {
            return new Entry(style, text, "", colour, "");
        }

        static Entry withIcon(Style style, String text, int colour, String icon) {
            return new Entry(style, text, "", colour, icon);
        }

        static Entry row(String label, String value, int colour) {
            return new Entry(Style.ROW, label, value, colour, "");
        }

        static Entry spacer() {
            return new Entry(Style.SPACER, "", "", 0, "");
        }
    }

    private SoulPages() {
    }

    /** The hand-made kit this snapshot's Aspect was rolled from, if any. */
    public static Optional<AspectKit> kitFor(SoulSnapshot snapshot) {
        return AspectKitCatalog.all().stream()
                .filter(kit -> snapshot.abilityIds().contains(kit.abilityId().toString())
                        || snapshot.abilityIds().contains(kit.innateId().toString()))
                .findFirst();
    }

    public static List<Entry> page(Tab tab, SoulSnapshot snapshot, AbilityStatusPayload status, long remainingTicks,
                                   String keyName) {
        return switch (tab) {
            case ASPECT -> aspect(snapshot);
            case ABILITIES -> abilities(snapshot, status, remainingTicks, keyName);
            case FLAW -> flaw(snapshot);
            case SOUL -> soul(snapshot);
        };
    }

    private static boolean named(SoulSnapshot snapshot) {
        return !snapshot.aspectId().isBlank() || !snapshot.aspectName().isBlank();
    }

    private static List<Entry> unnamed() {
        return List.of(
                Entry.of(Style.VOICE, "Your Aspect has not been named.", SpellStyle.VOICE),
                Entry.spacer(),
                Entry.of(Style.BODY, "Sleep beside a lit Dream Candle, and the Spell will name it when you wake.",
                        SpellStyle.MUTED)
        );
    }

    static List<Entry> aspect(SoulSnapshot snapshot) {
        if (!named(snapshot)) {
            return unnamed();
        }
        Optional<AspectKit> kit = kitFor(snapshot);
        List<Entry> entries = new ArrayList<>();
        entries.add(Entry.of(Style.NAME, snapshot.displayedAspect(), SpellStyle.ASPECT));
        String nature = kit.map(value -> " " + value.natureName()).orElse("");
        entries.add(Entry.of(Style.SUBTITLE, humanize(snapshot.aspectRank()) + nature + " Aspect", SpellStyle.MUTED));
        kit.ifPresent(value -> {
            entries.add(Entry.spacer());
            entries.add(Entry.of(Style.HEADING, "Innate: " + value.innateName(), SpellStyle.TEXT));
            entries.add(Entry.of(Style.BODY, value.innateDescription(), SpellStyle.MUTED));
            entries.add(Entry.spacer());
            entries.add(Entry.of(Style.HEADING, "Ability: " + value.abilityName(), SpellStyle.ABILITY));
            entries.add(Entry.of(Style.HEADING, "Flaw: " + value.flawName(), SpellStyle.FLAW));
        });
        return entries;
    }

    static List<Entry> abilities(SoulSnapshot snapshot, AbilityStatusPayload status, long remainingTicks, String keyName) {
        if (!named(snapshot)) {
            return unnamed();
        }
        Optional<AspectKit> kit = kitFor(snapshot);
        List<Entry> entries = new ArrayList<>();
        for (String id : snapshot.abilityIds()) {
            Optional<AspectAbility> ability = Optional.ofNullable(ResourceLocation.tryParse(id)).flatMap(AbilityRegistry::get);
            if (ability.isEmpty()) {
                continue;
            }
            String description = kit.filter(value -> value.abilityId().toString().equals(id))
                    .map(AspectKit::abilityDescription)
                    .orElse("");
            entries.add(Entry.withIcon(Style.HEADING, ability.get().displayName(), SpellStyle.ABILITY, id));
            if (!description.isEmpty()) {
                entries.add(Entry.of(Style.BODY, description, SpellStyle.MUTED));
            }
            String state = readiness(id, status, remainingTicks);
            entries.add(Entry.of(Style.BODY, "Key " + keyName + "  ·  Cooldown " + seconds(ability.get().cooldownTicks())
                    + "s  ·  " + state, SpellStyle.HINT));
            entries.add(Entry.spacer());
        }
        if (entries.isEmpty()) {
            entries.add(Entry.of(Style.BODY, "Your Aspect has no ability to call on yet.", SpellStyle.MUTED));
            entries.add(Entry.spacer());
        }
        entries.add(Entry.of(Style.HEADING, "Sealed", SpellStyle.HINT));
        entries.add(Entry.of(Style.BODY, "Another ability awakens each time your Aspect reaches a new Rank.", SpellStyle.HINT));
        return entries;
    }

    private static String readiness(String id, AbilityStatusPayload status, long remainingTicks) {
        if (!id.equals(status.abilityId())) {
            return "Ready";
        }
        if (status.primed()) {
            return "Primed";
        }
        return remainingTicks > 0 ? "Recovering " + seconds(remainingTicks) + "s" : "Ready";
    }

    static List<Entry> flaw(SoulSnapshot snapshot) {
        if (!named(snapshot)) {
            return unnamed();
        }
        List<Entry> entries = new ArrayList<>();
        entries.add(Entry.withIcon(Style.NAME, snapshot.displayedFlaw(), SpellStyle.FLAW, snapshot.flawEffectId()));
        entries.add(Entry.spacer());
        kitFor(snapshot).ifPresentOrElse(
                kit -> entries.add(Entry.of(Style.BODY, kit.flawDescription(), SpellStyle.TEXT)),
                () -> entries.add(Entry.of(Style.BODY, compactId(snapshot.flawEffectId()), SpellStyle.TEXT)));
        entries.add(Entry.spacer());
        entries.add(Entry.of(Style.VOICE, "Every gift has its price.", SpellStyle.VOICE));
        return entries;
    }

    static List<Entry> soul(SoulSnapshot snapshot) {
        return List.of(
                Entry.row("Status", humanize(snapshot.spellState()), SpellStyle.TEXT),
                Entry.row("Path", humanize(snapshot.awakeningPath()), SpellStyle.TEXT),
                Entry.row("Soul Rank", humanize(snapshot.soulRank()), SpellStyle.TEXT),
                Entry.row("Aspect Rank", humanize(snapshot.aspectRank()), SpellStyle.ASPECT),
                Entry.row("Origin", snapshot.importedFromDatapack() ? "Imported datapack identity" : "Native Java identity",
                        SpellStyle.MUTED)
        );
    }

    static long seconds(long ticks) {
        return (ticks + 19) / 20;
    }

    static String humanize(String value) {
        if (value == null || value.isBlank() || "—".equals(value)) {
            return "—";
        }
        String spaced = value.replace('_', ' ');
        return spaced.substring(0, 1).toUpperCase(Locale.ROOT) + spaced.substring(1);
    }

    static String compactId(String value) {
        if (value == null || value.isBlank()) {
            return "—";
        }
        int start = Math.max(value.lastIndexOf('/'), value.indexOf(':'));
        return humanize(start >= 0 ? value.substring(start + 1) : value);
    }
}
