package dev.spud.shadowslave.preview;

import dev.spud.shadowslave.attachment.ModAttachments;
import dev.spud.shadowslave.echo.EchoManifestationService;
import dev.spud.shadowslave.echo.EchoOwnershipData;
import dev.spud.shadowslave.echo.EchoOwnershipService;
import dev.spud.shadowslave.memory.BellglassHeldNoteService;
import dev.spud.shadowslave.memory.MemoryManifestationService;
import dev.spud.shadowslave.memory.MemoryOwnershipData;
import dev.spud.shadowslave.memory.MemoryOwnershipService;
import dev.spud.shadowslave.memory.RedThreadCompanionService;
import dev.spud.shadowslave.migration.ImportedIdentityData;
import dev.spud.shadowslave.migration.ImportedIdentityService;
import dev.spud.shadowslave.network.SoulSyncService;
import dev.spud.shadowslave.nightmare.NightmareService;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.identity.AttributeOwnershipData;
import dev.spud.shadowslave.soul.identity.AttributeOwnershipService;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

/** Coordinates the complete preview reset before publishing one client snapshot. */
public final class PreviewResetService {
    private PreviewResetService() {}

    public static void reset(ServerPlayer player) { reset(new ServerOperations(Objects.requireNonNull(player, "player"))); }

    static void reset(Operations operations) {
        Operations checked = Objects.requireNonNull(operations, "operations");
        checked.abortNightmareIfActive();
        SoulData resetSoul = checked.resetSoulWithoutSync();
        checked.clearSoulIdentity();
        checked.clearAttributes();
        checked.clearMemories();
        checked.clearEchoes();
        checked.clearImportedIdentity();
        checked.clearPreviewPower();
        checked.sync(resetSoul);
    }

    interface Operations {
        void abortNightmareIfActive();
        SoulData resetSoulWithoutSync();
        void clearSoulIdentity();
        void clearAttributes();
        void clearMemories();
        void clearEchoes();
        void clearImportedIdentity();
        void clearPreviewPower();
        void sync(SoulData resetSoul);
    }

    private record ServerOperations(ServerPlayer player) implements Operations {
        @Override public void abortNightmareIfActive() { if (NightmareService.activeFor(player).isPresent()) NightmareService.abortForPreviewReset(player); }
        @Override public SoulData resetSoulWithoutSync() { return SoulService.resetWithoutSync(player); }
        @Override public void clearSoulIdentity() { SoulIdentityService.replace(player, SoulIdentityData.empty()); }
        @Override public void clearAttributes() { AttributeOwnershipService.replace(player, AttributeOwnershipData.empty()); }
        @Override public void clearMemories() {
            MemoryManifestationService.clearAshCompassManifestations(player);
            MemoryManifestationService.clearBellglassTokenManifestations(player);
            MemoryManifestationService.clearRedThreadBraceletManifestations(player);
            BellglassHeldNoteService.clear(player);
            RedThreadCompanionService.clear(player);
            MemoryOwnershipService.replace(player, MemoryOwnershipData.empty());
        }
        @Override public void clearEchoes() {
            EchoManifestationService.clearOwnedManifestations(player);
            EchoOwnershipService.replace(player, EchoOwnershipData.empty());
        }
        @Override public void clearImportedIdentity() { ImportedIdentityService.replace(player, ImportedIdentityData.empty()); }
        @Override public void clearPreviewPower() { player.setData(ModAttachments.PREVIEW_POWER, PreviewPowerData.empty()); }
        @Override public void sync(SoulData resetSoul) { SoulSyncService.sync(player, resetSoul, false); }
    }
}
