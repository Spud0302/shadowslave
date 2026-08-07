package dev.spud.shadowslave.nightmare;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.appraisal.PreviewAppraisalService;
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
                    "Cannot start a new Nightmare while already inside the Nightmare dimension"
            );
        }

        MinecraftServer server = player.getServer();
        NightmareRegistryData registry = NightmareRegistryData.get(server);
        if (registry.findByPlayer(player.getUUID()).isPresent()) {
            throw new IllegalStateException("You already own an active Nightmare instance");
        }
        if (registry.findSuccessfulCompletionByPlayer(player.getUUID()).isPresent()) {
            throw new IllegalStateException(
                    "A retained successful Nightmare receipt must be cleared by preview_reset before another First Nightmare"
            );
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
            teleportCommitted = entryTeleportCommitted(player.serverLevel().dimension());
            if (!teleportCommitted) {
                throw new IllegalStateException("Nightmare entry teleport returned without moving the player into the Nightmare dimension");
            }
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
            if (shouldRollbackFailedEntry(teleportCommitted)) {
                rollbackFailedEntry(server, prepared);
                SoulService.replace(player, beforeSoul);
                throw new IllegalStateException("Nightmare entry failed and was rolled back", exception);
            }
            throw new IllegalStateException(
                    "Nightmare entry committed at teleport but post-entry handling failed; ownership was retained",
                    exception
            );
        }
    }

    static boolean entryOriginAllowed(ResourceKey<Level> actualDimension) {
        return !NIGHTMARE_LEVEL.equals(Objects.requireNonNull(actualDimension, "actualDimension"));
    }

    static boolean entryTeleportCommitted(ResourceKey<Level> actualDimension) {
        return NIGHTMARE_LEVEL.equals(Objects.requireNonNull(actualDimension, "actualDimension"));
    }

    static boolean returnTeleportCommitted(
            ResourceKey<Level> actualDimension,
            ResourceKey<Level> expectedDimension
    ) {
        return Objects.requireNonNull(expectedDimension, "expectedDimension")
                .equals(Objects.requireNonNull(actualDimension, "actualDimension"));
    }

    static boolean shouldRollbackFailedEntry(boolean teleportCommitted) {
        return !teleportCommitted;
    }

    public static boolean resolveSignalFire(ServerPlayer player, net.minecraft.core.BlockPos interactedPos) {
        NightmareInstance instance = activeFor(player).orElse(null);
        if (instance == null
                || !player.serverLevel().dimension().equals(NIGHTMARE_LEVEL)
                || !instance.altar().equals(interactedPos)) {
            return false;
        }

        LastSignalScenario.igniteAltar(player.serverLevel(), instance);
        NightmareRegistryData registry = NightmareRegistryData.get(player.getServer());
        registry.beginSuccessfulCompletion(instance, player.serverLevel().getGameTime());
        persistRegistry(player.getServer());

        if (!resumeSuccessfulCompletion(player)) {
            throw new IllegalStateException("Successful Nightmare receipt disappeared before completion recovery");
        }
        return true;
    }

    /**
     * Reconciles and finishes a durable successful completion receipt.
     *
     * <p>This method is safe to call on login and after any durable boundary.
     * It derives required actions from the receipt plus actual player/registry
     * state rather than trusting that SavedData and player attachments reached
     * disk in one particular order.</p>
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

        ServerCompletionOperations operations = new ServerCompletionOperations(
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
        NightmareCompletionRecord finished = registry.findSuccessfulCompletionByPlayer(player.getUUID())
                .orElseThrow(() -> new IllegalStateException("Successful Nightmare receipt disappeared"));

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
        NightmareInstance instance = exit(player, NightmareExitReason.TECHNICAL_RECOVERY);
        NightmareRegistryData registry = NightmareRegistryData.get(player.getServer());
        registry.clearSuccessfulCompletion(instance);
        persistRegistry(player.getServer());
        SoulIdentityService.replace(player, SoulIdentityData.empty());
        SoulService.replace(player, SoulTransitions.infect(SoulData.uninfected()));
        persistPlayer(player);
        player.sendSystemMessage(Component.literal(
                "Technical recovery completed. This is an administrative path, not mercy from the Nightmare Spell."
        ).withStyle(ChatFormatting.YELLOW));
        return instance;
    }

    public static NightmareInstance adminAbort(ServerPlayer player) {
        NightmareInstance instance = exit(player, NightmareExitReason.ADMIN_ABORT);
        NightmareRegistryData registry = NightmareRegistryData.get(player.getServer());
        registry.clearSuccessfulCompletion(instance);
        persistRegistry(player.getServer());
        SoulIdentityService.replace(player, SoulIdentityData.empty());
        SoulService.replace(player, SoulTransitions.infect(SoulData.uninfected()));
        persistPlayer(player);
        return instance;
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

    /** Clears the exact retained success receipt as part of an explicit development reset. */
    public static void clearSuccessfulCompletionForPreviewReset(ServerPlayer player) {
        NightmareRegistryData registry = NightmareRegistryData.get(player.getServer());
        NightmareCompletionRecord completion = registry
                .findSuccessfulCompletionByPlayer(player.getUUID())
                .orElse(null);
        if (completion != null && registry.clearSuccessfulCompletion(completion.instance()).isPresent()) {
            persistRegistry(player.getServer());
        }
    }

    public static void canonicalDeath(ServerPlayer player) {
        NightmareInstance instance = activeFor(player).orElse(null);
        if (instance == null) {
            return;
        }
        teardown(player.getServer(), instance);
        NightmareRegistryData registry = NightmareRegistryData.get(player.getServer());
        registry.clearSuccessfulCompletion(instance);
        persistRegistry(player.getServer());
        SoulIdentityService.replace(player, SoulIdentityData.empty());
        SoulService.reset(player);
        persistPlayer(player);
        player.sendSystemMessage(Component.literal(
                "Canonical First-Nightmare outcome: death. Minecraft respawn is a development accommodation; the Spell did not safely eject you."
        ).withStyle(ChatFormatting.RED));
    }

    public static Optional<NightmareInstance> activeFor(ServerPlayer player) {
        return NightmareRegistryData.get(player.getServer()).findByPlayer(player.getUUID());
    }

    public static Optional<NightmareCompletionRecord> successfulCompletionFor(ServerPlayer player) {
        return NightmareRegistryData.get(player.getServer())
                .findSuccessfulCompletionByPlayer(player.getUUID());
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
        persistPlayer(player);
        teardown(server, instance);
        persistRegistry(server);
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

        player.teleportTo(
                returnLevel,
                instance.returnX(),
                instance.returnY(),
                instance.returnZ(),
                Set.of(),
                instance.returnYaw(),
                instance.returnPitch()
        );
        return returnLevel.dimension();
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

    private static void persistRegistry(MinecraftServer server) {
        server.overworld().getDataStorage().save();
    }

    private static void persistPlayer(ServerPlayer player) {
        // The mapped per-player save method is protected in NeoForge 1.21.1.
        // Successful completion is rare enough to use the public synchronous boundary.
        player.getServer().getPlayerList().saveAll();
    }

    private record ServerCompletionOperations(
            ServerPlayer player,
            MinecraftServer server,
            NightmareRegistryData registry,
            NightmareInstance instance
    ) implements NightmareCompletionCoordinator.Operations {
        @Override
        public NightmareCompletionPhase phase() {
            return registry.findSuccessfulCompletionByPlayer(player.getUUID())
                    .orElseThrow(() -> new IllegalStateException("Successful Nightmare receipt disappeared"))
                    .phase();
        }

        @Override
        public boolean appraisalApplied() {
            return PreviewAppraisalService.isApplied(player, instance);
        }

        @Override
        public boolean playerInNightmare() {
            return player.serverLevel().dimension().equals(NIGHTMARE_LEVEL);
        }

        @Override
        public boolean activeOwnershipPresent() {
            return registry.findByPlayer(player.getUUID())
                    .map(active -> active.instanceId().equals(instance.instanceId()))
                    .orElse(false);
        }

        @Override
        public void applyAppraisal() {
            PreviewAppraisalService.appraise(player, instance);
        }

        @Override
        public void returnPlayer() {
            teleportToReturn(player, instance, NightmareExitReason.SUCCESS);
        }

        @Override
        public void teardownActiveInstance() {
            teardown(server, instance);
        }

        @Override
        public void advancePhase(NightmareCompletionPhase target) {
            registry.advanceSuccessfulCompletion(instance, target);
        }

        @Override
        public void persistPlayer() {
            NightmareService.persistPlayer(player);
        }

        @Override
        public void persistRegistry() {
            NightmareService.persistRegistry(server);
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, path);
    }
}
