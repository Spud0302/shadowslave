package dev.spud.shadowslave.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * DESIGN scenario: the Aspirant inhabits the last watchkeeper of a fallen road
 * and must restore the signal fire while a corrupted pursuer closes in. The
 * role/conflict structure is lore-shaped; the specific history is project design.
 */
public final class LastSignalScenario {
    public static final String SCENARIO_ID = "last_signal";
    public static final String ROLE_ID = "last_watchkeeper";

    private LastSignalScenario() {
    }

    public static BlockPos originForSlot(int slot) {
        return new BlockPos(slot * 192, 96, 0);
    }

    public static BlockPos altarForOrigin(BlockPos origin) {
        return origin.offset(0, 1, 28);
    }

    public static NightmareInstance prepare(
            ServerLevel level,
            ServerPlayer player,
            NightmareInstance instance
    ) {
        BlockPos origin = originForSlot(instance.slot());
        BlockPos altar = altarForOrigin(origin);
        Husk[] createdPursuer = new Husk[1];

        return NightmarePreparationTransaction.run(
                () -> {
                    clearVolume(level, origin);
                    buildRoadAndWatch(level, origin, altar);

                    Husk pursuer = EntityType.HUSK.create(level);
                    if (pursuer == null) {
                        throw new IllegalStateException("Could not create Last Signal pursuer");
                    }
                    createdPursuer[0] = pursuer;
                    pursuer.moveTo(origin.getX() + 0.5, origin.getY() + 1.0, origin.getZ() + 18.5, 180.0F, 0.0F);
                    pursuer.setPersistenceRequired();
                    pursuer.setTarget(player);
                    pursuer.addTag("shadowslave_preview_pursuer");
                    if (!level.addFreshEntity(pursuer)) {
                        throw new IllegalStateException("Could not add Last Signal pursuer to the Nightmare level");
                    }

                    return instance.withLayout(origin, altar).withPursuer(pursuer.getUUID());
                },
                () -> NightmarePreparationTransaction.rollbackAll(
                        () -> {
                            Husk pursuer = createdPursuer[0];
                            if (pursuer != null) {
                                pursuer.discard();
                            }
                        },
                        () -> clearVolume(level, origin)
                )
        );
    }

    public static boolean igniteAltar(ServerLevel level, NightmareInstance instance) {
        BlockState state = level.getBlockState(instance.altar());
        if (!state.is(Blocks.SOUL_CAMPFIRE) || !state.hasProperty(CampfireBlock.LIT)) {
            throw new IllegalStateException("The Last Signal altar is missing or corrupt");
        }
        if (!state.getValue(CampfireBlock.LIT)) {
            level.setBlockAndUpdate(instance.altar(), state.setValue(CampfireBlock.LIT, true));
        }
        return true;
    }

    public static void removeOwnedEntities(ServerLevel level, NightmareInstance instance) {
        instance.pursuerId().ifPresent(entityId -> {
            Entity entity = level.getEntity(entityId);
            if (entity != null) {
                entity.discard();
            }
        });
    }

    /**
     * Rolls back world state from an entry attempt that already allocated this
     * scenario slot. The physical namespace comes from the immutable slot, not
     * from the instance's persisted layout fields, because registry.update(...)
     * may have failed before the prepared layout became authoritative.
     */
    static void rollbackFailedEntryWorld(ServerLevel level, NightmareInstance attempted) {
        BlockPos origin = rollbackOriginFor(attempted);
        NightmarePreparationTransaction.rollbackAll(
                () -> removeOwnedEntities(level, attempted),
                () -> clearVolume(level, origin)
        );
    }

    static BlockPos rollbackOriginFor(NightmareInstance attempted) {
        return originForSlot(attempted.slot());
    }

    private static void clearVolume(ServerLevel level, BlockPos origin) {
        for (BlockPos cursor : BlockPos.betweenClosed(
                origin.offset(-7, 0, -5),
                origin.offset(7, 8, 35)
        )) {
            level.setBlock(cursor, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private static void buildRoadAndWatch(ServerLevel level, BlockPos origin, BlockPos altar) {
        for (int z = -3; z <= 32; z++) {
            for (int x = -5; x <= 5; x++) {
                BlockPos floor = origin.offset(x, 0, z);
                boolean edge = Math.abs(x) == 5;
                level.setBlock(
                        floor,
                        (edge ? Blocks.DEEPSLATE_BRICKS : Blocks.POLISHED_DEEPSLATE).defaultBlockState(),
                        3
                );
            }
        }

        for (int z = -2; z <= 31; z += 5) {
            for (int y = 1; y <= 3; y++) {
                level.setBlock(origin.offset(-5, y, z), Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState(), 3);
                level.setBlock(origin.offset(5, y, z), Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState(), 3);
            }
        }

        for (int x = -2; x <= 2; x++) {
            for (int z = 26; z <= 30; z++) {
                level.setBlock(origin.offset(x, 1, z), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 3);
            }
        }
        level.setBlock(altar, Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false), 3);
        level.setBlock(origin.offset(0, 1, -2), Blocks.SOUL_LANTERN.defaultBlockState(), 3);
    }
}
