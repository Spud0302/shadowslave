package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.world.entity.NightmareCreatureEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/** Physical executor for the deterministic Storm Lantern Coast encounter budget. */
public final class StormLanternCoastEncounterService {
    private static final BlockPos ORIGIN = new BlockPos(224, 148, 0);
    /**
     * Current fixed Ashen Expanse/Cinder Rest regression-fixture origin. This is only
     * a migration input until native generated settlement positions exist.
     */
    private static final BlockPos CINDER_REST_DEVELOPMENT_ORIGIN = new BlockPos(0, 160, 0);
    private static final String ENCOUNTER_TAG = "shadowslave_storm_lantern_encounter";
    private static final AABB SITE_BOUNDS = new AABB(
            ORIGIN.getX() - 48, ORIGIN.getY() - 8, ORIGIN.getZ() - 48,
            ORIGIN.getX() + 48, ORIGIN.getY() + 28, ORIGIN.getZ() + 48
    );

    private StormLanternCoastEncounterService() {
    }

    /** Compatibility execution for non-player callers using the existing development fixture context. */
    public static StormLanternCoastEncounterPlan.Plan populate(ServerLevel level, StormLanternCoastSitePlan.Plan sitePlan) {
        return populate(level, sitePlan, StormLanternCoastEncounterPlan.EncounterContext.developmentFixture());
    }

    /**
     * Player-aware preview execution. SoulService remains authority for progression;
     * the physical preview only derives a coarse encounter band from that state.
     */
    public static StormLanternCoastEncounterPlan.Plan populate(ServerPlayer player, StormLanternCoastSitePlan.Plan sitePlan) {
        ServerPlayer checkedPlayer = java.util.Objects.requireNonNull(player, "player");
        StormLanternCoastEncounterPlan.EncounterContext context = new StormLanternCoastEncounterPlan.EncounterContext(
                progressionBand(checkedPlayer),
                nearestPhysicalSettlementBlocks()
        );
        return populate(checkedPlayer.serverLevel(), sitePlan, context);
    }

    static StormLanternCoastEncounterPlan.ProgressionBand progressionBand(ServerPlayer player) {
        var soulRank = SoulService.get(player).soulRank();
        if (soulRank.isEmpty()) return StormLanternCoastEncounterPlan.ProgressionBand.UNRANKED;
        if (soulRank.get() == SoulRank.DORMANT) return StormLanternCoastEncounterPlan.ProgressionBand.DORMANT;
        return StormLanternCoastEncounterPlan.ProgressionBand.AWAKENED_OR_HIGHER;
    }

    /**
     * Development-fixture distance to the only currently physical Java-owned safe settlement,
     * Cinder Rest. Native generated settlements should replace this input without changing the planner.
     */
    static int nearestPhysicalSettlementBlocks() {
        int dx = ORIGIN.getX() - CINDER_REST_DEVELOPMENT_ORIGIN.getX();
        int dz = ORIGIN.getZ() - CINDER_REST_DEVELOPMENT_ORIGIN.getZ();
        return (int) Math.round(Math.hypot(dx, dz));
    }

    private static StormLanternCoastEncounterPlan.Plan populate(
            ServerLevel level,
            StormLanternCoastSitePlan.Plan sitePlan,
            StormLanternCoastEncounterPlan.EncounterContext context
    ) {
        clearExisting(level);
        StormLanternCoastEncounterPlan.Plan encounterPlan = StormLanternCoastEncounterPlan.forSite(sitePlan, context);
        StormLanternCoastDiscoveryPlan.Plan discoveryPlan = StormLanternCoastDiscoveryPlan.fromEncounters(encounterPlan);
        for (StormLanternCoastDiscoveryPlan.Clue clue : discoveryPlan.clues()) {
            presentClue(level, clue);
        }
        for (StormLanternCoastEncounterPlan.Encounter encounter : encounterPlan.encounters()) {
            spawn(level, encounter, context);
        }
        return encounterPlan;
    }

    private static void clearExisting(ServerLevel level) {
        for (Entity entity : level.getEntities((Entity) null, SITE_BOUNDS,
                candidate -> candidate.getTags().contains(ENCOUNTER_TAG))) {
            entity.discard();
        }
    }

    private static void presentClue(ServerLevel level, StormLanternCoastDiscoveryPlan.Clue clue) {
        BlockPos pos = ORIGIN.offset(clue.x(), clue.y(), clue.z());
        switch (clue.kind()) {
            case DISTURBED_FLOOD_EDGE -> {
                level.setBlock(pos, Blocks.MUD.defaultBlockState(), 3);
                level.setBlock(pos.east(), Blocks.CLAY.defaultBlockState(), 3);
                level.setBlock(pos.west(), Blocks.GRAVEL.defaultBlockState(), 3);
            }
            case CHAIN_SCAR -> {
                level.setBlock(pos, Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState(), 3);
                level.setBlock(pos.above(), Blocks.CHAIN.defaultBlockState(), 3);
                level.setBlock(pos.east(), Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState(), 3);
            }
            case EXPOSED_ROUTE_DAMAGE -> {
                level.setBlock(pos, Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState(), 3);
                level.setBlock(pos.east(), Blocks.GRAVEL.defaultBlockState(), 3);
                level.setBlock(pos.west(), Blocks.GRAVEL.defaultBlockState(), 3);
            }
        }
    }

    private static void spawn(
            ServerLevel level,
            StormLanternCoastEncounterPlan.Encounter encounter,
            StormLanternCoastEncounterPlan.EncounterContext context
    ) {
        Mob mob = switch (encounter.creatureId()) {
            case "drowned_listener" -> NightmareCreatureEntities.DROWNED_LISTENER.get().create(level);
            case "chainback" -> NightmareCreatureEntities.CHAINBACK.get().create(level);
            default -> throw new IllegalStateException("No physical Storm Lantern executor for " + encounter.creatureId());
        };
        if (mob == null) {
            throw new IllegalStateException("Failed to create Storm Lantern encounter " + encounter.creatureId());
        }

        BlockPos pos = ORIGIN.offset(encounter.x(), encounter.y(), encounter.z());
        mob.moveTo(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, 0.0F, 0.0F);
        mob.addTag(ENCOUNTER_TAG);
        mob.addTag("shadowslave_encounter_" + encounter.pressure().name().toLowerCase(java.util.Locale.ROOT));
        mob.addTag("shadowslave_ecology_" + encounter.ecologyContext().name().toLowerCase(java.util.Locale.ROOT));
        mob.addTag("shadowslave_progression_" + context.progressionBand().name().toLowerCase(java.util.Locale.ROOT));
        mob.addTag("shadowslave_settlement_" + context.settlementDistanceBand().name().toLowerCase(java.util.Locale.ROOT));
        mob.setPersistenceRequired();
        level.addFreshEntity(mob);
    }
}
