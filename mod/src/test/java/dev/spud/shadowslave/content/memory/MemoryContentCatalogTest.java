package dev.spud.shadowslave.content.memory;

import dev.spud.shadowslave.soul.SoulRank;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryContentCatalogTest {
    @Test
    void waveOneHasBroadTypesRanksAndRoles() {
        MemoryContentCatalog catalog = MemoryContentCatalog.waveOne();
        assertEquals(12, catalog.memories().size());

        Set<MemoryContentCatalog.MemoryType> types = EnumSet.noneOf(MemoryContentCatalog.MemoryType.class);
        Set<SoulRank> ranks = EnumSet.noneOf(SoulRank.class);
        Set<MemoryContentCatalog.EnchantmentRole> roles = EnumSet.noneOf(MemoryContentCatalog.EnchantmentRole.class);
        catalog.memories().forEach(memory -> {
            types.add(memory.type());
            ranks.add(memory.rank());
            memory.enchantments().forEach(enchantment -> roles.add(enchantment.role()));
        });

        assertEquals(EnumSet.allOf(MemoryContentCatalog.MemoryType.class), types);
        assertTrue(ranks.containsAll(Set.of(SoulRank.DORMANT, SoulRank.AWAKENED, SoulRank.ASCENDED)));
        assertEquals(EnumSet.allOf(MemoryContentCatalog.EnchantmentRole.class), roles);
    }

    @Test
    void everyMemoryCarriesUsableContentAndStableIdentity() {
        MemoryContentCatalog catalog = MemoryContentCatalog.waveOne();
        Set<String> ids = new HashSet<>();
        Set<String> hooks = new HashSet<>();

        catalog.memories().forEach(memory -> {
            assertTrue(ids.add(memory.id().toString()));
            assertTrue(memory.tier() >= 1 && memory.tier() <= 7);
            assertTrue(memory.themeTags().size() >= 2);
            assertTrue(memory.provenance().startsWith("design/memory-wave1/"));
            memory.enchantments().forEach(enchantment -> {
                assertTrue(enchantment.gameplayHook().length() >= 20);
                hooks.add(enchantment.gameplayHook());
            });
        });

        assertTrue(hooks.size() >= 24, "wave one should not collapse into repeated enchantment hooks");
    }

    @Test
    void waveOneIncludesNonCombatUtilityAndMeaningfulTradeoffs() {
        MemoryContentCatalog catalog = MemoryContentCatalog.waveOne();

        long toolsAndCharms = catalog.memories().stream()
                .filter(memory -> memory.type() == MemoryContentCatalog.MemoryType.TOOL
                        || memory.type() == MemoryContentCatalog.MemoryType.CHARM)
                .count();
        long tradeoffs = catalog.memories().stream()
                .flatMap(memory -> memory.enchantments().stream())
                .filter(enchantment -> enchantment.role() == MemoryContentCatalog.EnchantmentRole.TRADEOFF)
                .count();
        long supportLike = catalog.memories().stream()
                .flatMap(memory -> memory.enchantments().stream())
                .filter(enchantment -> Set.of(
                        MemoryContentCatalog.EnchantmentRole.UTILITY,
                        MemoryContentCatalog.EnchantmentRole.COMMUNICATION,
                        MemoryContentCatalog.EnchantmentRole.DETECTION,
                        MemoryContentCatalog.EnchantmentRole.SUPPORT,
                        MemoryContentCatalog.EnchantmentRole.SURVIVAL
                ).contains(enchantment.role()))
                .count();

        assertTrue(toolsAndCharms >= 5);
        assertTrue(tradeoffs >= 3);
        assertTrue(supportLike >= 8);
    }
}
