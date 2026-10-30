package dev.spud.shadowslave.trial.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.CreatureRank;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * One hand-made Nightmare, read from {@code data/<ns>/nightmare_trial/nightmare/*.json}
 * (DESIGN, Jack 2026-10-29). Every Nightmare replays a real place in the Dream
 * Realm at a point in its past, as Sunny's First Nightmare replayed the Black
 * Mountain long ago (ch. 1-2).
 *
 * <ul>
 *   <li>{@code structure} is placed for each run; {@code points} name spots in it
 *       (relative to its corner) that the rest of the pack refers to.</li>
 *   <li>{@code opening} is what the Spell says on entry. It never states the goal.</li>
 *   <li>{@code roles}: the Spell gives each challenger one, weighted, with a starting kit
 *       that cannot leave the Nightmare. A role with {@code min} is handed out first.</li>
 *   <li>{@code events}: timed lines and spawns; {@code per_member} adds creatures for
 *       each challenger after the first, so the Nightmare scales to its cohort.</li>
 *   <li>{@code tyrant}: the Nightmare's ruler, with extra health per extra challenger.</li>
 *   <li>{@code endings}: the hidden ways out. The first one met ends the Nightmare.</li>
 *   <li>{@code comments} and {@code boons}: the appraisal's comment and boon loot table per grade.</li>
 *   <li>{@code long}: makes it a Long Nightmare (Jack, 2026-10-29): the run survives logouts
 *       and restarts, its clock counts days, and later cohorts join the challengers still inside.</li>
 * </ul>
 */
public record NightmarePack(String displayName, String setting, ResourceLocation structure, String arrival,
                            Map<String, BlockPos> points, List<String> opening, List<Role> roles, List<Event> events,
                            Optional<Tyrant> tyrant, List<Ending> endings, Map<String, String> comments,
                            Map<String, ResourceLocation> boons, String lore, Optional<LongRun> longRun) {

    /**
     * A Long Nightmare's clock (DESIGN, after the novel's current arc, where a
     * Saint is shown still alive inside one). {@code day_seconds} is the length
     * of one of its days on its own clock; {@code dawn} is what the Spell says
     * as each new day begins ({@code {day}} is the day's number); after
     * {@code fade_after_days} of its days with no ending met, it fades and
     * whoever is inside wakes as they entered. {@code returning} greets a
     * cohort that comes into a run others are still inside ({@code {count}} is
     * how many of those still live).
     */
    public record LongRun(int daySeconds, String dawn, int fadeAfterDays, String returning) {
        public static final Codec<LongRun> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(60, 24 * 3600).optionalFieldOf("day_seconds", 600).forGetter(LongRun::daySeconds),
                Codec.STRING.optionalFieldOf("dawn", "Day {day} begins.").forGetter(LongRun::dawn),
                Codec.intRange(1, 1000).optionalFieldOf("fade_after_days", 10).forGetter(LongRun::fadeAfterDays),
                Codec.STRING.optionalFieldOf("returning", "Others came before you. {count} of them still live here.")
                        .forGetter(LongRun::returning)
        ).apply(instance, LongRun::new));

        /** The day a clock reading falls in, from 1. */
        public int dayAt(int ticks) {
            return 1 + ticks / (daySeconds * 20);
        }
    }

    public boolean isLong() {
        return longRun.isPresent();
    }

    public record Role(String id, String name, int weight, int min, String description, List<ItemStack> kit) {
        public static final Codec<Role> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(Role::id),
                Codec.STRING.fieldOf("name").forGetter(Role::name),
                Codec.intRange(1, 1000).optionalFieldOf("weight", 1).forGetter(Role::weight),
                Codec.intRange(0, 64).optionalFieldOf("min", 0).forGetter(Role::min),
                Codec.STRING.optionalFieldOf("description", "").forGetter(Role::description),
                ItemStack.CODEC.listOf().optionalFieldOf("kit", List.of()).forGetter(Role::kit)
        ).apply(instance, Role::new));
    }

    public record Spawn(ResourceLocation entity, Optional<CreatureRank> rank, Optional<CreatureClass> creatureClass,
                        int count, int perMember, String point) {
        public static final Codec<Spawn> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("entity").forGetter(Spawn::entity),
                CreatureRank.CODEC.optionalFieldOf("rank").forGetter(Spawn::rank),
                CreatureClass.CODEC.optionalFieldOf("class").forGetter(Spawn::creatureClass),
                Codec.intRange(0, 64).optionalFieldOf("count", 1).forGetter(Spawn::count),
                Codec.intRange(0, 64).optionalFieldOf("per_member", 0).forGetter(Spawn::perMember),
                Codec.STRING.fieldOf("point").forGetter(Spawn::point)
        ).apply(instance, Spawn::new));

        /** How many come for a cohort of {@code members}. */
        public int countFor(int members) {
            return count + perMember * Math.max(0, members - 1);
        }
    }

    public record Event(int at, String spell, List<Spawn> spawns) {
        public static final Codec<Event> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, 24 * 3600).fieldOf("at").forGetter(Event::at),
                Codec.STRING.optionalFieldOf("spell", "").forGetter(Event::spell),
                Spawn.CODEC.listOf().optionalFieldOf("spawns", List.of()).forGetter(Event::spawns)
        ).apply(instance, Event::new));
    }

    public record Tyrant(int at, ResourceLocation entity, CreatureRank rank, CreatureClass creatureClass, String point,
                         String name, String spell, float healthPerMember) {
        public static final Codec<Tyrant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, 24 * 3600).fieldOf("at").forGetter(Tyrant::at),
                ResourceLocation.CODEC.fieldOf("entity").forGetter(Tyrant::entity),
                CreatureRank.CODEC.fieldOf("rank").forGetter(Tyrant::rank),
                CreatureClass.CODEC.optionalFieldOf("class", CreatureClass.TYRANT).forGetter(Tyrant::creatureClass),
                Codec.STRING.fieldOf("point").forGetter(Tyrant::point),
                Codec.STRING.fieldOf("name").forGetter(Tyrant::name),
                Codec.STRING.optionalFieldOf("spell", "").forGetter(Tyrant::spell),
                Codec.floatRange(0.0F, 10.0F).optionalFieldOf("health_per_member", 0.5F).forGetter(Tyrant::healthPerMember)
        ).apply(instance, Tyrant::new));
    }

    /** How an ending is met. */
    public enum EndingKind {
        /** The Tyrant dies. */
        SLAY_TYRANT("slay_tyrant"),
        /** Every block at {@code points} is lit (a campfire, a candle, anything with a {@code lit} state). */
        LIGHT("light"),
        /** Someone is still alive {@code after} seconds in. */
        SURVIVE("survive");

        public static final Codec<EndingKind> CODEC = Codec.STRING.comapFlatMap(name -> {
            for (EndingKind kind : values()) {
                if (kind.serializedName.equals(name)) {
                    return DataResult.success(kind);
                }
            }
            return DataResult.error(() -> "Unknown ending kind: " + name);
        }, kind -> kind.serializedName);

        private final String serializedName;

        EndingKind(String serializedName) {
            this.serializedName = serializedName;
        }
    }

    /**
     * A hidden way to end the Nightmare. {@code after} is the earliest second it
     * can be met. {@code role} names the role whose fate it fulfils: a challenger
     * holding that role earns {@code role_score} more and the {@code role_deed}.
     */
    public record Ending(String id, EndingKind kind, int after, List<String> points, int score, String deed,
                         String comment, Optional<String> role, int roleScore, String roleDeed) {
        public static final Codec<Ending> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(Ending::id),
                EndingKind.CODEC.fieldOf("kind").forGetter(Ending::kind),
                Codec.intRange(0, 24 * 3600).optionalFieldOf("after", 0).forGetter(Ending::after),
                Codec.STRING.listOf().optionalFieldOf("points", List.of()).forGetter(Ending::points),
                Codec.intRange(0, 10000).optionalFieldOf("score", 0).forGetter(Ending::score),
                Codec.STRING.optionalFieldOf("deed", "").forGetter(Ending::deed),
                Codec.STRING.optionalFieldOf("comment", "").forGetter(Ending::comment),
                Codec.STRING.optionalFieldOf("role").forGetter(Ending::role),
                Codec.intRange(0, 10000).optionalFieldOf("role_score", 0).forGetter(Ending::roleScore),
                Codec.STRING.optionalFieldOf("role_deed", "").forGetter(Ending::roleDeed)
        ).apply(instance, Ending::new));
    }

    private static final Codec<BlockPos> POINT = Codec.INT.listOf(3, 3).xmap(
            list -> new BlockPos(list.get(0), list.get(1), list.get(2)),
            pos -> List.of(pos.getX(), pos.getY(), pos.getZ()));

    public static final Codec<NightmarePack> CODEC = RecordCodecBuilder.<NightmarePack>create(instance -> instance.group(
            Codec.STRING.fieldOf("display_name").forGetter(NightmarePack::displayName),
            Codec.STRING.optionalFieldOf("setting", "").forGetter(NightmarePack::setting),
            ResourceLocation.CODEC.fieldOf("structure").forGetter(NightmarePack::structure),
            Codec.STRING.fieldOf("arrival").forGetter(NightmarePack::arrival),
            Codec.unboundedMap(Codec.STRING, POINT).fieldOf("points").forGetter(NightmarePack::points),
            Codec.STRING.listOf().optionalFieldOf("opening", List.of()).forGetter(NightmarePack::opening),
            Role.CODEC.listOf(1, 64).fieldOf("roles").forGetter(NightmarePack::roles),
            Event.CODEC.listOf().optionalFieldOf("events", List.of()).forGetter(NightmarePack::events),
            Tyrant.CODEC.optionalFieldOf("tyrant").forGetter(NightmarePack::tyrant),
            Ending.CODEC.listOf(1, 32).fieldOf("endings").forGetter(NightmarePack::endings),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("comments", Map.of()).forGetter(NightmarePack::comments),
            Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC).optionalFieldOf("boons", Map.of()).forGetter(NightmarePack::boons),
            Codec.STRING.optionalFieldOf("lore", "").forGetter(NightmarePack::lore),
            LongRun.CODEC.optionalFieldOf("long").forGetter(NightmarePack::longRun)
    ).apply(instance, NightmarePack::new)).validate(NightmarePack::check);

    public NightmarePack {
        points = Map.copyOf(points);
        opening = List.copyOf(opening);
        roles = List.copyOf(roles);
        events = events.stream().sorted(java.util.Comparator.comparingInt(Event::at)).toList();
        endings = List.copyOf(endings);
        comments = Map.copyOf(comments);
        boons = Map.copyOf(boons);
    }

    /** Every point the pack names must exist, and role ids must be unique. */
    private static DataResult<NightmarePack> check(NightmarePack pack) {
        Set<String> named = new HashSet<>();
        named.add(pack.arrival);
        pack.events.forEach(event -> event.spawns().forEach(spawn -> named.add(spawn.point())));
        pack.tyrant.ifPresent(tyrant -> named.add(tyrant.point()));
        pack.endings.forEach(ending -> named.addAll(ending.points()));
        for (String point : named) {
            if (!pack.points.containsKey(point)) {
                return DataResult.error(() -> "Unknown point: " + point);
            }
        }
        Set<String> roleIds = new HashSet<>();
        for (Role role : pack.roles) {
            if (!roleIds.add(role.id())) {
                return DataResult.error(() -> "Duplicate role: " + role.id());
            }
        }
        for (Ending ending : pack.endings) {
            if (ending.kind() == EndingKind.SLAY_TYRANT && pack.tyrant.isEmpty()) {
                return DataResult.error(() -> "Ending " + ending.id() + " needs a tyrant");
            }
            if (ending.kind() == EndingKind.LIGHT && ending.points().isEmpty()) {
                return DataResult.error(() -> "Ending " + ending.id() + " lights no points");
            }
            if (ending.role().isPresent() && !roleIds.contains(ending.role().get())) {
                return DataResult.error(() -> "Ending " + ending.id() + " names an unknown role");
            }
        }
        return DataResult.success(pack);
    }

    public BlockPos point(String name) {
        return points.getOrDefault(name, BlockPos.ZERO);
    }

    public Optional<Role> role(String id) {
        return roles.stream().filter(role -> role.id().equals(id)).findFirst();
    }
}
