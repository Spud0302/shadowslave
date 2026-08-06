package dev.spud.shadowslave.appraisal;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.nightmare.NightmareInstance;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.SpellState;
import dev.spud.shadowslave.soul.identity.AspectInstanceData;
import dev.spud.shadowslave.soul.identity.FlawInstanceData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

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
     * <p>A durable Nightmare receipt can be saved before or after the player's
     * attachment file. Replaying this method therefore accepts the exact
     * already-appraised state and repairs either safe half-state: expected
     * identity with an Aspirant Soul, or expected Dreamer Soul with empty
     * identity. Unrelated identity/progression is never overwritten.</p>
     */
    public static void appraise(ServerPlayer player, NightmareInstance completedInstance) {
        requireSupportedScenario(completedInstance);

        SoulIdentityData expectedIdentity = expectedIdentity();
        SoulData currentSoul = SoulService.get(player);
        SoulIdentityData currentIdentity = SoulIdentityService.get(player);
        boolean soulApplied = hasExpectedSoul(currentSoul);
        boolean identityApplied = expectedIdentity.equals(currentIdentity);

        if (soulApplied && identityApplied) {
            return;
        }

        if (currentSoul.spellState() == SpellState.ASPIRANT
                && (currentIdentity.equals(SoulIdentityData.empty()) || identityApplied)) {
            SoulIdentityService.replace(player, expectedIdentity);
            try {
                SoulService.completeFirstNightmare(player, ASPECT_ID, SoulRank.AWAKENED, FLAW_ID);
            } catch (RuntimeException exception) {
                if (!hasExpectedSoul(SoulService.get(player))) {
                    SoulIdentityService.replace(player, SoulIdentityData.empty());
                } else {
                    SoulIdentityService.replace(player, expectedIdentity);
                }
                throw exception;
            }
        } else if (soulApplied && currentIdentity.equals(SoulIdentityData.empty())) {
            SoulIdentityService.replace(player, expectedIdentity);
        } else {
            throw new IllegalStateException(
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

    private static boolean hasExpectedSoul(SoulData soul) {
        return soul.spellState() == SpellState.DREAMER
                && soul.aspectId().equals(Optional.of(ASPECT_ID))
                && soul.aspectRank().equals(Optional.of(SoulRank.AWAKENED))
                && soul.flawId().equals(Optional.of(FLAW_ID));
    }

    private static SoulIdentityData expectedIdentity() {
        AspectInstanceData aspect = new AspectInstanceData(
                ASPECT_ID,
                "Last Light",
                SoulRank.AWAKENED,
                id("preview/nature/ember_resolve"),
                ABILITY_ID,
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

    private static void requireSupportedScenario(NightmareInstance completedInstance) {
        if (!"last_signal".equals(completedInstance.scenarioId())) {
            throw new IllegalArgumentException("Preview appraisal does not know scenario " + completedInstance.scenarioId());
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, path);
    }
}
