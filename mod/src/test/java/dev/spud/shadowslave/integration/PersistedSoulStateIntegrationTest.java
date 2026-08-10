package dev.spud.shadowslave.integration;

import com.mojang.serialization.Codec;
import dev.spud.shadowslave.network.payload.SoulSnapshot;
import dev.spud.shadowslave.network.payload.SoulSnapshotPayload;
import dev.spud.shadowslave.preview.PreviewPowerData;
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
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistedSoulStateIntegrationTest {
    private static final ResourceLocation ASPECT_ID = id("test/generated/aspect/persisted");
    private static final ResourceLocation FLAW_ID = id("test/generated/flaw/persisted");
    private static final ResourceLocation NATURE_ID = id("generation/nature/ember");
    private static final ResourceLocation ABILITY_ID = id("generation/ability/kindle");
    private static final ResourceLocation FLAW_EFFECT_ID = id("generation/flaw_effect/cold_ash");

    @Test
    void persistedDreamerStateRebuildsTheSameAuthoritativeClientSnapshot() {
        SoulData soulBeforeSave = SoulTransitions.completeFirstNightmare(
                SoulTransitions.beginFirstNightmare(SoulTransitions.infect(SoulData.uninfected())),
                ASPECT_ID,
                SoulRank.AWAKENED,
                FLAW_ID
        );
        SoulIdentityData identityBeforeSave = new SoulIdentityData(
                Optional.of(new AspectInstanceData(
                        ASPECT_ID,
                        "Persistent Ember",
                        SoulRank.AWAKENED,
                        NATURE_ID,
                        new AspectAbilitySetData(List.of(AspectAbilityData.legacyUnclassified(
                                ABILITY_ID,
                                "integration fixture for persisted generated identity"
                        ))),
                        "integration_fixture"
                )),
                Optional.of(new FlawInstanceData(
                        FLAW_ID,
                        "Cold Ash",
                        FLAW_EFFECT_ID,
                        "integration_fixture"
                ))
        );
        PreviewPowerData powerBeforeSave = new PreviewPowerData(12345L);
        SoulSnapshot snapshotBeforeSave = SoulSnapshot.from(soulBeforeSave, identityBeforeSave);

        SoulData soulAfterLoad = roundTrip(SoulData.CODEC.codec(), soulBeforeSave);
        SoulIdentityData identityAfterLoad = roundTrip(SoulIdentityData.CODEC.codec(), identityBeforeSave);
        PreviewPowerData powerAfterLoad = roundTrip(PreviewPowerData.CODEC.codec(), powerBeforeSave);
        SoulSnapshot snapshotAfterLoad = SoulSnapshot.from(soulAfterLoad, identityAfterLoad);

        assertEquals(soulBeforeSave, soulAfterLoad);
        assertEquals(identityBeforeSave, identityAfterLoad);
        assertEquals(powerBeforeSave, powerAfterLoad);
        assertEquals(snapshotBeforeSave, snapshotAfterLoad);
        assertEquals("dreamer", snapshotAfterLoad.spellState());
        assertEquals("dormant", snapshotAfterLoad.soulRank());
        assertEquals("awakened", snapshotAfterLoad.aspectRank());
        assertEquals("Persistent Ember", snapshotAfterLoad.displayedAspect());
        assertEquals("Cold Ash", snapshotAfterLoad.displayedFlaw());

        SoulSnapshotPayload sentAfterLogin = new SoulSnapshotPayload(snapshotAfterLoad, false);
        ByteBuf buffer = Unpooled.buffer();
        try {
            SoulSnapshotPayload.STREAM_CODEC.encode(buffer, sentAfterLogin);
            assertEquals(sentAfterLogin, SoulSnapshotPayload.STREAM_CODEC.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    private static <T> T roundTrip(Codec<T> codec, T value) {
        Tag stored = codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow();
        return codec.parse(NbtOps.INSTANCE, stored).getOrThrow();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
