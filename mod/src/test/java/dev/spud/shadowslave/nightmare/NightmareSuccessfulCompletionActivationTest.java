package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class NightmareSuccessfulCompletionActivationTest {
    @Test
    void verifiesReceiptDurabilityBeforePublishingWorldResolutionOrRecovery() {
        List<String> calls = new ArrayList<>();

        NightmareSuccessfulCompletionActivation.run(new NightmareSuccessfulCompletionActivation.Operations() {
            @Override
            public void validateTerminalResolution() {
                calls.add("validate");
            }

            @Override
            public void captureRegistryBeforeTerminalResolution() {
                calls.add("capture");
            }

            @Override
            public void recordTerminalResolution() {
                calls.add("record");
            }

            @Override
            public void persistRegistry() {
                calls.add("persist");
            }

            @Override
            public void verifyTerminalRegistryDurable() {
                calls.add("verify");
            }

            @Override
            public void afterTerminalRegistryDurable() {
                calls.add("durable-boundary");
            }

            @Override
            public void applyWorldResolutionPresentation() {
                calls.add("presentation");
            }

            @Override
            public boolean resumeCompletion() {
                calls.add("resume");
                return true;
            }
        });

        assertEquals(
                List.of("validate", "capture", "record", "persist", "verify", "durable-boundary", "presentation", "resume"),
                calls
        );
    }

    @Test
    void failedReceiptVerificationStopsBeforeDurableBoundaryAndPresentation() {
        List<String> calls = new ArrayList<>();
        RuntimeException failure = new RuntimeException("completion receipt did not reach disk");

        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                NightmareSuccessfulCompletionActivation.run(new NightmareSuccessfulCompletionActivation.Operations() {
                    @Override
                    public void validateTerminalResolution() {
                        calls.add("validate");
                    }

                    @Override
                    public void captureRegistryBeforeTerminalResolution() {
                        calls.add("capture");
                    }

                    @Override
                    public void recordTerminalResolution() {
                        calls.add("record");
                    }

                    @Override
                    public void persistRegistry() {
                        calls.add("persist");
                    }

                    @Override
                    public void verifyTerminalRegistryDurable() {
                        calls.add("verify");
                        throw failure;
                    }

                    @Override
                    public void afterTerminalRegistryDurable() {
                        calls.add("durable-boundary");
                    }

                    @Override
                    public void applyWorldResolutionPresentation() {
                        calls.add("presentation");
                    }

                    @Override
                    public boolean resumeCompletion() {
                        calls.add("resume");
                        return true;
                    }
                })
        );

        assertSame(failure, thrown);
        assertEquals(List.of("validate", "capture", "record", "persist", "verify"), calls);
    }
}
