package dev.spud.shadowslave.echo;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.echo.content.EchoContentCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Objects;
import java.util.Optional;

/**
 * NeoForge/Minecraft executor for the first playable Echo manifestation.
 * Canonical ownership, identity and command mode remain in {@link EchoOwnershipData};
 * the vanilla Armadillo body is an explicitly replaceable DESIGN adapter for Ash Burrower.
 */
public final class EchoManifestationService {
    public static final ResourceLocation ASH_BURROWER_ID =
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "echo/ash_burrower");
    private static final String ASH_BURROWER_PROFILE_ID = "ash_burrower";
    private static final String MANIFESTATION_TAG = "shadowslave_echo_manifestation";
    private static final double FOLLOW_STOP_DISTANCE_SQUARED = 9.0D;
    private static final double FOLLOW_SPEED = 1.1D;

    private EchoManifestationService() {
    }

    public enum ManifestResult {
        SUMMONED,
        ALREADY_SUMMONED,
        DISMISSED,
        NOT_SUMMONED,
        NOT_OWNED,
        SPAWN_FAILED
    }

    public enum CommandResult {
        COMMAND_SET,
        NOT_OWNED,
        UNSUPPORTED
    }

    public static EchoContentCatalog.EchoProfile ashBurrowerProfile() {
        return EchoContentCatalog.waveOne().stream()
                .filter(profile -> profile.id().equals(ASH_BURROWER_PROFILE_ID))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Ash Burrower Echo profile is missing"));
    }

    public static ManifestResult summonAshBurrower(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        Optional<EchoInstanceData> owned = EchoOwnershipService.get(player).find(ASH_BURROWER_ID);
        if (owned.isEmpty()) {
            return ManifestResult.NOT_OWNED;
        }

        EchoInstanceData state = owned.get();
        if (state.manifestationUuid().isPresent()) {
            Optional<Entity> existing = findManifestation(player, state);
            if (existing.isPresent()) {
                executeCommand(player, state, existing.get());
                return ManifestResult.ALREADY_SUMMONED;
            }
            EchoOwnershipService.clearManifestation(player, ASH_BURROWER_ID);
        }

        ServerLevel level = player.serverLevel();
        Mob echo = EntityType.ARMADILLO.create(level);
        if (echo == null) {
            return ManifestResult.SPAWN_FAILED;
        }
        echo.moveTo(player.getX() + 1.5, player.getY(), player.getZ() + 0.5, player.getYRot(), 0.0F);
        echo.setCustomName(Component.literal(ashBurrowerProfile().displayName()));
        echo.setCustomNameVisible(true);
        echo.setPersistenceRequired();
        echo.setInvulnerable(true);
        echo.addTag(MANIFESTATION_TAG);
        if (!level.addFreshEntity(echo)) {
            return ManifestResult.SPAWN_FAILED;
        }

        EchoOwnershipService.setManifestation(
                player,
                ASH_BURROWER_ID,
                echo.getUUID(),
                level.dimension().location(),
                echo.blockPosition()
        );
        EchoInstanceData persisted = EchoOwnershipService.get(player).find(ASH_BURROWER_ID).orElseThrow();
        executeCommand(player, persisted, echo);
        return ManifestResult.SUMMONED;
    }

    public static ManifestResult dismissAshBurrower(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        Optional<EchoInstanceData> owned = EchoOwnershipService.get(player).find(ASH_BURROWER_ID);
        if (owned.isEmpty()) {
            return ManifestResult.NOT_OWNED;
        }
        EchoInstanceData state = owned.get();
        if (state.manifestationUuid().isEmpty()) {
            return ManifestResult.NOT_SUMMONED;
        }

        Optional<Entity> entity = findManifestation(player, state);
        entity.ifPresent(Entity::discard);
        EchoOwnershipService.clearManifestation(player, ASH_BURROWER_ID);
        return entity.isPresent() ? ManifestResult.DISMISSED : ManifestResult.NOT_SUMMONED;
    }

    public static CommandResult commandAshBurrower(ServerPlayer player, EchoContentCatalog.CommandMode commandMode) {
        Objects.requireNonNull(player, "player");
        EchoContentCatalog.CommandMode checked = Objects.requireNonNull(commandMode, "commandMode");
        Optional<EchoInstanceData> owned = EchoOwnershipService.get(player).find(ASH_BURROWER_ID);
        if (owned.isEmpty()) {
            return CommandResult.NOT_OWNED;
        }
        if (!ashBurrowerProfile().commandModes().contains(checked)) {
            return CommandResult.UNSUPPORTED;
        }

        EchoOwnershipService.setCommandMode(player, ASH_BURROWER_ID, checked);
        EchoInstanceData updated = EchoOwnershipService.get(player).find(ASH_BURROWER_ID).orElseThrow();
        findManifestation(player, updated).ifPresent(entity -> executeCommand(player, updated, entity));
        return CommandResult.COMMAND_SET;
    }

    public static void clearOwnedManifestations(ServerPlayer player) {
        for (EchoInstanceData echo : EchoOwnershipService.get(Objects.requireNonNull(player, "player")).echoes()) {
            findManifestation(player, echo).ifPresent(Entity::discard);
        }
    }

    /** Re-applies durable Java command state to the manifested executor and refreshes its stored location. */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 10 != 0) {
            return;
        }
        Optional<EchoInstanceData> owned = EchoOwnershipService.get(player).find(ASH_BURROWER_ID);
        if (owned.isEmpty() || owned.get().manifestationUuid().isEmpty()) {
            return;
        }
        EchoInstanceData state = owned.get();
        Optional<Entity> manifestation = findManifestation(player, state);
        if (manifestation.isEmpty()) {
            return;
        }
        Entity entity = manifestation.get();
        executeCommand(player, state, entity);
        if (!state.manifestationDimension().orElseThrow().equals(entity.level().dimension().location())
                || !state.manifestationPos().orElseThrow().equals(entity.blockPosition())) {
            EchoOwnershipService.setManifestation(
                    player,
                    ASH_BURROWER_ID,
                    entity.getUUID(),
                    entity.level().dimension().location(),
                    entity.blockPosition()
            );
        }
    }

    private static void executeCommand(ServerPlayer player, EchoInstanceData state, Entity entity) {
        if (!(entity instanceof Mob mob)) {
            return;
        }
        switch (state.commandMode()) {
            case FOLLOW -> {
                if (entity.level() != player.level()) {
                    mob.getNavigation().stop();
                    mob.setNoAi(true);
                    return;
                }
                if (entity.distanceToSqr(player) <= FOLLOW_STOP_DISTANCE_SQUARED) {
                    mob.getNavigation().stop();
                    mob.setNoAi(true);
                } else {
                    mob.setNoAi(false);
                    mob.getNavigation().moveTo(player, FOLLOW_SPEED);
                }
            }
            case HOLD -> {
                mob.getNavigation().stop();
                mob.setNoAi(true);
            }
            default -> {
                // Other catalogue commands remain definition-only until a concrete runtime executor is added.
                mob.getNavigation().stop();
                mob.setNoAi(true);
            }
        }
    }

    /**
     * Loads the exact stored manifestation chunk before checking the UUID. The stored
     * position is refreshed while the Echo moves, preventing FOLLOW from creating a
     * stale-location duplicate after a save/restart.
     */
    private static Optional<Entity> findManifestation(ServerPlayer player, EchoInstanceData echo) {
        if (echo.manifestationUuid().isEmpty()
                || echo.manifestationDimension().isEmpty()
                || echo.manifestationPos().isEmpty()) {
            return Optional.empty();
        }
        MinecraftServer server = Objects.requireNonNull(player.getServer(), "server");
        ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, echo.manifestationDimension().get());
        ServerLevel level = server.getLevel(levelKey);
        if (level == null) {
            return Optional.empty();
        }
        BlockPos storedPos = echo.manifestationPos().get();
        level.getChunkAt(storedPos);
        Entity entity = level.getEntity(echo.manifestationUuid().get());
        if (entity != null && entity.getTags().contains(MANIFESTATION_TAG)) {
            return Optional.of(entity);
        }
        return Optional.empty();
    }
}
