package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ability.AbilityRegistry;
import dev.spud.shadowslave.ability.AspectAbility;
import dev.spud.shadowslave.awakening.AspectKit;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.network.payload.AbilityStatusPayload;
import dev.spud.shadowslave.network.payload.CoreStatusPayload;
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
        NAME, SUBTITLE, HEADING, BODY, VOICE, ROW, METER, SPACER
    }

    /**
     * One laid-out line. {@code value} is only used by ROW and METER entries;
     * {@code icon} is an ability or Flaw effect id whose icon sits beside the
     * text, or empty; {@code fill} is how full a METER's bar is, from 0 to 1.
     */
    public record Entry(Style style, String text, String value, int colour, String icon, float fill) {
        static Entry of(Style style, String text, int colour) {
            return new Entry(style, text, "", colour, "", 0.0F);
        }

        static Entry withIcon(Style style, String text, int colour, String icon) {
            return new Entry(style, text, "", colour, icon, 0.0F);
        }

        static Entry row(String label, String value, int colour) {
            return new Entry(Style.ROW, label, value, colour, "", 0.0F);
        }

        static Entry meter(String label, String value, int colour, float fill) {
            return new Entry(Style.METER, label, value, colour, "", Math.max(0.0F, Math.min(1.0F, fill)));
        }

        static Entry spacer() {
            return new Entry(Style.SPACER, "", "", 0, "", 0.0F);
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

    /**
     * @param essence  the pool as the client predicts it now
     * @param keyNames the name of each ability key, in slot order
     */
    public static List<Entry> page(Tab tab, SoulSnapshot snapshot, AbilityStatusPayload status, float essence,
                                   List<String> keyNames, CoreStatusPayload core) {
        return switch (tab) {
            case ASPECT -> aspect(snapshot);
            case ABILITIES -> abilities(snapshot, status, essence, keyNames);
            case FLAW -> flaw(snapshot);
            case SOUL -> soul(snapshot, status, essence, core);
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

    static List<Entry> abilities(SoulSnapshot snapshot, AbilityStatusPayload status, float essence,
                                 List<String> keyNames) {
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
            int slot = slotOf(status, id);
            int cost = slot >= 0 ? status.slots().get(slot).cost() : ability.get().essenceCost();
            String key = slot >= 0 && slot < keyNames.size() ? "Key " + keyNames.get(slot) + "  ·  " : "";
            entries.add(Entry.of(Style.BODY, key + "Costs " + cost + " essence  ·  " + readiness(status, slot, cost, essence),
                    SpellStyle.HINT));
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

    private static int slotOf(AbilityStatusPayload status, String id) {
        for (int i = 0; i < status.slots().size(); i++) {
            if (status.slots().get(i).abilityId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private static String readiness(AbilityStatusPayload status, int slot, int cost, float essence) {
        if (slot >= 0 && status.slots().get(slot).primed()) {
            return "Primed";
        }
        return essence + 1.0e-3F >= cost ? "Ready" : "Gathering essence";
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

    static List<Entry> soul(SoulSnapshot snapshot, AbilityStatusPayload status, float essence, CoreStatusPayload core) {
        List<Entry> entries = new ArrayList<>(List.of(
                Entry.row("Status", humanize(snapshot.spellState()), SpellStyle.TEXT),
                Entry.row("Path", humanize(snapshot.awakeningPath()), SpellStyle.TEXT),
                Entry.row("Soul Rank", humanize(snapshot.soulRank()), SpellStyle.TEXT),
                Entry.row("Aspect Rank", humanize(snapshot.aspectRank()), SpellStyle.ASPECT)
        ));
        if (core.hasCore()) {
            entries.add(Entry.meter("Core", coreLabel(core), SpellStyle.ASPECT, core.fraction()));
        }
        if (status.capacity() > 0) {
            entries.add(Entry.row("Essence", Math.round(Math.floor(essence)) + " / " + status.capacity(), SpellStyle.ABILITY));
        }
        entries.add(Entry.row("Origin", snapshot.importedFromDatapack() ? "Imported datapack identity" : "Native Java identity",
                SpellStyle.MUTED));
        return entries;
    }

    static String coreLabel(CoreStatusPayload core) {
        return core.saturated() ? "Saturated" : Math.round(Math.floor(100.0 * core.fraction())) + "%";
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
