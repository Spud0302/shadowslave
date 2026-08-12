package dev.spud.shadowslave.combat;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class CombatPrototypeSpawnAdmissionTest {
    @Test
    void setupOnlyClaimsReadyAfterServerAcceptsTaggedChainbackSpawn() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/combat/CombatPrototypeCommands.java"));

        int spawnCheck = source.indexOf("if (!level.addFreshEntity(chainback))");
        int equipSword = source.indexOf("player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD))");
        int readyMessage = source.indexOf("Combat prototype ready: Better Combat is loaded");

        assertTrue(spawnCheck >= 0);
        assertTrue(source.contains("the server rejected the tagged Chainback spawn"));
        assertTrue(source.contains("No sword was equipped; move to a valid test area and retry."));
        assertTrue(spawnCheck < equipSword);
        assertTrue(spawnCheck < readyMessage);
    }
}
