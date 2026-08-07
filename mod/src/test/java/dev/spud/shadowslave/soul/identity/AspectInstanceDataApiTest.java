package dev.spud.shadowslave.soul.identity;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;

class AspectInstanceDataApiTest {
    @Test
    void scalarAbilityAccessorDoesNotReturnToPublicApi() {
        boolean hasScalarAccessor = Arrays.stream(AspectInstanceData.class.getDeclaredMethods())
                .anyMatch(method -> method.getName().equals("abilityId") && method.getParameterCount() == 0);

        assertFalse(hasScalarAccessor,
                "AspectInstanceData must expose abilitySet(), not a first-entry abilityId() compatibility accessor");
    }
}
