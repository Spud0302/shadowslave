package dev.spud.shadowslave.soul;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SoulRitualTransitionTest {
    private static final ResourceLocation ASPECT = ResourceLocation.fromNamespaceAndPath("shadowslave", "generated/aspect/a");
    private static final ResourceLocation FLAW = ResourceLocation.fromNamespaceAndPath("shadowslave", "generated/flaw/f");
    private static final ResourceLocation OTHER_ASPECT = ResourceLocation.fromNamespaceAndPath("shadowslave", "generated/aspect/b");

    @Test
    void ritualTakesAnUninfectedPlayerStraightToDormantDreamer() {
        SoulData next = SoulTransitions.awakenThroughRitual(SoulData.uninfected(), ASPECT, SoulRank.DORMANT, FLAW);
        assertEquals(SpellState.DREAMER, next.spellState());
        assertEquals(Optional.of(SoulRank.DORMANT), next.soulRank());
        assertEquals(Optional.of(ASPECT), next.aspectId());
        assertEquals(Optional.of(FLAW), next.flawId());
    }

    @Test
    void ritualWorksFromCarrierToo() {
        SoulData carrier = SoulTransitions.infect(SoulData.uninfected());
        assertEquals(SpellState.DREAMER,
                SoulTransitions.awakenThroughRitual(carrier, ASPECT, SoulRank.DORMANT, FLAW).spellState());
    }

    @Test
    void rerollReplacesIdentityButKeepsTheReachedStage() {
        SoulData dreamer = SoulTransitions.awakenThroughRitual(SoulData.uninfected(), ASPECT, SoulRank.DORMANT, FLAW);
        SoulData rerolled = SoulTransitions.awakenThroughRitual(dreamer, OTHER_ASPECT, SoulRank.DORMANT, FLAW);
        assertEquals(SpellState.DREAMER, rerolled.spellState());
        assertEquals(Optional.of(OTHER_ASPECT), rerolled.aspectId());
    }

    @Test
    void awakeningRaisesTheSoulAndAspectButKeepsTheIdentity() {
        SoulData dreamer = SoulTransitions.awakenThroughRitual(SoulData.uninfected(), ASPECT, SoulRank.DORMANT, FLAW);
        SoulData awakened = SoulTransitions.awaken(dreamer);
        assertEquals(SpellState.AWAKENED, awakened.spellState());
        assertEquals(Optional.of(SoulRank.AWAKENED), awakened.soulRank());
        assertEquals(Optional.of(SoulRank.AWAKENED), awakened.aspectRank());
        assertEquals(Optional.of(ASPECT), awakened.aspectId());
        assertEquals(Optional.of(FLAW), awakened.flawId());
    }

    @Test
    void onlyADreamerCanAwaken() {
        assertThrows(IllegalStateException.class, () -> SoulTransitions.awaken(SoulData.uninfected()));
        SoulData dreamer = SoulTransitions.awakenThroughRitual(SoulData.uninfected(), ASPECT, SoulRank.DORMANT, FLAW);
        SoulData awakened = SoulTransitions.awaken(dreamer);
        assertThrows(IllegalStateException.class, () -> SoulTransitions.awaken(awakened));
    }
}
