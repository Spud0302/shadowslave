package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.world.entity.NightmareCreatureEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/** Physical executor for the deterministic Storm Lantern Coast encounter budget. */
public final class StormLanternCoastEncounterService {
    private static final BlockPos ORIGIN = new BlockPos(224, 148, 0);
    private static final String ENCOUNTER_TAG = "shadowslave_storm_lantern_encounter";
    private static final AABB SITE_BOUNDS = new AABB(
            ORIGIN.getX() - 48, ORIGIN.getY() - 8, ORIGIN.getZ() - 48,
            ORIGIN.getX() + 48, ORIGIN.getY() + 28, ORIGIN.getZ() + 48
    );

    private StormLanternCoastEncounterService() {
    }

    public static StormLanternCoastEncounterPlan.Plan populate(ServerLevel level, StormLanternCoastSitePlan.Plan sitePlan) {
        clearExisting(level);
        StormLanternCoastEncounterPlan.Plan encounterPlan = StormLanternCoastEncounterPlan.forSite(sitePlan);
        StormLanternCoastDiscoveryPlan.Plan discoveryPlan = StormLanternCoastDiscoveryPlan.fromEncounters(encounterPlan);
        for (StormLanternCoastDiscoveryPlan.Clue clue : discoveryPlan.clues()) {
            presentClue(level, clue);
        }
        for (StormLanternCoastEncounterPlan.Encounter encounter : encounterPlan.encounters()) {
            spawn(level, encounter);
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

    private static void spawn(ServerLevel level, StormLanternCoastEncounterPlan.Encounter encounter) {
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
        mob.setPersistenceRequired();
        level.addFreshEntity(mob);
    }
}
