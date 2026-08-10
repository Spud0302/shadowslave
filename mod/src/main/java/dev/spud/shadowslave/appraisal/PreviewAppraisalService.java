package dev.spud.shadowslave.appraisal;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.appraisal.generation.AttributeContentCatalog;
import dev.spud.shadowslave.appraisal.generation.GeneratedIdentityCandidate;
import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import dev.spud.shadowslave.echo.EchoInstanceData;
import dev.spud.shadowslave.echo.EchoManifestationService;
import dev.spud.shadowslave.echo.EchoOwnershipData;
import dev.spud.shadowslave.echo.EchoOwnershipService;
import dev.spud.shadowslave.echo.content.EchoContentCatalog;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** Runtime appraisal boundary for the playable First-Nightmare slice. */
public final class PreviewAppraisalService {
    private PreviewAppraisalService() {}

    /**
     * Exact generated result before any player attachment is mutated.
     *
     * <p>The recovery transaction can persist this value before consuming active
     * Nightmare ownership, then commit or replay the same records without invoking
     * the generator or current content catalogues a second time.</p>
     */
    public record PreparedAppraisal(
            FirstNightmareAppraisalResolver.Award award,
            SoulIdentityData identity,
            AttributeInstanceData attribute,
            MemoryInstanceData memory,
            EchoInstanceData echo
    ) {
        public PreparedAppraisal {
            award = Objects.requireNonNull(award, "award");
            identity = Objects.requireNonNull(identity, "identity");
            attribute = Objects.requireNonNull(attribute, "attribute");
            memory = Objects.requireNonNull(memory, "memory");
            echo = Objects.requireNonNull(echo, "echo");
        }
    }

    /**
     * Exact state committed by the appraisal transaction. Presentation and
     * recovery consumers may use these values after success, but must not
     * recalculate or replace them from the current generator/catalogues.
     */
    public record CommittedAppraisal(
            FirstNightmareAppraisalResolver.Award award,
            SoulIdentityData identity,
            AttributeInstanceData attribute,
            MemoryInstanceData memory,
            EchoInstanceData echo
    ) {
        public CommittedAppraisal {
            award = Objects.requireNonNull(award, "award");
            identity = Objects.requireNonNull(identity, "identity");
            attribute = Objects.requireNonNull(attribute, "attribute");
            memory = Objects.requireNonNull(memory, "memory");
            echo = Objects.requireNonNull(echo, "echo");
        }
    }

    public static FirstNightmareAppraisalResolver.Award appraise(ServerPlayer player, NightmareInstance completedInstance) {
        return appraiseWithRewards(player, completedInstance).award();
    }

    public static FirstNightmareAppraisalResolver.Award appraise(
            ServerPlayer player,
            NightmareInstance completedInstance,
            String resolutionId
    ) {
        return appraiseWithRewards(player, completedInstance, resolutionId).award();
    }

    public static PreparedAppraisal prepareWithRewards(NightmareInstance completedInstance) {
        Objects.requireNonNull(completedInstance, "completedInstance");
        String resolutionId = completedInstance.terminalResolutionId().orElseGet(() ->
                completedInstance.scenarioId().equals("last_signal") ? "signal_restored" : "completed");
        return prepareWithRewards(completedInstance, resolutionId);
    }

    /** Resolves the exact award payload without mutating a player. */
    public static PreparedAppraisal prepareWithRewards(
            NightmareInstance completedInstance,
            String resolutionId
    ) {
        NightmareInstance checkedInstance = Objects.requireNonNull(completedInstance, "completedInstance");
        String checkedResolution = Objects.requireNonNull(resolutionId, "resolutionId");
        FirstNightmareAppraisalResolver.Award award = FirstNightmareAppraisalResolver.resolve(
                checkedInstance,
                checkedResolution
        );
        GeneratedIdentityCandidate generated = award.identity();
        GeneratedIdentityCandidate.Aspect generatedAspect = generated.aspect();
        GeneratedIdentityCandidate.Flaw generatedFlaw = generated.flaw();

        AspectInstanceData aspect = new AspectInstanceData(generatedAspect.instanceId(), generatedAspect.formalName(),
                generatedAspect.aspectRank(), generatedAspect.natureId(), new AspectAbilitySetData(List.of(
                AspectAbilityData.legacyUnclassified(generatedAspect.abilityId(),
                        "generated First-Nightmare Dormant ability; classification integration pending"))), generated.provenance());
        FlawInstanceData flaw = new FlawInstanceData(generatedFlaw.instanceId(), generatedFlaw.formalName(),
                generatedFlaw.effectId(), generated.provenance());
        SoulIdentityData identity = new SoulIdentityData(Optional.of(aspect), Optional.of(flaw));
        AttributeContentCatalog.AttributeProfile profile = award.attribute();
        AttributeInstanceData attribute = new AttributeInstanceData(profile.id(), profile.formalName(),
                profile.origin().name().toLowerCase(Locale.ROOT), profile.visibility().name().toLowerCase(Locale.ROOT),
                generated.provenance() + "/attribute-selection");

        MemoryContentCatalog.MemoryProfile memoryProfile = MemoryContentCatalog.waveOne().memories().stream()
                .filter(memory -> memory.id().equals(AshCompassMemoryItem.MEMORY_ID)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Ash Compass Memory profile is missing"));
        String rewardProvenance = "nightmare/" + checkedInstance.instanceId() + "/resolution/" + checkedResolution;
        MemoryInstanceData memory = new MemoryInstanceData(memoryProfile.id(), memoryProfile.formalName(),
                "first_nightmare_appraisal_design", rewardProvenance);
        EchoContentCatalog.EchoProfile echoProfile = EchoManifestationService.ashBurrowerProfile();
        EchoInstanceData echo = new EchoInstanceData(EchoManifestationService.ASH_BURROWER_ID, echoProfile.displayName(),
                "first_nightmare_appraisal_design", rewardProvenance,
                Optional.empty(), Optional.empty(), Optional.empty());

        return new PreparedAppraisal(award, identity, attribute, memory, echo);
    }

    public static CommittedAppraisal appraiseWithRewards(ServerPlayer player, NightmareInstance completedInstance) {
        return commitPrepared(player, prepareWithRewards(completedInstance));
    }

    public static CommittedAppraisal appraiseWithRewards(
            ServerPlayer player,
            NightmareInstance completedInstance,
            String resolutionId
    ) {
        return commitPrepared(player, prepareWithRewards(completedInstance, resolutionId));
    }

    /** Commits an already-resolved award without re-running generation. */
    public static CommittedAppraisal commitPrepared(ServerPlayer player, PreparedAppraisal prepared) {
        Objects.requireNonNull(player, "player");
        PreparedAppraisal checked = Objects.requireNonNull(prepared, "prepared");
        GeneratedIdentityCandidate generated = checked.award().identity();
        GeneratedIdentityCandidate.Aspect generatedAspect = generated.aspect();
        GeneratedIdentityCandidate.Flaw generatedFlaw = generated.flaw();

        SoulIdentityData beforeIdentity = SoulIdentityService.get(player);
        AttributeOwnershipData beforeAttributes = AttributeOwnershipService.get(player);
        MemoryOwnershipData beforeMemories = MemoryOwnershipService.get(player);
        EchoOwnershipData beforeEchoes = EchoOwnershipService.get(player);
        SoulIdentityService.replace(player, checked.identity());
        AttributeOwnershipService.award(player, checked.attribute());
        MemoryOwnershipService.award(player, checked.memory());
        EchoOwnershipService.award(player, checked.echo());
        try {
            SoulService.completeFirstNightmare(player, generatedAspect.instanceId(), generatedAspect.aspectRank(), generatedFlaw.instanceId());
        } catch (RuntimeException exception) {
            SoulIdentityService.replace(player, beforeIdentity);
            AttributeOwnershipService.replace(player, beforeAttributes);
            MemoryOwnershipService.replace(player, beforeMemories);
            EchoOwnershipService.replace(player, beforeEchoes);
            throw exception;
        }

        GeneratedAppraisalRecoverySnapshot snapshot = GeneratedAppraisalRecoverySnapshot.fromPrepared(checked);
        GeneratedAppraisalRecoveryService.PlayerState committedState = GeneratedAppraisalRecoveryService.currentState(player);
        player.getServer().getPlayerList().saveAll();
        Path playerDataFile = player.getServer().getWorldPath(LevelResource.PLAYER_DATA_DIR)
                .resolve(player.getStringUUID() + ".dat");
        PersistedGeneratedAppraisalPlayerVerifier.requireCommitted(playerDataFile, committedState, snapshot);

        ShadowSlaveMod.LOGGER.info("Generated appraisal {} committed and persistence-verified for player {}: Aspect {}, Flaw {}, Attribute {}, Memory {}, Echo {}",
                generated.generationFingerprint(), player.getScoreboardName(), generatedAspect.instanceId(),
                generatedFlaw.instanceId(), checked.attribute().attributeId(), checked.memory().memoryId(), checked.echo().echoId());
        return new CommittedAppraisal(
                checked.award(),
                checked.identity(),
                checked.attribute(),
                checked.memory(),
                checked.echo()
        );
    }
}
