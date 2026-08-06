package dev.spud.shadowslave.nightmare;

import dev.spud.shadowslave.ShadowSlaveMod;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

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

    public static Optional<NightmareCompletionFaultPoint> configuredPoint() {
        return NightmareCompletionFaultPoint.parse(System.getProperty(PROPERTY));
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
