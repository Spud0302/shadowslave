package dev.spud.shadowslave.nightmare;

import net.minecraft.nbt.CompoundTag;

import java.util.Objects;

/**
 * Durable success receipt for one resolved Nightmare instance.
 *
 * <p>The complete instance snapshot remains available after active ownership is
 * consumed, allowing restart recovery to finish appraisal, return and teardown
 * without reconstructing information from transient entities or player
 * position.</p>
 */
public record NightmareCompletionRecord(
        NightmareInstance instance,
        NightmareCompletionPhase phase,
        long resolvedGameTime
) {
    public NightmareCompletionRecord {
        instance = Objects.requireNonNull(instance, "instance");
        phase = Objects.requireNonNull(phase, "phase");
        if (resolvedGameTime < 0L) {
            throw new IllegalArgumentException("resolvedGameTime cannot be negative");
        }
    }

    public NightmareCompletionRecord advanceTo(NightmareCompletionPhase target) {
        NightmareCompletionPhase checkedTarget = Objects.requireNonNull(target, "target");
        if (checkedTarget.ordinal() < phase.ordinal()) {
            throw new IllegalArgumentException(
                    "Nightmare completion phase cannot move backwards from " + phase + " to " + checkedTarget
            );
        }
        return checkedTarget == phase
                ? this
                : new NightmareCompletionRecord(instance, checkedTarget, resolvedGameTime);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put("instance", instance.save());
        tag.putString("phase", phase.serializedName());
        tag.putLong("resolved_game_time", resolvedGameTime);
        return tag;
    }

    public static NightmareCompletionRecord load(CompoundTag tag) {
        return new NightmareCompletionRecord(
                NightmareInstance.load(tag.getCompound("instance")),
                NightmareCompletionPhase.parse(tag.getString("phase")),
                tag.getLong("resolved_game_time")
        );
    }
}
