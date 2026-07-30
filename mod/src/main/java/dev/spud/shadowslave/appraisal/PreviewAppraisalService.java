package dev.spud.shadowslave.appraisal;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.nightmare.NightmareInstance;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
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

    public static void appraise(ServerPlayer player, NightmareInstance completedInstance) {
        if (!"last_signal".equals(completedInstance.scenarioId())) {
            throw new IllegalArgumentException("Preview appraisal does not know scenario " + completedInstance.scenarioId());
        }

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

        SoulIdentityData identity = new SoulIdentityData(Optional.of(aspect), Optional.of(flaw));
        SoulIdentityService.replace(player, identity);
        try {
            SoulService.completeFirstNightmare(player, ASPECT_ID, SoulRank.AWAKENED, FLAW_ID);
        } catch (RuntimeException exception) {
            SoulIdentityService.replace(player, SoulIdentityData.empty());
            throw exception;
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, path);
    }
}
