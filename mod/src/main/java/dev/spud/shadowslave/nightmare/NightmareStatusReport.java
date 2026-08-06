package dev.spud.shadowslave.nightmare;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Pure operator-facing status report for active ownership and retained completion receipts. */
public record NightmareStatusReport(List<String> lines) {
    public NightmareStatusReport {
        lines = List.copyOf(Objects.requireNonNull(lines, "lines"));
    }

    public static NightmareStatusReport from(
            Optional<NightmareInstance> active,
            Optional<NightmareCompletionRecord> completion
    ) {
        Optional<NightmareInstance> checkedActive = Objects.requireNonNull(active, "active");
        Optional<NightmareCompletionRecord> checkedCompletion = Objects.requireNonNull(completion, "completion");
        List<String> lines = new ArrayList<>();

        checkedActive.ifPresentOrElse(instance -> {
            lines.add("Active Nightmare: " + instance.scenarioId());
            lines.add("Historical role: " + instance.historicalRoleId());
            lines.add("Active instance: " + instance.instanceId() + " / slot " + instance.slot());
        }, () -> lines.add("No active Nightmare instance."));

        checkedCompletion.ifPresentOrElse(receipt -> {
            NightmareInstance instance = receipt.instance();
            lines.add("Completion receipt: " + receipt.phase().serializedName());
            lines.add("Receipt instance: " + instance.instanceId() + " / slot " + instance.slot());
            lines.add("Resolved game time: " + receipt.resolvedGameTime());
            lines.add("Ownership consistency: " + consistency(checkedActive, receipt));
        }, () -> lines.add("No retained successful-completion receipt."));

        return new NightmareStatusReport(lines);
    }

    private static String consistency(
            Optional<NightmareInstance> active,
            NightmareCompletionRecord completion
    ) {
        if (active.isEmpty()) {
            return completion.phase() == NightmareCompletionPhase.TEARDOWN_COMMITTED
                    ? "teardown committed; active ownership absent"
                    : "recovery pending; active ownership absent";
        }

        NightmareInstance activeInstance = active.orElseThrow();
        return activeInstance.instanceId().equals(completion.instance().instanceId())
                ? "active ownership matches retained receipt"
                : "CONFLICT: active ownership belongs to another instance";
    }
}
