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

import java.util.Objects;

/** Coordinates the complete preview reset before publishing one client snapshot. */
public final class PreviewResetService {
    private PreviewResetService() {
    }

    public static void reset(ServerPlayer player) {
        reset(new ServerOperations(Objects.requireNonNull(player, "player")));
    }

    /** Replays a durable compound preview reset before Nightmare recovery on login. */
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

        checkedOperations.abortNightmareIfActive();
        checkedOperations.clearNightmareCompletion();
        SoulData resetSoul = checkedOperations.resetSoulWithoutSync();
        checkedOperations.clearSoulIdentity();
        checkedOperations.clearImportedIdentity();
        checkedOperations.clearPreviewPower();
        checkedOperations.persistPlayer();

        checkedOperations.sync(resetSoul);

        checkedOperations.completeResetIntent();
        checkedOperations.persistRegistry();
    }

    interface Operations {
        void beginResetIntent();

        void persistRegistry();

        void abortNightmareIfActive();

        void clearNightmareCompletion();

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
            registry().begin(player.getUUID());
        }

        @Override
        public void persistRegistry() {
            SavedDataPersistence.saveAndWait(player.getServer());
        }

        @Override
        public void abortNightmareIfActive() {
            if (NightmareService.activeFor(player).isPresent()) {
                NightmareService.abortForPreviewReset(player);
            }
        }

        @Override
        public void clearNightmareCompletion() {
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
