package dev.spud.shadowslave.echoes;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.core.ShardRank;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The pure Echo rules and the Echo data in the mod's resources. */
class EchoRulesTest {
    private static final Path SETTINGS = Path.of("src/main/resources/data/shadowslave/echo/settings.json");
    private static final Path CREATURES = Path.of("src/main/resources/data/shadowslave/echo/creature");

    private static EchoSettings settings() throws IOException {
        return EchoSettings.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(SETTINGS))).getOrThrow();
    }

    private static EchoRecord echo(CreatureRank rank, float health) {
        EchoBody body = new EchoBody(ResourceLocation.withDefaultNamespace("zombie"), "Zombie", rank, "", new CompoundTag(), 20.0F);
        return new EchoRecord(java.util.UUID.randomUUID(), body, health, "Jack");
    }

    @Test
    void slotsFollowRankAsJackChose() throws IOException {
        EchoSettings settings = settings();
        assertEquals(0, EchoRules.slots(settings, Optional.empty()));
        assertEquals(0, EchoRules.slots(settings, Optional.of(SoulRank.DORMANT)));
        assertEquals(1, EchoRules.slots(settings, Optional.of(SoulRank.AWAKENED)));
        assertEquals(2, EchoRules.slots(settings, Optional.of(SoulRank.ASCENDED)), "a Master holds two");
        assertEquals(3, EchoRules.slots(settings, Optional.of(SoulRank.TRANSCENDENT)), "a Saint holds three");
    }

    @Test
    void remnantsAreRareAndRarerForWeakerCreatures() throws IOException {
        EchoSettings settings = settings();
        float previous = 0.0F;
        for (CreatureRank rank : CreatureRank.values()) {
            float chance = settings.dropChance(rank);
            assertTrue(chance > 0.0F && chance < 0.2F, rank + " chance " + chance);
            assertTrue(chance >= previous, "chance grows with rank");
            previous = chance;
        }
        EchoCreature override = new EchoCreature(ResourceLocation.withDefaultNamespace("zombie"), Optional.empty(), Optional.of(0.5F), true);
        assertEquals(0.5F, EchoRules.dropChance(settings, CreatureRank.DORMANT, Optional.of(override)));
    }

    @Test
    void bindingNeedsLevelAnchorAndEssence() throws IOException {
        EchoSettings settings = settings();
        Optional<ShardRank> awakened = Optional.of(ShardRank.AWAKENED);
        assertTrue(EchoRules.bindLock(settings, CreatureRank.AWAKENED, 1, awakened, 500).orElseThrow().contains("Binding level 3"));
        assertTrue(EchoRules.bindLock(settings, CreatureRank.AWAKENED, 3, Optional.of(ShardRank.DORMANT), 500).orElseThrow().contains("Awakened"));
        assertTrue(EchoRules.bindLock(settings, CreatureRank.AWAKENED, 3, Optional.empty(), 500).isPresent());
        assertTrue(EchoRules.bindLock(settings, CreatureRank.AWAKENED, 3, awakened, 10).orElseThrow().contains("essence"));
        assertFalse(EchoRules.bindLock(settings, CreatureRank.AWAKENED, 3, awakened, 500).isPresent());
        assertFalse(EchoRules.bindLock(settings, CreatureRank.DORMANT, 1, Optional.of(ShardRank.FALLEN), 500).isPresent(),
                "a stronger anchor does");
        assertEquals(ShardRank.CORRUPTED, EchoRules.anchorFor(CreatureRank.UNHOLY), "anchors stop at the strongest shard there is");
    }

    @Test
    void upkeepAddsUpAndAnEchoMendsWhileItRests() throws IOException {
        EchoSettings settings = settings();
        assertEquals(1.5F, EchoRules.upkeep(settings, List.of(echo(CreatureRank.DORMANT, 20), echo(CreatureRank.AWAKENED, 20))), 1.0E-4);
        assertEquals(0.0F, EchoRules.upkeep(settings, List.of()));
        EchoRecord hurt = echo(CreatureRank.DORMANT, 10);
        assertEquals(10.4F, EchoRules.rested(settings, hurt, 1).health(), 1.0E-4);
        assertEquals(20.0F, EchoRules.rested(settings, hurt, 1000).health(), "never past full");
    }

    @Test
    void anEchoKeepsItsIdentityAndNeverGrows() {
        EchoRecord echo = echo(CreatureRank.FALLEN, 20);
        EchoRecord hurt = echo.withHealth(5);
        assertEquals(echo.id(), hurt.id());
        assertEquals(CreatureRank.FALLEN, hurt.body().rank());
        assertEquals("Zombie Echo", echo.name());
        EchoRecord decoded = EchoRecord.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE,
                EchoRecord.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, hurt).getOrThrow()).getOrThrow();
        assertEquals(hurt, decoded, "round-trips through save data");
    }

    @Test
    void everyCreatureFileParses() throws IOException {
        try (Stream<Path> files = Files.list(CREATURES)) {
            for (Path file : files.toList()) {
                EchoCreature.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(file))).getOrThrow();
            }
        }
    }

    @Test
    void ordersParseAndAttackIsNeverStanding() {
        assertEquals(Optional.of(EchoCommand.STAY), EchoCommand.byName("stay"));
        assertFalse(EchoCommand.ATTACK.standing());
        assertEquals(EchoCommand.GUARD, new SoulEchoes(List.of(), EchoCommand.ATTACK).order());
    }
}
