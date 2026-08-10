package dev.spud.shadowslave.nightmare;

import dev.spud.shadowslave.nightmare.content.DrownedBellScenarioDefinition;
import dev.spud.shadowslave.nightmare.resolution.ResolutionGraph;
import dev.spud.shadowslave.nightmare.resolution.ResolutionState;
import dev.spud.shadowslave.nightmare.resolution.ResolutionStep;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Physical DESIGN execution for the authored Drowned Bell scenario.
 *
 * <p>The Java-owned {@link DrownedBellScenarioDefinition} remains authority for the
 * conflict graph and terminal resolution identities. Blocks and the vanilla Drowned
 * are replaceable execution adapters only.</p>
 */
public final class DrownedBellScenario {
    private static final int SLOT_SPACING = 192;

    private DrownedBellScenario() {
    }

    public static BlockPos originForSlot(int slot) {
        if (slot < 0) {
            throw new IllegalArgumentException("slot cannot be negative");
        }
        return new BlockPos(slot * SLOT_SPACING, 96, 0);
    }

    public static BlockPos bellStation(BlockPos origin) {
        return origin.offset(0, 1, 22);
    }

    public static BlockPos quarryStation(BlockPos origin) {
        return origin.offset(-8, 1, 10);
    }

    public static BlockPos floodgateStation(BlockPos origin) {
        return origin.offset(8, 1, 10);
    }

    public static BlockPos lureStation(BlockPos origin) {
        return origin.offset(0, 1, 14);
    }

    public static BlockPos collapseStation(BlockPos origin) {
        return origin.offset(-8, 1, 18);
    }

    public static NightmareInstance prepare(ServerLevel level, ServerPlayer player, NightmareInstance instance) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(instance, "instance");
        if (!DrownedBellScenarioDefinition.SCENARIO_ID.equals(instance.scenarioId())) {
            throw new IllegalArgumentException("Drowned Bell executor received scenario " + instance.scenarioId());
        }

        BlockPos origin = originForSlot(instance.slot());
        clearVolume(level, origin);
        buildVillage(level, origin);

        Drowned listener = EntityType.DROWNED.create(level);
        if (listener == null) {
            throw new IllegalStateException("Could not create Drowned Listener placeholder");
        }
        listener.moveTo(origin.getX() + 0.5, origin.getY() + 1.0, origin.getZ() + 30.5, 180.0F, 0.0F);
        listener.setPersistenceRequired();
        listener.setTarget(player);
        listener.addTag("shadowslave_drowned_listener_placeholder");
        if (!level.addFreshEntity(listener)) {
            throw new IllegalStateException("Could not add Drowned Listener placeholder to Nightmare level");
        }

        ResolutionState initial = DrownedBellScenarioDefinition.resolutionGraph().initial();
        return instance
                .withLayout(origin, bellStation(origin))
                .withPursuer(listener.getUUID())
                .withResolutionState(initial.stateId(), initial.terminalResolutionId());
    }

    /** Maps one physical interaction to the event legal at the current persisted graph state. */
    public static Optional<String> eventForInteraction(NightmareInstance instance, BlockPos interactedPos) {
        Objects.requireNonNull(instance, "instance");
        Objects.requireNonNull(interactedPos, "interactedPos");
        if (!DrownedBellScenarioDefinition.SCENARIO_ID.equals(instance.scenarioId())
                || instance.resolutionStateId().isEmpty()
                || instance.terminalResolutionId().isPresent()) {
            return Optional.empty();
        }

        String state = instance.resolutionStateId().orElseThrow();
        BlockPos origin = instance.origin();
        if (interactedPos.equals(bellStation(origin))) {
            return switch (state) {
                case "storm_approaching", "tunnels_open", "floodgate_reached" -> Optional.of("repair_bell");
                case "warning_ready" -> Optional.of("ring_bell");
                default -> Optional.empty();
            };
        }
        if (interactedPos.equals(quarryStation(origin))) {
            return switch (state) {
                case "storm_approaching", "warning_ready", "floodgate_reached" -> Optional.of("open_quarry_route");
                case "tunnels_open" -> Optional.of("guide_evacuation");
                default -> Optional.empty();
            };
        }
        if (interactedPos.equals(floodgateStation(origin))) {
            return switch (state) {
                case "storm_approaching", "tunnels_open" -> Optional.of("reach_floodgate");
                case "floodgate_reached" -> Optional.of("divert_flood");
                default -> Optional.empty();
            };
        }
        if (interactedPos.equals(lureStation(origin)) && state.equals("warning_ready")) {
            return Optional.of("lure_creature");
        }
        if (interactedPos.equals(collapseStation(origin)) && state.equals("creature_lured")) {
            return Optional.of("collapse_quarry");
        }
        return Optional.empty();
    }

    /** Applies an authored event against persisted state and returns the updated authoritative instance. */
    public static ResolutionAdvance applyEvent(NightmareInstance instance, String eventId) {
        Objects.requireNonNull(instance, "instance");
        if (!DrownedBellScenarioDefinition.SCENARIO_ID.equals(instance.scenarioId())) {
            throw new IllegalArgumentException("Drowned Bell executor received scenario " + instance.scenarioId());
        }
        String currentStateId = instance.resolutionStateId()
                .orElseThrow(() -> new IllegalStateException("Drowned Bell instance has no persisted resolution state"));
        ResolutionState current = new ResolutionState(currentStateId, instance.terminalResolutionId());
        ResolutionGraph graph = DrownedBellScenarioDefinition.resolutionGraph();
        ResolutionStep step = graph.apply(current, eventId);
        if (!step.accepted()) {
            return new ResolutionAdvance(instance, false, step.rejectionReason());
        }
        ResolutionState next = step.state();
        NightmareInstance updated = instance.withResolutionState(next.stateId(), next.terminalResolutionId());
        return new ResolutionAdvance(updated, true, Optional.empty());
    }

    public static String interactionHint(NightmareInstance instance) {
        if (instance.terminalResolutionId().isPresent()) {
            return "The conflict has reached a terminal resolution.";
        }
        return switch (instance.resolutionStateId().orElse("")) {
            case "storm_approaching" -> "Choose a first response: ring the bell frame, open the quarry route, or reach the floodgate.";
            case "warning_ready" -> "The bell is ready. Ring it, open the quarry route, or use the note block to lure the Listener.";
            case "tunnels_open" -> "The quarry route is open. Use it to evacuate, repair the bell, or reach the floodgate.";
            case "floodgate_reached" -> "The sea gate is reached. Divert the flood, repair the bell, or return to the quarry route.";
            case "creature_lured" -> "The Listener followed the resonance. Trigger the quarry collapse to finish the trap.";
            default -> "The reconstructed conflict state is unreadable; use technical recovery rather than guessing.";
        };
    }

    public static void applyAcceptedWorldCue(ServerLevel level, NightmareInstance instance, String eventId) {
        BlockPos origin = instance.origin();
        switch (eventId) {
            case "repair_bell" -> level.setBlockAndUpdate(bellStation(origin).above(), Blocks.SOUL_LANTERN.defaultBlockState());
            case "open_quarry_route" -> {
                level.setBlockAndUpdate(quarryStation(origin).above(), Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(quarryStation(origin).above(2), Blocks.AIR.defaultBlockState());
            }
            case "reach_floodgate" -> level.setBlockAndUpdate(floodgateStation(origin).above(), Blocks.SEA_LANTERN.defaultBlockState());
            case "ring_bell" -> level.setBlockAndUpdate(bellStation(origin).above(2), Blocks.SOUL_FIRE.defaultBlockState());
            case "guide_evacuation" -> level.setBlockAndUpdate(quarryStation(origin).above(), Blocks.SOUL_LANTERN.defaultBlockState());
            case "divert_flood" -> level.setBlockAndUpdate(floodgateStation(origin).below(), Blocks.WATER.defaultBlockState());
            case "lure_creature" -> level.setBlockAndUpdate(lureStation(origin).above(), Blocks.SOUL_LANTERN.defaultBlockState());
            case "collapse_quarry" -> {
                for (int y = 1; y <= 3; y++) {
                    level.setBlockAndUpdate(collapseStation(origin).offset(0, y, 0), Blocks.COBBLESTONE.defaultBlockState());
                }
            }
            default -> throw new IllegalArgumentException("Unknown Drowned Bell event " + eventId);
        }
    }

    public static void removeOwnedEntities(ServerLevel level, NightmareInstance instance) {
        instance.pursuerId().ifPresent(entityId -> {
            Entity entity = level.getEntity(entityId);
            if (entity != null) {
                entity.discard();
            }
        });
    }

    public static Optional<DrownedBellScenarioDefinition.ResolutionContent> resolutionContent(NightmareInstance instance) {
        return instance.terminalResolutionId()
                .map(DrownedBellScenarioDefinition.content().resolutions()::get)
                .filter(Objects::nonNull);
    }

    private static void clearVolume(ServerLevel level, BlockPos origin) {
        for (BlockPos cursor : BlockPos.betweenClosed(origin.offset(-14, 0, -5), origin.offset(14, 10, 36))) {
            level.setBlock(cursor, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private static void buildVillage(ServerLevel level, BlockPos origin) {
        for (int z = -3; z <= 32; z++) {
            for (int x = -12; x <= 12; x++) {
                BlockState floor = Math.abs(x) > 9 ? Blocks.WATER.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
                level.setBlock(origin.offset(x, 0, z), floor, 3);
            }
        }
        for (int y = 1; y <= 5; y++) {
            level.setBlock(origin.offset(-2, y, 22), Blocks.STONE_BRICKS.defaultBlockState(), 3);
            level.setBlock(origin.offset(2, y, 22), Blocks.STONE_BRICKS.defaultBlockState(), 3);
        }
        level.setBlock(bellStation(origin), Blocks.BELL.defaultBlockState(), 3);
        level.setBlock(quarryStation(origin), Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 3);
        level.setBlock(quarryStation(origin).above(), Blocks.IRON_BARS.defaultBlockState(), 3);
        level.setBlock(quarryStation(origin).above(2), Blocks.IRON_BARS.defaultBlockState(), 3);
        level.setBlock(floodgateStation(origin), Blocks.IRON_BLOCK.defaultBlockState(), 3);
        level.setBlock(lureStation(origin), Blocks.NOTE_BLOCK.defaultBlockState(), 3);
        level.setBlock(collapseStation(origin), Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), 3);
        level.setBlock(origin.offset(0, 1, -2), Blocks.SOUL_LANTERN.defaultBlockState(), 3);
    }

    public record ResolutionAdvance(
            NightmareInstance instance,
            boolean accepted,
            Optional<String> rejectionReason
    ) {
        public ResolutionAdvance {
            instance = Objects.requireNonNull(instance, "instance");
            rejectionReason = Objects.requireNonNull(rejectionReason, "rejectionReason");
            if (accepted == rejectionReason.isPresent()) {
                throw new IllegalArgumentException("Accepted advances cannot have a rejection reason and rejected advances require one");
            }
        }
    }
}
