package dev.spud.shadowslave.appraisal;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.appraisal.generation.AttributeContentCatalog;
import dev.spud.shadowslave.appraisal.generation.GeneratedIdentityCandidate;
import dev.spud.shadowslave.nightmare.NightmareInstance;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.identity.AspectAbilityData;
import dev.spud.shadowslave.soul.identity.AspectAbilitySetData;
import dev.spud.shadowslave.soul.identity.AspectInstanceData;
import dev.spud.shadowslave.soul.identity.AttributeInstanceData;
import dev.spud.shadowslave.soul.identity.AttributeOwnershipData;
import dev.spud.shadowslave.soul.identity.AttributeOwnershipService;
import dev.spud.shadowslave.soul.identity.FlawInstanceData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Compatibility-named runtime appraisal boundary. It now resolves and persists
 * generated First-Nightmare Aspect, Flaw and Attribute identities from the
 * completed Java-owned Nightmare state instead of awarding the old fixed pair.
 *
 * <p>The generator and evidence weights are Minecraft DESIGN. Canon establishes
 * the identity/appraisal concepts but does not provide this deterministic formula.</p>
 */
public final class PreviewAppraisalService {
    private PreviewAppraisalService() {
    }

    public static FirstNightmareAppraisalResolver.Award appraise(
            ServerPlayer player,
            NightmareInstance completedInstance
    ) {
        Objects.requireNonNull(completedInstance, "completedInstance");
        String resolutionId = completedInstance.terminalResolutionId().orElseGet(() ->
                completedInstance.scenarioId().equals("last_signal")
                        ? "signal_restored"
                        : "completed"
        );
        return appraise(player, completedInstance, resolutionId);
    }

    public static FirstNightmareAppraisalResolver.Award appraise(
            ServerPlayer player,
            NightmareInstance completedInstance,
            String resolutionId
    ) {
        Objects.requireNonNull(player, "player");
        FirstNightmareAppraisalResolver.Award award = FirstNightmareAppraisalResolver.resolve(
                Objects.requireNonNull(completedInstance, "completedInstance"),
                resolutionId
        );
        GeneratedIdentityCandidate generated = award.identity();
        GeneratedIdentityCandidate.Aspect generatedAspect = generated.aspect();
        GeneratedIdentityCandidate.Flaw generatedFlaw = generated.flaw();

        AspectInstanceData aspect = new AspectInstanceData(
                generatedAspect.instanceId(),
                generatedAspect.formalName(),
                generatedAspect.aspectRank(),
                generatedAspect.natureId(),
                new AspectAbilitySetData(List.of(AspectAbilityData.legacyUnclassified(
                        generatedAspect.abilityId(),
                        "generated First-Nightmare Dormant ability; classification integration pending"
                ))),
                generated.provenance()
        );
        FlawInstanceData flaw = new FlawInstanceData(
                generatedFlaw.instanceId(),
                generatedFlaw.formalName(),
                generatedFlaw.effectId(),
                generated.provenance()
        );
        AttributeContentCatalog.AttributeProfile profile = award.attribute();
        AttributeInstanceData attribute = new AttributeInstanceData(
                profile.id(),
                profile.formalName(),
                profile.origin().name().toLowerCase(Locale.ROOT),
                profile.visibility().name().toLowerCase(Locale.ROOT),
                generated.provenance() + "/attribute-selection"
        );

        SoulIdentityData beforeIdentity = SoulIdentityService.get(player);
        AttributeOwnershipData beforeAttributes = AttributeOwnershipService.get(player);
        SoulIdentityService.replace(player, new SoulIdentityData(Optional.of(aspect), Optional.of(flaw)));
        AttributeOwnershipService.award(player, attribute);
        try {
            SoulService.completeFirstNightmare(
                    player,
                    generatedAspect.instanceId(),
                    generatedAspect.aspectRank(),
                    generatedFlaw.instanceId()
            );
        } catch (RuntimeException exception) {
            SoulIdentityService.replace(player, beforeIdentity);
            AttributeOwnershipService.replace(player, beforeAttributes);
            throw exception;
        }

        ShadowSlaveMod.LOGGER.info(
                "Generated appraisal {} committed for Nightmare {} and player {}: Aspect {}, Flaw {}, Attribute {}",
                generated.generationFingerprint(),
                completedInstance.instanceId(),
                player.getScoreboardName(),
                generatedAspect.instanceId(),
                generatedFlaw.instanceId(),
                profile.id()
        );
        return award;
    }
}
