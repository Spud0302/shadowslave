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

        MinecraftServer server = player.getServer();
        NightmareRegistryData registry = NightmareRegistryData.get(server);
        if (registry.findByPlayer(player.getUUID()).isPresent()) {
            throw new IllegalStateException("You already own an active Nightmare instance");
        }
        ServerLevel nightmareLevel = server.getLevel(NIGHTMARE_LEVEL);
        if (nightmareLevel == null) {
            throw new IllegalStateException("The bundled Nightmare dimension is unavailable");
        }

        NightmareEntryAssignment.Assignment assignment = NightmareEntryAssignment.resolveFirstNightmare(
                player.getUUID(),
                player.serverLevel().getGameTime()
        );
        NightmareInstance instance = registry.create(
                player,
                assignment.scenarioId(),
                assignment.historicalRoleId()
        );
        NightmareInstance prepared = instance;
        try {
            prepared = prepareScenario(nightmareLevel, player, instance);
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
            player.sendSystemMessage(Component.literal("First Nightmare — The Last Signal")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
            player.sendSystemMessage(Component.literal(
                    "Historical role: " + assignment.roleMatch().role().displayName()
            ).withStyle(ChatFormatting.AQUA));
            player.sendSystemMessage(Component.literal(assignment.roleMatch().variant().entryHook())
                    .withStyle(ChatFormatting.GRAY));
            player.sendSystemMessage(Component.literal(assignment.roleMatch().variant().conflictPressure())
                    .withStyle(ChatFormatting.GRAY));
            player.sendSystemMessage(Component.literal(
                    "Leverage: " + assignment.roleMatch().variant().leverage()
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
            player.sendSystemMessage(Component.literal(
                    "Reach the dead signal fire and rekindle it. Right-click the unlit soul campfire at the far watch. Fighting the pursuer is optional; resolving the conflict is not."
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
            return prepared;
        } catch (RuntimeException exception) {
            teardown(server, prepared);
            SoulService.replace(player, beforeSoul);
            throw new IllegalStateException("Nightmare entry failed and was rolled back", exception);
        }
    }

    public static boolean resolveSignalFire(ServerPlayer player, net.minecraft.core.BlockPos interactedPos) {
        NightmareInstance instance = activeFor(player).orElse(null);
        if (instance == null
                || !instance.scenarioId().equals(LastSignalScenario.SCENARIO_ID)
                || !player.serverLevel().dimension().equals(NIGHTMARE_LEVEL)
                || !instance.altar().equals(interactedPos)) {
            return false;
        }

        LastSignalScenario.igniteAltar(player.serverLevel(), instance);
        NightmareInstance completed = exit(player, NightmareExitReason.SUCCESS);
        try {
            PreviewAppraisalService.appraise(player, completed);
        } catch (RuntimeException exception) {
            SoulIdentityService.replace(player, SoulIdentityData.empty());
            SoulService.replace(player, SoulTransitions.infect(SoulData.uninfected()));
            throw new IllegalStateException(
                    "The preview appraisal failed after lifecycle teardown; Java state was recovered to Carrier",
                    exception
            );
        }
        player.sendSystemMessage(Component.literal("The signal answers. The Spell appraises the life you lived in the borrowed role.")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.literal("Aspect revealed: [Last Light] — Awakened Rank. Flaw revealed: [Cold Ash].")
                .withStyle(ChatFormatting.AQUA));
        return true;
    }

    public static NightmareInstance technicalRecover(ServerPlayer player) {
        NightmareInstance instance = exit(player, NightmareExitReason.TECHNICAL_RECOVERY);
        SoulIdentityService.replace(player, SoulIdentityData.empty());
        SoulService.replace(player, SoulTransitions.infect(SoulData.uninfected()));
        player.sendSystemMessage(Component.literal(
                "Technical recovery completed. This is an administrative path, not mercy from the Nightmare Spell."
        ).withStyle(ChatFormatting.YELLOW));
        return instance;
    }

    public static NightmareInstance adminAbort(ServerPlayer player) {
        NightmareInstance instance = exit(player, NightmareExitReason.ADMIN_ABORT);
        SoulIdentityService.replace(player, SoulIdentityData.empty());
        SoulService.replace(player, SoulTransitions.infect(SoulData.uninfected()));
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

    public static void canonicalDeath(ServerPlayer player) {
        NightmareInstance instance = activeFor(player).orElse(null);
        if (instance == null) {
            return;
        }
        teardown(player.getServer(), instance);
        SoulIdentityService.replace(player, SoulIdentityData.empty());
        SoulService.reset(player);
        player.sendSystemMessage(Component.literal(
                "Canonical First-Nightmare outcome: death. Minecraft respawn is a development accommodation; the Spell did not safely eject you."
        ).withStyle(ChatFormatting.RED));
    }

    public static Optional<NightmareInstance> activeFor(ServerPlayer player) {
        return NightmareRegistryData.get(player.getServer()).findByPlayer(player.getUUID());
    }

    private static NightmareInstance prepareScenario(ServerLevel nightmareLevel, ServerPlayer player, NightmareInstance instance) {
        if (instance.scenarioId().equals(LastSignalScenario.SCENARIO_ID)) {
            return LastSignalScenario.prepare(nightmareLevel, player, instance);
        }
        throw new IllegalArgumentException("Scenario is authored but not physically playable yet: " + instance.scenarioId());
    }

    private static NightmareInstance exit(ServerPlayer player, NightmareExitReason reason) {
        MinecraftServer server = player.getServer();
        NightmareInstance instance = activeFor(player)
                .orElseThrow(() -> new IllegalStateException("Player does not own an active Nightmare"));

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
        teardown(server, instance);
        ShadowSlaveMod.LOGGER.info(
                "Nightmare {} exited for player {} with reason {}",
                instance.instanceId(),
                player.getScoreboardName(),
                reason
        );
        return instance;
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

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, path);
    }
}
