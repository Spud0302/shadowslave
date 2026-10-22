package dev.spud.shadowslave.client;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.network.payload.AbilityStatusPayload;
import dev.spud.shadowslave.network.payload.SoulSnapshot;
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
    void abilitiesTabShowsKeyCooldownAndLiveState() {
        AbilityStatusPayload recovering = new AbilityStatusPayload(KINDLE, "Kindle", 0, 600, false);
        List<String> texts = texts(SoulPages.abilities(ember(), recovering, 205, "R"));
        assertTrue(texts.contains("Kindle"));
        assertTrue(texts.contains("Key R  ·  Cooldown 30s  ·  Recovering 11s"), texts.toString());
        assertTrue(texts.contains("Sealed"));

        AbilityStatusPayload primed = new AbilityStatusPayload(KINDLE, "Kindle", 0, 600, true);
        assertTrue(texts(SoulPages.abilities(ember(), primed, 0, "R")).contains("Key R  ·  Cooldown 30s  ·  Primed"));
    }

    @Test
    void flawTabDescribesTheFlaw() {
        List<String> texts = texts(SoulPages.flaw(ember()));
        assertEquals("Cold Ash", texts.get(0));
        assertTrue(texts.contains(AspectKitCatalog.EMBER.flawDescription()));
    }

    @Test
    void unnamedSoulPointsAtTheDreamCandle() {
        SoulSnapshot none = new SoulSnapshot(1, "dormant", "none", "", "", "", "", List.of(), "", "", "", false);
        for (SoulPages.Tab tab : new SoulPages.Tab[] {SoulPages.Tab.ASPECT, SoulPages.Tab.ABILITIES, SoulPages.Tab.FLAW}) {
            assertEquals("Your Aspect has not been named.",
                    SoulPages.page(tab, none, AbilityStatusPayload.NONE, 0, "R").get(0).text());
        }
        assertEquals("Status", SoulPages.page(SoulPages.Tab.SOUL, none, AbilityStatusPayload.NONE, 0, "R").get(0).text());
    }
}
