package dev.spud.shadowslave.nightmare;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.persistence.PersistenceFileCheckpoint;
import dev.spud.shadowslave.persistence.SavedDataPersistence;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.Objects;

/** Canonical First-Nightmare death handling, distinct from technical/admin recovery. */
public final class NightmareDeathService {
    private NightmareDeathService() {
    }

    public static void record(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        NightmareInstance instance = NightmareService.activeFor(player).orElse(null);
        if (instance == null) {
            return;
        }
        commit(player, instance);
        player.sendSystemMessage(Component.literal(
                "Canonical First-Nightmare outcome: death. Minecraft respawn is a development accommodation; the Spell did not safely eject you."
        ).withStyle(ChatFormatting.RED));
    }

    /** Replays a death already recorded durably before other Nightmare recovery modes can reinterpret it. */
    public static boolean resumePending(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        NightmareDeathRegistryData deaths = NightmareDeathRegistryData.get(player.getServer());
        if (deaths.recoveryBlocked()) {
            throw new IllegalStateException(
                    "Canonical Nightmare death recovery is blocked: " + deaths.loadFailure().orElse("unknown marker error")
            );
        }
        NightmareInstance instance = deaths.findByPlayer(player.getUUID()).orElse(null);
        if (instance == null) {
            return false;
        }
        commit(player, instance);
        return true;
    }

    private static void commit(ServerPlayer player, NightmareInstance instance) {
        MinecraftServer server = player.getServer();
        NightmareRegistryData registry = NightmareRegistryData.get(server);
        NightmareDeathRegistryData deaths = NightmareDeathRegistryData.get(server);
        Path deathRegistryFile = server.getWorldPath(LevelResource.ROOT)
                .resolve("data")
                .resolve("shadowslave_nightmare_deaths.dat");
        PersistenceFileCheckpoint.Snapshot[] deathIntentBaseline = new PersistenceFileCheckpoint.Snapshot[1];
        boolean deathIntentAlreadyDurable = deaths.isDurablyTrusted(instance);

        NightmareInstance active = registry.findByPlayer(player.getUUID()).orElse(null);
        if (active != null && !active.equals(instance)) {
            throw new IllegalStateException("Canonical death marker does not match current active Nightmare ownership");
        }

        NightmareDeathCoordinator.commit(new NightmareDeathCoordinator.Operations() {
            @Override
            public boolean deathIntentAlreadyDurable() {
                return deathIntentAlreadyDurable;
            }

            @Override
            public void captureDeathIntentBaseline() {
                deathIntentBaseline[0] = PersistenceFileCheckpoint.capture(deathRegistryFile);
            }

            @Override
            public void recordDeathIntent() {
                deaths.begin(instance);
            }

            @Override
            public void persistDeathIntent() {
                SavedDataPersistence.saveAndWait(server);
            }

            @Override
            public void verifyDeathIntentPersisted() {
                PersistenceFileCheckpoint.Snapshot before = deathIntentBaseline[0];
                if (before == null) {
                    throw new IllegalStateException("Canonical death persistence checkpoint was not captured before recording death intent");
                }
                PersistenceFileCheckpoint.requireChanged(
                        deathRegistryFile,
                        before,
                        "Canonical Nightmare death intent"
                );
            }

            @Override
            public void markDeathIntentDurable() {
                deaths.markDurablyTrusted(instance);
            }

            @Override
            public void clearCompletionReceipt() {
                registry.clearSuccessfulCompletion(instance);
            }

            @Override
            public void persistNightmareRegistry() {
                SavedDataPersistence.saveAndWait(server);
            }

            @Override
            public void resetPlayerState() {
                SoulIdentityService.replace(player, SoulIdentityData.empty());
                SoulService.reset(player);
            }

            @Override
            public void persistPlayer() {
                server.getPlayerList().saveAll();
            }

            @Override
            public void teardownActiveInstance() {
                ServerLevel nightmareLevel = server.getLevel(NightmareService.NIGHTMARE_LEVEL);
                if (nightmareLevel != null) {
                    LastSignalScenario.removeOwnedEntities(nightmareLevel, instance);
                }
                if (registry.findByPlayer(player.getUUID()).isPresent()) {
                    registry.remove(instance);
                }
            }

            @Override
            public void clearDeathIntent() {
                deaths.complete(instance);
            }
        });

        ShadowSlaveMod.LOGGER.info(
                "Canonical Nightmare death reconciled for player {} instance {}",
                player.getScoreboardName(),
                instance.instanceId()
        );
    }
}
