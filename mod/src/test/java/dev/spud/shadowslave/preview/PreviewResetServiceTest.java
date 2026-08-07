package dev.spud.shadowslave.preview;

import dev.spud.shadowslave.migration.ImportedAspect;
import dev.spud.shadowslave.migration.ImportedFlaw;
import dev.spud.shadowslave.migration.ImportedIdentityData;
import dev.spud.shadowslave.network.payload.SoulSnapshot;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulTransitions;
import dev.spud.shadowslave.soul.identity.AspectAbilityData;
import dev.spud.shadowslave.soul.identity.AspectAbilitySetData;
import dev.spud.shadowslave.soul.identity.AspectInstanceData;
import dev.spud.shadowslave.soul.identity.FlawInstanceData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PreviewResetServiceTest {
    private static final ResourceLocation ASPECT = id("preview/aspect/last_light");
    private static final ResourceLocation FLAW = id("preview/flaw/cold_ash");
    private static final ResourceLocation NATURE = id("preview/nature/ember_resolve");
    private static final ResourceLocation ABILITY = id("preview/ability/kindle");
    private static final ResourceLocation EFFECT = id("preview/flaw_effect/cold_ash");

    @Test
    void resetCompletesAllMutationsBeforeOneAuthoritativeSnapshot() {
        FakeOperations operations = new FakeOperations();

        PreviewResetService.reset(operations);

        assertEquals(List.of(
                "abort_nightmare",
                "reset_soul",
                "clear_soul_identity",
                "clear_imported_identity",
                "clear_preview_power",
                "sync"
        ), operations.calls);
        assertFalse(operations.nightmareActive);
        assertEquals(SoulData.uninfected(), operations.soul);
        assertEquals(SoulIdentityData.empty(), operations.identity);
        assertEquals(ImportedIdentityData.empty(), operations.importedIdentity);
        assertEquals(PreviewPowerData.empty(), operations.previewPower);
        assertEquals(1, operations.snapshots.size());
        assertEquals(ImportedIdentityData.empty(), operations.importedIdentityAtSync);
        assertEquals(PreviewPowerData.empty(), operations.previewPowerAtSync);

        assertEquals(
                SoulSnapshot.from(SoulData.uninfected(), SoulIdentityData.empty()),
                operations.snapshots.getFirst(),
                "the only published snapshot must contain the complete cleared state"
        );
    }

    private static final class FakeOperations implements PreviewResetService.Operations {
        private final List<String> calls = new ArrayList<>();
        private final List<SoulSnapshot> snapshots = new ArrayList<>();
        private boolean nightmareActive = true;
        private SoulData soul = SoulTransitions.completeFirstNightmare(
                SoulTransitions.beginFirstNightmare(SoulTransitions.infect(SoulData.uninfected())),
                ASPECT,
                SoulRank.AWAKENED,
                FLAW
        );
        private SoulIdentityData identity = new SoulIdentityData(
                Optional.of(new AspectInstanceData(
                        ASPECT,
                        "Last Light",
                        SoulRank.AWAKENED,
                        NATURE,
                        new AspectAbilitySetData(List.of(AspectAbilityData.legacyUnclassified(
                                ABILITY,
                                "compatibility: preview reset fixture predates ability classification"
                        ))),
                        "preview"
                )),
                Optional.of(new FlawInstanceData(FLAW, "Cold Ash", EFFECT, "preview"))
        );
        private ImportedIdentityData importedIdentity = new ImportedIdentityData(
                Optional.of(new ImportedAspect(
                        ASPECT,
                        "Last Light",
                        SoulRank.AWAKENED,
                        "ember_resolve",
                        "last_light",
                        NATURE,
                        12,
                        true
                )),
                Optional.of(new ImportedFlaw(
                        FLAW,
                        "Cold Ash",
                        "burdened",
                        EFFECT,
                        12,
                        true
                ))
        );
        private PreviewPowerData previewPower = new PreviewPowerData(200L);
        private ImportedIdentityData importedIdentityAtSync;
        private PreviewPowerData previewPowerAtSync;

        @Override
        public void abortNightmareIfActive() {
            calls.add("abort_nightmare");
            nightmareActive = false;
        }

        @Override
        public SoulData resetSoulWithoutSync() {
            calls.add("reset_soul");
            soul = SoulTransitions.reset();
            return soul;
        }

        @Override
        public void clearSoulIdentity() {
            calls.add("clear_soul_identity");
            identity = SoulIdentityData.empty();
        }

        @Override
        public void clearImportedIdentity() {
            calls.add("clear_imported_identity");
            importedIdentity = ImportedIdentityData.empty();
        }

        @Override
        public void clearPreviewPower() {
            calls.add("clear_preview_power");
            previewPower = PreviewPowerData.empty();
        }

        @Override
        public void sync(SoulData resetSoul) {
            calls.add("sync");
            snapshots.add(SoulSnapshot.from(resetSoul, identity));
            importedIdentityAtSync = importedIdentity;
            previewPowerAtSync = previewPower;
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
