package dev.spud.shadowslave.echo;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.echo.content.EchoContentCatalog;
import dev.spud.shadowslave.world.entity.NightmareCreatureEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

/** Minecraft executor for the first playable Echo; Java state remains authoritative. */
public final class EchoManifestationService {
    public static final ResourceLocation ASH_BURROWER_ID =
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "echo/ash_burrower");
    private static final String ASH_BURROWER_PROFILE_ID = "ash_burrower";
    private static final String MANIFESTATION_TAG = "shadowslave_echo_manifestation";
    private static final double FOLLOW_STOP_DISTANCE_SQUARED = 9.0D;
    private static final double FOLLOW_SPEED = 1.1D;
    private static final double GUARD_STOP_DISTANCE_SQUARED = 2.25D;
    private static final double GUARD_SPEED = 1.0D;
    private static final double GUARD_THREAT_RADIUS = 8.0D;
    private static final double GUARD_COMBAT_SPEED = 1.1D;
    private static final double CARGO_INTERACTION_DISTANCE_SQUARED = 16.0D;

    private EchoManifestationService() {}

    public enum ManifestResult { SUMMONED, ALREADY_SUMMONED, DISMISSED, NOT_SUMMONED, NOT_OWNED, SPAWN_FAILED }
    public enum CommandResult { COMMAND_SET, NOT_OWNED, UNSUPPORTED }
    public enum CargoResult { LOADED, UNLOADED, NOT_OWNED, NOT_SUMMONED, TOO_FAR, EMPTY_HAND, ALREADY_CARRYING, NO_CARGO, UNSUPPORTED_ITEM, SPAWN_FAILED }

    public static EchoContentCatalog.EchoProfile ashBurrowerProfile() {
        return EchoContentCatalog.waveOne().stream()
                .filter(profile -> profile.id().equals(ASH_BURROWER_PROFILE_ID))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Ash Burrower Echo profile is missing"));
    }

    public static ManifestResult summonAshBurrower(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        Optional<EchoInstanceData> owned = EchoOwnershipService.get(player).find(ASH_BURROWER_ID);
        if (owned.isEmpty()) return ManifestResult.NOT_OWNED;
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
        AshBurrowerEchoEntity echo = NightmareCreatureEntities.ASH_BURROWER_ECHO.get().create(level);
        if (echo == null) return ManifestResult.SPAWN_FAILED;
        echo.moveTo(player.getX() + 1.5, player.getY(), player.getZ() + 0.5, player.getYRot(), 0.0F);
        echo.setCustomName(Component.literal(ashBurrowerProfile().displayName()));
        echo.setCustomNameVisible(true);
        echo.setPersistenceRequired();
        echo.setInvulnerable(true);
        echo.addTag(MANIFESTATION_TAG);
        if (!level.addFreshEntity(echo)) return ManifestResult.SPAWN_FAILED;

        EchoOwnershipService.setManifestation(player, ASH_BURROWER_ID, echo.getUUID(),
                level.dimension().location(), echo.blockPosition());
        EchoInstanceData persisted = EchoOwnershipService.get(player).find(ASH_BURROWER_ID).orElseThrow();
        executeCommand(player, persisted, echo);
        return ManifestResult.SUMMONED;
    }

    public static ManifestResult dismissAshBurrower(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        Optional<EchoInstanceData> owned = EchoOwnershipService.get(player).find(ASH_BURROWER_ID);
        if (owned.isEmpty()) return ManifestResult.NOT_OWNED;
        EchoInstanceData state = owned.get();
        if (state.manifestationUuid().isEmpty()) return ManifestResult.NOT_SUMMONED;
        Optional<Entity> entity = findManifestation(player, state);
        entity.ifPresent(Entity::discard);
        EchoOwnershipService.clearManifestation(player, ASH_BURROWER_ID);
        return entity.isPresent() ? ManifestResult.DISMISSED : ManifestResult.NOT_SUMMONED;
    }

    public static CommandResult commandAshBurrower(ServerPlayer player, EchoContentCatalog.CommandMode commandMode) {
        Objects.requireNonNull(player, "player");
        EchoContentCatalog.CommandMode checked = Objects.requireNonNull(commandMode, "commandMode");
        Optional<EchoInstanceData> owned = EchoOwnershipService.get(player).find(ASH_BURROWER_ID);
        if (owned.isEmpty()) return CommandResult.NOT_OWNED;
        if (!ashBurrowerProfile().commandModes().contains(checked)) return CommandResult.UNSUPPORTED;
        if (checked == EchoContentCatalog.CommandMode.GUARD_POINT) {
            EchoOwnershipService.setGuardPoint(player, ASH_BURROWER_ID,
                    player.serverLevel().dimension().location(), player.blockPosition());
        } else {
            EchoOwnershipService.setCommandMode(player, ASH_BURROWER_ID, checked);
        }
        EchoInstanceData updated = EchoOwnershipService.get(player).find(ASH_BURROWER_ID).orElseThrow();
        findManifestation(player, updated).ifPresent(entity -> executeCommand(player, updated, entity));
        return CommandResult.COMMAND_SET;
    }

    public static CargoResult loadAshBurrower(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        Optional<EchoInstanceData> owned = EchoOwnershipService.get(player).find(ASH_BURROWER_ID);
        if (owned.isEmpty()) return CargoResult.NOT_OWNED;
        EchoInstanceData state = owned.get();
        if (state.cargoItemId().isPresent()) return CargoResult.ALREADY_CARRYING;
        Optional<Entity> manifestation = findManifestation(player, state);
        if (manifestation.isEmpty()) return CargoResult.NOT_SUMMONED;
        if (!isNearPlayer(player, manifestation.get())) return CargoResult.TOO_FAR;
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) return CargoResult.EMPTY_HAND;
        if (!held.getComponentsPatch().isEmpty()) return CargoResult.UNSUPPORTED_ITEM;
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(held.getItem());
        int count = held.getCount();
        EchoOwnershipService.setCargo(player, ASH_BURROWER_ID, itemId, count);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        EchoInstanceData updated = EchoOwnershipService.get(player).find(ASH_BURROWER_ID).orElseThrow();
        executeCommand(player, updated, manifestation.get());
        return CargoResult.LOADED;
    }

    public static CargoResult unloadAshBurrower(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        Optional<EchoInstanceData> owned = EchoOwnershipService.get(player).find(ASH_BURROWER_ID);
        if (owned.isEmpty()) return CargoResult.NOT_OWNED;
        EchoInstanceData state = owned.get();
        if (state.cargoItemId().isEmpty() || state.cargoCount().isEmpty()) return CargoResult.NO_CARGO;
        Optional<Entity> manifestation = findManifestation(player, state);
        if (manifestation.isEmpty()) return CargoResult.NOT_SUMMONED;
        Entity echo = manifestation.get();
        if (!isNearPlayer(player, echo)) return CargoResult.TOO_FAR;
        Optional<Item> item = BuiltInRegistries.ITEM.getOptional(state.cargoItemId().orElseThrow());
        if (item.isEmpty()) return CargoResult.UNSUPPORTED_ITEM;
        ItemStack stack = new ItemStack(item.get(), state.cargoCount().orElseThrow());
        ItemEntity dropped = new ItemEntity(echo.level(), echo.getX(), echo.getY() + 0.5D, echo.getZ(), stack);
        if (!echo.level().addFreshEntity(dropped)) return CargoResult.SPAWN_FAILED;
        EchoOwnershipService.clearCargo(player, ASH_BURROWER_ID);
        EchoInstanceData updated = EchoOwnershipService.get(player).find(ASH_BURROWER_ID).orElseThrow();
        executeCommand(player, updated, echo);
        return CargoResult.UNLOADED;
    }

    public static void clearOwnedManifestations(ServerPlayer player) {
        for (EchoInstanceData echo : EchoOwnershipService.get(Objects.requireNonNull(player, "player")).echoes()) {
            findManifestation(player, echo).ifPresent(Entity::discard);
        }
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 10 != 0) return;
        Optional<EchoInstanceData> owned = EchoOwnershipService.get(player).find(ASH_BURROWER_ID);
        if (owned.isEmpty() || owned.get().manifestationUuid().isEmpty()) return;
        EchoInstanceData state = owned.get();
        Optional<Entity> manifestation = findManifestation(player, state);
        if (manifestation.isEmpty()) return;
        Entity entity = manifestation.get();
        executeCommand(player, state, entity);
        if (!state.manifestationDimension().orElseThrow().equals(entity.level().dimension().location())
                || !state.manifestationPos().orElseThrow().equals(entity.blockPosition())) {
            EchoOwnershipService.setManifestation(player, ASH_BURROWER_ID, entity.getUUID(),
                    entity.level().dimension().location(), entity.blockPosition());
        }
    }

    private static void executeCommand(ServerPlayer player, EchoInstanceData state, Entity entity) {
        if (!(entity instanceof Mob mob)) return;
        switch (state.commandMode()) {
            case FOLLOW -> follow(player, entity, mob);
            case CARRY -> {
                if (state.cargoItemId().isEmpty() || state.cargoCount().isEmpty()) hold(mob);
                else follow(player, entity, mob);
            }
            case GUARD_POINT -> executeGuardPoint(state, entity, mob);
            case HOLD -> hold(mob);
            default -> hold(mob);
        }
    }

    private static void executeGuardPoint(EchoInstanceData state, Entity entity, Mob mob) {
        if (state.commandTargetDimension().isEmpty()
                || state.commandTargetPos().isEmpty()
                || !state.commandTargetDimension().orElseThrow().equals(entity.level().dimension().location())) {
            hold(mob);
            return;
        }

        BlockPos target = state.commandTargetPos().orElseThrow();
        Optional<LivingEntity> threat = findGuardThreat(entity, target);
        if (threat.isPresent()) {
            LivingEntity selected = threat.get();
            mob.setNoAi(false);
            mob.setTarget(selected);
            mob.getNavigation().moveTo(selected, GUARD_COMBAT_SPEED);
            return;
        }

        double distance = entity.distanceToSqr(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D);
        if (distance <= GUARD_STOP_DISTANCE_SQUARED) {
            hold(mob);
        } else {
            mob.setTarget(null);
            mob.setNoAi(false);
            mob.getNavigation().moveTo(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, GUARD_SPEED);
        }
    }

    private static Optional<LivingEntity> findGuardThreat(Entity manifestation, BlockPos guardPoint) {
        if (!(manifestation.level() instanceof ServerLevel level)) return Optional.empty();
        AABB area = new AABB(guardPoint).inflate(GUARD_THREAT_RADIUS);
        double centerX = guardPoint.getX() + 0.5D;
        double centerY = guardPoint.getY() + 0.5D;
        double centerZ = guardPoint.getZ() + 0.5D;
        double radiusSquared = GUARD_THREAT_RADIUS * GUARD_THREAT_RADIUS;
        return level.getEntitiesOfClass(LivingEntity.class, area, EchoManifestationService::isGuardThreat).stream()
                .filter(candidate -> candidate.distanceToSqr(centerX, centerY, centerZ) <= radiusSquared)
                .min(Comparator.comparingDouble(manifestation::distanceToSqr));
    }

    static boolean isGuardThreat(LivingEntity entity) {
        if (!entity.isAlive()) return false;
        return entity.getType() == NightmareCreatureEntities.ASH_BURROWER.get()
                || entity.getType() == NightmareCreatureEntities.CHAINBACK.get()
                || entity.getType() == NightmareCreatureEntities.DROWNED_LISTENER.get();
    }

    private static void follow(ServerPlayer player, Entity entity, Mob mob) {
        if (entity.level() != player.level() || entity.distanceToSqr(player) <= FOLLOW_STOP_DISTANCE_SQUARED) {
            hold(mob);
        } else {
            mob.setTarget(null);
            mob.setNoAi(false);
            mob.getNavigation().moveTo(player, FOLLOW_SPEED);
        }
    }

    private static boolean isNearPlayer(ServerPlayer player, Entity entity) {
        return entity.level() == player.level() && entity.distanceToSqr(player) <= CARGO_INTERACTION_DISTANCE_SQUARED;
    }

    private static void hold(Mob mob) {
        mob.getNavigation().stop();
        mob.setTarget(null);
        mob.setNoAi(true);
    }

    private static Optional<Entity> findManifestation(ServerPlayer player, EchoInstanceData echo) {
        if (echo.manifestationUuid().isEmpty() || echo.manifestationDimension().isEmpty() || echo.manifestationPos().isEmpty()) {
            return Optional.empty();
        }
        MinecraftServer server = Objects.requireNonNull(player.getServer(), "server");
        ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, echo.manifestationDimension().get());
        ServerLevel level = server.getLevel(levelKey);
        if (level == null) return Optional.empty();
        BlockPos storedPos = echo.manifestationPos().get();
        level.getChunkAt(storedPos);
        Entity entity = level.getEntity(echo.manifestationUuid().get());
        if (entity != null && entity.getTags().contains(MANIFESTATION_TAG)) return Optional.of(entity);
        return Optional.empty();
    }
}
