package dev.spud.shadowslave.soul.identity;

import net.minecraft.resources.ResourceLocation;
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

    @Test
    void scalarAbilityConstructorDoesNotReturnToPublicApi() {
        boolean hasScalarConstructor = Arrays.stream(AspectInstanceData.class.getDeclaredConstructors())
                .map(constructor -> constructor.getParameterTypes())
                .anyMatch(parameters -> parameters.length == 6
                        && parameters[4] == ResourceLocation.class);

        assertFalse(hasScalarConstructor,
                "AspectInstanceData construction must require AspectAbilitySetData instead of a scalar ability ID");
    }
}
