package dev.spud.shadowslave.client;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.network.payload.AbilityStatusPayload;
import dev.spud.shadowslave.network.payload.CoreStatusPayload;
import dev.spud.shadowslave.network.payload.SoulSnapshot;
import dev.spud.shadowslave.network.payload.TrueNamePayload;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoulPagesTest {
    private static final String KINDLE = AspectKitCatalog.EMBER.abilityId().toString();

    private static SoulSnapshot ember() {
        return new SoulSnapshot(1, "dreamer", "dream_candle", "", "shadowslave:aspect/x", "Lantern of Ash", "dormant",
                List.of(AspectKitCatalog.EMBER.innateId().toString(), KINDLE),
                "shadowslave:flaw/x", "Cold Ash", AspectKitCatalog.EMBER.flawEffectId().toString(), false);
    }

    private static List<String> texts(List<SoulPages.Entry> entries) {
        return entries.stream().map(SoulPages.Entry::text).toList();
    }

    @Test
    void findsTheKitFromTheHeldAbilities() {
        assertEquals(AspectKitCatalog.EMBER, SoulPages.kitFor(ember()).orElseThrow());
    }

    @Test
    void aspectTabNamesTheAspectRankNatureAndInnate() {
        List<String> texts = texts(SoulPages.aspect(ember()));
        assertEquals("Lantern of Ash", texts.get(0));
        assertEquals("Dormant Ember Aspect", texts.get(1));
        assertTrue(texts.contains("Innate: Ember's Warmth"));
        assertTrue(texts.contains(AspectKitCatalog.EMBER.innateDescription()));
    }

    @Test
    void abilitiesTabShowsKeyCostAndLiveState() {
        AbilityStatusPayload short_ = status(false);
        List<String> texts = texts(SoulPages.abilities(ember(), short_, 20.0F, List.of("R")));
        assertTrue(texts.contains("Kindle"));
        assertTrue(texts.contains("Key R  ·  Costs 35 essence  ·  Gathering essence"), texts.toString());
        assertTrue(texts.contains("Sealed"));
        assertEquals(KINDLE, SoulPages.abilities(ember(), short_, 20.0F, List.of("R")).get(0).icon());

        assertTrue(texts(SoulPages.abilities(ember(), short_, 35.0F, List.of("R")))
                .contains("Key R  ·  Costs 35 essence  ·  Ready"));
        assertTrue(texts(SoulPages.abilities(ember(), status(true), 0.0F, List.of("R")))
                .contains("Key R  ·  Costs 35 essence  ·  Primed"));
    }

    @Test
    void soulTabShowsTheEssencePool() {
        List<SoulPages.Entry> rows = SoulPages.soul(ember(), status(false), 64.7F, new CoreStatusPayload("dormant", 99, 160));
        assertTrue(rows.stream().anyMatch(row -> row.text().equals("Essence") && row.value().equals("64 / 100")),
                rows.toString());
        SoulPages.Entry core = rows.stream().filter(row -> row.style() == SoulPages.Style.METER).findFirst().orElseThrow();
        assertEquals("Core", core.text());
        assertEquals("61%", core.value());
        assertEquals(99.0F / 160.0F, core.fill(), 1.0e-6);
        assertEquals("Saturated", SoulPages.coreLabel(new CoreStatusPayload("dormant", 160, 160)));
    }

    private static AbilityStatusPayload status(boolean primed) {
        return new AbilityStatusPayload(List.of(new AbilityStatusPayload.Slot(KINDLE, "Kindle", 35, primed)),
                20.0F, 100, 0.1F);
    }

    @Test
    void flawTabDescribesTheFlaw() {
        List<String> texts = texts(SoulPages.flaw(ember()));
        assertEquals("Cold Ash", texts.get(0));
        assertEquals(AspectKitCatalog.EMBER.flawEffectId().toString(), SoulPages.flaw(ember()).get(0).icon());
        assertTrue(texts.contains(AspectKitCatalog.EMBER.flawDescription()));
    }

    @Test
    void unnamedSoulPointsAtTheDreamCandle() {
        SoulSnapshot none = new SoulSnapshot(1, "dormant", "none", "", "", "", "", List.of(), "", "", "", false);
        for (SoulPages.Tab tab : new SoulPages.Tab[] {SoulPages.Tab.ASPECT, SoulPages.Tab.ABILITIES, SoulPages.Tab.FLAW}) {
            assertEquals("Your Aspect has not been named.",
                    SoulPages.page(tab, none, AbilityStatusPayload.NONE, 0.0F, List.of("R"), CoreStatusPayload.NONE).get(0).text());
        }
        assertEquals("Status", SoulPages.page(SoulPages.Tab.SOUL, none, AbilityStatusPayload.NONE, 0.0F, List.of("R"), CoreStatusPayload.NONE).get(0).text());
    }

    @Test
    void namePageShowsTheNameItsEffectsAndTheNamesYouKnow() {
        assertEquals("You have no True Name.", texts(SoulPages.trueName(TrueNamePayload.NONE, 0L)).get(0));
        TrueNamePayload payload = new TrueNamePayload("Lone Wolf", "You walked into the Nightmare alone.", "", false, 50, 10, 40,
                List.of(new TrueNamePayload.Known("Smiling Knife", "Alex", 0L), new TrueNamePayload.Known("Iron Hearth", "Sam", 5000L)),
                false);
        List<SoulPages.Entry> entries = SoulPages.trueName(payload, 1000L);
        List<String> texts = texts(entries);
        assertEquals("Lone Wolf", texts.get(0));
        assertEquals("Hidden from others", texts.get(1));
        assertTrue(texts.stream().anyMatch(text -> text.startsWith("Anchored Soul") && text.contains("50%")));
        assertTrue(texts.stream().anyMatch(text -> text.contains("10% more damage")));
        List<SoulPages.Entry> links = entries.stream().filter(entry -> entry.style() == SoulPages.Style.LINK).toList();
        assertEquals(2, links.size());
        assertEquals(SoulPages.SENSE + "Smiling Knife", links.get(0).value());
        assertTrue(links.get(0).text().endsWith("answers"), "sensed long ago, so it answers");
        assertTrue(links.get(1).text().endsWith("quiet"), "sensed today, so it is quiet");
    }
}
