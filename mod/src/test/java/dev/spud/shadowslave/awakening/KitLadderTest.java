package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.soul.SoulRank;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KitLadderTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/shadowslave");

    @Test
    void everyNatureOffersTwoAwakenedVariantsAndAtMostOneForm() {
        for (AspectKit kit : AspectKitCatalog.all()) {
            assertEquals(2, KitLadder.variants(kit.natureId(), SoulRank.AWAKENED).size(), kit.natureName());
            int master = KitLadder.variants(kit.natureId(), SoulRank.ASCENDED).size();
            assertTrue(master == 0 || master == 2, kit.natureName() + " Master choices");
            assertTrue(KitLadder.variants(kit.natureId(), SoulRank.TRANSCENDENT).size() <= 1, kit.natureName() + " form");
            KitLadder.variants(kit.natureId(), SoulRank.TRANSCENDENT)
                    .forEach(form -> assertEquals(SoulRank.TRANSCENDENT, form.rank(), form.name()));
        }
        assertTrue(KitLadder.variants(AspectKitCatalog.EMBER.natureId(), SoulRank.TRANSCENDENT).contains(KitLadder.LIVING_FLAME));
        assertTrue(KitLadder.variants(AspectKitCatalog.EMBER.natureId(), SoulRank.SUPREME).isEmpty());
        Set<Object> ids = new HashSet<>();
        KitLadder.all().forEach(variant -> assertTrue(ids.add(variant.abilityId()), "duplicate " + variant.abilityId()));
    }

    @Test
    void everyVariantHasAnIconAndACastingPose() throws IOException {
        String casts = Files.readString(ASSETS.resolve("player_animations/ability_casts.json"));
        for (AbilityVariant variant : KitLadder.all()) {
            String path = variant.abilityId().getPath();
            String name = path.substring(path.lastIndexOf('/') + 1);
            assertTrue(Files.exists(ASSETS.resolve("textures/gui/ability/" + name + ".png")), name + " icon");
            assertTrue(casts.contains("\"cast_" + name + "\""), name + " casting pose");
        }
    }
}
