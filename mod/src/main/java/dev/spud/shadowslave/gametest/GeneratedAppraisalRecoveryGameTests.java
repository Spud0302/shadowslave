package dev.spud.shadowslave.gametest;

import com.mojang.authlib.GameProfile;
import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.appraisal.GeneratedAppraisalRecoveryService;
import dev.spud.shadowslave.appraisal.GeneratedAppraisalRecoverySnapshot;
import dev.spud.shadowslave.appraisal.PreviewAppraisalService;
import dev.spud.shadowslave.attachment.ModAttachments;
import dev.spud.shadowslave.echo.EchoOwnershipData;
import dev.spud.shadowslave.memory.MemoryOwnershipData;
import dev.spud.shadowslave.nightmare.NightmareCompletionReceiptData;
import dev.spud.shadowslave.nightmare.NightmareInstance;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulTransitions;
import dev.spud.shadowslave.soul.identity.AttributeOwnershipData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;
import java.util.UUID;

/**
 * Server-runtime recovery probes for issue #34.
 *
 * <p>These tests deliberately exercise the production recovery service with a NeoForge FakePlayer.
 * They are one-process server-side evidence only: they do not represent a real network login or a
 * player reconnecting across two dedicated-server JVMs.</p>
 */
@GameTestHolder(ShadowSlaveMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GeneratedAppraisalRecoveryGameTests {
    private static final UUID PLAYER_ID = UUID.fromString("6d77885f-9d86-4ff0-91b7-f49723e5a1f4");
    private static final UUID INSTANCE_ID = UUID.fromString("76a917d1-dfaa-4f1c-aa0a-358a9eb0a60a");
    private static final String RESOLUTION_ID = "flood_diverted";

    private GeneratedAppraisalRecoveryGameTests() {
    }

    @GameTest(template = "recovery_empty", timeoutTicks = 200)
    public static void alreadyCommittedAppraisalConsumesReceiptExactlyOnce(GameTestHelper helper) {
        var level = helper.getLevel();
        var server = level.getServer();
        FakePlayer player = FakePlayerFactory.get(level, new GameProfile(PLAYER_ID, "RecoveryProbe"));

        NightmareInstance instance = new NightmareInstance(
                INSTANCE_ID,
                PLAYER_ID,
                2,
                "drowned_bell",
                "cistern_keeper",
                ResourceLocation.parse("minecraft:overworld"),
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot(),
                new BlockPos(0, 64, 0),
                new BlockPos(3, 64, 3),
                Optional.empty(),
                100L,
                Optional.of("resolved"),
                Optional.of(RESOLUTION_ID)
        );
        GeneratedAppraisalRecoverySnapshot snapshot = GeneratedAppraisalRecoverySnapshot.fromPrepared(
                PreviewAppraisalService.prepareWithRewards(instance, RESOLUTION_ID)
        );
        GeneratedAppraisalRecoveryService.PlayerState target = GeneratedAppraisalRecoveryService.plan(
                emptyAspirant(),
                snapshot
        ).target();

        // Model the crash cut after exact player appraisal persistence but before receipt deletion.
        player.setData(ModAttachments.SOUL, target.soul());
        player.setData(ModAttachments.IDENTITY, target.identity());
        player.setData(ModAttachments.ATTRIBUTES, target.attributes());
        player.setData(ModAttachments.MEMORIES, target.memories());
        player.setData(ModAttachments.ECHOES, target.echoes());
        server.getPlayerList().save(player);

        NightmareCompletionReceiptData receipts = NightmareCompletionReceiptData.get(server);
        receipts.begin(instance, snapshot);
        if (receipts.find(PLAYER_ID).isEmpty()) {
            throw new IllegalStateException("Recovery receipt was not durable before replay");
        }

        if (!GeneratedAppraisalRecoveryService.replayPending(player)) {
            throw new IllegalStateException("Production recovery did not consume the pending receipt");
        }
        if (receipts.find(PLAYER_ID).isPresent()) {
            throw new IllegalStateException("Production recovery left the completion receipt pending");
        }
        if (!GeneratedAppraisalRecoveryService.currentState(player).equals(target)) {
            throw new IllegalStateException("Already-committed appraisal changed during receipt replay");
        }
        if (GeneratedAppraisalRecoveryService.replayPending(player)) {
            throw new IllegalStateException("Completion receipt replay was not exactly-once");
        }

        helper.succeed();
    }

    private static GeneratedAppraisalRecoveryService.PlayerState emptyAspirant() {
        SoulData aspirant = SoulTransitions.beginFirstNightmare(SoulTransitions.infect(SoulData.uninfected()));
        return new GeneratedAppraisalRecoveryService.PlayerState(
                aspirant,
                SoulIdentityData.empty(),
                AttributeOwnershipData.empty(),
                MemoryOwnershipData.empty(),
                EchoOwnershipData.empty()
        );
    }
}
