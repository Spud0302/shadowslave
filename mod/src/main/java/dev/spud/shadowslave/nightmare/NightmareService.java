package dev.spud.shadowslave.nightmare;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.appraisal.FirstNightmareSpellPresentation;
import dev.spud.shadowslave.appraisal.GeneratedAppraisalRecoverySnapshot;
import dev.spud.shadowslave.appraisal.PreviewAppraisalService;
import dev.spud.shadowslave.content.spell.SpellPresentationCatalog;
import dev.spud.shadowslave.nightmare.content.DrownedBellScenarioDefinition;
import dev.spud.shadowslave.nightmare.content.NightmareRoleContentCatalog;
import dev.spud.shadowslave.persistence.SavedDataPersistence;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.SoulTransitions;
import dev.spud.shadowslave.soul.SpellState;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** One entry choke point and one teardown path for playable First Nightmare slices. */
public final class NightmareService {
    public static final ResourceKey<Level> NIGHTMARE_LEVEL = ResourceKey.create(
            Registries.DIMENSION,
            id("nightmare")
    );

    private NightmareService() {
    }

    public static NightmareInstance tryEnter(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        SoulData beforeSoul = SoulService.get(player);
        if (beforeSoul.spellState() != SpellState.CARRIER) {
            throw new IllegalStateException("Only a Carrier can enter the preview First Nightmare");
        }

        MinecraftServer server = player.getServer();
        if (NightmareCompletionReceiptData.get(server).find(player.getUUID()).isPresent()) {
            throw new IllegalStateException("A successful Nightmare completion is still awaiting appraisal recovery");
        }
        NightmareRegistryData registry = NightmareRegistryData.get(server);
        if (registry.findByPlayer(player.getUUID()).isPresent()) {
            throw new IllegalStateException("You already own an active Nightmare instance");
        }
        ServerLevel nightmareLevel = server.getLevel(NIGHTMARE_LEVEL);
        if (nightmareLevel == null) {
            throw new IllegalStateException("The bundled Nightmare dimension is unavailable");
        }

        NightmareEntryAssignment.Assignment assignment = NightmareEntryAssignment.resolveFirstNightmare(
                player.getUUID(),
                player.serverLevel().getGameTime()
        );
        NightmareInstance instance = registry.create(
                player,
                assignment.scenarioId(),
                assignment.historicalRoleId()
        );
        NightmareInstance prepared = instance;
        try {
            prepared = prepareScenario(nightmareLevel, player, instance);
            registry.update(prepared);
            SoulService.beginFirstNightmare(player);
            player.teleportTo(
                    nightmareLevel,
                    prepared.origin().getX() + 0.5,
                    prepared.origin().getY() + 1.0,
                    prepared.origin().getZ() - 1.5,
                    Set.of(),
                    0.0F,
                    0.0F
            );
            player.sendSystemMessage(Component.literal("First Nightmare — " + scenarioDisplayName(prepared.scenarioId()))
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
            player.sendSystemMessage(Component.literal(
                    "Historical role: " + assignment.roleMatch().role().displayName()
            ).withStyle(ChatFormatting.AQUA));
            player.sendSystemMessage(Component.literal(assignment.roleMatch().variant().entryHook())
                    .withStyle(ChatFormatting.GRAY));
            player.sendSystemMessage(Component.literal(assignment.roleMatch().variant().conflictPressure())
                    .withStyle(ChatFormatting.GRAY));
            player.sendSystemMessage(Component.literal(
                    "Leverage: " + assignment.roleMatch().variant().leverage()
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
            player.sendSystemMessage(Component.literal(entryHint(prepared))
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            return prepared;
        } catch (RuntimeException exception) {
            teardown(server, prepared);
            SoulService.replace(player, beforeSoul);
            throw new IllegalStateException("Nightmare entry failed and was rolled back", exception);
        }
    }

    /** Routes a physical block interaction through the Java-owned scenario state. */
    public static boolean resolveScenarioInteraction(ServerPlayer player, BlockPos interactedPos) {
        NightmareInstance instance = activeFor(player).orElse(null);
        if (instance == null || !player.serverLevel().dimension().equals(NIGHTMARE_LEVEL)) {
            return false;
        }
        if (instance.scenarioId().equals(LastSignalScenario.SCENARIO_ID)) {
            return resolveSignalFire(player, interactedPos);
        }
        if (instance.scenarioId().equals(DrownedBellScenarioDefinition.SCENARIO_ID)) {
            return resolveDrownedBellInteraction(player, instance, interactedPos);
        }
        return false;
    }

    public static boolean resolveSignalFire(ServerPlayer player, BlockPos interactedPos) {
        NightmareInstance instance = activeFor(player).orElse(null);
        if (instance == null
                || !instance.scenarioId().equals(LastSignalScenario.SCENARIO_ID)
                || !player.serverLevel().dimension().equals(NIGHTMARE_LEVEL)
                || !instance.altar().equals(interactedPos)) {
            return false;
        }

        LastSignalScenario.igniteAltar(player.serverLevel(), instance);
        completePreview(player, instance);
        return true;
    }

    private static boolean resolveDrownedBellInteraction(
            ServerPlayer player,
            NightmareInstance instance,
            BlockPos interactedPos
    ) {
        Optional<String> event = DrownedBellScenario.eventForInteraction(instance, interactedPos);
        if (event.isEmpty()) {
            return false;
        }

        DrownedBellScenario.ResolutionAdvance advance = DrownedBellScenario.applyEvent(instance, event.orElseThrow());
        if (!advance.accepted()) {
            player.sendSystemMessage(Component.literal("That action does not resolve the conflict from its current state.")
                    .withStyle(ChatFormatting.YELLOW));
            return true;
        }

        NightmareInstance updated = advance.instance();
        DrownedBellScenario.applyAcceptedWorldCue(player.serverLevel(), updated, event.orElseThrow());
        NightmareRegistryData.get(player.getServer()).update(updated);

        if (updated.terminalResolutionId().isPresent()) {
            DrownedBellScenarioDefinition.ResolutionContent resolution = DrownedBellScenario.resolutionContent(updated)
                    .orElseThrow(() -> new IllegalStateException("Drowned Bell terminal state has no authored resolution content"));
            player.sendSystemMessage(Component.literal("Terminal resolution — " + resolution.name())
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            player.sendSystemMessage(Component.literal(resolution.description()).withStyle(ChatFormatting.GRAY));
            completePreview(player, updated);
        } else {
            player.sendSystemMessage(Component.literal("Conflict event accepted: " + event.orElseThrow())
                    .withStyle(ChatFormatting.AQUA));
            player.sendSystemMessage(Component.literal(DrownedBellScenario.interactionHint(updated))
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        return true;
    }

    private static void completePreview(ServerPlayer player, NightmareInstance instance) {
        MinecraftServer server = player.getServer();
        NightmareInstance active = activeFor(player)
                .orElseThrow(() -> new IllegalStateException("Player does not own an active Nightmare"));
        if (!active.equals(instance)) {
            throw new IllegalStateException("Nightmare completion attempted from a stale instance snapshot");
        }

        PreviewAppraisalService.PreparedAppraisal prepared = PreviewAppraisalService.prepareWithRewards(active);
        GeneratedAppraisalRecoverySnapshot snapshot = GeneratedAppraisalRecoverySnapshot.fromPrepared(prepared);
        NightmareCompletionReceiptData receipts = NightmareCompletionReceiptData.get(server);
        NightmareCompletionReceiptData.Receipt receipt = receipts.begin(active, snapshot);

        // Recovery authority must reach the SavedData durability barrier before active ownership is consumed.
        SavedDataPersistence.saveAndWait(server);

        NightmareInstance completed = exit(player, NightmareExitReason.SUCCESS);
        if (!completed.instanceId().equals(instance.instanceId())) {
            throw new IllegalStateException("Nightmare completion consumed the wrong active instance");
        }

        PreviewAppraisalService.CommittedAppraisal committed;
        try {
            committed = PreviewAppraisalService.commitPrepared(player, prepared);
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                    "The preview appraisal failed after teardown; exact generated recovery authority remains durable",
                    exception
            );
        }

        receipts.clear(receipt);
        SavedDataPersistence.saveAndWait(server);

        FirstNightmareSpellPresentation.ResolvedView view = FirstNightmareSpellPresentation.fromCommitted(
                scenarioDisplayName(completed.scenarioId()),
                historicalRoleDisplayName(completed.historicalRoleId()),
                terminalResolutionDisplayName(completed),
                committed
        );
        for (SpellPresentationCatalog.PresentationLine line : FirstNightmareSpellPresentation.render(view)) {
            ChatFormatting style = line.surface() == SpellPresentationCatalog.Surface.VOICE
                    ? ChatFormatting.LIGHT_PURPLE
                    : ChatFormatting.AQUA;
            player.sendSystemMessage(Component.literal(line.text()).withStyle(style));
        }
        if (view.revealedAttributeName().isEmpty()) {
            player.sendSystemMessage(Component.literal("An Attribute was established, but its identity remains obscured.")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        player.sendSystemMessage(Component.literal("Summon Ash Compass with /shadowslave_memory summon ash_compass.")
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal("Summon Ash Burrower with /shadowslave_echo summon ash_burrower.")
                .withStyle(ChatFormatting.AQUA));
    }

    public static NightmareInstance technicalRecover(ServerPlayer player) {
        NightmareInstance instance = exit(player, NightmareExitReason.TECHNICAL_RECOVERY);
        SoulIdentityService.replace(player, SoulIdentityData.empty());
        SoulService.replace(player, SoulTransitions.infect(SoulData.uninfected()));
        player.sendSystemMessage(Component.literal(
                "Technical recovery completed. This is an administrative path, not mercy from the Nightmare Spell."
        ).withStyle(ChatFormatting.YELLOW));
        return instance;
    }

    public static NightmareInstance adminAbort(ServerPlayer player) {
        NightmareInstance instance = exit(player, NightmareExitReason.ADMIN_ABORT);
        SoulIdentityService.replace(player, SoulIdentityData.empty());
        SoulService.replace(player, SoulTransitions.infect(SoulData.uninfected()));
        return instance;
    }

    /**
     * Exits through the normal teleport and teardown path for a compound preview
     * reset, but deliberately omits the Carrier recovery mutations and their sync.
     */
    public static NightmareInstance abortForPreviewReset(ServerPlayer player) {
        return exit(player, NightmareExitReason.ADMIN_ABORT);
    }

    public static void canonicalDeath(ServerPlayer player) {
        NightmareInstance instance = activeFor(player).orElse(null);
        if (instance == null) {
            return;
        }
        teardown(player.getServer(), instance);
        SoulIdentityService.replace(player, SoulIdentityData.empty());
        SoulService.reset(player);
        player.sendSystemMessage(Component.literal(
                "Canonical First-Nightmare outcome: death. Minecraft respawn is a development accommodation; the Spell did not safely eject you."
        ).withStyle(ChatFormatting.RED));
    }

    public static Optional<NightmareInstance> activeFor(ServerPlayer player) {
        return NightmareRegistryData.get(player.getServer()).findByPlayer(player.getUUID());
    }

    public static String resumeHint(NightmareInstance instance) {
        if (instance.scenarioId().equals(LastSignalScenario.SCENARIO_ID)) {
            return "Active First Nightmare restored: The Last Signal. Reach and right-click the unlit soul campfire.";
        }
        if (instance.scenarioId().equals(DrownedBellScenarioDefinition.SCENARIO_ID)) {
            return "Active First Nightmare restored: The Drowned Bell. " + DrownedBellScenario.interactionHint(instance);
        }
        return "Active First Nightmare restored with an unknown scenario identity; use technical recovery.";
    }

    private static NightmareInstance prepareScenario(ServerLevel nightmareLevel, ServerPlayer player, NightmareInstance instance) {
        if (instance.scenarioId().equals(LastSignalScenario.SCENARIO_ID)) {
            return LastSignalScenario.prepare(nightmareLevel, player, instance);
        }
        if (instance.scenarioId().equals(DrownedBellScenarioDefinition.SCENARIO_ID)) {
            return DrownedBellScenario.prepare(nightmareLevel, player, instance);
        }
        throw new IllegalArgumentException("Scenario is authored but not physically playable yet: " + instance.scenarioId());
    }

    private static String scenarioDisplayName(String scenarioId) {
        if (scenarioId.equals(LastSignalScenario.SCENARIO_ID)) {
            return "The Last Signal";
        }
        if (scenarioId.equals(DrownedBellScenarioDefinition.SCENARIO_ID)) {
            return DrownedBellScenarioDefinition.content().displayName();
        }
        return scenarioId;
    }

    private static String historicalRoleDisplayName(String historicalRoleId) {
        return NightmareRoleContentCatalog.waveOne().stream()
                .filter(role -> role.id().equals(historicalRoleId))
                .map(NightmareRoleContentCatalog.RoleProfile::displayName)
                .findFirst()
                .orElse(humanize(historicalRoleId));
    }

    private static String terminalResolutionDisplayName(NightmareInstance instance) {
        if (instance.scenarioId().equals(LastSignalScenario.SCENARIO_ID)) {
            return "Signal Restored";
        }
        if (instance.scenarioId().equals(DrownedBellScenarioDefinition.SCENARIO_ID)) {
            return DrownedBellScenario.resolutionContent(instance)
                    .map(DrownedBellScenarioDefinition.ResolutionContent::name)
                    .orElseGet(() -> instance.terminalResolutionId().map(NightmareService::humanize).orElse("Completed"));
        }
        return instance.terminalResolutionId().map(NightmareService::humanize).orElse("Completed");
    }

    private static String humanize(String stableId) {
        String[] words = Objects.requireNonNull(stableId, "stableId").trim().toLowerCase(Locale.ROOT).split("_");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return builder.isEmpty() ? stableId : builder.toString();
    }

    private static String entryHint(NightmareInstance instance) {
        if (instance.scenarioId().equals(LastSignalScenario.SCENARIO_ID)) {
            return "Reach the dead signal fire and rekindle it. Right-click the unlit soul campfire at the far watch. Fighting the pursuer is optional; resolving the conflict is not.";
        }
        if (instance.scenarioId().equals(DrownedBellScenarioDefinition.SCENARIO_ID)) {
            return DrownedBellScenario.interactionHint(instance);
        }
        return "This scenario has no physical runtime hint.";
    }

    private static NightmareInstance exit(ServerPlayer player, NightmareExitReason reason) {
        MinecraftServer server = player.getServer();
        NightmareInstance instance = activeFor(player)
                .orElseThrow(() -> new IllegalStateException("Player does not own an active Nightmare"));

        ResourceKey<Level> returnKey = ResourceKey.create(Registries.DIMENSION, instance.returnDimension());
        ServerLevel returnLevel = server.getLevel(returnKey);
        if (returnLevel == null) {
            if (reason == NightmareExitReason.SUCCESS) {
                throw new IllegalStateException("Original return dimension is unavailable");
            }
            returnLevel = server.overworld();
        }

        player.teleportTo(
                returnLevel,
                instance.returnX(),
                instance.returnY(),
                instance.returnZ(),
                Set.of(),
                instance.returnYaw(),
                instance.returnPitch()
        );
        teardown(server, instance);
        ShadowSlaveMod.LOGGER.info(
                "Nightmare {} exited for player {} with reason {}",
                instance.instanceId(),
                player.getScoreboardName(),
                reason
        );
        return instance;
    }

    private static void teardown(MinecraftServer server, NightmareInstance instance) {
        ServerLevel nightmareLevel = server.getLevel(NIGHTMARE_LEVEL);
        if (nightmareLevel != null) {
            if (instance.scenarioId().equals(LastSignalScenario.SCENARIO_ID)) {
                LastSignalScenario.removeOwnedEntities(nightmareLevel, instance);
            } else if (instance.scenarioId().equals(DrownedBellScenarioDefinition.SCENARIO_ID)) {
                DrownedBellScenario.removeOwnedEntities(nightmareLevel, instance);
            }
        }

        Optional<NightmareInstance> removed = NightmareRegistryData.get(server).remove(instance);
        if (removed.isPresent()) {
            ShadowSlaveMod.LOGGER.info(
                    "Nightmare {} teardown completed for player {}",
                    instance.instanceId(),
                    instance.playerId()
            );
        } else {
            ShadowSlaveMod.LOGGER.warn(
                    "Nightmare {} teardown skipped because its ownership was already absent for player {}",
                    instance.instanceId(),
                    instance.playerId()
            );
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, path);
    }
}
