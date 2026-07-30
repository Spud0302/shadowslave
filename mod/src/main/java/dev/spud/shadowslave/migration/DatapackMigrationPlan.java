package dev.spud.shadowslave.migration;

import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SpellState;

import java.util.Objects;
import java.util.Optional;

/**
 * Fully validated in-memory result. A live migration writer persists this plan
 * before it marks or removes any datapack state.
 */
public record DatapackMigrationPlan(
        SoulData soulData,
        Optional<ImportedAspect> aspect,
        Optional<ImportedFlaw> flaw
) {
    public DatapackMigrationPlan {
        soulData = Objects.requireNonNull(soulData, "soulData");
        aspect = Objects.requireNonNull(aspect, "aspect");
        flaw = Objects.requireNonNull(flaw, "flaw");

        if (aspect.isPresent() != flaw.isPresent()) {
            throw new IllegalArgumentException("Imported completed identity requires both Aspect and Flaw");
        }
        if (soulData.spellState() == SpellState.DREAMER && aspect.isEmpty()) {
            throw new IllegalArgumentException("Dreamer migration plan requires imported identity metadata");
        }
        if (aspect.isPresent()) {
            ImportedAspect importedAspect = aspect.orElseThrow();
            ImportedFlaw importedFlaw = flaw.orElseThrow();
            if (!Optional.of(importedAspect.instanceId()).equals(soulData.aspectId())) {
                throw new IllegalArgumentException("SoulData Aspect ID does not match imported Aspect");
            }
            if (!Optional.of(importedFlaw.instanceId()).equals(soulData.flawId())) {
                throw new IllegalArgumentException("SoulData Flaw ID does not match imported Flaw");
            }
        }
    }
}
