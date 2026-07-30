package dev.spud.shadowslave.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class NightmareInstanceTest {
    @Test
    void nbtRoundTripsOwnershipRoleConflictAndRecoveryData() {
        NightmareInstance original = new NightmareInstance(
                UUID.randomUUID(),
                UUID.randomUUID(),
                3,
                "last_signal",
                "last_watchkeeper",
                ResourceLocation.parse("minecraft:overworld"),
                12.5,
                70.0,
                -4.5,
                90.0F,
                10.0F,
                new BlockPos(576, 96, 0),
                new BlockPos(576, 97, 28),
                Optional.of(UUID.randomUUID()),
                900L
        );

        CompoundTag tag = original.save();
        NightmareInstance decoded = NightmareInstance.load(tag);

        assertEquals(original, decoded);
    }

    @Test
    void scenarioSlotsDoNotShareAPlaySpace() {
        BlockPos first = LastSignalScenario.originForSlot(0);
        BlockPos second = LastSignalScenario.originForSlot(1);

        assertNotEquals(first, second);
        assertEquals(first.offset(0, 1, 28), LastSignalScenario.altarForOrigin(first));
    }
}
