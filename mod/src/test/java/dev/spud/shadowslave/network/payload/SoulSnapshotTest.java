package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulTransitions;
import dev.spud.shadowslave.soul.identity.AspectAbilityData;
import dev.spud.shadowslave.soul.identity.AspectAbilitySetData;
import dev.spud.shadowslave.soul.identity.AspectInstanceData;
import dev.spud.shadowslave.soul.identity.FlawInstanceData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoulSnapshotTest {
    private static final ResourceLocation ASPECT = id("preview/aspect/last_light");
    private static final ResourceLocation FLAW = id("preview/flaw/cold_ash");
    private static final ResourceLocation ABILITY = id("preview/ability/kindle");
    private static final ResourceLocation SECOND_ABILITY = id("preview/ability/ember_sense");
    private static final ResourceLocation EFFECT = id("preview/flaw_effect/cold_ash");

    @Test
    void snapshotSeparatesStatusRanksNamesAbilitiesAndFlawEffect() {
        SoulData imported = SoulTransitions.completeFirstNightmare(
                SoulTransitions.beginFirstNightmare(SoulTransitions.infect(SoulData.uninfected())),
                ASPECT,
                SoulRank.AWAKENED,
                FLAW
        ).markImported(1);
        SoulIdentityData identity = new SoulIdentityData(
                Optional.of(new AspectInstanceData(
                        ASPECT,
                        "Last Light",
                        SoulRank.AWAKENED,
                        id("preview/nature/ember_resolve"),
                        new AspectAbilitySetData(List.of(
                                AspectAbilityData.innate(SECOND_ABILITY, "test"),
                                AspectAbilityData.rankGranted(ABILITY, SoulRank.AWAKENED, "test")
                        )),
                        "test"
                )),
                Optional.of(new FlawInstanceData(FLAW, "Cold Ash", EFFECT, "test"))
        );

        SoulSnapshot snapshot = SoulSnapshot.from(imported, identity);

        assertEquals(SoulData.CURRENT_SCHEMA, snapshot.schemaVersion());
        assertEquals("dreamer", snapshot.spellState());
        assertEquals("nightmare_spell", snapshot.awakeningPath());
        assertEquals("dormant", snapshot.soulRank());
        assertEquals(ASPECT.toString(), snapshot.aspectId());
        assertEquals("Last Light", snapshot.displayedAspect());
        assertEquals("awakened", snapshot.aspectRank());
        assertEquals(List.of(SECOND_ABILITY.toString(), ABILITY.toString()), snapshot.abilityIds());
        assertEquals(SECOND_ABILITY + ", " + ABILITY, snapshot.displayedAbilities());
        assertEquals(snapshot.displayedAbilities(), snapshot.displayedAbility());
        assertEquals(FLAW.toString(), snapshot.flawId());
        assertEquals("Cold Ash", snapshot.displayedFlaw());
        assertEquals(EFFECT.toString(), snapshot.flawEffectId());
        assertTrue(snapshot.importedFromDatapack());
    }

    @Test
    void payloadRoundTripsCompleteAbilitySetWithoutClientSuppliedSoulData() {
        SoulData soul = SoulTransitions.completeFirstNightmare(
                SoulTransitions.beginFirstNightmare(SoulTransitions.infect(SoulData.uninfected())),
                ASPECT,
                SoulRank.AWAKENED,
                FLAW
        );
        SoulIdentityData identity = new SoulIdentityData(
                Optional.of(new AspectInstanceData(
                        ASPECT,
                        "Last Light",
                        SoulRank.AWAKENED,
                        id("preview/nature/ember_resolve"),
                        new AspectAbilitySetData(List.of(
                                AspectAbilityData.innate(SECOND_ABILITY, "test"),
                                AspectAbilityData.rankGranted(ABILITY, SoulRank.AWAKENED, "test")
                        )),
                        "test"
                )),
                Optional.of(new FlawInstanceData(FLAW, "Cold Ash", EFFECT, "test"))
        );
        SoulSnapshotPayload payload = new SoulSnapshotPayload(SoulSnapshot.from(soul, identity), true);
        ByteBuf buffer = Unpooled.buffer();

        try {
            SoulSnapshotPayload.STREAM_CODEC.encode(buffer, payload);
            SoulSnapshotPayload decoded = SoulSnapshotPayload.STREAM_CODEC.decode(buffer);

            assertEquals(payload, decoded);
            assertEquals(List.of(SECOND_ABILITY.toString(), ABILITY.toString()), decoded.snapshot().abilityIds());
            assertTrue(decoded.openScreen());
        } finally {
            buffer.release();
        }
    }

    @Test
    void emptyIdentityRoundTripsAndDisplaysNoAbilities() {
        SoulSnapshot original = SoulSnapshot.from(SoulData.uninfected(), SoulIdentityData.empty());
        SoulSnapshotPayload payload = new SoulSnapshotPayload(original, true);
        ByteBuf buffer = Unpooled.buffer();

        try {
            SoulSnapshotPayload.STREAM_CODEC.encode(buffer, payload);
            SoulSnapshotPayload decoded = SoulSnapshotPayload.STREAM_CODEC.decode(buffer);

            assertEquals(payload, decoded);
            assertEquals(List.of(), decoded.snapshot().abilityIds());
            assertTrue(decoded.openScreen());
            assertEquals("—", decoded.snapshot().displayedSoulRank());
            assertEquals("—", decoded.snapshot().displayedAspect());
            assertEquals("—", decoded.snapshot().displayedAspectRank());
            assertEquals("—", decoded.snapshot().displayedAbilities());
            assertEquals("—", decoded.snapshot().displayedAbility());
            assertEquals("—", decoded.snapshot().displayedFlaw());
            assertEquals("—", decoded.snapshot().displayedFlawEffect());
            assertFalse(decoded.snapshot().importedFromDatapack());
        } finally {
            buffer.release();
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
