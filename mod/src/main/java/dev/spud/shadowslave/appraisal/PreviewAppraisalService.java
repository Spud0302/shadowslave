package dev.spud.shadowslave.appraisal;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.appraisal.generation.AttributeContentCatalog;
import dev.spud.shadowslave.appraisal.generation.GeneratedIdentityCandidate;
import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import dev.spud.shadowslave.item.AshCompassMemoryItem;
import dev.spud.shadowslave.memory.MemoryInstanceData;
import dev.spud.shadowslave.memory.MemoryOwnershipData;
import dev.spud.shadowslave.memory.MemoryOwnershipService;
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
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Runtime appraisal boundary for the playable First-Nightmare slice.
 * Generated Aspect, Flaw and Attribute identities plus the preview Memory award
 * are persisted in Java-owned state before the progression transition completes.
 *
 * <p>The generator, evidence weights and exact Ash Compass reward are Minecraft
 * DESIGN. Canon establishes the identity/appraisal and Memory concepts but does
 * not provide these deterministic project formulas or reward tables.</p>
 */
public final class PreviewAppraisalService {
    private PreviewAppraisalService() {
    }

    public static FirstNightmareAppraisalResolver.Award appraise(
            ServerPlayer player,
            NightmareInstance completedInstance
    ) {
        String resolutionId = completedInstance.scenarioId().equals("last_signal")
                ? "signal_restored"
                : "completed";
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
        MemoryContentCatalog.MemoryProfile memoryProfile = MemoryContentCatalog.waveOne().memories().stream()
                .filter(memory -> memory.id().equals(AshCompassMemoryItem.MEMORY_ID))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Ash Compass Memory profile is missing"));
        MemoryInstanceData memory = new MemoryInstanceData(
                memoryProfile.id(),
                memoryProfile.formalName(),
                "first_nightmare_appraisal_design",
                "nightmare/" + completedInstance.instanceId() + "/resolution/" + resolutionId
        );

        SoulIdentityData beforeIdentity = SoulIdentityService.get(player);
        AttributeOwnershipData beforeAttributes = AttributeOwnershipService.get(player);
        MemoryOwnershipData beforeMemories = MemoryOwnershipService.get(player);
        SoulIdentityService.replace(player, new SoulIdentityData(Optional.of(aspect), Optional.of(flaw)));
        AttributeOwnershipService.award(player, attribute);
        MemoryOwnershipService.award(player, memory);
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
            MemoryOwnershipService.replace(player, beforeMemories);
            throw exception;
        }

        player.sendSystemMessage(Component.literal(
                "Memory acquired: [" + memory.formalName() + "]. Summon it with /shadowslave_memory summon ash_compass."
        ).withStyle(ChatFormatting.GOLD));
        ShadowSlaveMod.LOGGER.info(
                "Generated appraisal {} committed for Nightmare {} and player {}: Aspect {}, Flaw {}, Attribute {}, Memory {}",
                generated.generationFingerprint(),
                completedInstance.instanceId(),
                player.getScoreboardName(),
                generatedAspect.instanceId(),
                generatedFlaw.instanceId(),
                profile.id(),
                memory.memoryId()
        );
        return award;
    }
}
