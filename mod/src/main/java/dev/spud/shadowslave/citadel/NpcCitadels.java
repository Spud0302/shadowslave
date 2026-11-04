package dev.spud.shadowslave.citadel;

import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.dreamrealm.DreamCrossing;
import dev.spud.shadowslave.dreamrealm.DreamRealm;
import dev.spud.shadowslave.dreamrealm.DreamRealmContent;
import dev.spud.shadowslave.dreamrealm.DreamRegion;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * NPC-held Citadels (DESIGN, Jack 2026-10-31). Every generated Citadel wakes
 * as one of three kinds, fixed by the world seed and where it stands: held
 * by an NPC Lesser Legacy Clan under a Great Clan (CANON, ch. 365), wild
 * under its Guardian, or fallen, its Gateway open among prowling creatures.
 * A held Citadel's Saint lord binds it (CANON, ch. 1074), its guards keep the
 * walls (CANON, ch. 398), and its people only stand in the world while a
 * player is near; away from players they are a count. Any player the clan
 * is not set against may anchor to its Gateway (CANON, ch. 752: changing
 * anchor is as simple as using another Gateway) and trade there, and
 * standing with the clan rises and falls with what they do on its land.
 */
public final class NpcCitadels {
    /** What a generated Citadel wakes as. */
    public enum Kind {
        HELD, WILD, FALLEN
    }

    /** How a player stands with an NPC clan. */
    public enum Standing {
        HOSTILE("Hostile", ChatFormatting.DARK_RED),
        STRANGER("Stranger", ChatFormatting.GRAY),
        KNOWN("Known", ChatFormatting.WHITE),
        TRUSTED("Trusted", ChatFormatting.AQUA),
        SWORN("Sworn", ChatFormatting.GOLD);

        private final String displayName;
        private final ChatFormatting colour;

        Standing(String displayName, ChatFormatting colour) {
            this.displayName = displayName;
            this.colour = colour;
        }

        public String displayName() {
            return displayName;
        }

        public ChatFormatting colour() {
            return colour;
        }
    }

    /** The courtyard a generated Citadel is laid around reaches this far from its centre (worldgen/structure/citadel.json). */
    static final int HALF = 12;
    private static final long MINUTE = 1200L;

    private NpcCitadels() {
    }

    static NpcCitadelSettings settings() {
        return CitadelContentLoader.content().npc();
    }

    // Waking

    /** What the Citadel behind this Gateway is, fixed by the world seed so every server with the seed agrees. */
    public static Kind roll(ServerLevel level, BlockPos gateway) {
        double distance = Math.sqrt((double) gateway.getX() * gateway.getX() + (double) gateway.getZ() * gateway.getZ());
        return settings().mixAt(distance).pick(chance(level.getSeed(), gateway, 0x5EED));
    }

    /** Gives a freshly woken Citadel to an NPC clan: one near enough with room, or a new one founded for it. */
    static void hold(ServerLevel level, CitadelData.Citadel citadel) {
        NpcCitadelSettings settings = settings();
        NpcClanData clans = NpcClanData.get(level.getServer());
        CitadelData data = CitadelData.get(level);
        NpcClanData.NpcClan clan = null;
        double reach = (double) settings.clanReach() * settings.clanReach();
        for (NpcClanData.NpcClan candidate : clans.clans) {
            List<CitadelData.Citadel> held = data.citadels.stream().filter(other -> candidate.id.equals(other.npcClan)).toList();
            if (held.size() < settings.clanMaxCitadels() && held.stream().anyMatch(other -> other.gateway.distSqr(citadel.gateway) <= reach)) {
                clan = candidate;
                break;
            }
        }
        if (clan == null) {
            clan = found(level, clans, citadel.gateway);
        }
        citadel.hold = CitadelData.Hold.NPC;
        citadel.npcClan = clan.id;
        citadel.guards = settings.guards();
        citadel.residents = settings.residents();
        citadel.regrownAt = level.getGameTime();
        data.setDirty();
        hangBanners(level, citadel, clan.colour);
    }

    private static NpcClanData.NpcClan found(ServerLevel level, NpcClanData clans, BlockPos at) {
        NpcCitadelSettings settings = settings();
        long seed = level.getSeed();
        List<String> names = new ArrayList<>(settings.clanNames());
        names.removeIf(name -> clans.clans.stream().anyMatch(clan -> clan.name.equals("Clan " + name)));
        String name = names.isEmpty()
                ? "Clan " + pick(settings.clanNames(), seed, at, 1) + " " + (clans.clans.size() + 1)
                : "Clan " + pick(names, seed, at, 1);
        DyeColor[] colours = DyeColor.values();
        NpcClanData.NpcClan clan = new NpcClanData.NpcClan(UUID.randomUUID(), name, pick(settings.greatClans(), seed, at, 2),
                colours[Math.floorMod(Long.hashCode(Mth.getSeed(at) ^ seed * 7L), colours.length)],
                pick(settings.givenNames(), seed, at, 3), pick(settings.natures(), seed, at, 4));
        clans.clans.add(clan);
        clans.setDirty();
        return clan;
    }

    /** A fallen Citadel: its Gateway open, nobody holding it, creatures of the region prowling the courtyard. */
    static void fall(ServerLevel level, CitadelData.Citadel citadel) {
        citadel.hold = CitadelData.Hold.CONQUERED;
        citadel.guardian = null;
        CitadelData.get(level).setDirty();
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        Optional<DreamRegion> region = DreamRealmContent.get().regionAt(citadel.gateway).map(Map.Entry::getValue);
        List<DreamRegion.Roamer> roamers = region.map(DreamRegion::creatures).orElse(List.of());
        if (roamers.isEmpty()) {
            return;
        }
        for (int i = 0; i < settings().fallenCreatures(); i++) {
            DreamRegion.Roamer roamer = roamers.get(level.random.nextInt(roamers.size()));
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(roamer.entity());
            if (!(type.create(level) instanceof Mob mob)) {
                continue;
            }
            BlockPos at = citadel.gateway.offset(level.random.nextInt(13) - 6, -1, 5 + level.random.nextInt(4));
            mob.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(at), MobSpawnType.STRUCTURE, null);
            if (mob instanceof NightmareCreature creature) {
                roamer.rank().ifPresent(rank -> creature.setRankAndClass(rank, roamer.creatureClass().orElse(creature.creatureClass())));
            }
            mob.setPersistenceRequired();
            if (mob instanceof PathfinderMob pathfinder) {
                pathfinder.restrictTo(citadel.gateway, HALF);
            }
            level.addFreshEntity(mob);
        }
    }

    /** Hangs the clan's colours either side of the gate. */
    private static void hangBanners(ServerLevel level, CitadelData.Citadel citadel, DyeColor colour) {
        BlockPos centre = citadel.gateway.below();
        Block banner = BuiltInRegistries.BLOCK.get(ResourceLocation.withDefaultNamespace(colour.getSerializedName() + "_wall_banner"));
        if (!(banner instanceof WallBannerBlock)) {
            return;
        }
        BlockState state = banner.defaultBlockState().setValue(WallBannerBlock.FACING, Direction.SOUTH);
        for (int side = -1; side <= 1; side += 2) {
            BlockPos at = centre.offset(side * 3, 3, HALF + 1);
            if (level.isLoaded(at) && level.getBlockState(at).isAir()) {
                level.setBlock(at, state, Block.UPDATE_ALL);
            }
        }
    }

    // Its people

    /** Keeps a held Citadel's people at their posts while a player is near, and only then; replaces the lost in time. */
    static void tick(ServerLevel level, CitadelData.Citadel citadel) {
        NpcCitadelSettings settings = settings();
        long now = level.getGameTime();
        long regrow = settings.regrowMinutes() * MINUTE;
        if (now - citadel.regrownAt >= regrow) {
            citadel.regrownAt = now;
            if (citadel.guards < settings.guards()) {
                citadel.guards++;
            } else if (citadel.residents < settings.residents()) {
                citadel.residents++;
            }
            CitadelData.get(level).setDirty();
        }
        List<CitadelResident> present = present(level, citadel);
        double radius = settings.presenceRadius();
        boolean watched = !level.getPlayers(player -> !player.isSpectator()
                && player.blockPosition().distSqr(citadel.gateway) <= radius * radius).isEmpty();
        if (!watched) {
            present.forEach(CitadelResident::discard);
            return;
        }
        Optional<NpcClanData.NpcClan> clan = Optional.ofNullable(citadel.npcClan).flatMap(id -> NpcClanData.get(level.getServer()).byId(id));
        if (clan.isEmpty()) {
            return;
        }
        Map<CitadelResident.Role, Integer> standing = new EnumMap<>(CitadelResident.Role.class);
        present.forEach(resident -> standing.merge(resident.role(), 1, Integer::sum));
        int room = settings.maxPresent() - present.size();
        int traders = Math.min(1, citadel.residents);
        for (Map.Entry<CitadelResident.Role, Integer> wanted : Map.of(
                CitadelResident.Role.LORD, 1,
                CitadelResident.Role.TRADER, traders,
                CitadelResident.Role.GUARD, citadel.guards,
                CitadelResident.Role.RESIDENT, citadel.residents - traders).entrySet().stream()
                .sorted(Map.Entry.comparingByKey(java.util.Comparator.comparingInt(role -> List.of(CitadelResident.Role.LORD,
                        CitadelResident.Role.TRADER, CitadelResident.Role.GUARD, CitadelResident.Role.RESIDENT).indexOf(role))))
                .toList()) {
            CitadelResident.Role role = wanted.getKey();
            for (int index = standing.getOrDefault(role, 0); index < wanted.getValue() && room > 0; index++, room--) {
                BlockPos post = post(citadel, role, index);
                if (!level.isLoaded(post)) {
                    continue;
                }
                String name = role == CitadelResident.Role.LORD ? clan.get().lord
                        : pick(settings.givenNames(), level.getSeed(), citadel.gateway, 100 + role.ordinal() * 50 + index);
                level.addFreshEntity(CitadelResident.create(level, citadel, clan.get(), role, name, post));
            }
        }
    }

    /** The people of this Citadel standing in the world now. */
    public static List<CitadelResident> present(ServerLevel level, CitadelData.Citadel citadel) {
        return level.getEntitiesOfClass(CitadelResident.class, new AABB(citadel.gateway).inflate(HALF + 24),
                resident -> resident.citadel().filter(citadel.id::equals).isPresent());
    }

    /** Where the {@code index}th of a role stands: the lord behind the Gateway, guards on the towers and at the gate, the rest about the courtyard. */
    static BlockPos post(CitadelData.Citadel citadel, CitadelResident.Role role, int index) {
        BlockPos centre = citadel.gateway.below();
        return switch (role) {
            case LORD -> centre.offset(0, 1, -2);
            case TRADER -> centre.offset(-6, 0, 3);
            case GUARD -> {
                if (index < 4) {
                    int sx = index % 2 == 0 ? -1 : 1;
                    int sz = index < 2 ? -1 : 1;
                    yield centre.offset(sx * (HALF - 1) - sx, CitadelLayout.TOWER_HEIGHT + 1, sz * (HALF - 1));
                }
                if (index < 6) {
                    yield centre.offset(index == 4 ? -2 : 2, 0, HALF - 2);
                }
                int[][] rounds = {{-9, 0}, {9, 0}, {0, -9}, {-9, -6}, {9, -6}, {-4, -9}, {4, -9}};
                int[] round = rounds[(index - 6) % rounds.length];
                yield centre.offset(round[0], 0, round[1]);
            }
            case RESIDENT -> {
                int[][] spots = {{6, 4}, {5, -6}, {-5, -5}, {7, 0}, {-7, -1}, {3, 7}, {-3, 7}, {6, -3}, {-6, 6}, {0, -8}};
                int[] spot = spots[index % spots.length];
                yield centre.offset(spot[0], 0, spot[1]);
            }
        };
    }

    /** A resident has died: one fewer stands at this Citadel until it is replaced. The lord, a Saint, is never lost for good. */
    static void lost(ServerLevel level, CitadelResident resident) {
        Optional<CitadelData.Citadel> citadel = resident.citadel().flatMap(id -> CitadelData.get(level).byId(id));
        if (citadel.isEmpty()) {
            return;
        }
        switch (resident.role()) {
            case GUARD -> citadel.get().guards = Math.max(0, citadel.get().guards - 1);
            case RESIDENT, TRADER -> citadel.get().residents = Math.max(0, citadel.get().residents - 1);
            case LORD -> {
            }
        }
        if (resident.role() != CitadelResident.Role.LORD) {
            citadel.get().regrownAt = level.getGameTime();
        }
        CitadelData.get(level).setDirty();
    }

    // Standing

    public static int points(ServerPlayer player, UUID clan) {
        return NpcClanData.get(player.server).standing(player.getUUID(), clan);
    }

    public static Standing standing(ServerPlayer player, UUID clan) {
        return standing(points(player, clan));
    }

    public static Standing standing(int points) {
        NpcCitadelSettings.Standing steps = settings().standing();
        if (points < 0) {
            return Standing.HOSTILE;
        }
        if (points >= steps.sworn()) {
            return Standing.SWORN;
        }
        if (points >= steps.trusted()) {
            return Standing.TRUSTED;
        }
        return points >= steps.known() ? Standing.KNOWN : Standing.STRANGER;
    }

    public static boolean hostile(ServerPlayer player, UUID clan) {
        return points(player, clan) < 0;
    }

    /** Moves a player's standing with a clan, telling them when it crosses a step. */
    public static void change(ServerPlayer player, UUID clan, int by) {
        if (by == 0) {
            return;
        }
        NpcClanData data = NpcClanData.get(player.server);
        Standing before = standing(data.standing(player.getUUID(), clan));
        data.addStanding(player.getUUID(), clan, by);
        Standing after = standing(data.standing(player.getUUID(), clan));
        if (before != after) {
            String name = data.byId(clan).map(NpcClanData.NpcClan::name).orElse("The clan");
            DreamCrossing.spell(player, Component.literal(after == Standing.HOSTILE
                    ? name + " has turned against you. Its guards will strike on sight."
                    : name + " now counts you as " + after.displayName() + ".").withStyle(after.colour()));
        }
    }

    static void traded(ServerPlayer player, UUID clan) {
        change(player, clan, settings().standing().trade());
    }

    static void struck(ServerPlayer player, CitadelResident resident, boolean killed) {
        resident.clan().ifPresent(clan -> change(player, clan,
                -(killed ? settings().standing().kill() : settings().standing().hurt())));
    }

    /** A player who slays a Nightmare Creature on an NPC clan's land earns its thanks. */
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof NightmareCreature creature) || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || !DreamRealm.is(player.level())) {
            return;
        }
        double reach = CitadelContentLoader.settings().landRadius();
        CitadelService.near(player.serverLevel(), creature.blockPosition(), reach)
                .filter(citadel -> citadel.hold == CitadelData.Hold.NPC && citadel.npcClan != null)
                .ifPresent(citadel -> change(player, citadel.npcClan, settings().standing().hunt() * (creature.rank().ordinal() + 1)));
    }

    /** What a resident says when spoken to. */
    static void greet(ServerPlayer player, CitadelResident resident) {
        Optional<NpcClanData.NpcClan> clan = resident.clan().flatMap(id -> NpcClanData.get(player.server).byId(id));
        if (clan.isEmpty()) {
            return;
        }
        Standing standing = standing(player, clan.get().id);
        String line = switch (resident.role()) {
            case LORD -> "I am " + clan.get().lord + ", Saint of the " + clan.get().lordNature + " Aspect. " + clan.get().name
                    + " holds this Citadel for " + clan.get().greatClan + ".";
            case GUARD -> standing.compareTo(Standing.KNOWN) >= 0 ? "The walls are quiet. Keep your blade for the creatures."
                    : "Keep the peace inside these walls, stranger.";
            case TRADER -> "Shards for goods, goods for shards.";
            case RESIDENT -> standing.compareTo(Standing.TRUSTED) >= 0 ? "Good to see you back." : "You are new here.";
        };
        player.displayClientMessage(Component.literal(resident.getName().getString() + ": " + line).withStyle(ChatFormatting.GRAY), false);
        player.displayClientMessage(Component.literal("Your standing with " + clan.get().name + ": " + standing.displayName())
                .withStyle(standing.colour()), true);
    }

    // The Gateway

    /** A player uses a held Citadel's Gateway: they learn who holds it and, unless the clan is set against them, anchor there. */
    static void useGateway(ServerPlayer player, CitadelData.Citadel citadel) {
        Optional<NpcClanData.NpcClan> clan = Optional.ofNullable(citadel.npcClan).flatMap(id -> NpcClanData.get(player.server).byId(id));
        if (clan.isEmpty()) {
            return;
        }
        Standing standing = standing(player, clan.get().id);
        player.displayClientMessage(Component.literal(citadel.name + " is held by " + clan.get().name + ", sworn to "
                + clan.get().greatClan + ". Saint " + clan.get().lord + " has bound its Gateway.").withStyle(ChatFormatting.GRAY), false);
        if (standing == Standing.HOSTILE) {
            player.displayClientMessage(Component.literal("The Gateway will not hold the anchor of an enemy of " + clan.get().name + ".")
                    .withStyle(ChatFormatting.DARK_RED), true);
            return;
        }
        if (!CitadelService.isAnchoredHere(player, citadel)) {
            CitadelService.anchor(player, citadel);
        }
        player.displayClientMessage(Component.literal("Your standing with " + clan.get().name + ": " + standing.displayName())
                .withStyle(standing.colour()), true);
    }

    /** Whether a held Citadel's Gateway keeps this player's anchor. */
    static boolean welcomes(ServerPlayer player, CitadelData.Citadel citadel) {
        return citadel.npcClan != null && !hostile(player, citadel.npcClan);
    }

    public static Optional<NpcClanData.NpcClan> clanOf(MinecraftServer server, CitadelData.Citadel citadel) {
        return Optional.ofNullable(citadel.npcClan).flatMap(id -> NpcClanData.get(server).byId(id));
    }

    // Fixed chance

    private static String pick(List<String> from, long seed, BlockPos at, int salt) {
        return from.get(Math.min(from.size() - 1, (int) (chance(seed, at, salt) * from.size())));
    }

    /** A number in [0, 1) fixed by the world seed, the place and a salt. */
    static double chance(long seed, BlockPos at, int salt) {
        long mixed = Mth.getSeed(at) ^ seed ^ (salt * 0x9E3779B97F4A7C15L);
        mixed ^= mixed >>> 33;
        mixed *= 0xff51afd7ed558ccdL;
        mixed ^= mixed >>> 33;
        mixed *= 0xc4ceb9fe1a85ec53L;
        mixed ^= mixed >>> 33;
        return (mixed >>> 11) * 0x1.0p-53;
    }
}
