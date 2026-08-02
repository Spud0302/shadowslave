package dev.spud.shadowslave.migration;

import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulTransitions;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatapackMigrationPersistenceVerifierTest {
    private static final ResourceLocation ASPECT_ID = id("import/datapack/test/aspect_12");
    private static final ResourceLocation FLAW_ID = id("import/datapack/test/flaw_43");

    @Test
    void exactReadBackPasses() {
        SoulData soul = completedSoul();
        ImportedIdentityData identity = identity();

        assertDoesNotThrow(() -> DatapackMigrationPersistenceVerifier.verify(soul, identity, soul, identity));
    }

    @Test
    void mismatchedSoulReadBackFails() {
        SoulData expected = completedSoul();
        SoulData actual = SoulTransitions.infect(SoulData.uninfected());

        assertThrows(
                IllegalStateException.class,
                () -> DatapackMigrationPersistenceVerifier.verify(expected, identity(), actual, identity())
        );
    }

    @Test
    void missingIdentityMetadataFailsEvenWhenSoulIdsExist() {
        SoulData soul = completedSoul();

        assertThrows(
                IllegalStateException.class,
                () -> DatapackMigrationPersistenceVerifier.verify(
                        soul,
                        ImportedIdentityData.empty(),
                        soul,
                        ImportedIdentityData.empty()
                )
        );
    }

    private static SoulData completedSoul() {
        return SoulTransitions.completeFirstNightmare(
                SoulTransitions.beginFirstNightmare(SoulTransitions.infect(SoulData.uninfected())),
                ASPECT_ID,
                SoulRank.DORMANT,
                FLAW_ID
        ).markImported(DatapackMigrationTranslator.CURRENT_MIGRATION);
    }

    private static ImportedIdentityData identity() {
        ImportedAspect aspect = new ImportedAspect(
                ASPECT_ID,
                "Veiled Bearer",
                SoulRank.DORMANT,
                "veiled",
                "bearer",
                id("prototype/aspect_root/shadow"),
                12,
                false
        );
        ImportedFlaw flaw = new ImportedFlaw(
                FLAW_ID,
                "Shackled Pace",
                "burdened_movement",
                id("prototype/flaw_effect/burdened"),
                43,
                false
        );
        return new ImportedIdentityData(Optional.of(aspect), Optional.of(flaw));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
