package dev.spud.shadowslave.nightmare;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.appraisal.PreviewAppraisalService;
import dev.spud.shadowslave.persistence.SavedDataPersistence;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Server-backed successful-completion operations for the restart-replayable coordinator.
 *
 * <p>This adapter deliberately contains no event routing. A later runtime slice may construct it
 * only after resolving the authoritative retained completion receipt for the player.</p>
 */
final class ServerNightmareCompletionOperations implements NightmareCompletionCoordinator.Operations {
    private final ServerPlayer player;
    private final MinecraftServer server;
    private final NightmareRegistryData registry;
    private final NightmareInstance instance;

    ServerNightmareCompletionOperations(
            ServerPlayer player,
            MinecraftServer server,
            NightmareRegistryData registry,
            NightmareInstance instance
    ) {
        this.player = Objects.requireNonNull(player, "player");
        this.server = Objects.requireNonNull(server, "server");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.instance = Objects.requireNonNull(instance, "instance");
        if (!instance.playerId().equals(player.getUUID())) {
            throw new IllegalArgumentException("Successful Nightmare completion instance belongs to another player");
        }
        requireMatchingReceipt(instance, registry.findSuccessfulCompletionByPlayer(player.getUUID()));
    }

    @Override
    public NightmareCompletionPhase phase() {
        return requireMatchingReceipt(
                instance,
                registry.findSuccessfulCompletionByPlayer(player.getUUID())
        ).phase();
    }

    @Override
    public boolean appraisalApplied() {
        return PreviewAppraisalService.isApplied(player, instance);
    }

    @Override
    public boolean playerInNightmare() {
        return player.serverLevel().dimension().equals(NightmareService.NIGHTMARE_LEVEL);
    }

    @Override
    public boolean activeOwnershipPresent() {
        return exactActiveOwnershipPresent(
                instance,
                registry.findByPlayer(player.getUUID())
        );
    }

    @Override
    public void applyAppraisal() {
        PreviewAppraisalService.appraise(player, instance);
    }

    @Override
    public void returnPlayer() {
        ResourceKey<Level> returnKey = ResourceKey.create(Registries.DIMENSION, instance.returnDimension());
        ServerLevel returnLevel = server.getLevel(returnKey);
        if (returnLevel == null) {
            throw new IllegalStateException("Original return dimension is unavailable");
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
    }

    @Override
    public void teardownActiveInstance() {
        ServerLevel nightmareLevel = server.getLevel(NightmareService.NIGHTMARE_LEVEL);
        if (nightmareLevel != null) {
            LastSignalScenario.removeOwnedEntities(nightmareLevel, instance);
        }

        NightmareInstance removed = registry.remove(instance)
                .orElseThrow(() -> new IllegalStateException(
                        "Successful Nightmare teardown lost exact active ownership before removal"
                ));
        ShadowSlaveMod.LOGGER.info(
                "Nightmare {} successful-completion teardown completed for player {}",
                removed.instanceId(),
                removed.playerId()
        );
    }

    @Override
    public void advancePhase(NightmareCompletionPhase target) {
        registry.advanceSuccessfulCompletion(instance, target);
    }

    @Override
    public void persistPlayer() {
        // The mapped per-player save method is protected in NeoForge 1.21.1.
        // Successful completion is rare enough to use the public synchronous boundary.
        server.getPlayerList().saveAll();
    }

    @Override
    public void persistRegistry() {
        SavedDataPersistence.saveAndWait(server);
    }

    static NightmareCompletionRecord requireMatchingReceipt(
            NightmareInstance expected,
            Optional<NightmareCompletionRecord> receipt
    ) {
        NightmareInstance checkedExpected = Objects.requireNonNull(expected, "expected");
        NightmareCompletionRecord checkedReceipt = Objects.requireNonNull(receipt, "receipt")
                .orElseThrow(() -> new IllegalStateException("Successful Nightmare receipt disappeared"));
        if (!checkedReceipt.instance().equals(checkedExpected)) {
            throw new IllegalStateException("Successful Nightmare receipt no longer matches the exact resolved instance");
        }
        return checkedReceipt;
    }

    static boolean exactActiveOwnershipPresent(
            NightmareInstance expected,
            Optional<NightmareInstance> active
    ) {
        NightmareInstance checkedExpected = Objects.requireNonNull(expected, "expected");
        Optional<NightmareInstance> checkedActive = Objects.requireNonNull(active, "active");
        if (checkedActive.isEmpty()) {
            return false;
        }
        if (!checkedActive.orElseThrow().equals(checkedExpected)) {
            throw new IllegalStateException("Active Nightmare ownership no longer matches the exact completion snapshot");
        }
        return true;
    }
}
