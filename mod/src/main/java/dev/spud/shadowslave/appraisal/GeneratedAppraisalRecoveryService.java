package dev.spud.shadowslave.appraisal;

import dev.spud.shadowslave.echo.EchoOwnershipData;
import dev.spud.shadowslave.echo.EchoOwnershipService;
import dev.spud.shadowslave.memory.MemoryInstanceData;
import dev.spud.shadowslave.memory.MemoryOwnershipData;
import dev.spud.shadowslave.memory.MemoryOwnershipService;
import dev.spud.shadowslave.nightmare.NightmareCompletionReceiptData;
import dev.spud.shadowslave.nightmare.NightmareInstance;
import dev.spud.shadowslave.nightmare.NightmareService;
import dev.spud.shadowslave.nightmare.PersistedNightmareOwnershipVerifier;
import dev.spud.shadowslave.persistence.SavedDataPersistence;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.identity.AttributeInstanceData;
import dev.spud.shadowslave.soul.identity.AttributeOwnershipData;
import dev.spud.shadowslave.soul.identity.AttributeOwnershipService;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Idempotently converges player-owned appraisal state from one durable generated-appraisal receipt.
 *
 * <p>The receipt is the authority. Recovery never invokes the generator or current content catalogues.
 * It accepts an untouched Aspirant, a partially written appraisal, or an already-complete matching
 * Dreamer state. Contradictory state fails closed while the receipt remains available.</p>
 */
public final class GeneratedAppraisalRecoveryService {
    private GeneratedAppraisalRecoveryService() {
    }

    public record PlayerState(
            SoulData soul,
            SoulIdentityData identity,
            AttributeOwnershipData attributes,
            MemoryOwnershipData memories,
            EchoOwnershipData echoes
    ) {
        public PlayerState {
            soul = Objects.requireNonNull(soul, "soul");
            identity = Objects.requireNonNull(identity, "identity");
            attributes = Objects.requireNonNull(attributes, "attributes");
            memories = Objects.requireNonNull(memories, "memories");
            echoes = Objects.requireNonNull(echoes, "echoes");
        }
    }

    public record RecoveryPlan(PlayerState target, boolean alreadyComplete) {
        public RecoveryPlan {
            target = Objects.requireNonNull(target, "target");
        }
    }

    public static boolean replayPending(ServerPlayer player) {
        ServerPlayer checkedPlayer = Objects.requireNonNull(player, "player");
        MinecraftServer server = checkedPlayer.getServer();
        NightmareCompletionReceiptData receipts = NightmareCompletionReceiptData.get(server);
        Optional<NightmareCompletionReceiptData.Receipt> pending = receipts.find(checkedPlayer.getUUID());
        if (pending.isEmpty()) {
            return false;
        }

        NightmareCompletionReceiptData.Receipt receipt = pending.orElseThrow();
        if (!receipt.instance().playerId().equals(checkedPlayer.getUUID())) {
            throw new IllegalStateException("Completion receipt belongs to a different player");
        }

        RecoveryPlan plan = plan(currentState(checkedPlayer), receipt.appraisal());

        Optional<NightmareInstance> active = activeInstanceForReplay(NightmareService.activeFor(checkedPlayer), receipt);
        boolean ownershipRemoved = NightmareService.recoverSuccessfulCompletion(checkedPlayer, receipt.instance());
        if (ownershipRemoved != active.isPresent()) {
            throw new IllegalStateException("Successful-completion replay changed unexpected Nightmare ownership");
        }
        if (!ownershipRemoved) {
            replayStoredReturn(checkedPlayer, receipt.instance());
        }

        // The receipt remains independent authority while both possible return/teardown outcomes are
        // checkpointed. Absence of active ownership is not proof that a prior return teleport persisted.
        server.getPlayerList().saveAll();
        if (ownershipRemoved) {
            SavedDataPersistence.saveAndWait(server);
            PersistedNightmareOwnershipVerifier.requireAbsent(
                    server,
                    receipt.instance().playerId(),
                    receipt.instance().instanceId()
            );
        }

        apply(checkedPlayer, plan.target());

        server.getPlayerList().saveAll();
        Path playerDataFile = server.getWorldPath(LevelResource.PLAYER_DATA_DIR)
                .resolve(checkedPlayer.getStringUUID() + ".dat");
        PersistedGeneratedAppraisalPlayerVerifier.requireCommitted(
                playerDataFile,
                plan.target(),
                receipt.appraisal()
        );
        receipts.clearAndVerify(receipt);

        checkedPlayer.sendSystemMessage(Component.literal(
                plan.alreadyComplete()
                        ? "Verified a completed First Nightmare appraisal and cleared its recovery receipt."
                        : "Recovered your completed First Nightmare appraisal from its durable receipt."
        ).withStyle(ChatFormatting.YELLOW));
        return true;
    }

    static Optional<NightmareInstance> activeInstanceForReplay(
            Optional<NightmareInstance> active,
            NightmareCompletionReceiptData.Receipt receipt
    ) {
        Optional<NightmareInstance> checkedActive = Objects.requireNonNull(active, "active");
        NightmareCompletionReceiptData.Receipt checkedReceipt = Objects.requireNonNull(receipt, "receipt");
        if (checkedActive.isEmpty()) {
            return Optional.empty();
        }
        NightmareInstance instance = checkedActive.orElseThrow();
        if (!instance.equals(checkedReceipt.instance())) {
            throw new IllegalStateException("Active Nightmare ownership contradicts the completion receipt");
        }
        return Optional.of(instance);
    }

    private static void replayStoredReturn(ServerPlayer player, NightmareInstance instance) {
        MinecraftServer server = player.getServer();
        ResourceKey<Level> returnKey = ResourceKey.create(Registries.DIMENSION, instance.returnDimension());
        ServerLevel returnLevel = server.getLevel(returnKey);
        if (returnLevel == null) {
            throw new IllegalStateException("Original return dimension is unavailable during completion recovery");
        }
        player.teleportTo(
                returnLevel,
                instance.returnX(),
                instance.returnY(),
                instance.returnZ(),
                Set.of(),
                instance.returnYaw(),
                instance.returnPitch()
        );
    }

    public static RecoveryPlan plan(PlayerState current, GeneratedAppraisalRecoverySnapshot snapshot) {
        PlayerState checkedCurrent = Objects.requireNonNull(current, "current");
        GeneratedAppraisalRecoverySnapshot checkedSnapshot = Objects.requireNonNull(snapshot, "snapshot");

        var aspect = checkedSnapshot.identity().aspect().orElseThrow(
                () -> new IllegalStateException("Recovery snapshot is missing its Aspect"));
        var flaw = checkedSnapshot.identity().flaw().orElseThrow(
                () -> new IllegalStateException("Recovery snapshot is missing its Flaw"));

        SoulData targetSoul = switch (checkedCurrent.soul().spellState()) {
            case ASPIRANT -> checkedCurrent.soul().asDreamer(aspect.instanceId(), aspect.aspectRank(), flaw.instanceId());
            case DREAMER -> {
                SoulData expected = checkedCurrent.soul().asDreamer(
                        aspect.instanceId(), aspect.aspectRank(), flaw.instanceId());
                if (!checkedCurrent.soul().equals(expected)) {
                    throw new IllegalStateException("Persisted Dreamer Soul contradicts the completion receipt");
                }
                yield checkedCurrent.soul();
            }
            default -> throw new IllegalStateException(
                    "Completion receipt cannot replay from Spell state " + checkedCurrent.soul().spellState());
        };

        SoulIdentityData targetIdentity;
        if (!checkedCurrent.identity().isRevealed()) {
            targetIdentity = checkedSnapshot.identity();
        } else if (checkedCurrent.identity().equals(checkedSnapshot.identity())) {
            targetIdentity = checkedCurrent.identity();
        } else {
            throw new IllegalStateException("Persisted Aspect/Flaw identity contradicts the completion receipt");
        }

        AttributeOwnershipData targetAttributes = reconcileAttribute(
                checkedCurrent.attributes(), checkedSnapshot.attribute());
        MemoryOwnershipData targetMemories = reconcileMemory(
                checkedCurrent.memories(), checkedSnapshot.memory());
        EchoOwnershipData targetEchoes;
        try {
            targetEchoes = checkedCurrent.echoes().award(checkedSnapshot.echo());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Persisted Echo ownership contradicts the completion receipt", exception);
        }

        PlayerState target = new PlayerState(targetSoul, targetIdentity, targetAttributes, targetMemories, targetEchoes);
        return new RecoveryPlan(target, target.equals(checkedCurrent));
    }

    private static AttributeOwnershipData reconcileAttribute(
            AttributeOwnershipData current,
            AttributeInstanceData expected
    ) {
        Optional<AttributeInstanceData> existing = current.attributes().stream()
                .filter(attribute -> attribute.attributeId().equals(expected.attributeId()))
                .findFirst();
        if (existing.isPresent()) {
            if (!existing.orElseThrow().equals(expected)) {
                throw new IllegalStateException("Persisted Attribute ownership contradicts the completion receipt");
            }
            return current;
        }
        ArrayList<AttributeInstanceData> next = new ArrayList<>(current.attributes());
        next.add(expected);
        return new AttributeOwnershipData(next);
    }

    private static MemoryOwnershipData reconcileMemory(MemoryOwnershipData current, MemoryInstanceData expected) {
        Optional<MemoryInstanceData> existing = current.memories().stream()
                .filter(memory -> memory.memoryId().equals(expected.memoryId()))
                .findFirst();
        if (existing.isPresent()) {
            if (!existing.orElseThrow().equals(expected)) {
                throw new IllegalStateException("Persisted Memory ownership contradicts the completion receipt");
            }
            return current;
        }
        ArrayList<MemoryInstanceData> next = new ArrayList<>(current.memories());
        next.add(expected);
        return new MemoryOwnershipData(next);
    }

    public static PlayerState currentState(ServerPlayer player) {
        ServerPlayer checkedPlayer = Objects.requireNonNull(player, "player");
        return new PlayerState(
                SoulService.get(checkedPlayer),
                SoulIdentityService.get(checkedPlayer),
                AttributeOwnershipService.get(checkedPlayer),
                MemoryOwnershipService.get(checkedPlayer),
                EchoOwnershipService.get(checkedPlayer)
        );
    }

    private static void apply(ServerPlayer player, PlayerState target) {
        if (!SoulIdentityService.get(player).equals(target.identity())) {
            SoulIdentityService.replace(player, target.identity());
        }
        if (!AttributeOwnershipService.get(player).equals(target.attributes())) {
            AttributeOwnershipService.replace(player, target.attributes());
        }
        if (!MemoryOwnershipService.get(player).equals(target.memories())) {
            MemoryOwnershipService.replace(player, target.memories());
        }
        if (!EchoOwnershipService.get(player).equals(target.echoes())) {
            EchoOwnershipService.replace(player, target.echoes());
        }
        if (!SoulService.get(player).equals(target.soul())) {
            SoulService.replace(player, target.soul());
        }
    }
}
