package dev.spud.shadowslave.nightmare;

import dev.spud.shadowslave.appraisal.GeneratedAppraisalRecoverySnapshot;
import dev.spud.shadowslave.appraisal.PreviewAppraisalService;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NightmareCompletionReceiptDataTest {
    @Test
    void receiptRoundTripRetainsExactNightmareAndPreparedGeneratedAward() {
        NightmareInstance completed = instance(new UUID(71L, 73L), new UUID(79L, 83L));
        GeneratedAppraisalRecoverySnapshot appraisal = GeneratedAppraisalRecoverySnapshot.fromPrepared(
                PreviewAppraisalService.prepareWithRewards(completed, "flood_diverted")
        );
        NightmareCompletionReceiptData.Receipt before = new NightmareCompletionReceiptData.Receipt(completed, appraisal);

        NightmareCompletionReceiptData.Receipt after = NightmareCompletionReceiptData.Receipt.load(before.save());

        assertEquals(before, after);
        assertEquals(appraisal.generationFingerprint(), after.appraisal().generationFingerprint());
        assertEquals(completed.instanceId(), after.instance().instanceId());
    }

    @Test
    void beginIsIdempotentForExactReceiptAndRejectsContradictoryAward() {
        NightmareInstance completed = instance(new UUID(101L, 103L), new UUID(107L, 109L));
        GeneratedAppraisalRecoverySnapshot first = GeneratedAppraisalRecoverySnapshot.fromPrepared(
                PreviewAppraisalService.prepareWithRewards(completed, "tower_held")
        );
        GeneratedAppraisalRecoverySnapshot different = GeneratedAppraisalRecoverySnapshot.fromPrepared(
                PreviewAppraisalService.prepareWithRewards(completed, "villagers_evacuated")
        );
        NightmareCompletionReceiptData data = new NightmareCompletionReceiptData();

        NightmareCompletionReceiptData.Receipt stored = data.begin(completed, first);
        assertEquals(stored, data.begin(completed, first));
        assertThrows(IllegalStateException.class, () -> data.begin(completed, different));
        assertEquals(stored, data.find(completed.playerId()).orElseThrow());
    }

    @Test
    void clearRequiresExactReceiptAndRemovesRecoveryAuthorityOnlyAfterMatch() {
        NightmareInstance completed = instance(new UUID(127L, 131L), new UUID(137L, 139L));
        GeneratedAppraisalRecoverySnapshot appraisal = GeneratedAppraisalRecoverySnapshot.fromPrepared(
                PreviewAppraisalService.prepareWithRewards(completed, "flood_diverted")
        );
        NightmareCompletionReceiptData data = new NightmareCompletionReceiptData();
        NightmareCompletionReceiptData.Receipt stored = data.begin(completed, appraisal);

        assertTrue(data.clear(stored).isPresent());
        assertTrue(data.find(completed.playerId()).isEmpty());
        assertTrue(data.clear(stored).isEmpty());
    }

    @Test
    void incompleteReceiptFailsClosed() {
        CompoundTag malformed = new CompoundTag();
        malformed.put("instance", instance(new UUID(149L, 151L), new UUID(157L, 163L)).save());

        assertThrows(IllegalStateException.class, () -> NightmareCompletionReceiptData.Receipt.load(malformed));
    }

    private static NightmareInstance instance(UUID instanceId, UUID playerId) {
        return new NightmareInstance(
                instanceId,
                playerId,
                2,
                "drowned_bell",
                "cistern_keeper",
                ResourceLocation.parse("minecraft:overworld"),
                12.5,
                70.0,
                -4.5,
                15.0F,
                0.0F,
                new BlockPos(0, 64, 0),
                new BlockPos(3, 64, 3),
                Optional.of("flood_diverted"),
                100L
        );
    }
}
