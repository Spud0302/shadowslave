package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void canonicalDeathMarkerMutationsAreNotPublicApi() {
        Set<String> publicMethods = Arrays.stream(NightmareDeathRegistryData.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .map(Method::getName)
                .collect(java.util.stream.Collectors.toSet());

        assertFalse(publicMethods.contains("begin"),
                "Canonical death intent must only be created by the package-owned durable death transaction");
        assertFalse(publicMethods.contains("complete"),
                "Canonical death intent must not be publicly clearable before teardown/player persistence commit");
        assertTrue(publicMethods.contains("findByPlayer"),
                "Recovery inspection remains a supported public read boundary");
    }
}
