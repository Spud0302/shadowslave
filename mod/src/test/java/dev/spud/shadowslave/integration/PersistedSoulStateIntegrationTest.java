package dev.spud.shadowslave.integration;

import com.mojang.serialization.Codec;
import dev.spud.shadowslave.appraisal.PreviewAppraisalService;
import dev.spud.shadowslave.network.payload.SoulSnapshot;
import dev.spud.shadowslave.network.payload.SoulSnapshotPayload;
import dev.spud.shadowslave.preview.PreviewPowerData;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulTransitions;
import dev.spud.shadowslave.soul.identity.AspectInstanceData;
import dev.spud.shadowslave.soul.identity.FlawInstanceData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistedSoulStateIntegrationTest {
    @Test
    void persistedDreamerStateRebuildsTheSameAuthoritativeClientSnapshot() {
        SoulData soulBeforeSave = SoulTransitions.completeFirstNightmare(
                SoulTransitions.beginFirstNightmare(SoulTransitions.infect(SoulData.uninfected())),
                PreviewAppraisalService.ASPECT_ID,
                SoulRank.AWAKENED,
                PreviewAppraisalService.FLAW_ID
        );
        SoulIdentityData identityBeforeSave = new SoulIdentityData(
                Optional.of(new AspectInstanceData(
                        PreviewAppraisalService.ASPECT_ID,
                        "Last Light",
                        SoulRank.AWAKENED,
                        id("preview/nature/ember_resolve"),
                        PreviewAppraisalService.ABILITY_ID,
                        "preview_appraisal_design"
                )),
                Optional.of(new FlawInstanceData(
                        PreviewAppraisalService.FLAW_ID,
                        "Cold Ash",
                        PreviewAppraisalService.FLAW_EFFECT_ID,
                        "preview_appraisal_design"
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
        assertEquals("Last Light", snapshotAfterLoad.displayedAspect());
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
