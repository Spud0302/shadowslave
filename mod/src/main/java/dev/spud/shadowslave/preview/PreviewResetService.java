package dev.spud.shadowslave.preview;

import dev.spud.shadowslave.attachment.ModAttachments;
import dev.spud.shadowslave.migration.ImportedIdentityData;
import dev.spud.shadowslave.migration.ImportedIdentityService;
import dev.spud.shadowslave.network.SoulSyncService;
import dev.spud.shadowslave.nightmare.NightmareService;
import dev.spud.shadowslave.persistence.SavedDataPersistence;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.Objects;

/** Coordinates the complete preview reset before publishing one client snapshot. */
public final class PreviewResetService {
    private PreviewResetService() {
    }

    public static void reset(ServerPlayer player) {
        reset(new ServerOperations(Objects.requireNonNull(player, "player")));
    }

    /** Replays a durable compound preview reset before ordinary Nightmare recovery on login. */
    public static boolean resumePending(ServerPlayer player) {
        ServerPlayer checkedPlayer = Objects.requireNonNull(player, "player");
        PreviewResetRegistryData registry = PreviewResetRegistryData.get(checkedPlayer.getServer());
        if (registry.recoveryBlocked()) {
            checkedPlayer.sendSystemMessage(Component.literal(
                    "Preview reset recovery is blocked because persisted reset metadata is unreadable. "
                            + "Administrator repair is required before Nightmare recovery can continue."
            ).withStyle(ChatFormatting.RED));
            return true;
        }
        if (!registry.isPending(checkedPlayer.getUUID())) {
            return false;
        }
        reset(new ServerOperations(checkedPlayer));
        return true;
    }

    static void reset(Operations operations) {
        Operations checkedOperations = Objects.requireNonNull(operations, "operations");

        checkedOperations.beginResetIntent();
        checkedOperations.persistRegistry();
        checkedOperations.verifyResetIntentDurable();

        checkedOperations.abortNightmareIfActive();
        checkedOperations.clearSuccessfulCompletion();
        SoulData resetSoul = checkedOperations.resetSoulWithoutSync();
        checkedOperations.clearSoulIdentity();
        checkedOperations.clearImportedIdentity();
        checkedOperations.clearPreviewPower();
        checkedOperations.persistPlayer();

        checkedOperations.sync(resetSoul);

        checkedOperations.completeResetIntent();
        checkedOperations.persistRegistry();
    }

    /**
     * Production reset reconciliation policy shared by the live adapter and restart-boundary tests.
     * A narrower retained technical/admin exit must finish before ordinary preview teardown is attempted.
     */
    static void reconcileNightmareForReset(NightmareResetOperations operations) {
        NightmareResetOperations checkedOperations = Objects.requireNonNull(operations, "operations");
        if (checkedOperations.resumeTechnicalExit()) {
            return;
        }
        if (checkedOperations.activeNightmarePresent()) {
            checkedOperations.abortForPreviewReset();
        }
    }

    interface NightmareResetOperations {
        boolean resumeTechnicalExit();

        boolean activeNightmarePresent();

        void abortForPreviewReset();
    }

    interface Operations {
        void beginResetIntent();

        void persistRegistry();

        void verifyResetIntentDurable();

        void abortNightmareIfActive();

        void clearSuccessfulCompletion();

        SoulData resetSoulWithoutSync();

        void clearSoulIdentity();

        void clearImportedIdentity();

        void clearPreviewPower();

        void persistPlayer();

        void sync(SoulData resetSoul);

        void completeResetIntent();
    }

    private record ServerOperations(ServerPlayer player) implements Operations {
        private PreviewResetRegistryData registry() {
            return PreviewResetRegistryData.get(player.getServer());
        }

        @Override
        public void beginResetIntent() {
            PreviewResetRegistryData resetRegistry = registry();
            resetRegistry.begin(player.getUUID());
            // A same-process retry after an ambiguous write must serialize again even
            // when the matching marker is already present in memory.
            resetRegistry.setDirty();
        }

        @Override
        public void persistRegistry() {
            SavedDataPersistence.saveAndWait(player.getServer());
        }

        @Override
        public void verifyResetIntentDurable() {
            Path resetRegistryFile = player.getServer()
                    .getWorldPath(LevelResource.ROOT)
                    .resolve("data")
                    .resolve("shadowslave_preview_resets.dat");
            PersistedPreviewResetIntentVerifier.requirePresent(resetRegistryFile, player.getUUID());
        }

        @Override
        public void abortNightmareIfActive() {
            reconcileNightmareForReset(new NightmareResetOperations() {
                @Override
                public boolean resumeTechnicalExit() {
                    return NightmareService.resumeTechnicalExit(player);
                }

                @Override
                public boolean activeNightmarePresent() {
                    return NightmareService.activeFor(player).isPresent();
                }

                @Override
                public void abortForPreviewReset() {
                    NightmareService.abortForPreviewReset(player);
                }
            });
        }

        @Override
        public void clearSuccessfulCompletion() {
            NightmareService.clearSuccessfulCompletionForPreviewReset(player);
        }

        @Override
        public SoulData resetSoulWithoutSync() {
            return SoulService.resetWithoutSync(player);
        }

        @Override
        public void clearSoulIdentity() {
            SoulIdentityService.replace(player, SoulIdentityData.empty());
        }

        @Override
        public void clearImportedIdentity() {
            ImportedIdentityService.replace(player, ImportedIdentityData.empty());
        }

        @Override
        public void clearPreviewPower() {
            player.setData(ModAttachments.PREVIEW_POWER, PreviewPowerData.empty());
        }

        @Override
        public void persistPlayer() {
            player.getServer().getPlayerList().saveAll();
        }

        @Override
        public void sync(SoulData resetSoul) {
            SoulSyncService.sync(player, resetSoul, false);
        }

        @Override
        public void completeResetIntent() {
            registry().complete(player.getUUID());
        }
    }
}
