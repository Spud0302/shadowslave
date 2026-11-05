package dev.spud.shadowslave.trial;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.FortuneLuck;
import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.dreamrealm.DreamCrossing;
import dev.spud.shadowslave.dreamrealm.seed.SeedData;
import dev.spud.shadowslave.dreamrealm.seed.SeedService;
import dev.spud.shadowslave.item.ModDataComponents;
import dev.spud.shadowslave.network.payload.NightmareAppraisalPayload;
import dev.spud.shadowslave.soul.core.CoreService;
import dev.spud.shadowslave.spell.SpellAppraisal;
import dev.spud.shadowslave.spell.SpellJudge;
import dev.spud.shadowslave.spell.SpellRecord;
import dev.spud.shadowslave.spell.SpellRecorder;
import dev.spud.shadowslave.spell.truename.TrueNames;
import dev.spud.shadowslave.trial.content.EchoSite;
import dev.spud.shadowslave.trial.content.NightmarePack;
import dev.spud.shadowslave.trial.content.TrialContent;
import dev.spud.shadowslave.trial.content.TrialSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Runs Nightmares (DESIGN, Jack 2026-10-29; after ch. 1-17, 459 and 1584).
 *
 * <p>Sneak-using a Seed at an echo site challenges it. The Seed wakes a few
 * seconds later and draws in everyone close enough: an agreed party, or
 * strangers who slip in uninvited. Each of them is snapshotted, given a role
 * from the Nightmare's pool and sent into a private run of its structure. The
 * goal is never stated; the first ending met closes the Nightmare and the
 * Spell appraises every survivor. A death inside reverts that challenger to
 * their snapshot and bars them from that Seed. When it is over, everyone
 * returns to where they stood when they went in.</p>
 *
 * <p>Inside, challengers are known only by their roles: names are hidden above
 * heads and in chat. PvP follows the server's setting.</p>
 *
 * <p>A Long Nightmare (its pack has {@code long}) outlives its challengers'
 * logouts and server restarts: it is saved in {@link LongNightmareData}, its
 * clock runs only while someone is awake inside and counts days, a challenger
 * who logs out stays in it as a {@link LingeringChallenger}, and challenging its
 * Seed again draws a new cohort into the same run, among whoever still lives
 * there. Logging back in puts you back inside, where your figure stood.</p>
 */
public final class TrialService {
    /** Marks the Tyrant of a run. */
    static final String TYRANT_TAG = "shadowslave_trial_tyrant";
    private static final ResourceLocation TYRANT_HEALTH = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "trial_cohort_health");
    private static final DustParticleOptions RING = new DustParticleOptions(new Vector3f(0.55F, 0.12F, 0.75F), 1.4F);
    private static final int ENDING_CHECK_INTERVAL = 20;
    /** How far around a run's corner its creatures and leftovers are cleared when it ends. */
    private static final int MAX_STRUCTURE_SIZE = 256;

    private static final Map<UUID, TrialInstance> INSTANCES = new LinkedHashMap<>();
    private static final Map<UUID, UUID> BY_PLAYER = new HashMap<>();
    private static final List<Gathering> GATHERINGS = new ArrayList<>();
    /** Players this service is moving between dimensions right now, so the move is not taken for an escape. */
    private static final Set<UUID> MOVING = new HashSet<>();

    /** A Seed that has been challenged and is about to wake. */
    static final class Gathering {
        final UUID seed;
        final ResourceKey<Level> realm;
        final BlockPos pos;
        final ResourceLocation site;
        final UUID challenger;
        int ticksLeft;

        Gathering(UUID seed, ResourceKey<Level> realm, BlockPos pos, ResourceLocation site, UUID challenger, int ticksLeft) {
            this.seed = seed;
            this.realm = realm;
            this.pos = pos;
            this.site = site;
            this.challenger = challenger;
            this.ticksLeft = ticksLeft;
        }
    }

    private TrialService() {
    }

    // ---- looking things up ---------------------------------------------------------------------------------------

    public static Optional<TrialInstance> instanceOf(ServerPlayer player) {
        return Optional.ofNullable(BY_PLAYER.get(player.getUUID())).map(INSTANCES::get);
    }

    public static boolean inside(ServerPlayer player) {
        return BY_PLAYER.containsKey(player.getUUID());
    }

    public static List<TrialInstance> instances() {
        return List.copyOf(INSTANCES.values());
    }

    /** The run whose slot holds this spot of the Nightmare dimension. */
    static Optional<TrialInstance> instanceAt(BlockPos pos) {
        return INSTANCES.values().stream()
                .filter(instance -> Math.abs(pos.getX() - instance.origin.getX()) < TrialWorld.SLOT_SPACING / 2
                        && Math.abs(pos.getZ() - instance.origin.getZ()) < TrialWorld.SLOT_SPACING / 2)
                .findFirst();
    }

    public static boolean judgedBy(ServerPlayer player, UUID seed) {
        return TrialData.get(player.server).find(player.getUUID()).filter(record -> record.judgedBy(seed)).isPresent();
    }

    // ---- challenging a Seed --------------------------------------------------------------------------------------

    /**
     * Sneak-use on a Seed at an echo site: it wakes after the gathering time and
     * draws in everyone near it. Returns false if it cannot be challenged now.
     */
    public static boolean challenge(ServerPlayer player, ServerLevel realm, SeedData.Seed seed) {
        Optional<ResourceLocation> siteId = seed.echoSite();
        Optional<EchoSite> site = siteId.flatMap(TrialContent.get()::site);
        if (site.isEmpty() || TrialContent.get().nightmare(site.get().nightmare()).isEmpty()) {
            player.displayClientMessage(Component.literal("This Seed's Nightmare is missing from the data packs.")
                    .withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        if (TrialWorld.level(player.server) == null) {
            player.displayClientMessage(Component.literal("The Nightmare dimension is missing.").withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        if (!DreamCrossing.canDream(player)) {
            player.displayClientMessage(Component.literal("Only the Awakened can challenge a Nightmare Seed.")
                    .withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        if (judgedBy(player, seed.id())) {
            refuse(player);
            return false;
        }
        Optional<TrialInstance> running = runningLong(seed.id());
        if (gathering(seed.id()) || (waking(seed.id()) && running.isEmpty())) {
            player.displayClientMessage(Component.literal("The Seed is already waking.").withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        TrialSettings settings = TrialContent.get().settings();
        GATHERINGS.add(new Gathering(seed.id(), realm.dimension(), seed.pos(), siteId.get(), player.getUUID(),
                Math.max(1, settings.gatherSeconds() * 20)));
        realm.playSound(null, seed.pos(), SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE, 1.5F, 0.6F);
        for (ServerPlayer near : playersNear(realm, seed.pos(), settings.joinRange() * 4.0)) {
            if (running.isPresent()) {
                spell(near, Component.literal("The Seed of " + site.get().displayName() + " stirs. Its Nightmare has not ended: it is day "
                        + running.get().day() + " inside. It opens in " + settings.gatherSeconds() + " seconds.")
                        .withStyle(ChatFormatting.DARK_PURPLE));
            } else {
                spell(near, Component.literal("The Seed of " + site.get().displayName() + " stirs. It wakes in "
                        + settings.gatherSeconds() + " seconds.").withStyle(ChatFormatting.DARK_PURPLE));
            }
            spell(near, Component.literal("Everyone within " + settings.joinRange() + " blocks of it will be drawn into its Nightmare."));
        }
        return true;
    }

    /** Whether a Seed is gathering its cohort or its Nightmare is running. */
    public static boolean waking(UUID seed) {
        return GATHERINGS.stream().anyMatch(gathering -> gathering.seed.equals(seed))
                || INSTANCES.values().stream().anyMatch(instance -> instance.seed.filter(seed::equals).isPresent());
    }

    private static boolean gathering(UUID seed) {
        return GATHERINGS.stream().anyMatch(gathering -> gathering.seed.equals(seed));
    }

    /** The Long Nightmare a Seed holds that is still running, which a new cohort can be drawn into. */
    public static Optional<TrialInstance> runningLong(UUID seed) {
        return INSTANCES.values().stream()
                .filter(instance -> instance.isLong() && !instance.over && instance.seed.filter(seed::equals).isPresent())
                .findFirst();
    }

    private static void refuse(ServerPlayer player) {
        spell(player, Component.literal("[This Nightmare has already judged you.]").withStyle(ChatFormatting.DARK_RED));
    }

    private static void tickGatherings(MinecraftServer server) {
        Iterator<Gathering> gatherings = GATHERINGS.iterator();
        while (gatherings.hasNext()) {
            Gathering gathering = gatherings.next();
            ServerLevel realm = server.getLevel(gathering.realm);
            if (realm == null) {
                gatherings.remove();
                continue;
            }
            gathering.ticksLeft--;
            int range = TrialContent.get().settings().joinRange();
            if (gathering.ticksLeft % 20 == 0 && gathering.ticksLeft > 0) {
                showGathering(realm, gathering, range);
            }
            if (gathering.ticksLeft > 0) {
                continue;
            }
            gatherings.remove();
            wake(realm, gathering, range);
        }
    }

    /** Wakes every gathering Seed now; for tests. */
    static void wakeGatheringsNow(MinecraftServer server) {
        GATHERINGS.forEach(gathering -> gathering.ticksLeft = 1);
        tickGatherings(server);
    }

    private static void showGathering(ServerLevel realm, Gathering gathering, int range) {
        Vec3 center = Vec3.atBottomCenterOf(gathering.pos);
        for (int i = 0; i < 48; i++) {
            double angle = Math.PI * 2 * i / 48;
            realm.sendParticles(RING, center.x + Math.cos(angle) * range, center.y + 0.3, center.z + Math.sin(angle) * range,
                    1, 0, 0, 0, 0);
        }
        realm.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 0.6, center.z, 20, 0.3, 0.5, 0.3, 0.08);
        int seconds = gathering.ticksLeft / 20;
        realm.playSound(null, gathering.pos, SoundEvents.NOTE_BLOCK_BASEDRUM.value(), SoundSource.HOSTILE, 1.5F, 0.5F);
        for (ServerPlayer player : playersNear(realm, gathering.pos, range * 2.0)) {
            boolean in = player.position().distanceToSqr(center) <= (double) range * range;
            player.displayClientMessage(Component.literal(in
                    ? "The Seed wakes in " + seconds + "... Step away to stay out."
                    : "The Seed wakes in " + seconds + "... Step closer to be drawn in.").withStyle(ChatFormatting.DARK_PURPLE), true);
        }
    }

    /** The Seed wakes: everyone close enough who may go in is drawn into its Nightmare. */
    private static void wake(ServerLevel realm, Gathering gathering, int range) {
        Optional<SeedData.Seed> seed = SeedData.get(realm).byId(gathering.seed);
        Optional<EchoSite> site = TrialContent.get().site(gathering.site);
        if (seed.isEmpty() || site.isEmpty()) {
            return;
        }
        List<ServerPlayer> cohort = new ArrayList<>();
        Vec3 center = Vec3.atBottomCenterOf(gathering.pos);
        for (ServerPlayer player : playersNear(realm, gathering.pos, range)) {
            if (player.isSpectator() || !player.isAlive() || inside(player) || !DreamCrossing.canDream(player)
                    || player.position().distanceToSqr(center) > (double) range * range) {
                continue;
            }
            if (judgedBy(player, gathering.seed)) {
                refuse(player);
                continue;
            }
            cohort.add(player);
        }
        if (cohort.isEmpty()) {
            for (ServerPlayer player : playersNear(realm, gathering.pos, range * 4.0)) {
                spell(player, Component.literal("The Seed falls still. Nobody answered it.").withStyle(ChatFormatting.GRAY));
            }
            return;
        }
        Optional<TrialInstance> running = runningLong(gathering.seed);
        if (running.isPresent()) {
            join(cohort, running.get());
            return;
        }
        begin(cohort, site.get().nightmare(), Optional.of(gathering.site), Optional.of(gathering.seed),
                TrialContent.get().category(site.get()));
    }

    // ---- going in ------------------------------------------------------------------------------------------------

    /**
     * Sends a cohort into a fresh run of a Nightmare. Operators can call it
     * without a Seed. Returns empty, with nobody moved, if the Nightmare or its
     * structure is missing.
     */
    public static Optional<TrialInstance> begin(List<ServerPlayer> cohort, ResourceLocation nightmareId,
                                                Optional<ResourceLocation> site, Optional<UUID> seed, int category) {
        if (cohort.isEmpty()) {
            return Optional.empty();
        }
        MinecraftServer server = cohort.get(0).server;
        Optional<NightmarePack> pack = TrialContent.get().nightmare(nightmareId);
        ServerLevel level = TrialWorld.level(server);
        if (pack.isEmpty() || level == null) {
            ShadowSlaveMod.LOGGER.error("Cannot start Nightmare {}: {}", nightmareId, pack.isEmpty() ? "no such pack" : "no dimension");
            return Optional.empty();
        }
        Optional<StructureTemplate> template = server.getStructureManager().get(pack.get().structure());
        if (template.isEmpty()) {
            ShadowSlaveMod.LOGGER.error("Nightmare {} names a missing structure {}", nightmareId, pack.get().structure());
            return Optional.empty();
        }
        int slot = freeSlot();
        BlockPos origin = TrialWorld.slotOrigin(slot);
        place(level, origin, template.get());

        TrialInstance instance = new TrialInstance(UUID.randomUUID(), nightmareId, pack.get(), site, seed, category, slot, origin);
        INSTANCES.put(instance.id, instance);
        if (instance.isLong()) {
            instance.lastWalked = server.overworld().getGameTime();
            LongNightmareData.get(server).keep(instance);
        }
        List<NightmarePack.Role> roles = assignRoles(pack.get().roles(), cohort.size(), level.getRandom());
        for (int i = 0; i < cohort.size(); i++) {
            enter(level, instance, cohort.get(i), roles.get(i), i);
        }
        ShadowSlaveMod.LOGGER.info("Nightmare {} began in slot {} with {} challengers", nightmareId, slot, cohort.size());
        return Optional.of(instance);
    }

    /**
     * Draws a new cohort into a Long Nightmare that is still running. Those who
     * were in it before and left unjudged keep the role the Spell gave them, if
     * the settings say so; the rest are dealt new ones.
     */
    public static void join(List<ServerPlayer> cohort, TrialInstance instance) {
        if (cohort.isEmpty() || instance.over) {
            return;
        }
        MinecraftServer server = cohort.get(0).server;
        ServerLevel level = TrialWorld.level(server);
        if (level == null) {
            return;
        }
        int living = instance.inside().size();
        boolean keepRoles = TrialContent.get().longNightmares().rejoinKeepsRole();
        List<ServerPlayer> newcomers = cohort.stream()
                .filter(player -> !keepRoles || instance.member(player.getUUID()).isEmpty())
                .toList();
        Iterator<NightmarePack.Role> dealt = assignRoles(instance.pack.roles(), newcomers.size(), level.getRandom()).iterator();
        for (int i = 0; i < cohort.size(); i++) {
            ServerPlayer player = cohort.get(i);
            NightmarePack.Role role = newcomers.contains(player) ? dealt.next() : instance.members.get(player.getUUID()).role;
            enter(level, instance, player, role, i);
            if (living > 0) {
                instance.pack.longRun().ifPresent(run -> spell(player, Component.literal(
                        run.returning().replace("{count}", Integer.toString(living))).withStyle(ChatFormatting.DARK_PURPLE)));
            }
        }
        instance.lastWalked = server.overworld().getGameTime();
        LongNightmareData.get(server).setDirty();
        ShadowSlaveMod.LOGGER.info("{} more challengers entered the Long Nightmare {} on day {}", cohort.size(), instance.nightmareId,
                instance.day());
    }

    private static int freeSlot() {
        Set<Integer> used = new HashSet<>();
        INSTANCES.values().forEach(instance -> used.add(instance.slot));
        int slot = 0;
        while (used.contains(slot)) {
            slot++;
        }
        return slot;
    }

    /** Places a run's structure, clearing whatever a run before it left behind. */
    private static void place(ServerLevel level, BlockPos origin, StructureTemplate template) {
        var size = template.getSize();
        for (int cx = origin.getX() >> 4; cx <= (origin.getX() + size.getX()) >> 4; cx++) {
            for (int cz = origin.getZ() >> 4; cz <= (origin.getZ() + size.getZ()) >> 4; cz++) {
                level.getChunk(cx, cz);
            }
        }
        AABB area = new AABB(origin).expandTowards(size.getX(), size.getY(), size.getZ()).inflate(48);
        level.getEntities((Entity) null, area, entity -> !(entity instanceof Player)).forEach(Entity::discard);
        template.placeInWorld(level, origin, origin, new StructurePlaceSettings(), level.getRandom(), Block.UPDATE_CLIENTS);
    }

    /**
     * Deals roles to a cohort: each role's {@code min} first, in the pack's
     * order, then the rest by weight; shuffled so the order says nothing.
     */
    public static List<NightmarePack.Role> assignRoles(List<NightmarePack.Role> pool, int members, RandomSource random) {
        List<NightmarePack.Role> dealt = new ArrayList<>();
        for (NightmarePack.Role role : pool) {
            for (int i = 0; i < role.min() && dealt.size() < members; i++) {
                dealt.add(role);
            }
        }
        int total = pool.stream().mapToInt(NightmarePack.Role::weight).sum();
        while (dealt.size() < members) {
            int roll = random.nextInt(total);
            for (NightmarePack.Role role : pool) {
                roll -= role.weight();
                if (roll < 0) {
                    dealt.add(role);
                    break;
                }
            }
        }
        List<NightmarePack.Role> shuffled = new ArrayList<>(dealt);
        for (int i = shuffled.size() - 1; i > 0; i--) {
            Collections.swap(shuffled, i, random.nextInt(i + 1));
        }
        return shuffled;
    }

    private static void enter(ServerLevel level, TrialInstance instance, ServerPlayer player, NightmarePack.Role role, int index) {
        String team = player.getTeam() == null ? "" : player.getTeam().getName();
        TrialData.get(player.server).begin(player.getUUID(), new TrialData.Entry(instance.id, instance.nightmareId, instance.seed,
                PlayerSnapshot.capture(player), player.level().dimension(), player.position(), player.getYRot(), player.getXRot(), team));
        TrialInstance.Member member = new TrialInstance.Member(player.getUUID(), player.getScoreboardName(), role);
        instance.members.put(player.getUUID(), member);
        BY_PLAYER.put(player.getUUID(), instance.id);
        joinTeam(player, instance);

        BlockPos arrival = instance.at(instance.pack.arrival()).offset(index % 3 - 1, 0, index / 3);
        move(player, level, Vec3.atBottomCenterOf(arrival), 180.0F, 0.0F);
        for (ItemStack stack : dev.spud.shadowslave.ability.MasqueFace.roleKit(player, role.kit())) {
            ItemStack bound = bind(stack.copy());
            if (!player.getInventory().add(bound)) {
                player.drop(bound, false);
            }
        }
        player.refreshDisplayName();
        player.refreshTabListName();
        if (instance.tyrantBar != null) {
            instance.tyrantBar.addPlayer(player);
        }

        level.playSound(null, arrival, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 0.5F);
        title(player, Component.literal("[" + role.name() + "]").withStyle(ChatFormatting.LIGHT_PURPLE),
                Component.literal(instance.pack.displayName()).withStyle(ChatFormatting.GRAY));
        for (String line : instance.pack.opening()) {
            spell(player, Component.literal(line).withStyle(ChatFormatting.DARK_PURPLE));
        }
        if (instance.isLong() && instance.day() > 1) {
            spell(player, Component.literal("It is day " + instance.day() + " of this Nightmare.").withStyle(ChatFormatting.DARK_PURPLE));
        }
        spell(player, Component.literal("You are [" + role.name() + "]."));
        if (!role.description().isEmpty()) {
            spell(player, Component.literal(role.description()));
        }
    }

    /** Marks a kit item as the Nightmare's: it is gone when you leave. */
    static ItemStack bind(ItemStack stack) {
        stack.set(ModDataComponents.NIGHTMARE_BOUND.get(), Unit.INSTANCE);
        stack.set(net.minecraft.core.component.DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Bound to the Nightmare").withStyle(style -> style.withItalic(true).withColor(ChatFormatting.DARK_PURPLE)))));
        return stack;
    }

    private static void stripBound(ServerPlayer player) {
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).has(ModDataComponents.NIGHTMARE_BOUND.get())) {
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
        player.inventoryMenu.broadcastChanges();
    }

    // ---- the Nightmare's clock -----------------------------------------------------------------------------------

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        tickGatherings(server);
        if (INSTANCES.isEmpty()) {
            return;
        }
        ServerLevel level = TrialWorld.level(server);
        for (TrialInstance instance : List.copyOf(INSTANCES.values())) {
            if (level == null) {
                collapse(server, instance);
                continue;
            }
            tick(level, instance);
        }
    }

    /** Advances a run by one tick of its own clock. Package-private for game tests. */
    static void tick(ServerLevel level, TrialInstance instance) {
        if (instance.over) {
            return;
        }
        MinecraftServer server = level.getServer();
        if (instance.isLong()) {
            long now = server.overworld().getGameTime();
            if (players(server, instance).isEmpty()) {
                // Nobody awake inside: the Nightmare holds its breath, and the sleepers in it are safe.
                if (now - instance.lastWalked > TrialContent.get().longNightmares().abandonAfterDays() * 24000L) {
                    fade(server, instance, "Nobody has walked this Nightmare for a long time. It fades.");
                }
                return;
            }
            instance.lastWalked = now;
        }
        instance.ticks++;
        List<NightmarePack.Event> events = instance.pack.events();
        while (instance.nextEvent < events.size() && instance.ticks >= events.get(instance.nextEvent).at() * 20) {
            fire(level, instance, events.get(instance.nextEvent++));
        }
        if (instance.pack.longRun().isPresent()) {
            NightmarePack.LongRun run = instance.pack.longRun().get();
            int day = instance.day();
            if (day > run.fadeAfterDays()) {
                fade(server, instance, "The Nightmare's days run out. It fades, and nobody conquered it.");
                return;
            }
            if (day > instance.dayAnnounced) {
                instance.dayAnnounced = day;
                for (ServerPlayer player : players(server, instance)) {
                    title(player, Component.literal("Day " + day).withStyle(ChatFormatting.LIGHT_PURPLE),
                            Component.literal(instance.pack.displayName()).withStyle(ChatFormatting.GRAY));
                    spell(player, Component.literal(run.dawn().replace("{day}", Integer.toString(day))).withStyle(ChatFormatting.DARK_PURPLE));
                }
            }
        }
        instance.pack.tyrant().ifPresent(tyrant -> {
            if (!instance.tyrantSpawned && instance.ticks >= tyrant.at() * 20) {
                spawnTyrant(level, instance, tyrant);
            }
        });
        if (instance.tyrantBar != null && instance.tyrant != null
                && level.getEntity(instance.tyrant) instanceof Mob tyrant) {
            instance.tyrantBar.setProgress(Math.max(0.0F, tyrant.getHealth() / tyrant.getMaxHealth()));
        }
        if (instance.ticks % ENDING_CHECK_INTERVAL == 0) {
            for (TrialInstance.Member member : instance.inside()) {
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(member.player);
                if (player != null) {
                    recover(member, player);
                }
            }
            checkEndings(level, instance);
            if (instance.isLong() && !instance.over) {
                LongNightmareData.get(server).setDirty();
            }
        }
    }

    private static void fire(ServerLevel level, TrialInstance instance, NightmarePack.Event event) {
        if (!event.spell().isEmpty()) {
            for (ServerPlayer player : players(level.getServer(), instance)) {
                spell(player, Component.literal(event.spell()).withStyle(ChatFormatting.DARK_PURPLE));
            }
        }
        int cohort = instance.members.size();
        for (NightmarePack.Spawn spawn : event.spawns()) {
            for (int i = 0; i < spawn.countFor(cohort); i++) {
                spawn(level, instance, spawn.entity(), spawn.rank(), spawn.creatureClass(), spawn.point());
            }
        }
    }

    private static Optional<Mob> spawn(ServerLevel level, TrialInstance instance, ResourceLocation id, Optional<CreatureRank> rank,
                                       Optional<CreatureClass> creatureClass, String point) {
        Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id);
        if (type.isEmpty() || !(type.get().create(level) instanceof Mob mob)) {
            ShadowSlaveMod.LOGGER.error("Nightmare {} spawns {}, which is not a mob", instance.nightmareId, id);
            return Optional.empty();
        }
        BlockPos at = instance.at(point);
        RandomSource random = level.getRandom();
        mob.moveTo(at.getX() + 0.5 + random.nextInt(5) - 2, at.getY(), at.getZ() + 0.5 + random.nextInt(5) - 2,
                random.nextFloat() * 360.0F, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(at), MobSpawnType.EVENT, null);
        if (mob instanceof NightmareCreature creature) {
            creature.setRankAndClass(rank.orElse(creature.nativeRank()), creatureClass.orElse(creature.nativeClass()));
        }
        mob.setPersistenceRequired();
        nearestMember(level, instance, mob.position()).ifPresent(mob::setTarget);
        level.addFreshEntity(mob);
        level.sendParticles(ParticleTypes.ASH, mob.getX(), mob.getY() + 0.5, mob.getZ(), 30, 0.4, 0.6, 0.4, 0.02);
        instance.spawned.add(mob.getUUID());
        return Optional.of(mob);
    }

    private static void spawnTyrant(ServerLevel level, TrialInstance instance, NightmarePack.Tyrant tyrant) {
        instance.tyrantSpawned = true;
        Optional<Mob> spawned = spawn(level, instance, tyrant.entity(), Optional.of(tyrant.rank()),
                Optional.of(tyrant.creatureClass()), tyrant.point());
        if (spawned.isEmpty()) {
            return;
        }
        Mob mob = spawned.get();
        mob.getPersistentData().putUUID(TYRANT_TAG, instance.id);
        mob.setCustomName(Component.literal(tyrant.name()));
        mob.setCustomNameVisible(true);
        AttributeInstance health = mob.getAttribute(Attributes.MAX_HEALTH);
        double extra = tyrant.healthPerMember() * Math.max(0, instance.members.size() - 1);
        if (health != null && extra > 0) {
            health.addOrUpdateTransientModifier(new AttributeModifier(TYRANT_HEALTH, extra, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        mob.setHealth(mob.getMaxHealth());
        instance.tyrant = mob.getUUID();
        instance.tyrantBar = new ServerBossEvent(Component.literal(tyrant.name()), BossEvent.BossBarColor.RED,
                BossEvent.BossBarOverlay.NOTCHED_10);
        players(level.getServer(), instance).forEach(instance.tyrantBar::addPlayer);
        level.playSound(null, mob.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 2.0F, 0.6F);
        if (!tyrant.spell().isEmpty()) {
            for (ServerPlayer player : players(level.getServer(), instance)) {
                spell(player, Component.literal(tyrant.spell()).withStyle(ChatFormatting.DARK_RED));
            }
        }
    }

    private static Optional<ServerPlayer> nearestMember(ServerLevel level, TrialInstance instance, Vec3 from) {
        return players(level.getServer(), instance).stream()
                .filter(player -> !player.isCreative() && !player.isSpectator())
                .min((a, b) -> Double.compare(a.distanceToSqr(from), b.distanceToSqr(from)));
    }

    // ---- endings -------------------------------------------------------------------------------------------------

    /** Ends the run with the first ending that is met, in the pack's order. */
    static void checkEndings(ServerLevel level, TrialInstance instance) {
        if (instance.over) {
            return;
        }
        for (NightmarePack.Ending ending : instance.pack.endings()) {
            if (instance.ticks >= ending.after() * 20 && met(level, instance, ending)) {
                conclude(level, instance, ending);
                return;
            }
        }
    }

    private static boolean met(ServerLevel level, TrialInstance instance, NightmarePack.Ending ending) {
        return switch (ending.kind()) {
            case SLAY_TYRANT -> instance.tyrantSlain;
            case SURVIVE -> !instance.inside().isEmpty();
            case LIGHT -> ending.points().stream().allMatch(point -> lit(level.getBlockState(instance.at(point))));
            case REACH -> players(level.getServer(), instance).stream().anyMatch(player -> ending.points().stream()
                    .anyMatch(point -> player.position().closerThan(Vec3.atBottomCenterOf(instance.at(point)), ending.radius())));
        };
    }

    private static boolean lit(BlockState state) {
        return state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT);
    }

    /** Ends a run with an ending: every challenger still inside is appraised and sent home. */
    public static void conclude(ServerLevel level, TrialInstance instance, NightmarePack.Ending ending) {
        if (instance.over) {
            return;
        }
        instance.over = true;
        MinecraftServer server = level.getServer();
        TrialSettings settings = TrialContent.get().settings();
        instance.endedWith = ending.id();
        for (TrialInstance.Member member : instance.inside()) {
            ServerPlayer player = server.getPlayerList().getPlayer(member.player);
            if (player == null) {
                if (member.away) {
                    // Asleep inside when it ended: they shared the ending, and hear the Spell when they return.
                    member.away = false;
                    member.inside = false;
                    member.pending = true;
                }
                continue;
            }
            SpellJudge.Run run = run(instance, member);
            SpellJudge.Judgement judgement = dev.spud.shadowslave.ability.MasqueFace.trusted(player, ending, run,
                    SpellJudge.judge(ending, run, settings), settings);
            finish(player, instance, member, ending, judgement, settings);
        }
        instance.seed.ifPresent(seed -> SeedService.realm(server).ifPresent(realm -> SeedService.spend(realm, seed)));
        instance.site.ifPresent(site -> EchoSites.rest(server, site));
        teardown(server, instance);
    }

    /** What a challenger did in this run, as the Judge needs it. */
    static SpellJudge.Run run(TrialInstance instance, TrialInstance.Member member) {
        return new SpellJudge.Run(member.kills, member.killScore, member.uniqueKills, member.aboveRank, member.role.id(),
                member.role.deviation(),
                instance.members.size(), instance.falls, member.struck.size(), member.alliesSaved, member.closeCalls);
    }

    private static void finish(ServerPlayer player, TrialInstance instance, TrialInstance.Member member,
                               NightmarePack.Ending ending, SpellJudge.Judgement appraisal, TrialSettings settings) {
        member.inside = false;
        stripBound(player);
        TrialData data = TrialData.get(player.server);
        Optional<TrialData.Entry> entry = data.find(player.getUUID()).flatMap(TrialData.Record::active);
        leaveTeam(player, entry.map(TrialData.Entry::team).orElse(""));
        BY_PLAYER.remove(player.getUUID());
        entry.ifPresent(found -> goHome(player, found));
        data.end(player.getUUID());
        Optional<SpellRecord.Tally> before = SpellRecord.get(player.server).find(player.getUUID()).map(SpellRecord.Tally::copy);
        data.clear(player.getUUID(), instance.category, instance.nightmareId, appraisal.grade().name());
        SpellRecorder.cleared(player, instance.pack.displayName(), instance.category, appraisal, settings);
        player.refreshDisplayName();
        player.refreshTabListName();

        List<String> boons = new ArrayList<>();
        if (instance.first) {
            boons.add("The right to Awaken: your First Nightmare conquered");
        } else if (instance.category >= 2) {
            boons.add("The right to Ascend: a Category " + instance.category + " Nightmare conquered");
        } else {
            boons.add("A Category " + instance.category + " Nightmare conquered");
        }
        // A table can roll the same item in several pools; the appraisal names each item once.
        Map<String, Integer> received = new LinkedHashMap<>();
        for (ItemStack stack : rollBoons(player, instance.pack, appraisal.grade().name())) {
            received.merge(stack.getHoverName().getString(), stack.getCount(), Integer::sum);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
        received.forEach((name, count) -> boons.add(count + " × " + name));
        if (NightmareScar.stacks(player) > 0 && TrialContent.get().scar().clearedHeals()) {
            int left = NightmareScar.heal(player);
            boons.add(left == 0 ? "The last scar on your soul has closed" : "A scar on your soul has closed");
        }
        Optional<TrueNames.Grant> trueName = TrueNames.considerAppraisal(player, appraisal);
        trueName.ifPresent(grant -> TrueNames.revealedByAppraisal(player));
        NightmareAppraisalPayload payload = new NightmareAppraisalPayload(instance.pack.displayName(), instance.pack.setting(),
                ending.comment(), appraisal.deeds(), appraisal.acclaim(), appraisal.grade().name(),
                SpellAppraisal.comments(player, instance.pack, appraisal, before, settings), boons, instance.pack.lore(),
                trueName.map(TrueNames.Grant::name).orElse(""), trueName.map(TrueNames.Grant::deedLine).orElse(""));
        if (player.connection != null && player.connection.hasChannel(NightmareAppraisalPayload.TYPE)) {
            PacketDistributor.sendToPlayer(player, payload);
        }
        spell(player, Component.literal("The Nightmare is over. " + instance.pack.displayName() + ": " + appraisal.grade().name() + ".")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        if (instance.first) {
            spell(player, Component.literal("Your First Nightmare is conquered. Meditate beside a lit Dream Candle to Awaken.")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0F, 0.7F);
        ShadowSlaveMod.LOGGER.info("{} conquered {} ({}, score {})", player.getScoreboardName(), instance.nightmareId,
                appraisal.grade().name(), appraisal.score());
    }

    /**
     * The loot table for a grade: the pack's own for it, or else the one of the
     * highest grade below it that has one, so a pack need not name every grade.
     */
    static ResourceLocation boonTable(NightmarePack pack, String grade, TrialSettings settings) {
        ResourceLocation exact = pack.boons().get(grade);
        if (exact != null) {
            return exact;
        }
        List<TrialSettings.Grade> grades = settings.grades();
        for (int i = settings.standing(grade); i >= 0; i--) {
            ResourceLocation lower = pack.boons().get(grades.get(i).name());
            if (lower != null) {
                return lower;
            }
        }
        return null;
    }

    static List<ItemStack> rollBoons(ServerPlayer player, NightmarePack pack, String grade) {
        ResourceLocation tableId = boonTable(pack, grade, TrialContent.get().settings());
        if (tableId == null) {
            return List.of();
        }
        LootTable table = player.server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, tableId));
        LootParams params = new LootParams.Builder(player.serverLevel())
                .withParameter(LootContextParams.ORIGIN, player.position())
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withLuck(player.getLuck())
                .create(LootContextParamSets.GIFT);
        // Fortune's Favoured sometimes draws one boon more from a second roll.
        return FortuneLuck.boons(player, table.getRandomItems(params), () -> table.getRandomItems(params));
    }

    // ---- falling and leaving -------------------------------------------------------------------------------------

    /**
     * A challenger who dies inside does not die: the Nightmare throws them out
     * as they were when they entered, and its Seed will not have them again.
     */
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (inside(player)) {
                event.setCanceled(true);
                player.setHealth(1.0F);
                player.server.tell(new TickTask(player.server.getTickCount() + 1, () -> fall(player)));
            }
            return;
        }
        if (!(event.getEntity().level() instanceof ServerLevel level) || !TrialWorld.is(level)
                || event.getEntity() instanceof LingeringChallenger) {
            return;
        }
        Optional<TrialInstance> instance = instanceAt(event.getEntity().blockPosition());
        if (instance.isEmpty() || instance.get().over) {
            return;
        }
        boolean tyrant = event.getEntity().getUUID().equals(instance.get().tyrant);
        if (event.getSource().getEntity() instanceof ServerPlayer killer) {
            instance.get().member(killer.getUUID()).filter(TrialInstance.Member::inside)
                    .ifPresent(member -> recordKill(instance.get(), member, killer, event.getEntity(), tyrant));
        }
        if (tyrant) {
            instance.get().tyrantSlain = true;
            if (instance.get().tyrantBar != null) {
                instance.get().tyrantBar.removeAllPlayers();
            }
            checkEndings(level, instance.get());
        }
    }

    static void recordKill(TrialInstance instance, TrialInstance.Member member, ServerPlayer killer, Entity victim, boolean tyrant) {
        CreatureRank rank = victim instanceof NightmareCreature creature ? creature.rank() : CreatureRank.DORMANT;
        CreatureClass creatureClass = victim instanceof NightmareCreature creature ? creature.creatureClass() : CreatureClass.BEAST;
        String kind = rank.displayName() + " " + creatureClass.displayName();
        boolean named = tyrant && victim.hasCustomName();
        String name = named ? victim.getCustomName().getString() : victim.getType().getDescription().getString();
        boolean unique = SpellJudge.unique(creatureClass, named);
        boolean above = SpellJudge.aboveRank(rank, CoreService.rank(killer));
        member.kills.merge(kind + ", " + name, 1, Integer::sum);
        member.killScore += SpellJudge.killPoints(TrialContent.get().settings(), rank, creatureClass, unique, above);
        member.slewTyrant |= tyrant;
        if (unique) {
            member.uniqueKills++;
        }
        if (above) {
            member.aboveRank++;
        }
        if (victim instanceof Mob mob && mob.getTarget() instanceof ServerPlayer hunted && !hunted.getUUID().equals(killer.getUUID())
                && instance.member(hunted.getUUID()).filter(TrialInstance.Member::inside).isPresent()) {
            member.alliesSaved++;
        }
    }

    /** Notes a challenger hurting one of their own, and close calls with death, for the Judge. */
    public static void onDamaged(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer victim)) {
            return;
        }
        Optional<TrialInstance> instance = instanceOf(victim);
        if (instance.isEmpty() || instance.get().over) {
            return;
        }
        Optional<TrialInstance.Member> hurt = instance.get().member(victim.getUUID()).filter(TrialInstance.Member::inside);
        hurt.ifPresent(member -> {
            boolean low = victim.getHealth() <= victim.getMaxHealth() * 0.25F;
            if (low && !member.low) {
                member.closeCalls++;
            }
            member.low = low;
        });
        if (event.getSource().getEntity() instanceof ServerPlayer attacker && !attacker.getUUID().equals(victim.getUUID())) {
            instance.get().member(attacker.getUUID()).filter(TrialInstance.Member::inside)
                    .ifPresent(member -> member.struck.add(victim.getUUID()));
        }
    }

    /** A challenger's health climbing back clears their close call, so the next one counts. */
    static void recover(TrialInstance.Member member, ServerPlayer player) {
        if (member.low && player.getHealth() > player.getMaxHealth() * 0.5F) {
            member.low = false;
        }
    }

    /** A challenger died inside: revert them, send them home and bar them from the Seed. */
    public static void fall(ServerPlayer player) {
        Optional<TrialInstance> instance = instanceOf(player);
        if (instance.isEmpty()) {
            return;
        }
        TrialInstance.Member member = instance.get().members.get(player.getUUID());
        String role = member == null ? "A challenger" : "[" + member.role.name() + "]";
        instance.get().falls++;
        putBack(player, instance.get());
        SpellRecorder.failed(player, instance.get().pack.displayName());
        instance.get().seed.ifPresent(seed -> TrialData.get(player.server).judge(player.getUUID(), seed));

        spell(player, Component.literal("You have died in the Nightmare.").withStyle(ChatFormatting.DARK_RED));
        spell(player, Component.literal("You wake as you were when you entered it. Everything you gained inside is lost."));
        if (instance.get().seed.isPresent()) {
            spell(player, Component.literal("[This Nightmare has judged you.]").withStyle(ChatFormatting.DARK_RED));
        }
        SpellAppraisal.failed(player, instance.get().pack, TrialContent.get().settings());
        MinecraftServer server = player.server;
        penalties(player);
        if (instance.get().first) {
            FirstNightmare.fell(player, instance.get().pack);
        }
        for (ServerPlayer other : players(server, instance.get())) {
            spell(other, Component.literal(role + " has fallen.").withStyle(ChatFormatting.DARK_RED));
        }
        collapseIfEmpty(server, instance.get());
    }

    /** The costs the server options add to a death in a Nightmare, on top of the revert and the lockout. */
    private static void penalties(ServerPlayer player) {
        MinecraftServer server = player.server;
        if (TrialRules.scar(server)) {
            spell(player, NightmareScar.describe(NightmareScar.cut(player)));
        }
        if ((TrialRules.hardcore(server) || TrialRules.trueDeath(server)) && RankLoss.toDormant(player)) {
            spell(player, Component.literal("Your soul has fallen back to Dormant.").withStyle(ChatFormatting.DARK_RED));
            if (DreamCrossing.data(player).isDreaming() && !DreamCrossing.data(player).isLost()) {
                DreamCrossing.wake(player, DreamCrossing.Reason.COMMAND);
            }
        }
    }

    /**
     * Pulls a challenger out without judging them: their body was hurt, an
     * operator ended it, or they left the Nightmare some other way. They are
     * reverted like a death, but may challenge the Seed again.
     */
    public static boolean interrupt(ServerPlayer player, String line) {
        Optional<TrialInstance> instance = instanceOf(player);
        if (instance.isEmpty()) {
            return false;
        }
        putBack(player, instance.get());
        SpellRecorder.fled(player, instance.get().pack.displayName());
        spell(player, Component.literal(line).withStyle(ChatFormatting.GRAY));
        SpellAppraisal.fled(player, instance.get().pack);
        collapseIfEmpty(player.server, instance.get());
        return true;
    }

    /** Body hurt or killed while its dreamer is in a Nightmare: called by the Dream Realm before it wakes them. */
    public static void interrupt(ServerPlayer player) {
        interrupt(player, "Your body calls you back. The Nightmare lets you go, and keeps everything you found in it.");
    }

    /** Reverts a challenger to their snapshot and sends them back where they entered. */
    private static void putBack(ServerPlayer player, TrialInstance instance) {
        TrialInstance.Member member = instance.members.get(player.getUUID());
        if (member != null) {
            member.inside = false;
        }
        BY_PLAYER.remove(player.getUUID());
        if (instance.tyrantBar != null) {
            instance.tyrantBar.removePlayer(player);
        }
        TrialData data = TrialData.get(player.server);
        Optional<TrialData.Entry> entry = data.find(player.getUUID()).flatMap(TrialData.Record::active);
        if (entry.isPresent()) {
            PlayerSnapshot.restore(player, entry.get().snapshot());
            leaveTeam(player, entry.get().team());
            goHome(player, entry.get());
        } else {
            stripBound(player);
            leaveTeam(player, "");
        }
        data.end(player.getUUID());
        player.refreshDisplayName();
        player.refreshTabListName();
    }

    private static void goHome(ServerPlayer player, TrialData.Entry entry) {
        ServerLevel home = player.server.getLevel(entry.dimension());
        if (home == null) {
            home = player.server.overworld();
        }
        move(player, home, entry.pos(), entry.yRot(), entry.xRot());
    }

    private static void move(ServerPlayer player, ServerLevel level, Vec3 pos, float yRot, float xRot) {
        MOVING.add(player.getUUID());
        try {
            player.teleportTo(level, pos.x, pos.y, pos.z, Set.of(), yRot, xRot);
            player.resetFallDistance();
        } finally {
            MOVING.remove(player.getUUID());
        }
    }

    /** Logging out leaves the Nightmare; you are put back as you entered when you return. */
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Optional<TrialInstance> instance = instanceOf(player);
        if (instance.isEmpty()) {
            return;
        }
        TrialInstance.Member member = instance.get().members.get(player.getUUID());
        BY_PLAYER.remove(player.getUUID());
        if (instance.get().tyrantBar != null) {
            instance.get().tyrantBar.removePlayer(player);
        }
        if (member != null && instance.get().isLong() && !instance.get().over && member.inside) {
            // A Long Nightmare keeps you: a figure of you lingers where you stood until you return.
            member.away = true;
            member.lingerer = LingeringChallenger.leave(player.serverLevel(), player, instance.get(), member,
                    TrialContent.get().longNightmares().lingererHealth()).getUUID();
            LongNightmareData.get(player.server).setDirty();
            return;
        }
        if (member != null) {
            member.inside = false;
        }
        collapseIfEmpty(player.server, instance.get());
    }

    /**
     * Anyone who comes back with a Nightmare still on their record (they logged
     * out inside it, or the server stopped) is put back as they entered.
     * Registered ahead of the Dream Realm's own login hook.
     */
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || inside(player)) {
            return;
        }
        Optional<TrialInstance> longRun = LongNightmareData.get(player.server).runOf(player.getUUID());
        if (longRun.isPresent()) {
            TrialInstance.Member member = longRun.get().members.get(player.getUUID());
            if (member.pending) {
                appraiseOnReturn(player, longRun.get(), member);
                return;
            }
            if (member.fellAway) {
                fellWhileAway(player, longRun.get(), member);
                return;
            }
            if (member.inside && !longRun.get().over && INSTANCES.containsKey(longRun.get().id)) {
                wakeInside(player, longRun.get(), member);
                return;
            }
        }
        TrialData data = TrialData.get(player.server);
        Optional<TrialData.Entry> entry = data.find(player.getUUID()).flatMap(TrialData.Record::active);
        if (entry.isEmpty()) {
            return;
        }
        PlayerSnapshot.restore(player, entry.get().snapshot());
        leaveTeam(player, entry.get().team());
        goHome(player, entry.get());
        data.end(player.getUUID());
        player.refreshDisplayName();
        player.refreshTabListName();
        spell(player, Component.literal("The Nightmare you left has faded. You are as you were when you entered it.")
                .withStyle(ChatFormatting.GRAY));
    }

    // ---- Long Nightmares ------------------------------------------------------------------------------------------

    /** A challenger who slept inside a Long Nightmare comes back to it, where their figure stood. */
    private static void wakeInside(ServerPlayer player, TrialInstance instance, TrialInstance.Member member) {
        ServerLevel level = TrialWorld.level(player.server);
        member.away = false;
        BY_PLAYER.put(player.getUUID(), instance.id);
        joinTeam(player, instance);
        if (level != null && member.lingerer != null && level.getEntity(member.lingerer) instanceof LingeringChallenger figure) {
            move(player, level, figure.position(), figure.getYRot(), figure.getXRot());
            player.setHealth(Math.max(1.0F, Math.min(player.getMaxHealth(),
                    figure.getHealth() / TrialContent.get().longNightmares().lingererHealth())));
            figure.discard();
        }
        member.lingerer = null;
        if (instance.tyrantBar != null) {
            instance.tyrantBar.addPlayer(player);
        }
        player.refreshDisplayName();
        player.refreshTabListName();
        instance.lastWalked = player.server.overworld().getGameTime();
        LongNightmareData.get(player.server).setDirty();
        title(player, Component.literal("[" + member.role.name() + "]").withStyle(ChatFormatting.LIGHT_PURPLE),
                Component.literal(instance.pack.displayName() + ", day " + instance.day()).withStyle(ChatFormatting.GRAY));
        spell(player, Component.literal("The Nightmare kept you. You are still [" + member.role.name() + "], and it is day "
                + instance.day() + ".").withStyle(ChatFormatting.DARK_PURPLE));
        long others = instance.inside().stream().filter(other -> other != member).count();
        if (others > 0) {
            spell(player, Component.literal(others + (others == 1 ? " other still lives" : " others still live") + " in here."));
        }
    }

    /** A challenger whose lingering figure was slain while they were away has fallen, and learns it now. */
    private static void fellWhileAway(ServerPlayer player, TrialInstance instance, TrialInstance.Member member) {
        member.fellAway = false;
        TrialData data = TrialData.get(player.server);
        Optional<TrialData.Entry> entry = data.find(player.getUUID()).flatMap(TrialData.Record::active);
        if (entry.isPresent()) {
            PlayerSnapshot.restore(player, entry.get().snapshot());
            leaveTeam(player, entry.get().team());
            goHome(player, entry.get());
        }
        data.end(player.getUUID());
        player.refreshDisplayName();
        player.refreshTabListName();
        SpellRecorder.failed(player, instance.pack.displayName());
        spell(player, Component.literal("While you slept, the Nightmare found you. You died in it.").withStyle(ChatFormatting.DARK_RED));
        spell(player, Component.literal("You wake as you were when you entered it. Everything you gained inside is lost."));
        if (instance.seed.isPresent()) {
            spell(player, Component.literal("[This Nightmare has judged you.]").withStyle(ChatFormatting.DARK_RED));
        }
        SpellAppraisal.failed(player, instance.pack, TrialContent.get().settings());
        penalties(player);
        settle(player.server, instance);
    }

    /** The Nightmare ended while this challenger slept inside it: the Spell appraises them as they return. */
    private static void appraiseOnReturn(ServerPlayer player, TrialInstance instance, TrialInstance.Member member) {
        member.pending = false;
        Optional<NightmarePack.Ending> ending = instance.pack.endings().stream()
                .filter(candidate -> candidate.id().equals(instance.endedWith)).findFirst();
        if (ending.isEmpty()) {
            // The pack changed under it; put them back unjudged.
            member.inside = true;
            putBack(player, instance);
            settle(player.server, instance);
            return;
        }
        TrialSettings settings = TrialContent.get().settings();
        spell(player, Component.literal("While you slept, " + instance.pack.displayName() + " came to its end.")
                .withStyle(ChatFormatting.DARK_PURPLE));
        finish(player, instance, member, ending.get(), SpellJudge.judge(ending.get(), run(instance, member), settings), settings);
        settle(player.server, instance);
    }

    /** Whether a lingering figure still holds a place for someone. */
    static boolean lingers(LingeringChallenger figure, MinecraftServer server) {
        Optional<UUID> owner = figure.owner();
        Optional<TrialInstance> instance = figure.run().map(INSTANCES::get);
        return owner.isPresent() && instance.isPresent() && !instance.get().over
                && instance.get().member(owner.get())
                .filter(member -> member.away && member.inside && figure.getUUID().equals(member.lingerer)).isPresent();
    }

    /** A lingering figure was slain: its challenger has fallen while away. */
    static void lingererDied(LingeringChallenger figure, MinecraftServer server) {
        if (!lingers(figure, server)) {
            return;
        }
        TrialInstance instance = INSTANCES.get(figure.run().get());
        TrialInstance.Member member = instance.members.get(figure.owner().get());
        member.away = false;
        member.inside = false;
        member.lingerer = null;
        member.fellAway = true;
        instance.falls++;
        instance.seed.ifPresent(seed -> TrialData.get(server).judge(member.player, seed));
        for (ServerPlayer other : players(server, instance)) {
            spell(other, Component.literal("[" + member.role.name() + "] has fallen.").withStyle(ChatFormatting.DARK_RED));
        }
        LongNightmareData.get(server).setDirty();
        collapseIfEmpty(server, instance);
    }

    /**
     * A Long Nightmare fades without an ending: its days ran out, or nobody
     * walked it for too long. Everyone awake inside is put back unjudged; the
     * sleepers are put back when they return.
     */
    static void fade(MinecraftServer server, TrialInstance instance, String line) {
        for (TrialInstance.Member member : instance.inside()) {
            ServerPlayer player = server.getPlayerList().getPlayer(member.player);
            if (player != null && !member.away) {
                interrupt(player, line);
            } else {
                member.inside = false;
                member.away = false;
            }
        }
        ShadowSlaveMod.LOGGER.info("The Long Nightmare {} faded on day {}", instance.nightmareId, instance.day());
        collapse(server, instance);
    }

    /** Forgets a finished Long Nightmare once nobody is owed anything from it. */
    private static void settle(MinecraftServer server, TrialInstance instance) {
        if (instance.over && instance.members.values().stream().noneMatch(member -> member.pending || member.fellAway)) {
            LongNightmareData.get(server).forget(instance);
        } else {
            LongNightmareData.get(server).setDirty();
        }
    }

    /** Brings the Long Nightmares saved with the world back to life. */
    public static void onServerStarted(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        LongNightmareData data = LongNightmareData.get(server);
        for (TrialInstance instance : data.runs()) {
            if (instance.over) {
                continue;
            }
            for (TrialInstance.Member member : instance.members.values()) {
                if (member.inside) {
                    // Everyone inside is asleep until they log in again, whether or not they got to leave a figure.
                    member.away = true;
                }
            }
            if (instance.tyrantSpawned && !instance.tyrantSlain && instance.pack.tyrant().isPresent()) {
                instance.tyrantBar = new ServerBossEvent(Component.literal(instance.pack.tyrant().get().name()),
                        BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
            }
            INSTANCES.put(instance.id, instance);
        }
        if (!INSTANCES.isEmpty()) {
            ShadowSlaveMod.LOGGER.info("{} Long Nightmares carry on", INSTANCES.size());
        }
    }

    /** Runs live only as long as their server; a Long Nightmare comes back from its saved data. */
    public static void onServerStopped(net.neoforged.neoforge.event.server.ServerStoppedEvent event) {
        INSTANCES.clear();
        BY_PLAYER.clear();
        GATHERINGS.clear();
        MOVING.clear();
    }

    /** Leaving the Nightmare dimension any other way (a command, a portal) pulls you out. */
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !MOVING.contains(player.getUUID()) && inside(player)
                && event.getFrom().equals(TrialWorld.key(player.server))) {
            interrupt(player, "You slip out of the Nightmare. It keeps nothing you found in it.");
        }
    }

    /** Ends a run where it stands for everyone in it, without judging anyone; for operators. */
    public static void abort(MinecraftServer server, TrialInstance instance) {
        for (TrialInstance.Member member : instance.inside()) {
            ServerPlayer player = server.getPlayerList().getPlayer(member.player);
            if (player != null && !member.away) {
                interrupt(player, "The Nightmare dissolves around you.");
            } else {
                member.inside = false;
                member.away = false;
            }
        }
        collapse(server, instance);
    }

    private static void collapseIfEmpty(MinecraftServer server, TrialInstance instance) {
        if (instance.inside().isEmpty()) {
            collapse(server, instance);
        }
    }

    /** A run nobody is left in closes. Its Seed stays, for whoever dares next. */
    private static void collapse(MinecraftServer server, TrialInstance instance) {
        instance.over = true;
        teardown(server, instance);
    }

    private static void teardown(MinecraftServer server, TrialInstance instance) {
        INSTANCES.remove(instance.id);
        if (instance.isLong()) {
            settle(server, instance);
        }
        instance.members.keySet().forEach(player -> BY_PLAYER.remove(player, instance.id));
        if (instance.tyrantBar != null) {
            instance.tyrantBar.removeAllPlayers();
        }
        ServerLevel level = TrialWorld.level(server);
        if (level != null) {
            AABB area = new AABB(instance.origin).inflate(MAX_STRUCTURE_SIZE);
            level.getEntities((Entity) null, area, entity -> !(entity instanceof Player)).forEach(Entity::discard);
        }
        Scoreboard scoreboard = server.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(teamName(instance));
        if (team != null && team.getPlayers().isEmpty()) {
            scoreboard.removePlayerTeam(team);
        }
    }

    // ---- hidden names --------------------------------------------------------------------------------------------

    private static String teamName(TrialInstance instance) {
        return "ss_nightmare_" + instance.slot;
    }

    /** Challengers share a team that hides names above heads; chat and the tab list show their roles instead. */
    private static void joinTeam(ServerPlayer player, TrialInstance instance) {
        Scoreboard scoreboard = player.server.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(teamName(instance));
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName(instance));
            team.setNameTagVisibility(Team.Visibility.NEVER);
            team.setAllowFriendlyFire(true);
        }
        scoreboard.addPlayerToTeam(player.getScoreboardName(), team);
    }

    private static void leaveTeam(ServerPlayer player, String former) {
        Scoreboard scoreboard = player.server.getScoreboard();
        PlayerTeam current = scoreboard.getPlayersTeam(player.getScoreboardName());
        if (current != null && current.getName().startsWith("ss_nightmare_")) {
            scoreboard.removePlayerFromTeam(player.getScoreboardName(), current);
        }
        PlayerTeam previous = former.isEmpty() ? null : scoreboard.getPlayerTeam(former);
        if (previous != null) {
            scoreboard.addPlayerToTeam(player.getScoreboardName(), previous);
        }
    }

    public static void onNameFormat(PlayerEvent.NameFormat event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            roleName(player).ifPresent(event::setDisplayname);
        }
    }

    public static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            roleName(player).ifPresent(event::setDisplayName);
        }
    }

    private static Optional<Component> roleName(ServerPlayer player) {
        return instanceOf(player).flatMap(instance -> instance.member(player.getUUID()))
                .filter(TrialInstance.Member::inside)
                .map(member -> Component.literal("[" + member.role.name() + "]").withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    // ---- helpers -------------------------------------------------------------------------------------------------

    /** The challengers of a run who are online, inside and awake (not asleep in a Long Nightmare). */
    static List<ServerPlayer> players(MinecraftServer server, TrialInstance instance) {
        List<ServerPlayer> players = new ArrayList<>();
        for (TrialInstance.Member member : instance.inside()) {
            ServerPlayer player = member.away ? null : server.getPlayerList().getPlayer(member.player);
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    private static List<ServerPlayer> playersNear(ServerLevel level, BlockPos pos, double range) {
        Vec3 center = Vec3.atBottomCenterOf(pos);
        return level.players().stream().filter(player -> player.position().distanceToSqr(center) <= range * range).toList();
    }

    static void spell(ServerPlayer player, Component line) {
        DreamCrossing.spell(player, line);
    }

    private static void title(ServerPlayer player, Component title, Component subtitle) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        player.connection.send(new ClientboundSetTitleTextPacket(title));
    }

    /** Skips a run's clock forward, firing whatever comes due; for operators and tests. */
    public static void skip(ServerLevel level, TrialInstance instance, int seconds) {
        for (int i = 0; i < seconds * 20 && !instance.over; i++) {
            tick(level, instance);
        }
    }

    /** Forces an ending; for operators and tests. Returns false if the run has no such ending. */
    public static boolean forceEnding(ServerLevel level, TrialInstance instance, String endingId) {
        for (NightmarePack.Ending ending : instance.pack.endings()) {
            if (ending.id().equals(endingId)) {
                conclude(level, instance, ending);
                return true;
            }
        }
        return false;
    }
}
