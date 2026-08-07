package dev.spud.shadowslave.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NightmareInstancePersistenceTest {
    @Test
    void lastSignalRoundTripRetainsSlotDerivedLayout() {
        NightmareInstance instance = validLastSignalInstance(3);

        NightmareInstance restored = NightmareInstance.load(instance.save());

        assertEquals(instance, restored);
    }

    @Test
    void lastSignalSaveRejectsOriginOrAltarThatDriftsFromAllocatedSlot() {
        NightmareInstance valid = validLastSignalInstance(4);
        BlockPos expectedOrigin = LastSignalScenario.originForSlot(valid.slot());
        BlockPos expectedAltar = LastSignalScenario.altarForOrigin(expectedOrigin);

        NightmareInstance wrongOrigin = copyWithLayout(
                valid,
                expectedOrigin.offset(192, 0, 0),
                expectedAltar
        );
        NightmareInstance wrongAltar = copyWithLayout(
                valid,
                expectedOrigin,
                expectedAltar.offset(1, 0, 0)
        );

        assertThrows(IllegalStateException.class, wrongOrigin::save);
        assertThrows(IllegalStateException.class, wrongAltar::save);
    }

    @Test
    void lastSignalLoadRejectsPersistedOriginOrAltarThatDriftsFromAllocatedSlot() {
        NightmareInstance valid = validLastSignalInstance(5);
        BlockPos expectedOrigin = LastSignalScenario.originForSlot(valid.slot());
        BlockPos expectedAltar = LastSignalScenario.altarForOrigin(expectedOrigin);

        CompoundTag wrongOrigin = valid.save().copy();
        wrongOrigin.putLong("origin", expectedOrigin.offset(192, 0, 0).asLong());
        assertThrows(IllegalStateException.class, () -> NightmareInstance.load(wrongOrigin));

        CompoundTag wrongAltar = valid.save().copy();
        wrongAltar.putLong("altar", expectedAltar.offset(0, 0, 1).asLong());
        assertThrows(IllegalStateException.class, () -> NightmareInstance.load(wrongAltar));
    }

    @Test
    void unknownScenarioDoesNotInheritLastSignalLayoutRule() {
        NightmareInstance futureScenario = new NightmareInstance(
                UUID.randomUUID(),
                UUID.randomUUID(),
                2,
                "future_scenario",
                "future_role",
                ResourceLocation.parse("minecraft:overworld"),
                0.5,
                70.0,
                0.5,
                0.0F,
                0.0F,
                new BlockPos(13, 80, -27),
                new BlockPos(99, 44, 12),
                Optional.empty(),
                100L
        );

        assertEquals(futureScenario, NightmareInstance.load(futureScenario.save()));
    }

    private static NightmareInstance validLastSignalInstance(int slot) {
        BlockPos origin = LastSignalScenario.originForSlot(slot);
        return new NightmareInstance(
                UUID.randomUUID(),
                UUID.randomUUID(),
                slot,
                LastSignalScenario.SCENARIO_ID,
                LastSignalScenario.ROLE_ID,
                ResourceLocation.parse("minecraft:overworld"),
                12.5,
                72.0,
                -4.5,
                90.0F,
                5.0F,
                origin,
                LastSignalScenario.altarForOrigin(origin),
                Optional.of(UUID.randomUUID()),
                900L
        );
    }

    private static NightmareInstance copyWithLayout(
            NightmareInstance source,
            BlockPos origin,
            BlockPos altar
    ) {
        return new NightmareInstance(
                source.instanceId(),
                source.playerId(),
                source.slot(),
                source.scenarioId(),
                source.historicalRoleId(),
                source.returnDimension(),
                source.returnX(),
                source.returnY(),
                source.returnZ(),
                source.returnYaw(),
                source.returnPitch(),
                origin,
                altar,
                source.pursuerId(),
                source.createdGameTime()
        );
    }
}
