package dev.spud.shadowslave.echo.content;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EchoContentCatalogTest {
    @Test
    void waveOneProvidesCreatureDerivedAndArtificialEchoContent() {
        List<EchoContentCatalog.EchoProfile> profiles = EchoContentCatalog.waveOne();

        assertEquals(13, profiles.size());
        assertEquals(12, profiles.stream()
                .filter(profile -> profile.originKind() == EchoContentCatalog.OriginKind.CREATURE_DERIVED)
                .count());
        assertEquals(1, profiles.stream()
                .filter(profile -> profile.originKind() == EchoContentCatalog.OriginKind.ARTIFICIAL)
                .count());

        Set<String> ids = new HashSet<>();
        profiles.forEach(profile -> assertTrue(ids.add(profile.id()), () -> "duplicate id " + profile.id()));
    }

    @Test
    void creatureDerivedProfilesMatchTheFirstWaveCreatureIdentityIds() {
        Set<String> expectedCreatureIds = Set.of(
                "ash_burrower",
                "bell_eater",
                "chainback",
                "drowned_listener",
                "glasswing",
                "gutter_choir",
                "hollow_mimic",
                "mire_runner",
                "pale_ferryman",
                "stone_maw",
                "thorn_matron",
                "veil_stalker"
        );

        Set<String> actual = new HashSet<>();
        EchoContentCatalog.waveOne().stream()
                .filter(profile -> profile.originKind() == EchoContentCatalog.OriginKind.CREATURE_DERIVED)
                .forEach(profile -> actual.add(profile.sourceCreatureId().orElseThrow()));

        assertEquals(expectedCreatureIds, actual);
    }

    @Test
    void catalogueSupportsPracticalRolesBeyondDirectCombat() {
        Set<EchoContentCatalog.Role> roles = new HashSet<>();
        Set<EchoContentCatalog.CommandMode> commands = new HashSet<>();
        Set<String> utilityTags = new HashSet<>();

        EchoContentCatalog.waveOne().forEach(profile -> {
            roles.addAll(profile.roles());
            commands.addAll(profile.commandModes());
            utilityTags.addAll(profile.utilityTags());
            assertFalse(profile.tacticalUse().isBlank());
            assertFalse(profile.presentationCue().isBlank());
        });

        assertTrue(roles.containsAll(Set.of(
                EchoContentCatalog.Role.MOUNT,
                EchoContentCatalog.Role.CARRIER,
                EchoContentCatalog.Role.SCOUT,
                EchoContentCatalog.Role.GUARD,
                EchoContentCatalog.Role.TRACKER,
                EchoContentCatalog.Role.AREA_CONTROL,
                EchoContentCatalog.Role.ESCORT,
                EchoContentCatalog.Role.LABOUR
        )));
        assertTrue(commands.containsAll(Set.of(
                EchoContentCatalog.CommandMode.FOLLOW,
                EchoContentCatalog.CommandMode.HOLD,
                EchoContentCatalog.CommandMode.MOVE_TO,
                EchoContentCatalog.CommandMode.CARRY,
                EchoContentCatalog.CommandMode.MOUNT,
                EchoContentCatalog.CommandMode.SEARCH,
                EchoContentCatalog.CommandMode.INTERCEPT,
                EchoContentCatalog.CommandMode.SCREEN,
                EchoContentCatalog.CommandMode.GUARD_POINT,
                EchoContentCatalog.CommandMode.WITHDRAW
        )));
        assertTrue(utilityTags.size() >= 25);
    }

    @Test
    void artificialEchoDoesNotInventCreatureProvenance() {
        EchoContentCatalog.EchoProfile artificial = EchoContentCatalog.waveOne().stream()
                .filter(profile -> profile.originKind() == EchoContentCatalog.OriginKind.ARTIFICIAL)
                .findFirst()
                .orElseThrow();

        assertEquals("steel_courser", artificial.id());
        assertTrue(artificial.sourceCreatureId().isEmpty());
        assertTrue(artificial.sourceRank().isEmpty());
        assertTrue(artificial.sourceClass().isEmpty());
        assertTrue(artificial.roles().contains(EchoContentCatalog.Role.MOUNT));
    }
}
