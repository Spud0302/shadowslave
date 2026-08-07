package dev.spud.shadowslave.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class NightmareLateEntryWorldRollbackTest {
    @Test
    void failedEntryRollbackTargetsAllocatedSlotEvenWhenPreparedLayoutWasNeverCommitted() {
        NightmareInstance preUpdateSnapshot = new NightmareInstance(
                UUID.randomUUID(),
                UUID.randomUUID(),
                4,
                LastSignalScenario.SCENARIO_ID,
                LastSignalScenario.ROLE_ID,
                ResourceLocation.parse("minecraft:overworld"),
                10.0,
                64.0,
                -5.0,
                0.0F,
                0.0F,
                BlockPos.ZERO,
                BlockPos.ZERO,
                Optional.empty(),
                100L
        );

        BlockPos rollbackOrigin = LastSignalScenario.rollbackOriginFor(preUpdateSnapshot);

        assertEquals(LastSignalScenario.originForSlot(4), rollbackOrigin);
        assertNotEquals(preUpdateSnapshot.origin(), rollbackOrigin);
    }
}
