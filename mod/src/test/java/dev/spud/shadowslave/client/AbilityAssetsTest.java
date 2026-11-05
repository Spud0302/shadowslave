package dev.spud.shadowslave.client;

import dev.spud.shadowslave.awakening.AspectKit;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AbilityAssetsTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/shadowslave");

    @Test
    void everyKitAbilityAndFlawHasAnIconFile() {
        for (AspectKit kit : AspectKitCatalog.all()) {
            for (String id : new String[] {kit.abilityId().toString(), kit.flawEffectId().toString()}) {
                assertTrue(AbilityIcons.has(id), "no icon for " + id);
            }
            String ability = kit.abilityId().getPath().substring(kit.abilityId().getPath().lastIndexOf('/') + 1);
            String flaw = kit.flawEffectId().getPath().substring(kit.flawEffectId().getPath().lastIndexOf('/') + 1);
            assertTrue(Files.isRegularFile(ASSETS.resolve("textures/gui/ability/" + ability + ".png")), ability);
            assertTrue(Files.isRegularFile(ASSETS.resolve("textures/gui/flaw/" + flaw + ".png")), flaw);
        }
    }

    @Test
    void everyRankUpAbilityIsDrawnOnTheHud() {
        for (var variant : dev.spud.shadowslave.awakening.KitLadder.all()) {
            assertTrue(AbilityIcons.has(variant.abilityId().toString()), "no HUD icon for " + variant.name());
        }
    }

    @Test
    void everyKitAbilityHasACastingPose() throws IOException {
        String casts = Files.readString(ASSETS.resolve("player_animations/ability_casts.json"));
        for (AspectKit kit : AspectKitCatalog.all()) {
            String animation = AbilityCastAnimations.animationFor(kit.abilityId()).getPath();
            assertTrue(casts.contains("\"" + animation + "\""), "no casting pose " + animation);
        }
    }
}
