package dev.spud.shadowslave.appraisal;

import dev.spud.shadowslave.echo.EchoOwnershipData;
import dev.spud.shadowslave.memory.MemoryOwnershipData;
import dev.spud.shadowslave.nightmare.NightmareInstance;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulTransitions;
import dev.spud.shadowslave.soul.identity.AttributeOwnershipData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersistedGeneratedAppraisalPlayerVerifierTest {
    @TempDir
    Path tempDir;

    @Test
    void acceptsExactPersistedCommittedAward() throws IOException {
        Fixture fixture = fixture("flood_diverted");
        Path file = writePlayer(fixture.expected());

        assertDoesNotThrow(() -> PersistedGeneratedAppraisalPlayerVerifier.requireCommitted(
                file, fixture.expected(), fixture.snapshot()));
    }

    @Test
    void rejectsStaleOrPartialPersistedAward() throws IOException {
        Fixture fixture = fixture("tower_held");
        GeneratedAppraisalRecoveryService.PlayerState partial = new GeneratedAppraisalRecoveryService.PlayerState(
                fixture.expected().soul(),
                fixture.expected().identity(),
                fixture.expected().attributes(),
                MemoryOwnershipData.empty(),
                fixture.expected().echoes()
        );
        Path file = writePlayer(partial);

        assertThrows(IllegalStateException.class, () -> PersistedGeneratedAppraisalPlayerVerifier.requireCommitted(
                file, fixture.expected(), fixture.snapshot()));
    }

    @Test
    void rejectsMalformedPersistedAttachment() throws IOException {
        Fixture fixture = fixture("villagers_evacuated");
        CompoundTag attachments = encodedAttachments(fixture.expected());
        attachments.put("shadowslave:echoes", StringTag.valueOf("not-echo-ownership"));
        Path file = writeAttachments(attachments);

        assertThrows(IllegalStateException.class, () -> PersistedGeneratedAppraisalPlayerVerifier.requireCommitted(
                file, fixture.expected(), fixture.snapshot()));
    }

    @Test
    void rejectsExpectedStateThatDoesNotContainReceiptAward() throws IOException {
        Fixture fixture = fixture("quarry_collapsed");
        GeneratedAppraisalRecoveryService.PlayerState staleExpected = new GeneratedAppraisalRecoveryService.PlayerState(
                aspirantSoul(),
                SoulIdentityData.empty(),
                AttributeOwnershipData.empty(),
                MemoryOwnershipData.empty(),
                EchoOwnershipData.empty()
        );
        Path file = writePlayer(fixture.expected());

        assertThrows(IllegalStateException.class, () -> PersistedGeneratedAppraisalPlayerVerifier.requireCommitted(
                file, staleExpected, fixture.snapshot()));
    }

    @Test
    void rejectsMissingPlayerFile() {
        Fixture fixture = fixture("flood_diverted");
        assertThrows(IllegalStateException.class, () -> PersistedGeneratedAppraisalPlayerVerifier.requireCommitted(
                tempDir.resolve("missing.dat"), fixture.expected(), fixture.snapshot()));
    }

    private Fixture fixture(String resolutionId) {
        NightmareInstance instance = new NightmareInstance(
                new UUID(307L, 311L),
                new UUID(313L, 317L),
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
                Optional.empty(),
                100L,
                Optional.of("resolved"),
                Optional.of(resolutionId)
        );
        GeneratedAppraisalRecoverySnapshot snapshot = GeneratedAppraisalRecoverySnapshot.fromPrepared(
                PreviewAppraisalService.prepareWithRewards(instance, resolutionId)
        );
        GeneratedAppraisalRecoveryService.PlayerState initial = new GeneratedAppraisalRecoveryService.PlayerState(
                aspirantSoul(),
                SoulIdentityData.empty(),
                AttributeOwnershipData.empty(),
                MemoryOwnershipData.empty(),
                EchoOwnershipData.empty()
        );
        GeneratedAppraisalRecoveryService.PlayerState expected =
                GeneratedAppraisalRecoveryService.plan(initial, snapshot).target();
        return new Fixture(snapshot, expected);
    }

    private static SoulData aspirantSoul() {
        return SoulTransitions.beginFirstNightmare(SoulTransitions.infect(SoulData.uninfected()));
    }

    private Path writePlayer(GeneratedAppraisalRecoveryService.PlayerState state) throws IOException {
        return writeAttachments(encodedAttachments(state));
    }

    private CompoundTag encodedAttachments(GeneratedAppraisalRecoveryService.PlayerState state) {
        CompoundTag attachments = new CompoundTag();
        attachments.put("shadowslave:soul", encode(SoulData.CODEC.codec(), state.soul()));
        attachments.put("shadowslave:identity", encode(SoulIdentityData.CODEC.codec(), state.identity()));
        attachments.put("shadowslave:attributes", encode(AttributeOwnershipData.CODEC.codec(), state.attributes()));
        attachments.put("shadowslave:memories", encode(MemoryOwnershipData.CODEC.codec(), state.memories()));
        attachments.put("shadowslave:echoes", encode(EchoOwnershipData.CODEC.codec(), state.echoes()));
        return attachments;
    }

    private Path writeAttachments(CompoundTag attachments) throws IOException {
        CompoundTag root = new CompoundTag();
        root.put("neoforge:attachments", attachments);
        Path file = tempDir.resolve(UUID.randomUUID() + ".dat");
        NbtIo.writeCompressed(root, file);
        return file;
    }

    private static <T> Tag encode(com.mojang.serialization.Codec<T> codec, T value) {
        return codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow();
    }

    private record Fixture(
            GeneratedAppraisalRecoverySnapshot snapshot,
            GeneratedAppraisalRecoveryService.PlayerState expected
    ) {
    }
}
