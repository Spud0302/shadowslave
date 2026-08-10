package dev.spud.shadowslave.appraisal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.spud.shadowslave.echo.EchoInstanceData;
import dev.spud.shadowslave.memory.MemoryInstanceData;
import dev.spud.shadowslave.soul.identity.AttributeInstanceData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import java.util.Objects;

/**
 * Exact replay payload for one already-resolved generated First-Nightmare appraisal.
 *
 * <p>This object deliberately stores the persistent identity/reward records that
 * were resolved for the completion. Recovery must replay these values rather than
 * invoke the current generator or current content catalogue again.</p>
 */
public record GeneratedAppraisalRecoverySnapshot(
        String generatorVersion,
        long generatorSeed,
        String generationFingerprint,
        SoulIdentityData identity,
        AttributeInstanceData attribute,
        MemoryInstanceData memory,
        EchoInstanceData echo
) {
    private static final MapCodec<GeneratedAppraisalRecoverySnapshot> RAW_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.STRING.fieldOf("generator_version")
                            .forGetter(GeneratedAppraisalRecoverySnapshot::generatorVersion),
                    Codec.LONG.fieldOf("generator_seed")
                            .forGetter(GeneratedAppraisalRecoverySnapshot::generatorSeed),
                    Codec.STRING.fieldOf("generation_fingerprint")
                            .forGetter(GeneratedAppraisalRecoverySnapshot::generationFingerprint),
                    SoulIdentityData.CODEC.codec().fieldOf("identity")
                            .forGetter(GeneratedAppraisalRecoverySnapshot::identity),
                    AttributeInstanceData.CODEC.codec().fieldOf("attribute")
                            .forGetter(GeneratedAppraisalRecoverySnapshot::attribute),
                    MemoryInstanceData.CODEC.codec().fieldOf("memory")
                            .forGetter(GeneratedAppraisalRecoverySnapshot::memory),
                    EchoInstanceData.CODEC.codec().fieldOf("echo")
                            .forGetter(GeneratedAppraisalRecoverySnapshot::echo)
            ).apply(instance, GeneratedAppraisalRecoverySnapshot::new));

    public static final MapCodec<GeneratedAppraisalRecoverySnapshot> CODEC = RAW_CODEC.flatXmap(
            value -> {
                try {
                    return DataResult.success(new GeneratedAppraisalRecoverySnapshot(
                            value.generatorVersion(),
                            value.generatorSeed(),
                            value.generationFingerprint(),
                            value.identity(),
                            value.attribute(),
                            value.memory(),
                            value.echo()
                    ));
                } catch (IllegalArgumentException | NullPointerException exception) {
                    return DataResult.error(() -> "Invalid generated appraisal recovery snapshot: " + exception.getMessage());
                }
            },
            DataResult::success
    );

    public GeneratedAppraisalRecoverySnapshot {
        generatorVersion = requireText(generatorVersion, "generatorVersion");
        generationFingerprint = requireFingerprint(generationFingerprint);
        identity = Objects.requireNonNull(identity, "identity");
        attribute = Objects.requireNonNull(attribute, "attribute");
        memory = Objects.requireNonNull(memory, "memory");
        echo = Objects.requireNonNull(echo, "echo");
        if (!identity.isRevealed()) {
            throw new IllegalArgumentException("Recovery snapshot requires a resolved Aspect and Flaw");
        }
    }

    public static GeneratedAppraisalRecoverySnapshot fromPrepared(
            PreviewAppraisalService.PreparedAppraisal prepared
    ) {
        PreviewAppraisalService.PreparedAppraisal checked = Objects.requireNonNull(prepared, "prepared");
        var generated = checked.award().identity();
        return new GeneratedAppraisalRecoverySnapshot(
                generated.generatorVersion(),
                generated.seed(),
                generated.generationFingerprint(),
                checked.identity(),
                checked.attribute(),
                checked.memory(),
                checked.echo()
        );
    }

    public static GeneratedAppraisalRecoverySnapshot fromCommitted(
            PreviewAppraisalService.CommittedAppraisal committed
    ) {
        PreviewAppraisalService.CommittedAppraisal checked = Objects.requireNonNull(committed, "committed");
        var generated = checked.award().identity();
        return new GeneratedAppraisalRecoverySnapshot(
                generated.generatorVersion(),
                generated.seed(),
                generated.generationFingerprint(),
                checked.identity(),
                checked.attribute(),
                checked.memory(),
                checked.echo()
        );
    }

    public Tag save() {
        return CODEC.codec().encodeStart(NbtOps.INSTANCE, this).getOrThrow();
    }

    public static GeneratedAppraisalRecoverySnapshot load(Tag tag) {
        return CODEC.codec().parse(NbtOps.INSTANCE, Objects.requireNonNull(tag, "tag")).getOrThrow();
    }

    private static String requireFingerprint(String value) {
        String checked = requireText(value, "generationFingerprint");
        if (checked.length() != 64 || !checked.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("generationFingerprint must be a lowercase SHA-256 hex digest");
        }
        return checked;
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
