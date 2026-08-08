package dev.spud.shadowslave.appraisal;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.nightmare.NightmareInstance;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.SpellState;
import dev.spud.shadowslave.soul.identity.AspectAbilityData;
import dev.spud.shadowslave.soul.identity.AspectAbilitySetData;
import dev.spud.shadowslave.soul.identity.AspectInstanceData;
import dev.spud.shadowslave.soul.identity.FlawInstanceData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;

/**
 * DESIGN: fixed preview appraisal for the Last Signal scenario. Canon does not
 * provide a deterministic formula; this service exists to make one coherent
 * vertical slice playable while keeping the appraisal boundary replaceable.
 */
public final class PreviewAppraisalService {
    public static final ResourceLocation ASPECT_ID = id("preview/aspect/last_light");
    public static final ResourceLocation FLAW_ID = id("preview/flaw/cold_ash");
    public static final ResourceLocation ABILITY_ID = id("preview/ability/kindle");
    public static final ResourceLocation FLAW_EFFECT_ID = id("preview/flaw_effect/cold_ash");

    private PreviewAppraisalService() {
    }

    /**
     * Applies or reconciles the fixed preview appraisal.
     *
     * <p>The successful-completion receipt and player attachments are separate
     * persistence surfaces. Restart replay therefore accepts the exact applied
     * state and repairs only the two safe split-save states: expected identity
     * with an Aspirant Soul, or expected Dreamer Soul with empty identity.
     * Unrelated progression or identity fails closed instead of being
     * overwritten.</p>
     */
    public static void appraise(ServerPlayer player, NightmareInstance completedInstance) {
        requireSupportedScenario(completedInstance);

        SoulIdentityData expectedIdentity = expectedIdentity();
        SoulData currentSoul = SoulService.get(player);
        SoulIdentityData currentIdentity = SoulIdentityService.get(player);
        Reconciliation reconciliation = classify(currentSoul, currentIdentity);

        switch (reconciliation) {
            case ALREADY_APPLIED -> {
                return;
            }
            case APPLY_BOTH -> {
                SoulIdentityService.replace(player, expectedIdentity);
                completeSoulWithRollback(player, SoulIdentityData.empty(), expectedIdentity);
            }
            case APPLY_SOUL_ONLY -> completeSoulWithRollback(player, expectedIdentity, expectedIdentity);
            case APPLY_IDENTITY_ONLY -> SoulIdentityService.replace(player, expectedIdentity);
            case CONFLICT -> throw new IllegalStateException(
                    "Cannot reconcile preview appraisal with unrelated Soul or identity state"
            );
        }

        if (!isApplied(player, completedInstance)) {
            throw new IllegalStateException("Preview appraisal reconciliation did not reach the expected state");
        }

        ShadowSlaveMod.LOGGER.info(
                "Preview appraisal completed for Nightmare {} and player {}",
                completedInstance.instanceId(),
                player.getScoreboardName()
        );
    }

    public static boolean isApplied(ServerPlayer player, NightmareInstance completedInstance) {
        requireSupportedScenario(completedInstance);
        return hasExpectedSoul(SoulService.get(player))
                && expectedIdentity().equals(SoulIdentityService.get(player));
    }

    static Reconciliation classify(SoulData soul, SoulIdentityData identity) {
        SoulIdentityData expectedIdentity = expectedIdentity();
        boolean soulApplied = hasExpectedSoul(soul);
        boolean identityApplied = expectedIdentity.equals(identity);
        boolean identityEmpty = SoulIdentityData.empty().equals(identity);

        if (soulApplied && identityApplied) {
            return Reconciliation.ALREADY_APPLIED;
        }
        if (soul.spellState() == SpellState.ASPIRANT && identityEmpty) {
            return Reconciliation.APPLY_BOTH;
        }
        if (soul.spellState() == SpellState.ASPIRANT && identityApplied) {
            return Reconciliation.APPLY_SOUL_ONLY;
        }
        if (soulApplied && identityEmpty) {
            return Reconciliation.APPLY_IDENTITY_ONLY;
        }
        return Reconciliation.CONFLICT;
    }

    static boolean hasExpectedSoul(SoulData soul) {
        return soul.spellState() == SpellState.DREAMER
                && soul.aspectId().equals(Optional.of(ASPECT_ID))
                && soul.aspectRank().equals(Optional.of(SoulRank.AWAKENED))
                && soul.flawId().equals(Optional.of(FLAW_ID));
    }

    static SoulIdentityData expectedIdentity() {
        AspectInstanceData aspect = new AspectInstanceData(
                ASPECT_ID,
                "Last Light",
                SoulRank.AWAKENED,
                id("preview/nature/ember_resolve"),
                new AspectAbilitySetData(List.of(AspectAbilityData.legacyUnclassified(
                        ABILITY_ID,
                        "compatibility: fixed preview ability predates ability classification"
                ))),
                "preview_appraisal_design"
        );
        FlawInstanceData flaw = new FlawInstanceData(
                FLAW_ID,
                "Cold Ash",
                FLAW_EFFECT_ID,
                "preview_appraisal_design"
        );
        return new SoulIdentityData(Optional.of(aspect), Optional.of(flaw));
    }

    private static void completeSoulWithRollback(
            ServerPlayer player,
            SoulIdentityData rollbackIdentity,
            SoulIdentityData expectedIdentity
    ) {
        try {
            SoulService.completeFirstNightmare(player, ASPECT_ID, SoulRank.AWAKENED, FLAW_ID);
        } catch (RuntimeException exception) {
            if (hasExpectedSoul(SoulService.get(player))) {
                SoulIdentityService.replace(player, expectedIdentity);
            } else {
                SoulIdentityService.replace(player, rollbackIdentity);
            }
            throw exception;
        }
    }

    private static void requireSupportedScenario(NightmareInstance completedInstance) {
        if (!"last_signal".equals(completedInstance.scenarioId())) {
            throw new IllegalArgumentException("Preview appraisal does not know scenario " + completedInstance.scenarioId());
        }
    }

    enum Reconciliation {
        ALREADY_APPLIED,
        APPLY_BOTH,
        APPLY_SOUL_ONLY,
        APPLY_IDENTITY_ONLY,
        CONFLICT
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, path);
    }
}
