package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;

class NightmareServiceApiTest {
    @Test
    void canonicalDeathCannotBypassDurableDeathServiceThroughPublicApi() {
        boolean exposesLegacyBypass = Arrays.stream(NightmareService.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .map(Method::getName)
                .anyMatch("canonicalDeath"::equals);

        assertFalse(exposesLegacyBypass,
                "Canonical Nightmare death must route through NightmareDeathService's durable transaction");
    }
}
