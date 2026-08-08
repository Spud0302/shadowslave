package dev.spud.shadowslave.nightmare;

import dev.spud.shadowslave.ShadowSlaveMod;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Opt-in process termination for physical restart verification.
 *
 * <p>Set {@code -Dshadowslave.completionFault=<serialized point>} for one server
 * process. The matching boundary flushes first, logs the marker, then halts with
 * exit code 86. Remove the property before restarting so login recovery can run.</p>
 */
public final class NightmareCompletionFaultInjector {
    public static final String PROPERTY = "shadowslave.completionFault";
    public static final int EXIT_CODE = 86;
    private static final AtomicBoolean TRIGGERED = new AtomicBoolean();

    private NightmareCompletionFaultInjector() {
    }

    /**
     * Returns the configured one-shot completion boundary.
     *
     * <p>A non-blank but unknown value is a harness configuration error. Failing
     * closed here prevents a misspelled physical-fault run from silently executing
     * a normal successful completion and being mistaken for restart evidence.</p>
     */
    public static Optional<NightmareCompletionFaultPoint> configuredPoint() {
        String configured = System.getProperty(PROPERTY);
        if (configured == null || configured.isBlank()) {
            return Optional.empty();
        }
        return NightmareCompletionFaultPoint.parse(configured)
                .or(() -> {
                    String expected = Arrays.stream(NightmareCompletionFaultPoint.values())
                            .map(NightmareCompletionFaultPoint::serializedName)
                            .collect(Collectors.joining(", "));
                    throw new IllegalStateException(
                            "Invalid -D" + PROPERTY + "='" + configured + "'. Expected one of: " + expected
                    );
                });
    }

    public static void afterDurableBoundary(NightmareCompletionFaultPoint point) {
        Optional<NightmareCompletionFaultPoint> configured = configuredPoint();
        if (configured.isEmpty() || configured.get() != point || !TRIGGERED.compareAndSet(false, true)) {
            return;
        }

        ShadowSlaveMod.LOGGER.error(
                "INTENTIONAL COMPLETION FAULT after durable boundary {}. Halting with exit code {}.",
                point.serializedName(),
                EXIT_CODE
        );
        Runtime.getRuntime().halt(EXIT_CODE);
    }
}
