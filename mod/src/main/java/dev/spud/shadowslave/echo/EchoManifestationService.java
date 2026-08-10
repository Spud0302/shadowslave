package dev.spud.shadowslave.echo;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.echo.content.EchoContentCatalog;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * NeoForge/Minecraft executor for the first playable Echo manifestation.
 * Canonical ownership and identity remain in {@link EchoOwnershipData}; the vanilla
 * Armadillo body is an explicitly replaceable DESIGN adapter for Ash Burrower.
 */
public final class EchoManifestationService {
    public static final ResourceLocation ASH_BURROWER_ID =
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "echo/ash_burrower");
    private static final String ASH_BURROWER_PROFILE_ID = "ash_burrower";
    private static final String MANIFESTATION_TAG = "shadowslave_echo_manifestation";

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

        Optional<UUID> storedUuid = owned.get().manifestationUuid();
        if (storedUuid.isPresent() && findManifestation(player, storedUuid.get()).isPresent()) {
            return ManifestResult.ALREADY_SUMMONED;
        }
        if (storedUuid.isPresent()) {
            EchoOwnershipService.setManifestation(player, ASH_BURROWER_ID, Optional.empty());
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

        EchoOwnershipService.setManifestation(player, ASH_BURROWER_ID, Optional.of(echo.getUUID()));
        return ManifestResult.SUMMONED;
    }

    public static ManifestResult dismissAshBurrower(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        Optional<EchoInstanceData> owned = EchoOwnershipService.get(player).find(ASH_BURROWER_ID);
        if (owned.isEmpty()) {
            return ManifestResult.NOT_OWNED;
        }
        Optional<UUID> storedUuid = owned.get().manifestationUuid();
        if (storedUuid.isEmpty()) {
            return ManifestResult.NOT_SUMMONED;
        }

        Optional<Entity> entity = findManifestation(player, storedUuid.get());
        entity.ifPresent(Entity::discard);
        EchoOwnershipService.setManifestation(player, ASH_BURROWER_ID, Optional.empty());
        return entity.isPresent() ? ManifestResult.DISMISSED : ManifestResult.NOT_SUMMONED;
    }

    public static void clearOwnedManifestations(ServerPlayer player) {
        EchoOwnershipService.get(Objects.requireNonNull(player, "player")).echoes().forEach(echo ->
                echo.manifestationUuid().flatMap(uuid -> findManifestation(player, uuid)).ifPresent(Entity::discard));
    }

    private static Optional<Entity> findManifestation(ServerPlayer player, UUID entityUuid) {
        MinecraftServer server = Objects.requireNonNull(player.getServer(), "server");
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(entityUuid);
            if (entity != null && entity.getTags().contains(MANIFESTATION_TAG)) {
                return Optional.of(entity);
            }
        }
        return Optional.empty();
    }
}
