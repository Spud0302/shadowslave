package dev.spud.shadowslave.content.memory;

import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Player-facing authored Memory concepts.
 *
 * <p>This catalogue owns stable Shadow Slave content identity only. It does not
 * implement Spell drop chances, Minecraft items, persistence or enchantment
 * execution. Exact names, effects and balance are project DESIGN.</p>
 */
public record MemoryContentCatalog(List<MemoryProfile> memories) {
    private static final String NAMESPACE = "shadowslave";

    public MemoryContentCatalog {
        ArrayList<MemoryProfile> canonical = new ArrayList<>(Objects.requireNonNull(memories, "memories"));
        canonical.sort(Comparator.comparing(profile -> profile.id().toString()));
        HashSet<ResourceLocation> seen = new HashSet<>();
        for (MemoryProfile profile : canonical) {
            Objects.requireNonNull(profile, "memory profile");
            if (!seen.add(profile.id())) {
                throw new IllegalArgumentException("Duplicate Memory id: " + profile.id());
            }
        }
        memories = List.copyOf(canonical);
    }

    public static MemoryContentCatalog waveOne() {
        return new MemoryContentCatalog(List.of(
                memory("ash_compass", "Ash Compass", SoulRank.DORMANT, 2, MemoryType.TOOL,
                        Set.of("navigation", "survival"),
                        enchant("ember_north", EnchantmentRole.UTILITY, "points toward the safest recently travelled refuge rather than geographic north"),
                        enchant("warm_needle", EnchantmentRole.SURVIVAL, "warms when hostile essence-bearing life closes in")),
                memory("bellglass_token", "Bellglass Token", SoulRank.DORMANT, 3, MemoryType.CHARM,
                        Set.of("warning", "resonance"),
                        enchant("clear_warning", EnchantmentRole.DETECTION, "vibrates before nearby hidden movement breaks silence"),
                        enchant("held_note", EnchantmentRole.COMMUNICATION, "stores one short sound and releases it later")),
                memory("blackwater_hook", "Blackwater Hook", SoulRank.AWAKENED, 2, MemoryType.WEAPON,
                        Set.of("mobility", "water", "control"),
                        enchant("undertow_line", EnchantmentRole.MOBILITY, "anchors an essence line to terrain or a struck target"),
                        enchant("river_grip", EnchantmentRole.CONTROL, "resists forced movement while the line is fixed")),
                memory("borrowed_dawn", "Borrowed Dawn", SoulRank.AWAKENED, 3, MemoryType.CHARM,
                        Set.of("light", "recovery"),
                        enchant("first_light", EnchantmentRole.SUPPORT, "stores ambient light and releases it as a brief restorative warmth"),
                        enchant("night_debt", EnchantmentRole.TRADEOFF, "stored power decays quickly when repeatedly invoked without rest")),
                memory("glass_road", "Glass Road", SoulRank.AWAKENED, 4, MemoryType.WEAPON,
                        Set.of("precision", "mobility"),
                        enchant("mirror_step", EnchantmentRole.MOBILITY, "briefly redirects the wielder along the direction of a successful parry"),
                        enchant("clean_edge", EnchantmentRole.OFFENSE, "rewards precise strikes more than repeated impacts")),
                memory("last_watch_mantle", "Last Watch Mantle", SoulRank.AWAKENED, 5, MemoryType.ARMOR,
                        Set.of("guard", "vigilance", "rescue"),
                        enchant("unyielding_watch", EnchantmentRole.DEFENSE, "hardens while the wearer deliberately holds a fixed position"),
                        enchant("shared_shelter", EnchantmentRole.SUPPORT, "extends part of its protection to one nearby ally"),
                        enchant("wakeful_thread", EnchantmentRole.DETECTION, "sharpens warning cues while the wearer is protecting another")),
                memory("mirewalker_boots", "Mirewalker Boots", SoulRank.DORMANT, 4, MemoryType.ARMOR,
                        Set.of("terrain", "mobility"),
                        enchant("sure_foot", EnchantmentRole.MOBILITY, "reduces movement loss from mud, shallow water and unstable ground"),
                        enchant("light_trace", EnchantmentRole.STEALTH, "softens obvious tracks on wet terrain")),
                memory("pale_ferryman_lantern", "Pale Ferryman's Lantern", SoulRank.ASCENDED, 3, MemoryType.TOOL,
                        Set.of("guidance", "spirits", "risk"),
                        enchant("crossing_light", EnchantmentRole.UTILITY, "reveals a short safe route across dangerous ground"),
                        enchant("fare_due", EnchantmentRole.TRADEOFF, "repeated guidance consumes increasing essence until dismissed for recovery")),
                memory("red_thread_bracelet", "Red Thread Bracelet", SoulRank.DORMANT, 3, MemoryType.CHARM,
                        Set.of("connection", "rescue"),
                        enchant("tethered_pulse", EnchantmentRole.COMMUNICATION, "signals the direction of one deliberately marked companion within range"),
                        enchant("strain_warning", EnchantmentRole.DETECTION, "tightens when the marked companion suffers sudden danger")),
                memory("stonewake_shield", "Stonewake Shield", SoulRank.ASCENDED, 4, MemoryType.WEAPON,
                        Set.of("defense", "terrain", "weight"),
                        enchant("settle", EnchantmentRole.DEFENSE, "increases effective weight while braced against an incoming force"),
                        enchant("wake", EnchantmentRole.CONTROL, "releases stored impact into the ground as a short disruptive pulse")),
                memory("thorn_mercy", "Thorn Mercy", SoulRank.AWAKENED, 3, MemoryType.WEAPON,
                        Set.of("restraint", "growth", "control"),
                        enchant("living_bind", EnchantmentRole.CONTROL, "creates thorned bindings that strengthen against violent struggling"),
                        enchant("merciful_edge", EnchantmentRole.TRADEOFF, "loses potency when used for indiscriminate lethal attacks")),
                memory("veil_stitch_case", "Veil-Stitch Case", SoulRank.AWAKENED, 2, MemoryType.TOOL,
                        Set.of("repair", "concealment"),
                        enchant("quiet_mending", EnchantmentRole.UTILITY, "repairs ordinary equipment more effectively while out of combat"),
                        enchant("dull_seam", EnchantmentRole.STEALTH, "temporarily suppresses shine, clatter and obvious surface damage on repaired gear"))
        ));
    }

    public enum MemoryType { WEAPON, ARMOR, TOOL, CHARM }

    public enum EnchantmentRole {
        OFFENSE,
        DEFENSE,
        CONTROL,
        MOBILITY,
        DETECTION,
        STEALTH,
        SUPPORT,
        SURVIVAL,
        COMMUNICATION,
        UTILITY,
        TRADEOFF
    }

    public record MemoryProfile(
            ResourceLocation id,
            String formalName,
            SoulRank rank,
            int tier,
            MemoryType type,
            Set<String> themeTags,
            List<EnchantmentProfile> enchantments,
            String provenance
    ) {
        public MemoryProfile {
            id = Objects.requireNonNull(id, "id");
            formalName = requireText(formalName, "formalName");
            rank = Objects.requireNonNull(rank, "rank");
            if (tier < 1 || tier > 7) {
                throw new IllegalArgumentException("Memory tier must be between 1 and 7");
            }
            type = Objects.requireNonNull(type, "type");
            themeTags = normalizedTags(themeTags, "themeTags");
            enchantments = List.copyOf(Objects.requireNonNull(enchantments, "enchantments"));
            if (enchantments.isEmpty()) {
                throw new IllegalArgumentException("Memory must have at least one authored enchantment");
            }
            HashSet<ResourceLocation> ids = new HashSet<>();
            for (EnchantmentProfile enchantment : enchantments) {
                if (!ids.add(enchantment.id())) {
                    throw new IllegalArgumentException("Duplicate enchantment id in Memory " + id + ": " + enchantment.id());
                }
            }
            provenance = requireText(provenance, "provenance");
        }
    }

    public record EnchantmentProfile(ResourceLocation id, EnchantmentRole role, String gameplayHook) {
        public EnchantmentProfile {
            id = Objects.requireNonNull(id, "id");
            role = Objects.requireNonNull(role, "role");
            gameplayHook = requireText(gameplayHook, "gameplayHook");
        }
    }

    private static MemoryProfile memory(
            String path,
            String name,
            SoulRank rank,
            int tier,
            MemoryType type,
            Set<String> tags,
            EnchantmentProfile... enchantments
    ) {
        return new MemoryProfile(
                id("memory/" + path),
                name,
                rank,
                tier,
                type,
                tags,
                List.of(enchantments),
                "design/memory-wave1/" + path
        );
    }

    private static EnchantmentProfile enchant(String path, EnchantmentRole role, String hook) {
        return new EnchantmentProfile(id("memory_enchantment/" + path), role, hook);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }

    private static Set<String> normalizedTags(Set<String> source, String name) {
        HashSet<String> normalized = new HashSet<>();
        for (String value : Objects.requireNonNull(source, name)) {
            normalized.add(requireText(value, name).toLowerCase(Locale.ROOT).replace(' ', '_'));
        }
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be empty");
        }
        return Set.copyOf(normalized);
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
