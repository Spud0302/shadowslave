package dev.spud.shadowslave.nightmare;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.persistence.PersistenceFileCheckpoint;
import dev.spud.shadowslave.persistence.SavedDataPersistence;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.SoulTransitions;
import dev.spud.shadowslave.soul.SpellState;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** One entry choke point and one teardown path for the playable First Nightmare preview. */
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
        if (!entryOriginAllowed(player.serverLevel().dimension())) {
            throw new IllegalStateException(
                    "Cannot begin a new Nightmare while the player is already inside the Nightmare dimension"
            );
        }

        MinecraftServer server = player.getServer();
        NightmareRegistryData registry = NightmareRegistryData.get(server);
        if (registry.findByPlayer(player.getUUID()).isPresent()) {
            throw new IllegalStateException("You already own an active Nightmare instance");
        }
        ServerLevel nightmareLevel = server.getLevel(NIGHTMARE_LEVEL);
        if (nightmareLevel == null) {
            throw new IllegalStateException("The bundled Nightmare dimension is unavailable");
        }

        NightmareInstance instance = registry.create(
                player,
                LastSignalScenario.SCENARIO_ID,
                LastSignalScenario.ROLE_ID
        );
        NightmareInstance prepared = instance;
        boolean teleportCommitted = false;
        try {
            prepared = LastSignalScenario.prepare(nightmareLevel, player, instance);
            registry.update(prepared);
            NightmareInstance committedPrepared = prepared;
            Path nightmareRegistryFile = server.getWorldPath(LevelResource.ROOT)
                    .resolve("data")
                    .resolve("shadowslave_nightmares.dat");
            Path playerDataFile = server.getWorldPath(LevelResource.PLAYER_DATA_DIR)
                    .resolve(player.getStringUUID() + ".dat");
            PersistenceFileCheckpoint.Snapshot[] playerFileBeforeEntry = new PersistenceFileCheckpoint.Snapshot[1];
            NightmareEntryDurabilityCoordinator.commit(new NightmareEntryDurabilityCoordinator.Operations() {
                @Override
                public void persistPreparedOwnership() {
                    PersistenceFileCheckpoint.Snapshot before = PersistenceFileCheckpoint.capture(nightmareRegistryFile);
                    SavedDataPersistence.saveAndWait(server);
                    PersistenceFileCheckpoint.requireChanged(
                            nightmareRegistryFile,
                            before,
                            "Prepared Nightmare ownership"
                    );
                }

                @Override
                public void applyPlayerEntry() {
                    playerFileBeforeEntry[0] = PersistenceFileCheckpoint.capture(playerDataFile);
                    SoulService.beginFirstNightmare(player);
                    player.teleportTo(
                            nightmareLevel,
                            committedPrepared.origin().getX() + 0.5,
                            committedPrepared.origin().getY() + 1.0,
                            committedPrepared.origin().getZ() - 1.5,
                            Set.of(),
                            0.0F,
                            0.0F
                    );
                    if (!entryTeleportCommitted(player.serverLevel().dimension())) {
                        throw new IllegalStateException(
                                "Nightmare entry teleport returned without moving the player into the Nightmare dimension"
                        );
                    }
                }

                @Override
                public void persistCommittedPlayer() {
                    PersistenceFileCheckpoint.Snapshot before = playerFileBeforeEntry[0];
                    if (before == null) {
                        throw new IllegalStateException("Player persistence checkpoint was not captured before Nightmare entry");
                    }
                    server.getPlayerList().saveAll();
                    PersistenceFileCheckpoint.requireChanged(
                            playerDataFile,
                            before,
                            "Committed Nightmare player state"
                    );
                }
            });
            teleportCommitted = true;
            player.sendSystemMessage(Component.literal("First Nightmare — The Last Signal")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
            player.sendSystemMessage(Component.literal(
                    "Role: the last watchkeeper of a road already swallowed by ruin. Reach the dead signal fire and rekindle it."
            ).withStyle(ChatFormatting.GRAY));
            player.sendSystemMessage(Component.literal(
                    "Right-click the unlit soul campfire at the far watch. Fighting the pursuer is optional; resolving the conflict is not."
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
            return prepared;
        } catch (RuntimeException exception) {
            teleportCommitted = entryCommittedAtFailure(
                    teleportCommitted,
                    player.serverLevel().dimension()
            );
            if (shouldRollbackFailedEntry(teleportCommitted)) {
                NightmareInstance attempted = prepared;
                NightmareFailedEntryRollbackCoordinator.rollback(
                        new NightmareFailedEntryRollbackCoordinator.Operations() {
                            @Override
                            public void rollbackAuthoritativeState() {
                                rollbackFailedEntry(server, attempted);
                            }

                            @Override
                            public void restoreSoul() {
                                SoulService.replace(player, beforeSoul);
                            }
                        }
                );
                throw new IllegalStateException("Nightmare entry failed and was rolled back", exception);
            }
            throw new IllegalStateException(
                    "Nightmare entry committed at teleport but post-entry handling failed; ownership was retained",
                    exception
            );
        }
    }

    public static boolean resolveSignalFire(ServerPlayer player, net.minecraft.core.BlockPos interactedPos) {
        NightmareInstance instance = activeFor(player).orElse(null);
        if (instance == null
                || !player.serverLevel().dimension().equals(NIGHTMARE_LEVEL)
                || !instance.altar().equals(interactedPos)) {
            return false;
        }

        MinecraftServer server = player.getServer();
        NightmareRegistryData registry = NightmareRegistryData.get(server);
        Path nightmareRegistryFile = server.getWorldPath(LevelResource.ROOT)
                .resolve("data")
                .resolve("shadowslave_nightmares.dat");
        PersistenceFileCheckpoint.Snapshot[] registryBeforeTerminalResolution =
                new PersistenceFileCheckpoint.Snapshot[1];
        NightmareSuccessfulCompletionActivation.run(new NightmareSuccessfulCompletionActivation.Operations() {
            @Override
            public void validateTerminalResolution() {
                LastSignalScenario.requireResolvableAltar(player.serverLevel(), instance);
            }

            @Override
            public void captureRegistryBeforeTerminalResolution() {
                registryBeforeTerminalResolution[0] = PersistenceFileCheckpoint.capture(nightmareRegistryFile);
            }

            @Override
            public void recordTerminalResolution() {
                registry.beginSuccessfulCompletion(instance, player.serverLevel().getGameTime());
            }

            @Override
            public void persistRegistry() {
                SavedDataPersistence.saveAndWait(server);
            }

            @Override
            public void verifyTerminalRegistryDurable() {
                PersistenceFileCheckpoint.Snapshot before = registryBeforeTerminalResolution[0];
                if (before == null) {
                    throw new IllegalStateException(
                            "Nightmare registry persistence checkpoint was not captured before terminal resolution"
                    );
                }
                PersistenceFileCheckpoint.requireChanged(
                        nightmareRegistryFile,
                        before,
                        "Successful Nightmare completion receipt"
                );
            }

            @Override
            public void discardUnverifiedTerminalResolution() {
                registry.clearSuccessfulCompletion(instance)
                        .orElseThrow(() -> new IllegalStateException(
                                "Unverified successful Nightmare completion receipt disappeared before quarantine"
                        ));
            }

            @Override
            public void afterTerminalRegistryDurable() {
                NightmareCompletionFaultInjector.afterDurableBoundary(
                        NightmareCompletionFaultPoint.AFTER_TERMINAL_REGISTRY_SAVE
                );
            }

            @Override
            public void applyWorldResolutionPresentation() {
                LastSignalScenario.igniteAltar(player.serverLevel(), instance);
            }

            @Override
            public boolean resumeCompletion() {
                return resumeSuccessfulCompletion(player);
            }
        });
        return true;
    }

    /** Replays a durable technical/admin exit intent before any competing completion recovery. */
    public static boolean resumeTechnicalExit(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        NightmareRegistryData registry = NightmareRegistryData.get(player.getServer());
        NightmareExitReason reason = registry.findTechnicalExitReasonByPlayer(player.getUUID()).orElse(null);
        if (reason == null) {
            return false;
        }
        technicalExit(player, reason);
        return true;
    }

    /**
     * Reconciles and finishes a durable successful completion receipt.
     *
     * <p>Safe to call after the terminal event and on login. The coordinator derives
     * required actions from the retained receipt plus authoritative player/registry
     * state rather than assuming separate persistence surfaces committed together.</p>
     */
    public static boolean resumeSuccessfulCompletion(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        MinecraftServer server = player.getServer();
        NightmareRegistryData registry = NightmareRegistryData.get(server);
        NightmareCompletionRecord completion = registry
                .findSuccessfulCompletionByPlayer(player.getUUID())
                .orElse(null);
        if (completion == null) {
            return false;
        }
        if (!completion.instance().playerId().equals(player.getUUID())) {
            throw new IllegalStateException("Successful Nightmare receipt belongs to another player");
        }

        ServerNightmareCompletionOperations operations = new ServerNightmareCompletionOperations(
                player,
                server,
                registry,
                completion.instance()
        );
        boolean recoveryDidWork = !operations.appraisalApplied()
                || operations.playerInNightmare()
                || operations.activeOwnershipPresent();
        NightmareCompletionPhase startingPhase = completion.phase();

        NightmareCompletionCoordinator.resume(operations);
        NightmareCompletionRecord finished = ServerNightmareCompletionOperations.requireMatchingReceipt(
                completion.instance(),
                registry.findSuccessfulCompletionByPlayer(player.getUUID())
        );

        if (startingPhase != NightmareCompletionPhase.TEARDOWN_COMMITTED || recoveryDidWork) {
            player.sendSystemMessage(Component.literal(
                    "The signal answers. The Spell appraises the life you lived in the borrowed role."
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
            player.sendSystemMessage(Component.literal(
                    "Aspect revealed: [Last Light] — Awakened Rank. Flaw revealed: [Cold Ash]."
            ).withStyle(ChatFormatting.AQUA));
        }

        ShadowSlaveMod.LOGGER.info(
                "Nightmare {} successful completion reconciled for player {} at phase {}",
                completion.instance().instanceId(),
                player.getScoreboardName(),
                finished.phase()
        );
        return true;
    }

    public static NightmareInstance technicalRecover(ServerPlayer player) {
        NightmareInstance instance = technicalExit(player, NightmareExitReason.TECHNICAL_RECOVERY);
        player.sendSystemMessage(Component.literal(
                "Technical recovery completed. This is an administrative path, not mercy from the Nightmare Spell."
        ).withStyle(ChatFormatting.YELLOW));
        return instance;
    }

    public static NightmareInstance adminAbort(ServerPlayer player) {
        return technicalExit(player, NightmareExitReason.ADMIN_ABORT);
    }

    /**
     * Exits through the normal teleport and teardown path for a compound preview
     * reset, but deliberately omits the Carrier recovery mutations and their sync.
     * The caller is responsible for resetting every persistent preview attachment
     * and sending the one final authoritative snapshot.
     */
    public static NightmareInstance abortForPreviewReset(ServerPlayer player) {
        return exit(player, NightmareExitReason.ADMIN_ABORT);
    }

    /**
     * Clears a retained successful-completion receipt during an explicit development reset.
     * This is ordinary reset compatibility only; restart-atomic compound reset remains separate work.
     */
    public static void clearSuccessfulCompletionForPreviewReset(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        MinecraftServer server = player.getServer();
        NightmareRegistryData registry = NightmareRegistryData.get(server);
        NightmareCompletionRecord completion = registry.findSuccessfulCompletionByPlayer(player.getUUID()).orElse(null);
        if (completion == null) {
            return;
        }
        registry.clearSuccessfulCompletion(completion.instance())
                .orElseThrow(() -> new IllegalStateException("Successful Nightmare receipt disappeared during preview reset"));
        SavedDataPersistence.saveAndWait(server);
    }

    public static Optional<NightmareInstance> activeFor(ServerPlayer player) {
        return NightmareRegistryData.get(player.getServer()).findByPlayer(player.getUUID());
    }

    public static Optional<NightmareCompletionRecord> successfulCompletionFor(ServerPlayer player) {
        return NightmareRegistryData.get(player.getServer())
                .findSuccessfulCompletionByPlayer(player.getUUID());
    }

    static boolean entryOriginAllowed(ResourceKey<Level> currentDimension) {
        return !NIGHTMARE_LEVEL.equals(Objects.requireNonNull(currentDimension, "currentDimension"));
    }

    static boolean entryTeleportCommitted(ResourceKey<Level> actualDimension) {
        return NIGHTMARE_LEVEL.equals(Objects.requireNonNull(actualDimension, "actualDimension"));
    }

    static boolean entryCommittedAtFailure(
            boolean teleportCommitted,
            ResourceKey<Level> actualDimension
    ) {
        return teleportCommitted || entryTeleportCommitted(actualDimension);
    }

    static boolean shouldRollbackFailedEntry(boolean teleportCommitted) {
        return !teleportCommitted;
    }

    static boolean returnTeleportCommitted(
            ResourceKey<Level> actualDimension,
            ResourceKey<Level> expectedDimension
    ) {
        return Objects.requireNonNull(expectedDimension, "expectedDimension")
                .equals(Objects.requireNonNull(actualDimension, "actualDimension"));
    }

    private static NightmareInstance technicalExit(ServerPlayer player, NightmareExitReason reason) {
        Objects.requireNonNull(player, "player");
        MinecraftServer server = player.getServer();
        NightmareRegistryData registry = NightmareRegistryData.get(server);
        NightmareInstance instance = activeFor(player)
                .orElseThrow(() -> new IllegalStateException("Player does not own an active Nightmare"));

        ResourceKey<Level> expectedReturnDimension = teleportToReturn(player, instance, reason);
        if (!returnTeleportCommitted(player.serverLevel().dimension(), expectedReturnDimension)) {
            throw new IllegalStateException(
                    "Nightmare exit teleport did not reach its selected return dimension; ownership was retained"
            );
        }

        NightmareTechnicalExitCoordinator.commit(new ServerTechnicalExitOperations(
                player,
                server,
                registry,
                instance,
                reason
        ));
        ShadowSlaveMod.LOGGER.info(
                "Nightmare {} exited for player {} with reason {}",
                instance.instanceId(),
                player.getScoreboardName(),
                reason
        );
        return instance;
    }

    private static NightmareInstance exit(ServerPlayer player, NightmareExitReason reason) {
        MinecraftServer server = player.getServer();
        NightmareInstance instance = activeFor(player)
                .orElseThrow(() -> new IllegalStateException("Player does not own an active Nightmare"));

        ResourceKey<Level> expectedReturnDimension = teleportToReturn(player, instance, reason);
        if (!returnTeleportCommitted(player.serverLevel().dimension(), expectedReturnDimension)) {
            throw new IllegalStateException(
                    "Nightmare exit teleport did not reach its selected return dimension; ownership was retained"
            );
        }
        teardown(server, instance);
        ShadowSlaveMod.LOGGER.info(
                "Nightmare {} exited for player {} with reason {}",
                instance.instanceId(),
                player.getScoreboardName(),
                reason
        );
        return instance;
    }

    private static ResourceKey<Level> teleportToReturn(
            ServerPlayer player,
            NightmareInstance instance,
            NightmareExitReason reason
    ) {
        MinecraftServer server = player.getServer();
        ResourceKey<Level> returnKey = ResourceKey.create(Registries.DIMENSION, instance.returnDimension());
        ServerLevel returnLevel = server.getLevel(returnKey);
        if (returnLevel == null) {
            if (reason == NightmareExitReason.SUCCESS) {
                throw new IllegalStateException("Original return dimension is unavailable");
            }
            returnLevel = server.overworld();
        }

        ResourceKey<Level> expectedReturnDimension = returnLevel.dimension();
        player.teleportTo(
                returnLevel,
                instance.returnX(),
                instance.returnY(),
                instance.returnZ(),
                Set.of(),
                instance.returnYaw(),
                instance.returnPitch()
        );
        return expectedReturnDimension;
    }

    private static void rollbackFailedEntry(MinecraftServer server, NightmareInstance attempted) {
        ServerLevel nightmareLevel = server.getLevel(NIGHTMARE_LEVEL);
        if (nightmareLevel != null) {
            LastSignalScenario.rollbackFailedEntryWorld(nightmareLevel, attempted);
        }

        NightmareRegistryData registry = NightmareRegistryData.get(server);
        Optional<NightmareInstance> removed = removeMatchingEntryOwnership(registry, attempted);
        if (removed.isPresent()) {
            ShadowSlaveMod.LOGGER.info(
                    "Nightmare {} failed-entry ownership rollback completed for player {}",
                    attempted.instanceId(),
                    attempted.playerId()
            );
        } else {
            ShadowSlaveMod.LOGGER.warn(
                    "Nightmare {} failed-entry ownership rollback skipped because matching ownership was absent for player {}",
                    attempted.instanceId(),
                    attempted.playerId()
            );
        }
    }

    static Optional<NightmareInstance> removeMatchingEntryOwnership(
            NightmareRegistryData registry,
            NightmareInstance attempted
    ) {
        NightmareRegistryData checkedRegistry = Objects.requireNonNull(registry, "registry");
        NightmareInstance checkedAttempted = Objects.requireNonNull(attempted, "attempted");
        return checkedRegistry.findByPlayer(checkedAttempted.playerId())
                .filter(active -> active.instanceId().equals(checkedAttempted.instanceId()))
                .flatMap(checkedRegistry::remove);
    }

    private static void teardown(MinecraftServer server, NightmareInstance instance) {
        ServerLevel nightmareLevel = server.getLevel(NIGHTMARE_LEVEL);
        if (nightmareLevel != null) {
            LastSignalScenario.removeOwnedEntities(nightmareLevel, instance);
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

    private static void teardownTechnicalExit(
            MinecraftServer server,
            NightmareRegistryData registry,
            NightmareInstance instance
    ) {
        ServerLevel nightmareLevel = server.getLevel(NIGHTMARE_LEVEL);
        if (nightmareLevel != null) {
            LastSignalScenario.removeOwnedEntities(nightmareLevel, instance);
        }
        registry.completeTechnicalExit(instance);
        ShadowSlaveMod.LOGGER.info(
                "Nightmare {} technical-exit teardown completed for player {}",
                instance.instanceId(),
                instance.playerId()
        );
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, path);
    }

    private record ServerTechnicalExitOperations(
            ServerPlayer player,
            MinecraftServer server,
            NightmareRegistryData registry,
            NightmareInstance instance,
            NightmareExitReason reason
    ) implements NightmareTechnicalExitCoordinator.Operations {
        @Override
        public void recordTechnicalExitIntent() {
            registry.beginTechnicalExit(instance, reason);
        }

        @Override
        public void clearCompletionReceipt() {
            registry.clearSuccessfulCompletion(instance);
        }

        @Override
        public void persistRegistry() {
            SavedDataPersistence.saveAndWait(server);
        }

        @Override
        public void resetPlayerState() {
            SoulIdentityService.replace(player, SoulIdentityData.empty());
            SoulService.replace(player, SoulTransitions.infect(SoulData.uninfected()));
        }

        @Override
        public void persistPlayer() {
            server.getPlayerList().saveAll();
        }

        @Override
        public void teardownActiveInstance() {
            teardownTechnicalExit(server, registry, instance);
        }
    }
}
